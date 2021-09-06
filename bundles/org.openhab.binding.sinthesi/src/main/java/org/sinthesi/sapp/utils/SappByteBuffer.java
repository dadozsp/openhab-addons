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
package org.sinthesi.sapp.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SappByteBuffer} class contains the buffer to process Sapp protocol comunications
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SappByteBuffer {
    private final List<Byte> buffer;

    public SappByteBuffer() {
        buffer = new ArrayList<>();
    }

    public void clear() {
        buffer.clear();
    }

    public void add(byte value) {
        buffer.add(value);
    }

    public void addRange(byte[] values) {
        for (byte b : values) {
            buffer.add(b);
        }
    }

    public byte[] toArray() {
        byte[] result = new byte[buffer.size()];

        for (int i = 0; i < buffer.size(); i++) {
            result[i] = buffer.get(i);
        }

        return result;
    }

    public boolean isChecksumValid(int checksum) {
        int bufferChecksum = 0;

        for (Byte b : buffer) {
            bufferChecksum += SappUtils.byteToUnsigned(b);
        }

        return checksum == bufferChecksum;
    }
}
