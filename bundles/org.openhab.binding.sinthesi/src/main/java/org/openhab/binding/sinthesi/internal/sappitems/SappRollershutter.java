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
package org.openhab.binding.sinthesi.internal.sappitems;

import static org.openhab.binding.sinthesi.internal.SinthesiBindingConstants.ROLLER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SappRollershutter} class represents a roller shutter item
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SappRollershutter implements ISappDigitalItem {
    public String type;
    public int stateAddress;
    public int stateBit;
    public int upAddr;
    public int upBit;
    public int downAddr;
    public int downBit;
    public boolean value;
    private boolean changed;

    public SappRollershutter(String type, int stateAddress, int stateBit, int upAddr, int upBit, int downAddr,
            int downBit) {
        this.type = type;
        this.stateAddress = stateAddress;
        this.stateBit = stateBit;
        this.upAddr = upAddr;
        this.upBit = upBit;
        this.downAddr = downAddr;
        this.downBit = downBit;
    }

    @Override
    public int getReadAddress() {
        return stateAddress;
    }

    @Override
    public int getReadBit() {
        return stateBit;
    }

    @Override
    public int getWriteAddress() {
        return 0;
    }

    @Override
    public int getWriteBit() {
        return 0;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void updateDigitalValue(int value, int bit) {
        boolean newVal = (value & (int) Math.pow(2, bit - 1)) != 0;
        if (newVal != this.value) {
            this.value = newVal;
            changed = true;
        } else {
            changed = false;
        }
    }

    @Override
    public boolean getDigitalValue() {
        changed = false;
        return value;
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }

    @Override
    public String getItemString() {
        return ROLLER;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null) {
            return false;
        }

        if (o instanceof SappRollershutter) {
            return ((SappRollershutter) o).stateAddress == this.stateAddress
                    && ((SappRollershutter) o).stateBit == this.stateBit;
        }

        return false;
    }
}
