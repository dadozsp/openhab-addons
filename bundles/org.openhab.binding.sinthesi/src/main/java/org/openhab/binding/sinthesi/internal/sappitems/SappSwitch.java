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

import static org.openhab.binding.sinthesi.internal.SinthesiBindingConstants.SWITCH;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SappSwitch} class represents a switch item
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SappSwitch implements ISappDigitalItem {
    public String type;
    public int stateAddress;
    public int stateBit;
    public int trgAddr;
    public int trgBit;
    public int onVal;
    public int offVal;
    public int moduleValue;
    public boolean value;
    private boolean changed;

    public SappSwitch(String type, int stateAddress, int stateBit, int trgAddr, int trgBit, int onVal, int offVal) {
        this.type = type;
        this.stateAddress = stateAddress;
        this.stateBit = stateBit;
        this.trgAddr = trgAddr;
        this.trgBit = trgBit;
        this.onVal = onVal;
        this.offVal = offVal;
        changed = false;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null) {
            return false;
        }

        if (o instanceof SappSwitch) {
            return ((SappSwitch) o).stateAddress == this.stateAddress && ((SappSwitch) o).stateBit == this.stateBit;
        }

        return false;
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
        return trgAddr;
    }

    @Override
    public int getWriteBit() {
        return trgBit;
    }

    @Override
    public String getType() {
        assert type != null;
        return type;
    }

    @Override
    public void updateDigitalValue(int value, int bit) {
        boolean newVal = (value & (int) Math.pow(2, bit - 1)) != 0;
        if (newVal != this.value) {
            this.value = newVal;
            this.moduleValue = value;
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

    public int getModuleValue() {
        return moduleValue;
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }

    @Override
    public String getItemString() {
        return SWITCH;
    }
}
