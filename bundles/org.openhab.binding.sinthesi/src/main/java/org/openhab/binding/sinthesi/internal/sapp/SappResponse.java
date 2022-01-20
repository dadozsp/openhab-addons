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
package org.openhab.binding.sinthesi.internal.sapp;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.sinthesi.internal.sapp.constants.SappResponseCode;
import org.openhab.binding.sinthesi.internal.sapp.enums.SappCode;
import org.openhab.binding.sinthesi.internal.sapp.utils.SappUtils;

/**
 * The {@link SappResponse} class represents the response received after a sent command,
 *
 * @author Davide Stefani - Initial contribution
 */
public class SappResponse {
    private final byte status;
    private final byte[] data;

    public SappResponse(byte status, byte[] data) {
        this.status = status;
        this.data = data;
    }

    public byte getStatus() {
        return status;
    }

    public byte[] getData() {
        return data;
    }

    public SappCode getCommandResult() {
        if (this.status == 0x01) {
            return SappResponseCode.getErrorMap().get((byte) this.getDataAsWord());
        } else {
            return SappCode.COMMAND_PROCESSED;
        }
    }

    public byte getDataAsByte() {
        byte result = 0;

        for (int i = 0; i < 2; i++) {
            if (i < data.length) {
                result = (byte) (result << 4);
                result += SappUtils.getByteFromHexAsciiCode(data[i]);
            } else {
                break;
            }
        }

        return result;
    }

    public int getDataAsWord() {
        int result = 0;

        for (int i = 0; i < 4; i++) {
            if (i < data.length) {
                result = result << 4;
                result += SappUtils.getByteFromHexAsciiCode(data[i]);
            } else {
                break;
            }
        }

        return result;
    }

    public byte[] getDataAsByteArray() {
        if (data.length < 2) {
            return null;
        }

        byte[] resultArr = new byte[data.length / 2];
        byte result = 0;

        for (int i = 0; i < data.length; i += 2) {
            for (int j = 0; j < 2; j++) {
                if ((i + j) < data.length) {
                    result = (byte) (result << 4);
                    result += SappUtils.getByteFromHexAsciiCode(data[i + j]);
                } else {
                    break;
                }
            }
            resultArr[i / 2] = result;
        }

        return resultArr;
    }

    public int[] getDataAsWordArray() {
        if (data.length < 4) {
            return null;
        }

        int[] resultArr = new int[data.length / 4];
        int result = 0;

        for (int i = 0; i < data.length; i += 4) {
            for (int j = 0; j < 4; j++) {
                if ((i + j) < data.length) {
                    result = result << 4;
                    result += SappUtils.getByteFromHexAsciiCode(data[i + j]);
                } else {
                    break;
                }
            }
            resultArr[i / 4] = result;
        }

        return resultArr;
    }

    public Map<Integer, Integer> getDataAsByteWordMap() {
        Map<Integer, Integer> resultMap = new HashMap<>();

        for (int i = 0; i < data.length; i += 6) {
            int key = 0;
            for (int j = 0; j < 2; j++) {
                if ((i + j) < data.length) {
                    key = (byte) (key << 4);
                    key += SappUtils.getByteFromHexAsciiCode(data[i + j]);
                } else {
                    break;
                }
            }

            int value = 0;
            for (int j = 2; j < 6; j++) {
                if ((i + j) < data.length) {
                    value = value << 4;
                    value += SappUtils.getByteFromHexAsciiCode(data[i + j]);
                } else {
                    break;
                }
            }

            resultMap.put(key & 0xFF, value);
        }

        return resultMap;
    }

    public Map<Integer, Integer> getDataAsWordWordMap() {
        Map<Integer, Integer> resultMap = new HashMap<>();

        for (int i = 0; i < data.length; i += 8) {
            int key = 0;
            for (int j = 0; j < 4; j++) {
                if ((i + j) < data.length) {
                    key = key << 4;
                    key += SappUtils.getByteFromHexAsciiCode(data[i + j]);
                } else {
                    break;
                }
            }

            int value = 0;
            for (int j = 4; j < 8; j++) {
                if ((i + j) < data.length) {
                    value = value << 4;
                    value += SappUtils.getByteFromHexAsciiCode(data[i + j]);
                } else {
                    break;
                }
            }

            resultMap.put(key, value);
        }

        return resultMap;
    }
}
