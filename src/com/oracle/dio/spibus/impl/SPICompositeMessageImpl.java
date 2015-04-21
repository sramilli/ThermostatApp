/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.oracle.dio.spibus.impl;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.oracle.dio.utils.ExceptionMessage;

import jdk.dio.ClosedDeviceException;
import jdk.dio.UnavailableDeviceException;
import jdk.dio.spibus.SPICompositeMessage;
import jdk.dio.spibus.SPIDevice;


final class SPICompositeMessageImpl implements SPICompositeMessage {

    private ArrayList<Message> messageList = new ArrayList<>();

    private boolean isAlreadyTransferedOnce;

    /* Owner of the message */
    private SPISlaveImpl device;

    // delay between operations
    private int delay;

    private class Message {
        ByteBuffer tx, rx;
        int skip, delay;
        public Message(ByteBuffer tx, int skip, ByteBuffer rx, int delay) {
            this.rx = rx;
            this.skip = skip;
            this.tx = tx;
            this.delay = delay;
        }
    }

    private void checkStatus() {
        if (isAlreadyTransferedOnce) {
            throw new IllegalStateException(
                ExceptionMessage.format(ExceptionMessage.I2CBUS_ALREADY_TRANSFERRED_MESSAGE)
            );
        }
    }

    private void check(Message message) throws ClosedDeviceException {

        checkStatus();

        if (0 > message.skip) {
            throw new IllegalArgumentException(
                ExceptionMessage.format(ExceptionMessage.I2CBUS_NEGATIVE_SKIP_ARG)
            );
        }

        if (!device.isOpen()) {
            throw new ClosedDeviceException();
        }

        for (int i = 0; i < messageList.size(); i++) {
            ByteBuffer tx = messageList.get(i).tx;
            ByteBuffer rx = messageList.get(i).rx;
            if (    (null != tx && (tx == message.tx ||
                                    tx == message.rx) )
                 || (null != rx && (rx == message.tx ||
                                    rx == message.rx ) ) ) {
                throw new IllegalArgumentException(
                    ExceptionMessage.format(ExceptionMessage.I2CBUS_BUFFER_GIVEN_TWICE)
                );
            }
        }
    }

    /**
     * Creates a new {@code SPICompositeMessageImpl} instance.
     */
    SPICompositeMessageImpl(SPISlaveImpl device) {
        this.device = device;
    }

    @Override
    public SPICompositeMessage appendRead(ByteBuffer rxBuf)  throws IOException, ClosedDeviceException {
        return appendRead(0, rxBuf);
    }

    @Override
    public SPICompositeMessage appendRead(int rxSkip, ByteBuffer rxBuf) throws IOException, ClosedDeviceException {
        device.checkBuffer(rxBuf);
        return append(null, rxSkip, rxBuf);
    }

    @Override
    public SPICompositeMessage appendWrite( ByteBuffer txBuf) throws IOException,
            ClosedDeviceException {
        device.checkBuffer(txBuf);
        return append(txBuf, 0, null);
    }

    @Override
    public SPICompositeMessage appendWriteAndRead(ByteBuffer src, ByteBuffer dst) throws IOException, ClosedDeviceException {
        return appendWriteAndRead(src, 0, dst);
    }

    @Override
    public SPICompositeMessage appendWriteAndRead(ByteBuffer src, int skip, ByteBuffer dst) throws IOException, ClosedDeviceException{
        device.checkBuffer(src);
        device.checkBuffer(dst);
        return append(src,skip,dst);
    }

    private SPICompositeMessage append(ByteBuffer src, int skip, ByteBuffer dst)  throws IOException, ClosedDeviceException {
        Message message = new Message(src, skip, dst, delay);
        check(message);
        messageList.add(message);
        return this;
    }

    @Override
    public synchronized SPICompositeMessage appendDelay(int delay) {

        checkStatus();

        this.delay = delay;
        return this;
    }

    @Override
    public SPIDevice getTargetedDevice() {
        return device;
    }

    @Override
    public int[] transfer() throws IOException, UnavailableDeviceException, ClosedDeviceException {
        int bytesRead[];

        // global handle lock to prevent access from other threads.
        synchronized (device.getHandle()) {
            /* Forbid adding more messages to this combined message */
            isAlreadyTransferedOnce = true;
            if (0 == messageList.size()) {
                return null;
            }

            int transaction = device.beginTransaction();

            try {
                final int size = messageList.size();
                bytesRead = new int[size];

                for (int i = 0; i < size; i++) {
                    Message message = messageList.get(i);
                    int res = device.transfer(message.tx, message.skip, message.rx, transaction);
                    if (null != message.rx) {
                        bytesRead[i] = res;
                    }
                }

            } finally {
                device.endTransaction(transaction);
            }

        }
        return bytesRead;
    }
}
