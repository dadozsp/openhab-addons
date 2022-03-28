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

import static org.openhab.binding.sinthesi.internal.SinthesiBindingConstants.NUMBER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SappNumber} class rapresents a generic numerical item.
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SappNumber implements ISappAnalogItem {
    public String type;
    public int valueAddress;
    public String value;
    private boolean changed;

    public SappNumber(String type, int valueAddress) {
        this.type = type;
        this.valueAddress = valueAddress;
        value = "";
    }

    @Override
    public int getAddress() {
        return valueAddress;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void updateAnalogValue(@Nullable Integer newValue) {
        if (!newValue.toString().equals(this.value)) {
            this.value = newValue.toString();
            changed = true;
        } else {
            changed = false;
        }
    }

    @Override
    public String getAnalogValue() {
        return value;
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }

    @Override
    public String getItemString() {
        return NUMBER;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null) {
            return false;
        }

        if (o instanceof SappNumber) {
            return ((SappNumber) o).valueAddress == this.valueAddress;
        }

        return false;
    }
}
