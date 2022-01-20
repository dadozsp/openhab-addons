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
package org.openhab.binding.sinthesi.internal.sappItems;

import static org.openhab.binding.sinthesi.internal.SinthesiBindingConstants.DIMMER;
import static org.openhab.binding.sinthesi.internal.SinthesiBindingConstants.DIMM_DIVIDER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SappDimmer} class rapresents a generic dimmer item.
 *
 * @author Davide Stefani - Initial contribution
 */
public class SappDimmer implements ISappAnalogItem {
    public int valueAddress;
    public String value;
    private boolean changed;
    private static final Logger logger = LoggerFactory.getLogger(SappDimmer.class);

    public SappDimmer(int valueAddress) {
        this.valueAddress = valueAddress;
    }

    @Override
    public void updateAnalogValue(Integer value) {
        String dimmValue = Integer.toString((int) Math.round(value / DIMM_DIVIDER));
        if (!dimmValue.equals(this.value)) {
            this.value = dimmValue;
            changed = true;
        } else {
            changed = false;
        }
    }

    @Override
    public String getAnalogValue() {
        changed = false;
        return value;
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }

    @Override
    public String getItemString() {
        return DIMMER;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o instanceof SappDimmer) {
            return ((SappDimmer) o).valueAddress == this.valueAddress;
        }

        return false;
    }
}
