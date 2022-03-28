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
package org.openhab.binding.sinthesi.internal.sapp.commands;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sinthesi.internal.sapp.SappResponse;

/**
 * The {@link ISappCommand} interface represents a generic Sapp protocol command
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public interface ISappCommand<T> {
    byte[] getFullCommand();

    void setResponse(@Nullable SappResponse response);

    @Nullable
    SappResponse getResponse();

    T getResponseData();
}
