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
package org.openhab.binding.picnet.internal.SappItems;

/**
 * The {@link ISappAnalogItem} interface rapresents a generic analog items which can store a numerical value
 *
 * @author Davide Stefani - Initial contribution
 */
public interface ISappAnalogItem {

    void updateAnalogValue(Integer value);

    String getAnalogValue();

    boolean hasChanged();

    String getItemString();
}
