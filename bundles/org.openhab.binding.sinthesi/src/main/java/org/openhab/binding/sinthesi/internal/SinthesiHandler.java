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
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sinthesi.internal.models.CommandQueue;
import org.openhab.binding.sinthesi.internal.models.SappEntity;
import org.openhab.binding.sinthesi.internal.poller.SappPoller;
import org.openhab.binding.sinthesi.internal.sapp.Sapp;
import org.openhab.binding.sinthesi.internal.sapp.commands.SetVirtual;
import org.openhab.binding.sinthesi.internal.sappItems.*;
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
        logger.debug("ChannelUID: {}", channelUID);
        logger.debug("Command: {}", command);

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
            entity.addToQueue(
                    new CommandQueue(SetVirtual.class, number.valueAddress, Integer.parseInt(command.toString())));
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
        logger.debug("thing info: uuid; {}, label: {}, Id: {}", this.getThing().getUID(), this.getThing().getLabel(),
                this.getThing().getUID().getId());

        if (Objects.equals(this.getThing().getLabel(), SinthesiBindingConstants.MASTER_TYPE)) {
            try {
                entity.setSapp(new Sapp(config.ip, Integer.parseInt(config.port), 5000));
            } catch (IOException e) {
                logger.error("Error during comunication");
                logger.debug("Cause:{}\nException: {}", e.getCause(), e.getStackTrace());
            }
            updateStatus(ThingStatus.ONLINE);
            int pollerTiming = config.pollInterval;
            pollerClass = new SappPoller(entity, pollerTiming, this);
            watchdog = watchdogScheduler.scheduleWithFixedDelay(this::pollWatcher, 0, 1, TimeUnit.SECONDS);
            job = scheduler.schedule(pollerClass::statusPoller, 0, TimeUnit.MILLISECONDS);
            logger.debug("Connected with PN MAS");
            logger.debug("Channel number: {}", getThing().getChannels().size());
            entity.purgeEntity();
            for (Channel c : getThing().getChannels()) {
                this.channelLinked(c.getUID());
            }
            pollerActive = true;
        }
    }

    public ChannelConfig getConfigFromChannelStr(ChannelUID uid) {
        String[] strId = uid.toString().split(":");
        String[] cfgParam = strId[strId.length - 1].split("-");
        String[] status = cfgParam[0].split("#");
        ChannelConfig res = new ChannelConfig();

        res.itemType = cfgParam[cfgParam.length - 1];
        res.channelKind = status[0];

        switch (res.itemType) {
            case SinthesiBindingConstants.SWITCH:
                res.statusAddr = Integer.parseInt(status[1].split("B")[0]);
                res.statusBit = Integer.parseInt(status[1].split("B")[1]);
                res.trgAddr = Integer.parseInt(cfgParam[1]);
                res.trgBit = Integer.parseInt(cfgParam[2]);
                res.onVal = Integer.parseInt(cfgParam[3]);
                res.offVal = Integer.parseInt(cfgParam[4]);
                break;
            case SinthesiBindingConstants.CONTACT:
                res.statusAddr = Integer.parseInt(status[1].split("B")[0]);
                res.statusBit = Integer.parseInt(status[1].split("B")[0]);
                break;
            case SinthesiBindingConstants.NUMBER:
            case SinthesiBindingConstants.DIMMER:
                res.statusAddr = Integer.parseInt(status[1]);
                break;
            case SinthesiBindingConstants.ROLLER:
                res.statusAddr = Integer.parseInt(status[1].split("B")[0]);
                res.statusBit = Integer.parseInt(status[1].split("B")[1]);
                res.upAddr = Integer.parseInt(cfgParam[1]);
                res.upBit = Integer.parseInt(cfgParam[2]);
                res.downAddr = Integer.parseInt(cfgParam[3]);
                res.downBit = Integer.parseInt(cfgParam[4]);
                break;
            default:
                logger.warn("Item type {} not recognized", res.itemType);
        }

        return res;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        ChannelConfig cfg = getConfigFromChannelStr(channelUID);
        boolean validKind = true;
        logger.debug("Channel linked {}", channelUID);
        logger.debug("Item: {} - {} - {} - {} - {} - {} - {}", cfg.statusAddr, cfg.statusBit, cfg.upAddr, cfg.upBit,
                cfg.downAddr, cfg.downBit, cfg.itemType);

        switch (cfg.channelKind) {
            case SinthesiBindingConstants.OUT_TYPE:
                entity.tryAddOutput(cfg.statusAddr);
                logger.debug("Added output");
                break;
            case SinthesiBindingConstants.INP_TYPE:
                entity.tryAddInput(cfg.statusAddr);
                logger.debug("Added input");
                break;
            case SinthesiBindingConstants.VIRT_TYPE:
                entity.tryAddVirtual(cfg.statusAddr);
                logger.debug("Added virtual");
                break;
            default:
                validKind = false;
                logger.warn("Thing type not supported: {}", this.getThing().getUID());
        }

        if (validKind) {
            if (cfg.itemType.equalsIgnoreCase(SinthesiBindingConstants.SWITCH)) {
                entity.addDigitalSappItem(
                        new SappSwitch(cfg.statusAddr, cfg.statusBit, cfg.trgAddr, cfg.trgBit, cfg.onVal, cfg.offVal),
                        channelUID);
            } else if (cfg.itemType.equalsIgnoreCase(SinthesiBindingConstants.CONTACT)) {
                entity.addDigitalSappItem(new SappContact(cfg.statusAddr, cfg.statusBit), channelUID);
            } else if (cfg.itemType.equalsIgnoreCase(SinthesiBindingConstants.NUMBER)) {
                entity.addAnalogSappItem(new SappNumber(cfg.statusAddr), channelUID);
            } else if (cfg.itemType.equalsIgnoreCase(SinthesiBindingConstants.ROLLER)) {
                entity.addDigitalSappItem(new SappRollershutter(cfg.statusAddr, cfg.statusBit, cfg.upAddr, cfg.upBit,
                        cfg.downAddr, cfg.downBit), channelUID);
            } else if (cfg.itemType.equalsIgnoreCase(SinthesiBindingConstants.DIMMER)) {
                entity.addAnalogSappItem(new SappDimmer(cfg.statusAddr), channelUID);
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
