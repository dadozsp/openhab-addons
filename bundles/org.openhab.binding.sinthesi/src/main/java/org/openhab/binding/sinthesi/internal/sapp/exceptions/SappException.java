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
package org.openhab.binding.sinthesi.internal.sapp.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SappException} class contains a generic SappException
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SappException extends Exception {

    private static final long serialVersionUID = 42L;

    public SappException() {
        super();
    }

    public SappException(String message) {
        super(message);
    }
}
