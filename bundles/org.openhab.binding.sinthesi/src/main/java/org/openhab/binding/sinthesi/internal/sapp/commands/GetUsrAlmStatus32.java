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
 * The {@link GetUsrAlmStatus32} class represents the command 0x72 which retrives up to 32 user defined alarm
 * and their status
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class GetUsrAlmStatus32 implements ISappCommand<byte[]> {

    private final byte[] command;
    private final byte startAlm;
    private final byte almNum;
    private @Nullable SappResponse response;

    public GetUsrAlmStatus32(byte startAlm, byte almNum) {
        this.startAlm = startAlm;
        this.almNum = almNum;
        SappByteBuffer buffer = new SappByteBuffer();
        buffer.clear();
        buffer.add((byte) 0x72);
        buffer.addRange(SappUtils.getHexAsciiByte(startAlm));
        buffer.addRange(SappUtils.getHexAsciiByte(almNum));
        command = buffer.toArray();
        response = null;
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
    public void setResponse(@Nullable SappResponse response) {
        this.response = response;
    }

    @Override
    public @Nullable SappResponse getResponse() {
        return response;
    }

    @Override
    public byte[] getResponseData() {
        assert response != null;
        return response.getDataAsByteArray();
    }
}
