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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sinthesi.internal.sapp.Sapp;
import org.openhab.binding.sinthesi.internal.sapp.exceptions.SappException;
import org.openhab.binding.sinthesi.internal.sappitems.ISappAnalogItem;
import org.openhab.binding.sinthesi.internal.sappitems.ISappDigitalItem;
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
@NonNullByDefault
public class SappEntity {
    private Sapp sapp;
    private final Map<Integer, @Nullable Integer> usedInput;
    private final Map<Integer, @Nullable Integer> usedOutput;
    private final Map<Integer, @Nullable Integer> usedVirtual;
    private Map<Integer, @Nullable Integer> lastInput;
    private Map<Integer, @Nullable Integer> lastOutput;
    private Map<Integer, @Nullable Integer> lastVirtual;
    private final Map<ChannelUID, ISappDigitalItem> sappDigitalItems;
    private final Map<ChannelUID, ISappAnalogItem> sappAnalogItems;
    private final List<CommandQueue> commandQueues;
    private boolean firstOutRun;
    private boolean firstInRun;
    private boolean firstVirtRun;
    private final Logger logger = LoggerFactory.getLogger(SappEntity.class);

    public SappEntity() {
        sapp = new Sapp();
        usedInput = new HashMap<>();
        usedOutput = new HashMap<>();
        usedVirtual = new HashMap<>();
        lastInput = new HashMap<>();
        lastOutput = new HashMap<>();
        lastVirtual = new HashMap<>();
        sappDigitalItems = new HashMap<>();
        sappAnalogItems = new HashMap<>();
        commandQueues = new CopyOnWriteArrayList<>();
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

    public int tryGetInput(int addr) {
        if (usedInput.containsKey(addr)) {
            Integer value = usedInput.get(addr);

            if (value != null) {
                return value;
            }
        }

        return -1;
    }

    public int tryGetOutput(int addr) {
        if (usedOutput.containsKey(addr)) {
            Integer value = usedOutput.get(addr);

            if (value != null) {
                return value;
            }
        }

        return -1;
    }

    public int tryGetVirtual(int addr) {
        if (usedVirtual.containsKey(addr)) {
            Integer value = usedVirtual.get(addr);

            if (value != null) {
                return value;
            }
        }

        return -1;
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
                        Integer out = usedOutput.get(k);
                        assert out != null;
                        if (lastOutput.containsKey(k) && !out.equals(lastOutput.get(k))) {
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
                        Integer inp = usedInput.get(k);
                        assert inp != null;
                        if (!inp.equals(lastInput.get(k))) {
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
                        Integer virt = usedVirtual.get(k);
                        assert virt != null;
                        if (!virt.equals(lastVirtual.get(k))) {
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
        int addr;
        int bit;

        for (ISappDigitalItem item : sappDigitalItems.values()) {
            addr = item.getReadAddress();
            bit = item.getReadBit();

            if (bit == 0) {
                logger.warn("Item {} ignored, invalid bit ", item.getItemString());
                continue;
            }

            switch (item.getType()) {
                case OUT_TYPE:
                    Integer out = usedOutput.get(addr);
                    assert out != null;
                    item.updateDigitalValue(out, bit);
                    break;
                case INP_TYPE:
                    Integer ing = usedInput.get(addr);
                    assert ing != null;
                    item.updateDigitalValue(ing, bit);
                    break;
                case VIRT_TYPE:
                    Integer vir = usedVirtual.get(addr);
                    assert vir != null;
                    item.updateDigitalValue(vir, bit);
                    break;
                default:
                    logger.warn("Unknown channel type received: {}", item.getType());
            }
        }

        for (ISappAnalogItem item : sappAnalogItems.values()) {

            addr = item.getAddress();
            switch (item.getType()) {
                case OUT_TYPE:
                    item.updateAnalogValue(usedOutput.get(addr));
                    break;
                case INP_TYPE:
                    item.updateAnalogValue(usedInput.get(addr));
                    break;
                case VIRT_TYPE:
                    item.updateAnalogValue(usedVirtual.get(addr));
                    break;
                default:
                    logger.warn("Unknown channel type received: {}", item.getType());
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
    public boolean equals(@Nullable Object o) {
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
