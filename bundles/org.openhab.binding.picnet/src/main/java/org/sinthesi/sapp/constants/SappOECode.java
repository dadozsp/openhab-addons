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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SappOECode} class contains the codes used to compose a sapp protocol packet
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public final class SappOECode {
    /* Begin command */
    public static final byte STX = 0x02;
    /* End command */
    public static final byte ETX = 0x03;
    /* Command received */
    public static final byte ACK = 0x06;
    /* Command not received */
    public static final byte NAK = 0x15;
    /* Command executed */
    public static final byte OK = 0x00;
    /* Command failed */
    public static final byte KO = 0x01;
}
