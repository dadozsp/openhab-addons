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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sinthesi.internal.SinthesiBindingConstants;

/**
 * The {@link ISappDigitalItem} class represents a contact item
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SappContact implements ISappDigitalItem {
    public String type;
    public int stateAddress;
    public int stateBit;
    public boolean value;
    public boolean changed;

    public SappContact(String type, int stateAddress, int stateBit) {
        this.type = type;
        this.stateAddress = stateAddress;
        this.stateBit = stateBit;
        this.value = false;
        changed = true;
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
        return SinthesiBindingConstants.CONTACT;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null) {
            return false;
        }

        if (o instanceof SappContact) {
            return ((SappContact) o).stateAddress == this.stateAddress && ((SappContact) o).stateBit == this.stateBit;
        }

        return false;
    }
}
