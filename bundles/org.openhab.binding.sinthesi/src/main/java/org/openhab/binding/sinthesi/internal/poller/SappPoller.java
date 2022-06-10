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
package org.openhab.binding.sinthesi.internal.poller;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sinthesi.internal.SinthesiHandler;
import org.openhab.binding.sinthesi.internal.models.CommandQueue;
import org.openhab.binding.sinthesi.internal.models.SappEntity;
import org.openhab.binding.sinthesi.internal.sapp.commands.SetVirtual;
import org.openhab.binding.sinthesi.internal.sappitems.ISappAnalogItem;
import org.openhab.binding.sinthesi.internal.sappitems.ISappDigitalItem;
import org.openhab.binding.sinthesi.internal.sappitems.SappContact;
import org.openhab.binding.sinthesi.internal.sappitems.SappDimmer;
import org.openhab.binding.sinthesi.internal.sappitems.SappNumber;
import org.openhab.binding.sinthesi.internal.sappitems.SappRollershutter;
import org.openhab.binding.sinthesi.internal.sappitems.SappSwitch;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SinthesiHandler} class manages and execute the polling methods to get the new value from the master
 * and updates the linked items with the last changed value
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SappPoller {

    private final SappEntity entity;
    private boolean pollerActive = true;
    private final int pollerTiming;
    private final SinthesiHandler handler;
    private boolean toBeDisposed;
    private final Logger logger = LoggerFactory.getLogger(SappPoller.class);

    public SappPoller(SappEntity entity, int pollerTiming, SinthesiHandler handler) {
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
            while (pollerActive && !toBeDisposed && handler.getThing().getStatus() == ThingStatus.ONLINE) {
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
                    logger.error("Error while polling for updates");
                    logger.error("Cause: {}\nStack: {}", e.getCause(), e.getStackTrace());
                }
            }
        }
    }

    private void updateDigitalItems() {
        synchronized (this) {
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
                        handler.updateItemState(id,
                                (digitalItems.get(id).getDigitalValue()) ? PercentType.valueOf("100")
                                        : PercentType.valueOf("0"));
                    }
                } else {
                    logger.warn("Item with channel {} not recognized", id);
                }
            }
        }
    }

    private void updateAnalogItems() {
        synchronized (this) {
            Map<ChannelUID, ISappAnalogItem> analogItems;

            analogItems = entity.getSappAnalogItems();

            for (ChannelUID id : analogItems.keySet()) {
                if (analogItems.get(id) instanceof SappNumber) {
                    if (analogItems.get(id).hasChanged() && pollerActive) {
                        handler.updateItemState(id, DecimalType.valueOf((analogItems.get(id)).getAnalogValue()));
                    }
                } else if (analogItems.get(id) instanceof SappDimmer) {
                    if (analogItems.get(id).hasChanged() && pollerActive) {
                        handler.updateItemState(id, PercentType.valueOf(analogItems.get(id).getAnalogValue()));
                    }
                }
            }
        }
    }

    private void runQueuedCommands() {
        synchronized (this) {
            List<CommandQueue> queue = entity.getCommandQueues();
            // Collections.reverse(queue); // Run commands from first received through last
            for (CommandQueue elem : queue) {
                if (elem.command == SetVirtual.class) {
                    try {
                        entity.getSapp().sappSetVirtual(elem.address, elem.value.intValue());
                        queue.remove(elem);
                    } catch (Exception e) {
                        logger.error("Error while running queued command");
                    }
                }
            }
        }
    }
}