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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SappVariable} describes a base sapp variable that represents an output an input or a virtual variable
 * all of them can represent an integer value of the bit is set to 0 or a boolean value otherwise
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SappVariable {
    private final String VarType;
    private final int Address;
    private final int Bit;

    public SappVariable(String varType, int addr, int bit) {
        VarType = varType;
        Address = addr;
        Bit = bit;
    }

    public SappVariable(String variable) {
        String[] param = variable.split(":");

        if (param.length < 2 || param.length > 3)
            throw new IllegalArgumentException("Invalid variable received");

        VarType = param[0];
        Address = Integer.parseInt(param[1]);

        if (param.length == 3) {
            Bit = Integer.parseInt(param[2]);
        } else {
            Bit = 0;
        }
    }

    public String getVarType() {
        return VarType;
    }

    public int getAddr() {
        return Address;
    }

    public int getBit() {
        return Bit;
    }
}
