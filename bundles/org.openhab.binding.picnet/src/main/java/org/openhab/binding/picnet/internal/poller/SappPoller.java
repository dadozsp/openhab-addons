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
package org.openhab.binding.picnet.internal.poller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.openhab.binding.picnet.internal.PicnetHandler;
import org.openhab.binding.picnet.internal.SappItems.*;
import org.openhab.binding.picnet.internal.models.CommandQueue;
import org.openhab.binding.picnet.internal.models.SappEntity;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.sinthesi.sapp.commands.SetVirtual;
import org.sinthesi.sapp.exceptions.SappException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PicnetHandler} class manages and execute the polling methods to get the new value from the master
 * and updates the linked items with the last changed value
 *
 * @author Davide Stefani - Initial contribution
 */
public class SappPoller {

    private final SappEntity entity;
    private boolean pollerActive = true;
    private final int pollerTiming;
    private final PicnetHandler handler;
    private boolean toBeDisposed;
    private final Logger logger = LoggerFactory.getLogger(SappPoller.class);

    public SappPoller(SappEntity entity, int pollerTiming, PicnetHandler handler) {
        this.entity = entity;
        this.pollerTiming = pollerTiming;
        this.handler = handler;
        this.toBeDisposed = false;
    }

    public void prepareForDisposal() {
        toBeDisposed = true;
    }

    public void startPoller() {
        pollerActive = true;
    }

    public void stopPoller() {
        pollerActive = false;
    }

    public void statusPoller() {
        synchronized (this) {
            while (pollerActive && !toBeDisposed) {
                if (entity.getSapp().isDead()) {
                    handler.updateThingStatus(ThingStatus.OFFLINE);
                    this.stopPoller();
                }

                try {
                    entity.updateAll();

                    updateDigitalItems();
                    updateAnalogItems();

                    runQueuedCommands();
                    Thread.sleep(pollerTiming);
                } catch (Exception e) {
                    logger.error("{}->{}", e, Arrays.toString(e.getStackTrace()));
                }
            }
        }
    }

    private void updateDigitalItems() {
        Map<ChannelUID, ISappDigitalItem> digitalItems;

        digitalItems = entity.getSappDigitalItems();

        for (ChannelUID id : digitalItems.keySet()) {
            if (digitalItems.get(id) instanceof SappSwitch) {
                if (digitalItems.get(id).hasChanged() && pollerActive) {

                    handler.updateItemState(id,
                            (digitalItems.get(id).getDigitalValue()) ? OnOffType.ON : OnOffType.OFF);
                }
            } else if (digitalItems.get(id) instanceof SappContact) {
                if (digitalItems.get(id).hasChanged() && pollerActive) {
                    handler.updateItemState(id,
                            (digitalItems.get(id).getDigitalValue()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
                }
            } else if (digitalItems.get(id) instanceof SappRollershutter) {
                if (digitalItems.get(id).hasChanged() && pollerActive) {
                    handler.updateItemState(id, (digitalItems.get(id).getDigitalValue()) ? PercentType.valueOf("100")
                            : PercentType.valueOf("0"));
                }
            }
        }
    }

    private void updateAnalogItems() {
        Map<ChannelUID, ISappAnalogItem> analogItems;

        analogItems = entity.getSappAnalogItems();

        for (ChannelUID id : analogItems.keySet()) {
            if (analogItems.get(id) instanceof SappNumber) {
                if (analogItems.get(id).hasChanged()) {
                    handler.updateItemState(id, DecimalType.valueOf((analogItems.get(id)).getAnalogValue()));
                }
            }
        }
    }

    private void runQueuedCommands() {
        synchronized (this) {
            for (Iterator<CommandQueue> c = entity.getCommandQueues().iterator(); c.hasNext();) {
                CommandQueue elem = c.next();
                if (elem.command == SetVirtual.class) {
                    try {
                        entity.getSapp().sappSetVirtual(elem.address, elem.value);
                        c.remove();
                    } catch (IOException | SappException e) {
                        logger.error("Error while running a command");
                    }
                }
            }
        }
    }
}
