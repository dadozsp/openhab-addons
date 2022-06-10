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
package org.openhab.binding.sinthesi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link SinthesiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SinthesiBindingConstants {

    private static final String BINDING_ID = "sinthesi";

    public static final String MASTER_TYPE = "pnmas";
    /* Module Output */
    public static final String OUT_TYPE = "O";
    /* Module Input */
    public static final String INP_TYPE = "I";
    /* Virtual variable */
    public static final String VIRT_TYPE = "V";

    // List of all Thing Type UIDs
    public static final ThingTypeUID MASTER = new ThingTypeUID(BINDING_ID, "pnmas");
    public static final ThingTypeUID OUTPUT = new ThingTypeUID(BINDING_ID, "out");
    public static final ThingTypeUID INPUT = new ThingTypeUID(BINDING_ID, "inp");
    public static final ThingTypeUID VIRTUAL = new ThingTypeUID(BINDING_ID, "virt");

    // List of all Channels Type UIDs
    public static final ChannelTypeUID ANALOG_ITEM = new ChannelTypeUID(BINDING_ID, "analogItem");
    public static final ChannelTypeUID DIGITAL_ITEM = new ChannelTypeUID(BINDING_ID, "digitalItem");

    // List of supported items
    public static final String SWITCH = "Switch";
    public static final String CONTACT = "Contact";
    public static final String NUMBER = "Number";
    public static final String ROLLER = "Rollershutter";
    public static final String DIMMER = "Dimmer";

    public static final double DIMM_DIVIDER = 2.55;
}