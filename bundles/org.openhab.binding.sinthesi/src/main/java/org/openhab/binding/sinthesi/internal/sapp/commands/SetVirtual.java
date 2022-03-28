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
package org.openhab.binding.sinthesi.internal.sapp.commands;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sinthesi.internal.sapp.SappResponse;
import org.openhab.binding.sinthesi.internal.sapp.enums.SappCode;
import org.openhab.binding.sinthesi.internal.sapp.utils.SappByteBuffer;
import org.openhab.binding.sinthesi.internal.sapp.utils.SappUtils;

/**
 * The {@link SetVirtual} class represents the 0x7D command which sets the value of the chosen virtual variable to the
 * indicated value
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SetVirtual implements ISappCommand<SappCode> {
    private final byte[] command;
    private final int nVar;
    private final int value;
    private @Nullable SappResponse response;

    public SetVirtual(int nVar, int value) {
        this.nVar = nVar;
        this.value = value;
        SappByteBuffer buffer = new SappByteBuffer();
        buffer.clear();
        buffer.add((byte) 0x7D);
        buffer.addRange(SappUtils.getHexAsciiWord(nVar));
        buffer.addRange(SappUtils.getHexAsciiWord(value));
        this.command = buffer.toArray();
        response = null;
    }

    public int getVar() {
        return this.nVar;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public byte[] getFullCommand() {
        return SappUtils.composeCommand(this.command);
    }

    @Override
    public void setResponse(@Nullable SappResponse response) {
        this.response = response;
    }

    @Override
    public @Nullable SappResponse getResponse() {
        return response;
    }

    @Override
    public SappCode getResponseData() {
        return SappCode.NO_VALUE_TO_RETURN;
    }
}
