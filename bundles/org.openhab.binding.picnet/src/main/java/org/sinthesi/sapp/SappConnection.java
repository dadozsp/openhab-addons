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
package org.sinthesi.sapp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.openhab.binding.picnet.internal.PicnetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SappConnection} class manages the connection to a picnet device via Sapp protocol
 *
 * @author Davide Stefani - Initial contribution
 */
public class SappConnection implements AutoCloseable {
    private String masAddress;
    private int masPort;
    private SocketChannel masSocket;
    private int attempt = 0;
    private final static int ATTEMPT_LIMIT = 500;
    private boolean unableToConnect = false;
    private final Logger logger = LoggerFactory.getLogger(PicnetHandler.class);

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

    public void SappOEConnect() throws IOException {
        try {
            if (masSocket != null) {
                SappOEDisconnect();
            }

            masSocket = SocketChannel.open();
            masSocket.configureBlocking(false);
            masSocket.connect(new InetSocketAddress(masAddress, masPort));
            while (!masSocket.finishConnect()) {
                attempt++;
                Thread.sleep(10);
                if (attempt > ATTEMPT_LIMIT) {
                    logger.warn("Reached attemp limit");
                    unableToConnect = true;
                    break;
                }
            }

            if (unableToConnect) {
                SappOEDisconnect();
                logger.error("Impossibile connettersi all'idirizzo {}:{}", this.masAddress, this.masPort);
            }

        } catch (Exception e) {
            logger.error("Impossibile aprire la connessione con {}, Exception: {}", this.masAddress, e.toString());
            masSocket = null;
        }
    }

    public void SappOEDisconnect() {
        if (masSocket != null) {
            try {
                masSocket.close();
            } catch (IOException e) {
                logger.error("Imposibile chiudure la connessione con {}", this.masAddress);
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

    public SocketChannel getMasSocket() {
        return masSocket;
    }

    public boolean isConnected() {
        return masSocket != null && !unableToConnect;
    }

    public boolean isUnableToConnect() {
        return unableToConnect;
    }

    @Override
    public void close() throws Exception {
        this.SappOEDisconnect();
    }
}
