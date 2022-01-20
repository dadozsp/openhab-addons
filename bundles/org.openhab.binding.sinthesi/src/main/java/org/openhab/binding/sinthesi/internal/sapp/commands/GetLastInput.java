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

import java.util.Map;

import org.openhab.binding.sinthesi.internal.sapp.SappResponse;
import org.openhab.binding.sinthesi.internal.sapp.utils.SappByteBuffer;
import org.openhab.binding.sinthesi.internal.sapp.utils.SappUtils;

/**
 * The {@link GetLastInput} class represents the 0x81 command and gets all input value that changed since
 * the last request
 *
 * @author Davide Stefani - Initial contribution
 */
public class GetLastInput implements ISappCommand<Map<Integer, Integer>> {

    private final byte[] command;
    private SappResponse response;

    public GetLastInput() {
        SappByteBuffer buffer = new SappByteBuffer();
        buffer.clear();
        buffer.add((byte) 0x81);
        command = buffer.toArray();
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
    public Map<Integer, Integer> getResponseData() {
        return response.getDataAsByteWordMap();
    }
}
