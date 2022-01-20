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
package org.openhab.binding.sinthesi.internal.models;

import static org.openhab.binding.sinthesi.internal.SinthesiBindingConstants.*;

import java.io.IOException;
import java.util.*;

import org.openhab.binding.sinthesi.internal.sapp.Sapp;
import org.openhab.binding.sinthesi.internal.sapp.exceptions.SappException;
import org.openhab.binding.sinthesi.internal.sappItems.ISappAnalogItem;
import org.openhab.binding.sinthesi.internal.sappItems.ISappDigitalItem;
import org.openhab.core.thing.ChannelUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SappEntity} class contains all information about the current value of all used variable/inputs
 * and outputs, manages the command queue and contains the methods to update both
 * the displayed value of the various items and the current modules value
 *
 * @author Davide Stefani - Initial contribution
 */
public class SappEntity {
    private Sapp sapp;
    private final Map<Integer, Integer> usedInput;
    private final Map<Integer, Integer> usedOutput;
    private final Map<Integer, Integer> usedVirtual;
    private Map<Integer, Integer> lastInput;
    private Map<Integer, Integer> lastOutput;
    private Map<Integer, Integer> lastVirtual;
    private final Map<ChannelUID, ISappDigitalItem> sappDigitalItems;
    private final Map<ChannelUID, ISappAnalogItem> sappAnalogItems;
    private final List<CommandQueue> commandQueues;
    private boolean firstOutRun;
    private boolean firstInRun;
    private boolean firstVirtRun;
    private final Logger logger = LoggerFactory.getLogger(SappEntity.class);

    public SappEntity() {
        sapp = null;
        usedInput = new HashMap<>();
        usedOutput = new HashMap<>();
        usedVirtual = new HashMap<>();
        lastInput = new HashMap<>();
        lastOutput = new HashMap<>();
        lastVirtual = new HashMap<>();
        sappDigitalItems = new HashMap<>();
        sappAnalogItems = new HashMap<>();
        commandQueues = new ArrayList<>();
        firstOutRun = true;
        firstInRun = true;
        firstVirtRun = true;
    }

    public void purgeEntity() {
        sappDigitalItems.clear();
        sappAnalogItems.clear();
        commandQueues.clear();
        firstOutRun = true;
        firstInRun = true;
        firstVirtRun = true;
    }

    public void setSapp(Sapp sapp) {
        this.sapp = sapp;
    }

    public Sapp getSapp() {
        return sapp;
    }

    public Map<ChannelUID, ISappDigitalItem> getSappDigitalItems() {
        return sappDigitalItems;
    }

    public Map<ChannelUID, ISappAnalogItem> getSappAnalogItems() {
        return sappAnalogItems;
    }

    public Integer tryGetInput(int addr) {
        return usedInput.get(addr);
    }

    public Integer tryGetOutput(int addr) {
        return usedOutput.get(addr);
    }

    public Integer tryGetVirtual(int addr) {
        return usedVirtual.get(addr);
    }

    public List<CommandQueue> getCommandQueues() {
        return commandQueues;
    }

    public void removeFromQueue(CommandQueue command) {
        commandQueues.remove(command);
    }

    public void tryAddInput(int addr) {
        usedInput.put(addr, 0);
    }

    public void tryAddOutput(int addr) {
        usedOutput.put(addr, 0);
    }

    public void tryAddVirtual(int addr) {
        usedVirtual.put(addr, 0);
    }

    public void addToQueue(CommandQueue command) {
        commandQueues.add(command);
    }

    public void setVirtual(int addr, int value) {
        try {
            this.sapp.sappSetVirtual(addr, value);
        } catch (IOException | SappException e) {
            logger.error("Error setting variable {}", addr);
            logger.error("Cause: {}\nStacktrace: {}", e.getCause(), e.getStackTrace());
        }
    }

    public void updateAll() {
        this.updateOutputStatus();
        this.updateInputStatus();
        this.updateVirtualStatus();
        this.updateItemsValue();
        if (firstOutRun || firstInRun || firstVirtRun) {
            firstOutRun = false;
            firstInRun = false;
            firstVirtRun = false;
        }
    }

    public void updateOutputStatus() {
        try {
            lastOutput = sapp.sappGetLastOutput();
            for (Integer k : usedOutput.keySet()) {
                if (!firstOutRun) {
                    if (lastOutput.containsKey(k)) {
                        if (!usedOutput.get(k).equals(lastOutput.get(k))) {
                            usedOutput.put(k, lastOutput.get(k));
                        }
                    }
                } else {
                    usedOutput.put(k, sapp.sappGetOutput(k));
                }
            }
        } catch (Exception e) {
            try {
                sapp.refreshAndRetry();
            } catch (IOException ioException) {
                logger.error("Error on connection refresh");
            }
            logger.warn("Error on output update: {}", e.getMessage());
        }
    }

