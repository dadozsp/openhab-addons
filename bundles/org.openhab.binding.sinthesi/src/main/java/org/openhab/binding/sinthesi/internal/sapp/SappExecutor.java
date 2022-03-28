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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sinthesi.internal.sapp.commands.ISappCommand;
import org.openhab.binding.sinthesi.internal.sapp.constants.SappOECode;
import org.openhab.binding.sinthesi.internal.sapp.exceptions.SappException;
import org.openhab.binding.sinthesi.internal.sapp.utils.SappByteBuffer;
import org.openhab.binding.sinthesi.internal.sapp.utils.SappUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SappExecutor} class given a connection execute a set command and stores the response
 * inside the command object
 *
 * @author Davide Stefani - Initial contribution
 */
@NonNullByDefault
public class SappExecutor {
    private int timeout = 5000;
    private @Nullable ISappCommand<?> command;
    private @Nullable SappConnection connection;
    private final Logger logger = LoggerFactory.getLogger(SappExecutor.class);

    public SappExecutor() {
    }

    public SappExecutor(int timeout) {
        this.timeout = timeout;
        command = null;
        connection = null;
    }

    public int getTimeout() {
        return timeout;
    }

    public @Nullable ISappCommand<?> getCommand() {
        return command;
    }

    public void setCommand(ISappCommand<?> command) {
        this.command = command;
    }

    public void runCommand(String ip, int port) throws IOException {
        connection = new SappConnection(ip, port);

        try {
            connection.sappOeConnect();
            if (connection.isConnected()) {
                execCommand(connection);
            } else {
                logger.error("Unable to connect to {} at port {}", connection.getMasAddress(), connection.getMasPort());
                throw new SappException();
            }
        } catch (IOException | SappException e) {
            logger.error("Error while running a command");
            logger.debug("Cause: {}\nStacktrace: {}", e.getCause(), e.getStackTrace());
            throw new IOException();
        } finally {
            connection.sappOeDisconnect();
        }
    }

    public void runCommand(SappConnection connection) throws IOException, SappException {
        if (command != null) {
            execCommand(connection);
        } else {
            logger.error("No command set for execution");
            throw new SappException();
        }
    }

    protected void execCommand(@Nullable SappConnection connection) throws IOException {
        this.connection = connection;
        assert connection != null;
        SocketChannel channel = connection.getMasSocket();
        assert command != null;
        byte[] fullCommand = command.getFullCommand();
        ByteBuffer buffer = ByteBuffer.allocate(fullCommand.length);

        buffer.clear();
        buffer.put(fullCommand);
        buffer.flip();

        try {
            if (channel != null) {
                while (buffer.hasRemaining()) {
                    channel.write(buffer);
                }

                readResponse(channel);
            }
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    protected void readResponse(SocketChannel channel) {
        Selector selector = null;

        try {
            byte returnCode;
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);
            returnCode = readSingleByte(selector);

            if (returnCode == SappOECode.NAK) {
                logger.warn("Communication error: NAK received");
                throw new SappException();
            }
            if (returnCode != SappOECode.ACK) {
                logger.warn("Communication error: Unknown return code received");
                throw new SappException();
            }

            byte preamble = readSingleByte(selector);

            if (preamble != SappOECode.STX) {
                logger.warn("Communication error: Invalid preamble received");
                throw new SappException();
            }

            byte commandStatus = readSingleByte(selector);
            int[] checksum = new int[2];
            SappByteBuffer sappBuffer = new SappByteBuffer();

            while (true) {
                byte b = readSingleByte(selector);

                if (b == SappOECode.ETX) {
                    break;
                }
                sappBuffer.add(b);
            }

            byte ch;
            for (int i = 0; i < 2; i++) {
                ch = readSingleByte(selector);
                checksum[i] = SappUtils.byteToUnsigned(ch);
            }

            if (!sappBuffer.isChecksumValid((checksum[0] << 8) + checksum[1] - commandStatus)) {
                logger.warn("Communication error: Invalid checksum received");
                throw new SappException();
            }
            assert command != null;
            command.setResponse(new SappResponse(commandStatus, sappBuffer.toArray()));
        } catch (IOException | SappException e) {
            logger.error("Communication error: Error while reading the sapp response");
            logger.debug("Cause: {}\nStacktrace: {}", e.getCause(), e.getStackTrace());
        } finally {
            if (selector != null) {
                try {
                    selector.close();
                } catch (IOException e) {
                    logger.error("Error while closing the selector");
                    logger.debug("Cause: {}\nStacktrace: {}", e.getCause(), e.getStackTrace());
                }
            }
        }
    }

    private byte readSingleByte(Selector selector) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.clear();

        if (selector.select(this.timeout) > 0) {
            SelectionKey selectionKey = selector.keys().iterator().next();
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            selector.selectedKeys().clear();

            if (selectionKey.isReadable()) {
                if (channel.read(buffer) <= 0) {
                    logger.warn("Communication error: Response not available");
                    throw new IOException();
                }
            }

            buffer.flip();
            return buffer.get();
        } else {
            logger.warn("Communication error: Timeout expired");
            throw new IOException();
        }
    }
}
