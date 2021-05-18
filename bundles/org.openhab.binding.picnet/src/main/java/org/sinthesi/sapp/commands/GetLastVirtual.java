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

import java.util.Map;

import org.sinthesi.sapp.SappResponse;
import org.sinthesi.sapp.utils.SappByteBuffer;
import org.sinthesi.sapp.utils.SappUtils;

/**
 * The {@link GetLastVirtual} lass represents the 0x82 command and gets all virtual value that changed since
 * * the last request
 * 
 * @author Davide Stefani - Initial contribution
 */
public class GetLastVirtual implements ISappCommand<Map<Integer, Integer>> {

    private final byte[] command;
    private SappResponse response;

    public GetLastVirtual() {
        SappByteBuffer buffer = new SappByteBuffer();
        buffer.clear();
        buffer.add((byte) 0x82);
        command = buffer.toArray();
    }

    public boolean noNewData() {
        return response.getData().length == 0 && response.getStatus() == 0;
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
        return response.getDataAsWordWordMap();
    }
}