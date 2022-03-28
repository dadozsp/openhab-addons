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
package org.openhab.binding.sinthesi.internal.sapp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SappConnection} class manages the connection to a Sinthesi picnet device via Sapp protocol
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SappConnection implements AutoCloseable {
    private String masAddress;
    private int masPort;
    private @Nullable SocketChannel masSocket;
    private int attempt = 0;
    private static final int ATTEMPT_LIMIT = 500;
    private static final int RETRY_LIMIT = 3;
    private boolean unableToConnect = false;
    private final Logger logger = LoggerFactory.getLogger(SappConnection.class);

    public SappConnection(String ip, int port) {
        this.masAddress = ip;
        this.masPort = port;
        masSocket = null;
    }

    public SappConnection() {
        this.masAddress = "";
        this.masPort = 0;
        this.masSocket = null;
    }

    public void sappOeConnect() throws IOException {
        try {
            if (masSocket != null) {
                sappOeDisconnect();
            }

            masSocket = SocketChannel.open();
            masSocket.configureBlocking(false);
            masSocket.connect(new InetSocketAddress(masAddress, masPort));
            for (int i = 0; i < RETRY_LIMIT; i++) {
                while (!masSocket.finishConnect()) {
                    attempt++;
                    Thread.sleep(10);
                    if (attempt > ATTEMPT_LIMIT) {
                        logger.warn("Reached connection attempt limit {} out of {}", i, RETRY_LIMIT);
                        unableToConnect = true;
                        break;
                    }
                }

                if (!unableToConnect) {
                    break;
                }
            }

            if (unableToConnect) {
                sappOeDisconnect();
                logger.error("Unable to connect with host {}:{}", this.masAddress, this.masPort);
            }

        } catch (Exception e) {
            logger.error("Unable to connect with host {}:{}", this.masAddress, this.masPort);
            logger.debug("Cause: {}\nException: {}", e.getCause(), e.getStackTrace());
            masSocket = null;
        }
    }

    public void sappOeDisconnect() {
        if (masSocket != null) {
            try {
                masSocket.close();
            } catch (IOException e) {
                logger.error("Unable to connect with host {}:{}", this.masAddress, this.masPort);
                logger.trace("Cause: {}\nException: {}", e.getCause(), e.getStackTrace());
            } finally {
                masSocket = null;
            }
        }
    }

    public void setMasAddress(String ip) {
        this.masAddress = ip;
    }

    public void setMasPort(int port) {
        this.masPort = port;
    }

    public String getMasAddress() {
        return masAddress;
    }

    public int getMasPort() {
        return masPort;
    }

    public @Nullable SocketChannel getMasSocket() {
        return masSocket;
    }

    public boolean isConnected() {
        return masSocket != null && !unableToConnect;
    }

    public boolean isUnableToConnect() {
        return unableToConnect;
    }

    @Override
    public void close() {
        this.sappOeDisconnect();
    }
}
