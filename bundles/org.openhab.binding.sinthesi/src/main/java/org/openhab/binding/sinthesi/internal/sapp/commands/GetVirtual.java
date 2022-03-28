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
 * The {@link GetVirtual} class represents the 0x7C command which asks for the current value of the virtual variable
 * x to the master this command does NOT take advantage of differential value retrieval
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class GetVirtual implements ISappCommand<Integer> {
    private final byte[] command;
    private final int nVar;
    private @Nullable SappResponse response;

    public GetVirtual(int nVar) {
        this.nVar = nVar;
        SappByteBuffer buffer = new SappByteBuffer();
        buffer.clear();
        buffer.add((byte) 0x7C);
        buffer.addRange(SappUtils.getHexAsciiWord(nVar));
        this.command = buffer.toArray();
        response = null;
    }

    public int getVar() {
        return this.nVar;
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
    public Integer getResponseData() {
        return response.getDataAsWord();
    }
}
