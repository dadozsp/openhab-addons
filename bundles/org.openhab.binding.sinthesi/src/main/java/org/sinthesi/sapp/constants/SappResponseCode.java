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
package org.sinthesi.sapp.constants;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.sinthesi.sapp.enums.SappCode;

/**
 * The {@link SappResponseCode} class contains the possible response code of the Sapp protocol
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SappResponseCode {
    public static final byte COMMAND_PROCESSED = (byte) 0x80;
    public static final byte ADDRESS_OVER_RANGE = (byte) 0x81;
    public static final byte ERROR_WRITING_ADDR = (byte) 0x82;
    public static final byte ADDR_ALREADY_PROG = (byte) 0x83;
    public static final byte COMMAND_NOT_IMPLEM = (byte) 0x84;
    public static final byte MODADDR_OVER_RANGE = (byte) 0x87;
    public static final byte NO_USERPRG = (byte) 0x88;
    public static final byte NO_PRGMOD = (byte) 0x89;
    public static final byte VALUE_OVER_RANGE = (byte) 0x8A;
    public static final byte SLAVE_ADDR_NOT_FOUND = (byte) 0x8B;
    public static final byte NO_VALUE_TO_RETURN = (byte) 0x8C;
    public static final byte COMMAND_NOT_PROCESSED = (byte) 0x8D;
    public static final byte COMMAND_NOT_ALLOWED_IN_RUN = (byte) 0x8E;

    public static Map<Byte, SappCode> errorMap = new HashMap<>();

    public static Map<Byte, SappCode> getErrorMap() {
        if (errorMap.isEmpty()) {
            errorMap.put(COMMAND_PROCESSED, SappCode.COMMAND_PROCESSED);
            errorMap.put(ADDRESS_OVER_RANGE, SappCode.ADDRESS_OVER_RANGE);
            errorMap.put(ERROR_WRITING_ADDR, SappCode.ERROR_WRITING_ADDR);
            errorMap.put(ADDR_ALREADY_PROG, SappCode.ADDR_ALREADY_PROG);
            errorMap.put(COMMAND_NOT_IMPLEM, SappCode.COMMAND_NOT_IMPLEM);
            errorMap.put(MODADDR_OVER_RANGE, SappCode.MODADDR_OVER_RANGE);
            errorMap.put(NO_USERPRG, SappCode.NO_USERPRG);
            errorMap.put(NO_PRGMOD, SappCode.NO_PRGMOD);
            errorMap.put(VALUE_OVER_RANGE, SappCode.VALUE_OVER_RANGE);
            errorMap.put(SLAVE_ADDR_NOT_FOUND, SappCode.SLAVE_ADDR_NOT_FOUND);
            errorMap.put(NO_VALUE_TO_RETURN, SappCode.NO_VALUE_TO_RETURN);
            errorMap.put(COMMAND_NOT_PROCESSED, SappCode.COMMAND_NOT_PROCESSED);
            errorMap.put(COMMAND_NOT_ALLOWED_IN_RUN, SappCode.COMMAND_NOT_ALLOWED_IN_RUN);
        }

        return errorMap;
    }
}
