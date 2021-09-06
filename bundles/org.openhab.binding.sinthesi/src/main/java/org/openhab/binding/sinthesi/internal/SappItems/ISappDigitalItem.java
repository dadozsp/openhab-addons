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
package org.openhab.binding.sinthesi.internal.SappItems;

/**
 * The {@link ISappDigitalItem} interface rapresents a generic digital item which can hold only boolean statuses
 *
 * @author Davide Stefani - Initial contribution
 */
public interface ISappDigitalItem {

    void updateDigitalValue(int value, int bit);

    boolean getDigitalValue();

    boolean hasChanged();

    String getItemString();
}
