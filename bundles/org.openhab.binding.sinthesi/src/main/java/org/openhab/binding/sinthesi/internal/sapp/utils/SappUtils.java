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
package org.openhab.binding.sinthesi.internal.sapp.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sinthesi.internal.sapp.constants.SappOECode;

/**
 * The {@link SappUtils} class contains various utilities to correctly read and write with
 * Sapp protocol supported devices
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SappUtils {

    public static byte[] composeCommand(byte[] command) {
        byte[] completeCom = new byte[command.length + 4];
        byte[] checksum;

        completeCom[0] = SappOECode.STX;
        System.arraycopy(command, 0, completeCom, 1, command.length);
        completeCom[command.length + 1] = SappOECode.ETX;
        checksum = getChecksum(command);
        completeCom[command.length + 2] = checksum[0];
        completeCom[command.length + 3] = checksum[1];

        return completeCom;
    }

    public static byte[] getChecksum(byte[] command) {
        int checksum = 0;

        for (byte b : command) {
            checksum += (b >= 0) ? b : 256 + b;
        }

        return new byte[] { (byte) ((checksum >> 8) & 0xFF), (byte) (checksum & 0xFF) };
    }

    public static byte[] getHexAsciiByte(byte value) {
        byte[] bytes = new byte[2];

        bytes[0] = getHexAsciiCodeFromByte((byte) ((value >> 4) & 0xF));
        bytes[1] = getHexAsciiCodeFromByte((byte) ((value & 0xF)));

        return bytes;
    }

    public static byte[] getHexAsciiWord(int value) {
        byte[] bytes = new byte[4];

        bytes[0] = getHexAsciiCodeFromByte((byte) ((value >> 12) & 0xF));
        bytes[1] = getHexAsciiCodeFromByte((byte) ((value >> 8) & 0xF));
        bytes[2] = getHexAsciiCodeFromByte((byte) ((value >> 4) & 0xF));
        bytes[3] = getHexAsciiCodeFromByte((byte) (value & 0xF));

        return bytes;
    }

    public static byte getHexAsciiCodeFromByte(byte value) {
        return (byte) ((value < 10) ? '0' + value : 'A' + value - 10);
    }

    public static byte getByteFromHexAsciiCode(byte value) {
        return (byte) ((value >= 'A' && value <= 'F') ? value - 'A' + 10
                : (value >= 'a' && value <= 'f') ? value - 'a' + 10 : value - '0');
    }

    public static int byteToUnsigned(byte value) {
        return (value >= 0) ? (int) value : (1 << 8) + value;
    }
}