    public void updateInputStatus() {
        try {
            lastInput = sapp.sappGetLastInput();
            for (Integer k : usedInput.keySet()) {
                if (!firstInRun) {
                    if (lastInput.containsKey(k)) {
                        if (!usedInput.get(k).equals(lastInput.get(k))) {
                            usedInput.put(k, lastInput.get(k));
                        }
                    }
                } else {
                    usedInput.put(k, sapp.sappGetInput(k));
                }
            }
        } catch (Exception e) {
            try {
                sapp.refreshAndRetry();
            } catch (IOException ioException) {
                logger.error("Error on connection refresh");
                logger.error("Cause: {}\nStacktrace: {}", e.getCause(), e.getStackTrace());
            }
            logger.warn("Error on input update: {}", e.getMessage());
        }
    }

    public void updateVirtualStatus() {
        try {
            lastVirtual = sapp.sappGetLastVirtual();
            for (Integer k : usedVirtual.keySet()) {
                if (!firstVirtRun) {
                    if (lastVirtual.containsKey(k)) {
                        if (!usedVirtual.get(k).equals(lastVirtual.get(k))) {
                            usedVirtual.put(k, lastVirtual.get(k));
                        }
                    }
                } else {
                    usedVirtual.put(k, sapp.sappGetVirtual(k));
                    logger.debug("First value: {}->{}", k, usedVirtual.get(k));
                }
            }
        } catch (Exception e) {
            try {
                sapp.refreshAndRetry();
            } catch (IOException ioException) {
                logger.error("Error on connection refresh");
                logger.debug("Cause: {}\nStacktrace: {}", e.getCause(), e.getStackTrace());
            }
            logger.warn("Error on virtual update: {}", e.getMessage());
        }
    }

    public void updateItemsValue() {
        String[] channelKind;
        int addr;
        int bit = 0;
        for (ChannelUID id : sappDigitalItems.keySet()) {
            channelKind = id.toString().split(":")[3].split("#");
            if (channelKind[1].contains("B")) {
                addr = Integer.parseInt(channelKind[1].split("B")[0]);
                bit = Integer.parseInt(channelKind[1].split("B")[1].split("-")[0]);
            } else {
                addr = Integer.parseInt(channelKind[0]);
            }
            switch (channelKind[0]) {
                case OUT_TYPE:
                    if (sappDigitalItems.get(id) != null) {
                        sappDigitalItems.get(id).updateDigitalValue(usedOutput.get(addr), bit);
                    }
                    break;
                case INP_TYPE:
                    if (sappDigitalItems.get(id) != null) {
                        sappDigitalItems.get(id).updateDigitalValue(usedInput.get(addr), bit);
                    }
                    break;
                case VIRT_TYPE:
                    if (sappDigitalItems.get(id) != null) {
                        sappDigitalItems.get(id).updateDigitalValue(usedVirtual.get(addr), bit);
                    }
                    break;
                default:
                    logger.warn("Unknown channel type received: {}", Arrays.toString(channelKind));
            }
        }

        for (ChannelUID id : sappAnalogItems.keySet()) {
            channelKind = id.toString().split(":")[3].split("#");
            addr = Integer.parseInt(channelKind[1].split("-")[0]);
            switch (channelKind[0]) {
                case OUT_TYPE:
                    if (sappAnalogItems.get(id) != null) {
                        sappAnalogItems.get(id).updateAnalogValue(usedOutput.get(addr));
                    }
                    break;
                case INP_TYPE:
                    if (sappAnalogItems.get(id) != null) {
                        sappAnalogItems.get(id).updateAnalogValue(usedInput.get(addr));
                    }
                    break;
                case VIRT_TYPE:
                    if (sappAnalogItems.get(id) != null) {
                        sappAnalogItems.get(id).updateAnalogValue(usedVirtual.get(addr));
                    }
                    break;
                default:
                    logger.warn("Unknown channel type received: {}", Arrays.toString(channelKind));
            }
        }
    }

    public void addDigitalSappItem(ISappDigitalItem item, ChannelUID channelUuid) {
        logger.debug("Added digital item {} ", item.getItemString());
        sappDigitalItems.put(channelUuid, item);
    }

    public void addAnalogSappItem(ISappAnalogItem item, ChannelUID channelUuid) {
        logger.debug("Added digital item {} ", item.getItemString());
        sappAnalogItems.put(channelUuid, item);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o instanceof SappEntity) {
            return ((SappEntity) o).sapp.getSappConnection().getMasAddress()
                    .equals(this.sapp.getSappConnection().getMasAddress());
        }

        return false;
    }
}
