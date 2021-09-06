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
package org.openhab.binding.sinthesi.internal.SappItems;

import org.openhab.binding.sinthesi.internal.SinthesiBindingConstants;

/**
 * The {@link ISappDigitalItem} class represents a contact item
 *
 * @author Davide Stefani - Initial contribution
 */
public class SappContact implements ISappDigitalItem {

    public int stateAddress;
    public int stateBit;
    public boolean value;
    public boolean changed;

    public SappContact(int stateAddress, int stateBit) {
        this.stateAddress = stateAddress;
        this.stateBit = stateBit;
        changed = false;
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
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o instanceof SappContact) {
            return ((SappContact) o).stateAddress == this.stateAddress && ((SappContact) o).stateBit == this.stateBit;
        }

        return false;
    }
}
