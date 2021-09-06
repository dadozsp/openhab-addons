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
package org.openhab.binding.sinthesi.internal;

/**
 * The {@link ChannelConfig} class holds the parameter needed to setup a new channel
 *
 * @author Davide Stefani - Initial contribution
 */
public class ChannelConfig {
    public String channelKind;
    public int statusAddr;
    public int statusBit;
    public int trgAddr;
    public int trgBit;
    public int upAddr;
    public int upBit;
    public int downAddr;
    public int downBit;
    public int onVal;
    public int offVal;
    public String itemType;
}
