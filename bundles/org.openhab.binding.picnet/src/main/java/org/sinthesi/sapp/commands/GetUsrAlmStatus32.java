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
 * The {@link GetUsrAlmStatus32} class represents the command 0x72 which retrives up to 32 user defined alarm
 * and their status
 *
 * @author Davide Stefani - Initial contribution
 */
public class GetUsrAlmStatus32 implements ISappCommand<byte[]> {

    private final byte[] command;
    private final byte startAlm;
    private final byte almNum;
    private SappResponse response;

    public GetUsrAlmStatus32(byte startAlm, byte almNum) {
        this.startAlm = startAlm;
        this.almNum = almNum;
        SappByteBuffer buffer = new SappByteBuffer();
        buffer.clear();
        buffer.add((byte) 0x72);
        buffer.addRange(SappUtils.getHexAsciiByte(startAlm));
        buffer.addRange(SappUtils.getHexAsciiByte(almNum));
        command = buffer.toArray();
    }

    public byte getStartAlm() {
        return startAlm;
    }

    public byte getAlmNum() {
        return almNum;
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
    public byte[] getResponseData() {
        return response.getDataAsByteArray();
    }
}
