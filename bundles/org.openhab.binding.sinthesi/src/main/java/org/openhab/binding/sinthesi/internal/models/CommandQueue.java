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
package org.openhab.binding.sinthesi.internal.models;

/**
 * The {@link CommandQueue} is responsible for storing received commands before sending them to the PN MAS
 * via sapp protocol
 *
 * @author Davide Stefani - Initial contribution
 */
public class CommandQueue {
    public Class<?> command;
    public int address;
    public int value;

    public CommandQueue(Class<?> command, int address, int value) {
        this.command = command;
        this.address = address;
        this.value = value;
    }
}
