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
package org.openhab.binding.sinthesi.internal.sapp.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SappCode} enum contains the possible response code of the Sapp protocol
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public enum SappCode {
    COMMAND_PROCESSED,
    ADDRESS_OVER_RANGE,
    ERROR_WRITING_ADDR,
    ADDR_ALREADY_PROG,
    COMMAND_NOT_IMPLEM,
    MODADDR_OVER_RANGE,
    NO_USERPRG,
    NO_PRGMOD,
    VALUE_OVER_RANGE,
    SLAVE_ADDR_NOT_FOUND,
    NO_VALUE_TO_RETURN,
    COMMAND_NOT_PROCESSED,
    COMMAND_NOT_ALLOWED_IN_RUN
}
