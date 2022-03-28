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
 * The {@link GetUsrAlmStatus} class represents the 0x70 command which retrieves the indicated user defined alarm
 * and it's value
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class GetUsrAlmStatus implements ISappCommand<Integer> {

    private final byte nalm;
    private final byte[] command;
    private @Nullable SappResponse response;

    public GetUsrAlmStatus(byte nalm) {
        this.nalm = nalm;
        SappByteBuffer buffer = new SappByteBuffer();
        buffer.clear();
        buffer.add((byte) 0x70);
        buffer.addRange(SappUtils.getHexAsciiByte(nalm));
        command = buffer.toArray();
        response = null;
    }

    public byte getNalm() {
        return nalm;
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
    public Integer getResponseData() {
        return response.getDataAsWord();
    }
}
