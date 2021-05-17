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
 * The {@link GetUsrAlmStatus} class represents the 0x70 command which retrieves the indicated user defined alarm
 * and it's value
 *
 * @author Davide Stefani - Initial contribution
 */
public class GetUsrAlmStatus implements ISappCommand<Integer> {

    private final byte nalm;
    private final byte[] command;
    private SappResponse response;

    public GetUsrAlmStatus(byte nalm) {
        this.nalm = nalm;
        SappByteBuffer buffer = new SappByteBuffer();
        buffer.clear();
        buffer.add((byte) 0x70);
        buffer.addRange(SappUtils.getHexAsciiByte(nalm));
        command = buffer.toArray();
    }

    public byte getNalm() {
        return nalm;
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
