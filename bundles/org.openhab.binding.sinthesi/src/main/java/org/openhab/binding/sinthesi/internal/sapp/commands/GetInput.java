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
import org.openhab.binding.sinthesi.internal.sapp.utils.SappByteBuffer;
import org.openhab.binding.sinthesi.internal.sapp.utils.SappUtils;

/**
 * The {@link GetInput} class represents the 0x74 command which asks for the current value of input x to the master
 * thi command does NOT take advantage of differential value retrieval
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class GetInput implements ISappCommand<Integer> {
    private final byte[] command;
    private final byte mod;
    private @Nullable SappResponse response;

    public GetInput(byte mod) {
        this.mod = mod;
        SappByteBuffer buffer = new SappByteBuffer();
        buffer.clear();
        buffer.add((byte) 0x74);
        buffer.addRange(SappUtils.getHexAsciiByte((byte) mod));
        this.command = buffer.toArray();
        response = null;
    }

    public byte getMod() {
        return mod;
    }

    @Override
    public byte[] getFullCommand() {
        return SappUtils.composeCommand(command);
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
    @SuppressWarnings("null")
    public Integer getResponseData() {
        assert response != null;
        return response.getDataAsWord();
    }
}
