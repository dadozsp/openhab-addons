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
package org.sinthesi.sapp.commands;

import org.sinthesi.sapp.SappResponse;
import org.sinthesi.sapp.utils.SappByteBuffer;
import org.sinthesi.sapp.utils.SappUtils;

/**
 * The {@link GetInput} class represents the 0x74 command which asks for the current value of input x to the master
 * thi command does NOT take advantage of differential value retrieval
 *
 * @author Davide Stefani - Initial contribution
 */
public class GetInput implements ISappCommand<Integer> {
    private final byte[] command;
    private final byte mod;
    private SappResponse response;

    public GetInput(byte mod) {
        this.mod = mod;
        SappByteBuffer buffer = new SappByteBuffer();
        buffer.clear();
        buffer.add((byte) 0x74);
        buffer.addRange(SappUtils.getHexAsciiByte((byte) mod));
        this.command = buffer.toArray();
    }

    public byte getMod() {
        return mod;
    }

    @Override
    public byte[] getFullCommand() {
        return SappUtils.composeCommand(command);
    }

    @Override
    public void setResponse(SappResponse response) {
        this.response = response;
    }

    @Override
    public SappResponse getResponse() {
        return response;
    }

    @Override
    public Integer getResponseData() {
        return response.getDataAsWord();
    }
}
