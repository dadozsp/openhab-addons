/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.sinthesi.internal;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sinthesi.internal.models.CommandQueue;
import org.openhab.binding.sinthesi.internal.models.SappEntity;
import org.openhab.binding.sinthesi.internal.models.SappVariable;
import org.openhab.binding.sinthesi.internal.poller.SappPoller;
import org.openhab.binding.sinthesi.internal.sapp.Sapp;
import org.openhab.binding.sinthesi.internal.sapp.commands.SetVirtual;
import org.openhab.binding.sinthesi.internal.sappitems.*;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SinthesiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SinthesiHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SinthesiHandler.class);
    private final SappEntity entity = new SappEntity();
    @Nullable
    private static ScheduledFuture<?> job, watchdog;
    private static boolean pollerActive;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService watchdogScheduler = Executors.newScheduledThreadPool(1);
    private @Nullable SappPoller pollerClass;

    public SinthesiHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("ChannelUID: {}", channelUID);
        logger.info("Command: {}", command);

        if (entity.getSappDigitalItems().get(channelUID) != null && SinthesiBindingConstants.SWITCH
                .equals(entity.getSappDigitalItems().get(channelUID).getItemString())) {
            SappSwitch item = (SappSwitch) entity.getSappDigitalItems().get(channelUID);
            entity.addToQueue(new CommandQueue(SetVirtual.class, item.trgAddr, (int) Math.pow(2, item.trgBit - 1)));

        } else if (entity.getSappDigitalItems().get(channelUID) != null && SinthesiBindingConstants.ROLLER
                .equals(entity.getSappDigitalItems().get(channelUID).getItemString())) {
            SappRollershutter roller = (SappRollershutter) entity.getSappDigitalItems().get(channelUID);
            if (command == UpDownType.UP) {
                entity.addToQueue(
                        new CommandQueue(SetVirtual.class, roller.upAddr, (int) Math.pow(2, roller.upBit - 1)));
            } else if (command == UpDownType.DOWN) {
                entity.addToQueue(
                        new CommandQueue(SetVirtual.class, roller.downAddr, (int) Math.pow(2, roller.downBit - 1)));
            }

        } else if (entity.getSappAnalogItems().get(channelUID) != null && SinthesiBindingConstants.NUMBER
                .equals(entity.getSappAnalogItems().get(channelUID).getItemString())) {
            SappNumber number = (SappNumber) entity.getSappAnalogItems().get(channelUID);
            float comm = ((DecimalType) command).floatValue();
            try {
                entity.addToQueue(new CommandQueue(SetVirtual.class, number.valueAddress,
                        NumberFormat.getInstance().parse(Float.toString(comm))));
            } catch (ParseException e) {
                logger.error("Error parsing command {}", command);
            }
        } else if (entity.getSappAnalogItems().get(channelUID) != null && SinthesiBindingConstants.DIMMER
                .equals(entity.getSappAnalogItems().get(channelUID).getItemString())) {
            SappDimmer dimmer = (SappDimmer) entity.getSappAnalogItems().get(channelUID);
            entity.addToQueue(new CommandQueue(SetVirtual.class, dimmer.valueAddress,
                    (int) Math.round(Integer.parseInt(command.toString()) * SinthesiBindingConstants.DIMM_DIVIDER)));
        }
    }

    @Override
    public void initialize() {
        @Nullable
        SinthesiConfiguration config = getConfigAs(SinthesiConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);
        assert config != null;
        logger.info("thing info: uuid; {}, label: {}, Id: {}", this.getThing().getUID(), this.getThing().getLabel(),
                this.getThing().getUID().getId());

        try {
            entity.setSapp(new Sapp(config.ip, Integer.parseInt(config.port), 5000));
        } catch (IOException e) {
            logger.error("Error during communication");
            logger.debug("Cause:{}\nException: {}", e.getCause(), e.getStackTrace());
        }
        entity.purgeEntity();
        updateStatus(ThingStatus.ONLINE);
        int pollerTiming = config.pollInterval;
        pollerClass = new SappPoller(entity, pollerTiming, this);
        watchdog = watchdogScheduler.scheduleWithFixedDelay(this::pollWatcher, 0, 1, TimeUnit.SECONDS);
        job = scheduler.schedule(pollerClass::statusPoller, 0, TimeUnit.MILLISECONDS);
        logger.info("Connected with PN MAS");
        logger.info("Channel number: {}", this.getThing().getChannels().size());

        for (Channel c : this.getThing().getChannels()) {
            this.channelLinked(c.getUID());
        }
        pollerActive = true;
    }

    public ChannelConfig getConfigFromChannelStr(ChannelUID uid) {
        ChannelConfig res = new ChannelConfig();
        Channel current = getThing().getChannel(uid);
        assert current != null;
        Configuration channelSettings = current.getConfiguration();
        SappVariable read;

        res.channelKind = Objects.requireNonNull(Objects.requireNonNull(current.getAcceptedItemType()));
        read = new SappVariable(channelSettings.get("read").toString());
        res.sappItemType = read.getVarType();
        int divider = channelSettings.get("divider") != null ? ((BigDecimal) channelSettings.get("divider")).intValue()
                : 0;

        switch (res.channelKind) {
            case SinthesiBindingConstants.SWITCH:
                SappVariable write = new SappVariable(channelSettings.get("write").toString());

                res.statusAddr = read.getAddr();
                res.statusBit = read.getBit();
                res.trgAddr = write.getAddr();
                res.trgBit = write.getBit();
                res.onVal = ((BigDecimal) channelSettings.get("onValue")).intValue();
                res.offVal = ((BigDecimal) channelSettings.get("offValue")).intValue();
                break;
            case SinthesiBindingConstants.CONTACT:
                res.statusAddr = read.getAddr();
                res.statusBit = read.getBit();
                break;
            case SinthesiBindingConstants.NUMBER:
                res.statusAddr = read.getAddr();
                res.divider = divider;
                break;
            case SinthesiBindingConstants.DIMMER:
                res.statusAddr = read.getAddr();
                break;
            case SinthesiBindingConstants.ROLLER:
                SappVariable up = new SappVariable(channelSettings.get("up").toString());
                SappVariable down = new SappVariable(channelSettings.get("down").toString());

                res.statusAddr = read.getAddr();
                res.statusBit = read.getBit();
                res.upAddr = up.getAddr();
                res.upBit = up.getBit();
                res.downAddr = down.getAddr();
                res.downBit = down.getBit();
                break;
            default:
                logger.warn("Item type {} not recognized", res.channelKind);
        }

        return res;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        ChannelConfig cfg = getConfigFromChannelStr(channelUID);
        boolean validKind = true;

        logger.info("Channel linked {}", channelUID);
        logger.info("Item: {} - {} - {} - {} - {} - {} - {}", cfg.statusAddr, cfg.statusBit, cfg.upAddr, cfg.upBit,
                cfg.downAddr, cfg.downBit, cfg.sappItemType);

        switch (cfg.sappItemType) {
            case SinthesiBindingConstants.OUT_TYPE:
                entity.tryAddOutput(cfg.statusAddr);
                logger.info("Added output");
                break;
            case SinthesiBindingConstants.INP_TYPE:
                entity.tryAddInput(cfg.statusAddr);
                logger.info("Added input");
                break;
            case SinthesiBindingConstants.VIRT_TYPE:
                entity.tryAddVirtual(cfg.statusAddr);
                logger.info("Added virtual");
                break;
            default:
                validKind = false;
                logger.warn("Thing type not supported: {}", this.getThing().getUID());
        }

        if (validKind) {
            if (cfg.channelKind.equalsIgnoreCase(SinthesiBindingConstants.SWITCH)) {
                entity.addDigitalSappItem(new SappSwitch(cfg.sappItemType, cfg.statusAddr, cfg.statusBit, cfg.trgAddr,
                        cfg.trgBit, cfg.onVal, cfg.offVal), channelUID);
            } else if (cfg.channelKind.equalsIgnoreCase(SinthesiBindingConstants.CONTACT)) {
                entity.addDigitalSappItem(new SappContact(cfg.sappItemType, cfg.statusAddr, cfg.statusBit), channelUID);
            } else if (cfg.channelKind.equalsIgnoreCase(SinthesiBindingConstants.NUMBER)) {
                entity.addAnalogSappItem(new SappNumber(cfg.sappItemType, cfg.statusAddr, cfg.divider), channelUID);
            } else if (cfg.channelKind.equalsIgnoreCase(SinthesiBindingConstants.ROLLER)) {
                entity.addDigitalSappItem(new SappRollershutter(cfg.sappItemType, cfg.statusAddr, cfg.statusBit,
                        cfg.upAddr, cfg.upBit, cfg.downAddr, cfg.downBit), channelUID);
            } else if (cfg.channelKind.equalsIgnoreCase(SinthesiBindingConstants.DIMMER)) {
                entity.addAnalogSappItem(new SappDimmer(cfg.sappItemType, cfg.statusAddr), channelUID);
            }
        }
    }

    public void updateItemState(ChannelUID channelUID, State state) {
        updateState(channelUID, state);
    }

    public void updateThingStatus(ThingStatus status) {
        updateStatus(status);
    }

    private void pollWatcher() {
        if ((job == null || job.isDone()) && pollerActive && pollerClass != null) {
            job = scheduler.schedule(pollerClass::statusPoller, 50, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void handleRemoval() {
        disposeJob();
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void dispose() {
        disposeJob();
    }

    private void disposeJob() {
        pollerActive = false;

        if (watchdog != null) {
            watchdog.cancel(true);
        }

        if (job != null) {
            if (pollerClass != null) {
                pollerClass.prepareForDisposal();
            }
            job.cancel(false);
        }

        if (entity.getSapp().isInitialized()) {
            entity.getSapp().close();
        }
    }
}
