/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.picnet.internal;

import static org.openhab.binding.picnet.internal.PicnetBindingConstants.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.picnet.internal.SappItems.*;
import org.openhab.binding.picnet.internal.models.CommandQueue;
import org.openhab.binding.picnet.internal.models.SappEntity;
import org.openhab.binding.picnet.internal.poller.SappPoller;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.sinthesi.sapp.Sapp;
import org.sinthesi.sapp.commands.SetVirtual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PicnetHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class PicnetHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PicnetHandler.class);
    private final SappEntity entity = new SappEntity();
    @Nullable
    private static ScheduledFuture<?> job, watchdog;
    private static boolean pollerActive;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService watchdogScheduler = Executors.newScheduledThreadPool(1);
    private @Nullable SappPoller pollerClass;

    public PicnetHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("ChannelUID: {}", channelUID);
        logger.info("Command: {}", command);
        if (entity.getSappDigitalItems().get(channelUID) != null
                && SWITCH.equals(entity.getSappDigitalItems().get(channelUID).getItemString())) {
            SappSwitch item = (SappSwitch) entity.getSappDigitalItems().get(channelUID);
            entity.addToQueue(new CommandQueue(SetVirtual.class, item.trgAddr, (int) Math.pow(2, item.trgBit - 1)));

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        } else if (entity.getSappDigitalItems().get(channelUID) != null
                && ROLLER.equals(entity.getSappDigitalItems().get(channelUID).getItemString())) {
            SappRollershutter roller = (SappRollershutter) entity.getSappDigitalItems().get(channelUID);
            if (command == UpDownType.UP) {
                entity.addToQueue(
                        new CommandQueue(SetVirtual.class, roller.upAddr, (int) Math.pow(2, roller.upBit - 1)));
            } else if (command == UpDownType.DOWN) {
                entity.addToQueue(
                        new CommandQueue(SetVirtual.class, roller.downAddr, (int) Math.pow(2, roller.downBit - 1)));
            }

        } else if (entity.getSappAnalogItems().get(channelUID) != null
                && NUMBER.equals(entity.getSappAnalogItems().get(channelUID).getItemString())) {
            SappNumber number = (SappNumber) entity.getSappAnalogItems().get(channelUID);
            entity.addToQueue(
                    new CommandQueue(SetVirtual.class, number.valueAddress, Integer.parseInt(command.toString())));
        }
    }

    @Override
    public void initialize() {
        @Nullable
        PicnetConfiguration config = getConfigAs(PicnetConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);
        assert config != null;
        logger.info("Config valid");
        logger.info("thing info: uuid; {}, label: {}, Id: {}", this.getThing().getUID(), this.getThing().getLabel(),
                this.getThing().getUID().getId());

        if (Objects.equals(this.getThing().getLabel(), MASTER_TYPE)) {
            try {
                entity.setSapp(new Sapp(config.ip, Integer.parseInt(config.port), 5000));
            } catch (IOException e) {
                logger.error("Error during comunication {}", e.getMessage());
            }
            updateStatus(ThingStatus.ONLINE);
            int pollerTiming = config.pollInterval;
            pollerClass = new SappPoller(entity, pollerTiming, this);
            watchdog = watchdogScheduler.scheduleWithFixedDelay(this::pollWatcher, 0, 1, TimeUnit.SECONDS);
            job = scheduler.schedule(pollerClass::statusPoller, 0, TimeUnit.MILLISECONDS);
            logger.info("Poller created");
            logger.info("Connected with PN MAS");
            logger.info("Channel number: {}", getThing().getChannels().size());
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
            case SWITCH:
                res.statusAddr = Integer.parseInt(status[1].split("B")[0]);
                res.statusBit = Integer.parseInt(status[1].split("B")[1]);
                res.trgAddr = Integer.parseInt(cfgParam[1]);
                res.trgBit = Integer.parseInt(cfgParam[2]);
                res.onVal = Integer.parseInt(cfgParam[3]);
                res.offVal = Integer.parseInt(cfgParam[4]);
                break;
            case CONTACT:
                res.statusAddr = Integer.parseInt(status[1].split("B")[0]);
                res.statusBit = Integer.parseInt(status[1].split("B")[0]);
                break;
            case NUMBER:
                res.statusAddr = Integer.parseInt(status[1]);
                break;
            case ROLLER:
                res.statusAddr = Integer.parseInt(status[1].split("B")[0]);
                res.statusBit = Integer.parseInt(status[1].split("B")[1]);
                res.upAddr = Integer.parseInt(cfgParam[1]);
                res.upBit = Integer.parseInt(cfgParam[2]);
                res.downAddr = Integer.parseInt(cfgParam[3]);
                res.downBit = Integer.parseInt(cfgParam[4]);
                break;
            default:
                logger.error("Item type {} not recognized", res.itemType);
        }

        return res;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        ChannelConfig cfg = getConfigFromChannelStr(channelUID);
        boolean validKind = true;
        logger.info("Channel linked {}", channelUID);
        logger.info("Item: {} - {} - {} - {} - {} - {} - {}", cfg.statusAddr, cfg.statusBit, cfg.upAddr, cfg.upBit,
                cfg.downAddr, cfg.downBit, cfg.itemType);

        switch (cfg.channelKind) {
            case OUT_TYPE:
                entity.tryAddOutput(cfg.statusAddr);
                logger.info("Added output");
                break;
            case INP_TYPE:
                entity.tryAddInput(cfg.statusAddr);
                logger.info("Added input");
                break;
            case VIRT_TYPE:
                entity.tryAddVirtual(cfg.statusAddr);
                logger.info("Added virtual");
                break;
            default:
                validKind = false;
                logger.warn("Thing type not supported: {}", this.getThing().getUID());
        }

        if (validKind) {
            if (cfg.itemType.equalsIgnoreCase(SWITCH)) {
                entity.addDigitalSappItem(
                        new SappSwitch(cfg.statusAddr, cfg.statusBit, cfg.trgAddr, cfg.trgBit, cfg.onVal, cfg.offVal),
                        channelUID);
            } else if (cfg.itemType.equalsIgnoreCase(CONTACT)) {
                entity.addDigitalSappItem(new SappContact(cfg.statusAddr, cfg.statusBit), channelUID);
            } else if (cfg.itemType.equalsIgnoreCase(NUMBER)) {
                entity.addAnalogSappItem(new SappNumber(cfg.statusAddr), channelUID);
            } else if (cfg.itemType.equalsIgnoreCase(ROLLER)) {
                entity.addDigitalSappItem(new SappRollershutter(cfg.statusAddr, cfg.statusBit, cfg.upAddr, cfg.upBit,
                        cfg.downAddr, cfg.downBit), channelUID);
            }
        }
    }

    public void updateItemState(ChannelUID channelUID, State state) {
        updateState(channelUID, state);
    }

    public void updateThingStatus(ThingStatus status) {
        updateStatus(status);
    }

    @Override
    public void thingUpdated(Thing thing) {
        logger.info("Thing update called for {}", thing.getUID());
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
