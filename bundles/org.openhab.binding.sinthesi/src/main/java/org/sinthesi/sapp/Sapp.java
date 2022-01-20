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
import java.util.Map;

import org.sinthesi.sapp.commands.*;
import org.sinthesi.sapp.enums.SappCode;
import org.sinthesi.sapp.exceptions.SappException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Sapp} class contains the main commands used by the Sapp protocol, it alredy implements some measure of
 * control and automatic retry in case of unexpected errors.
 *
 * @author Davide Stefani - Initial contribution
 */
public class Sapp implements AutoCloseable {
    private final SappConnection connection;
    private final SappExecutor executor;
    private boolean initialized;
    private static final int RETRY_NUM = 3;
    private final Logger logger = LoggerFactory.getLogger(Sapp.class);

    public Sapp() {
        connection = null;
        executor = null;
        initialized = false;
    }

    public Sapp(String ip, int port, int timeout) throws IOException {
        connection = new SappConnection(ip, port);
        executor = new SappExecutor(timeout);
        connection.SappOEConnect();
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public SappConnection getSappConnection() {
        return connection;
    }

    public SappExecutor getExecutor() {
        return executor;
    }

    public Integer sappGetInput(int addr) throws SappException, IOException {
        if (addr < 0 || addr > 250) {
            logger.warn("Invalid module address: {}", addr);
            throw new SappException();
        }
        GetInput command = new GetInput((byte) addr);
        executor.setCommand(command);
        executor.runCommand(connection);
        return command.getResponseData();
    }

    public Integer sappGetOutput(int addr) throws SappException, IOException {
        if (addr < 0 || addr > 250) {
            logger.warn("Invalid module address: {}", addr);
            throw new SappException();
        }
        GetOutput command = new GetOutput((byte) addr);

        tryRun(command);

        return command.getResponseData();
    }

    public Integer sappGetVirtual(int addr) throws SappException, IOException {
        if (addr < 0 || addr > 2500) {
            logger.warn("Invalid virtual variable address: {}", addr);
            throw new SappException();
        }
        GetVirtual command = new GetVirtual(addr);

        tryRun(command);

        return command.getResponseData();
    }

    public void sappSetVirtual(int addr, int value) throws SappException, IOException {
        if (addr < 0 || addr > 2500) {
            logger.warn("Invalid virtual variable address: {}", addr);
            throw new SappException();
        }
        SetVirtual command = new SetVirtual(addr, value);

        tryRun(command);
    }

    public Map<Integer, Integer> sappGetLastVirtual() throws IOException, SappException {
        GetLastVirtual command = new GetLastVirtual();
        int retryCount = 0;

        do {
            tryRun(command);
            retryCount++;
        } while (command.getResponseData().isEmpty() && retryCount <= RETRY_NUM);

        return command.getResponseData();
    }

    public Map<Integer, Integer> sappGetLastOutput() throws IOException, SappException {
        GetLastOutput command = new GetLastOutput();
        int retryCount = 0;

        do {
            tryRun(command);
            retryCount++;
        } while (command.getResponseData().isEmpty() && retryCount <= RETRY_NUM);

        return command.getResponseData();
    }

    public Map<Integer, Integer> sappGetLastInput() throws IOException, SappException {
        GetLastInput command = new GetLastInput();
        int retryCount = 0;

        do {
            tryRun(command);
            retryCount++;
        } while (command.getResponseData().isEmpty() && retryCount <= RETRY_NUM);

        return command.getResponseData();
    }

    public Integer sappGetUsrAlm(int nAlm) throws IOException, SappException {
        GetUsrAlmStatus command = new GetUsrAlmStatus((byte) nAlm);

        tryRun(command);

        return command.getResponseData();
    }

    public byte[] sappGetUsrAlm32(int startAlm, int nAlm) throws IOException, SappException {
        GetUsrAlmStatus32 command = new GetUsrAlmStatus32((byte) startAlm, (byte) nAlm);

        tryRun(command);

        return command.getResponseData();
    }

    private void tryRun(ISappCommand command) throws IOException, SappException {
        int retryCount = 0;
        executor.setCommand(command);
        do {
            executor.runCommand(connection);
            if (command.getResponse() != null
                    && command.getResponse().getCommandResult() == SappCode.COMMAND_PROCESSED) {
                break;
            }
            refreshAndRetry();
            retryCount++;
        } while ((command.getResponse() == null
                || command.getResponse().getCommandResult() != SappCode.COMMAND_PROCESSED) && retryCount <= RETRY_NUM);
    }

    public boolean isDead() {
        return this.connection.isUnableToConnect();
    }

    public void refreshAndRetry() throws IOException {
        logger.debug("Connection refreshed");
        this.connection.SappOEDisconnect();
        this.connection.SappOEConnect();
    }

    @Override
    public void close() {
        initialized = false;
    }
}
