/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
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
import java.lang.Runnable;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.util.Objects;
import java.util.Vector;

import com.oracle.dio.impl.Transaction;
import com.oracle.dio.power.impl.PowerManagedBase;
import com.oracle.dio.utils.Constants;
import com.oracle.dio.utils.ExceptionMessage;
import com.oracle.dio.utils.Logging;

import jdk.dio.ClosedDeviceException;
import jdk.dio.DeviceConfig;
import jdk.dio.DeviceDescriptor;
import jdk.dio.DeviceManager;
import jdk.dio.DeviceNotFoundException;
import jdk.dio.DevicePermission;
import jdk.dio.InvalidDeviceConfigException;
import jdk.dio.InvalidDeviceConfigException;
import jdk.dio.Transactional;
import jdk.dio.UnavailableDeviceException;
import jdk.dio.spibus.InvalidWordLengthException;
import jdk.dio.spibus.InvalidWordLengthException;
import jdk.dio.spibus.SPIDevice;
import jdk.dio.spibus.SPIDeviceConfig;
import jdk.dio.spibus.SPIPermission;

import romizer.*;
/**
 *Implementation of SPISlave Interface.
 */
class SPISlaveImpl extends Transaction<SPIDevice> implements SPIDevice {

    //every call checkWordLen updates these two variables
    private int byteNum;
    private int bitNum;


    public SPISlaveImpl(DeviceDescriptor<SPIDevice> dscr, int mode) throws
            IOException, DeviceNotFoundException, InvalidDeviceConfigException {
        super(dscr, mode);

        SPIDeviceConfig cfg = dscr.getConfiguration();
        if(cfg.getControllerName() != null) {
            throw new InvalidDeviceConfigException(
                ExceptionMessage.format(ExceptionMessage.DEVICE_OPEN_WITH_DEVICENAME_UNSUPPORTED)
            );
        }

        SPIPermission permission = new SPIPermission(getSecurityName());
        AccessController.checkPermission(permission);

        openSPIDeviceByConfig0(cfg.getControllerNumber(), cfg.getAddress(),
                                        cfg.getCSActiveLevel(), cfg.getClockFrequency(),
                                        cfg.getClockMode(), cfg.getWordLength(),
                                        cfg.getBitOrdering(), mode == DeviceManager.EXCLUSIVE);

        initPowerManagement();

        bitNum = getWordLength0();
        if (bitNum > Constants.MAX_WORD_LEN) {
            throw new IOException (
                ExceptionMessage.format(ExceptionMessage.SPIBUS_SLAVE_WORD_LENGTH, bitNum)
            );
        }
        byteNum = (bitNum - 1)/8 + 1;
    }

    @Override
    public jdk.dio.spibus.SPICompositeMessage createCompositeMessage() {
        return new SPICompositeMessageImpl(this);
    }

    private String getSecurityName(){
        SPIDeviceConfig cfg = dscr.getConfiguration();
        String securityName = (DeviceConfig.DEFAULT == cfg.getControllerNumber()) ? "" : String.valueOf(cfg.getControllerNumber());
        securityName = (DeviceConfig.DEFAULT == cfg.getAddress()) ? securityName : securityName + ":" + cfg.getAddress();
        return securityName;
    }

    protected void checkPowerPermission(){
        SPIDeviceConfig cfg = dscr.getConfiguration();

        String securityName = (DeviceConfig.DEFAULT == cfg.getControllerNumber()) ? "" : String.valueOf(cfg.getControllerNumber());
        securityName = (DeviceConfig.DEFAULT == cfg.getAddress()) ? securityName : securityName + ":" + cfg.getAddress();

        SPIPermission permission = new SPIPermission(securityName, DevicePermission.POWER_MANAGE);

        AccessController.checkPermission(permission);
    }

    /**
     * Gets the transfer word length in bits supported by this slave device.
     * <p>
     * If the length of data to be exchanged belies a slave's word length an {@link InvalidWordLengthException} will be
     * thrown.
     *
     * @return this slave's transfer word length in bits.
     * @throws IOException
     *             if some other I/O error occurs.
     * @throws UnavailableDeviceException
     *             if this peripheral is not currently available - such as it is locked by another application.
     * @throws ClosedDeviceException
     *             if the peripheral has been closed.
     */
    @Override
    public synchronized int getWordLength() throws IOException,
            UnavailableDeviceException, ClosedDeviceException {
        checkPowerState();
        return getWordLength0();
    }

    /**
     * Reads one data word of up to 32 bits from this slave device.
     *
     * @return the data word read.
     * @throws IOException
     *             if some other I/O error occurs.
     * @throws InvalidWordLengthException
     *             if the number of bytes to receive belies word length; that is this slave's word length is bigger than
     *             32 bits.
     * @throws UnavailableDeviceException
     *             if this peripheral is not currently available - such as it is locked by another application.
     * @throws ClosedDeviceException
     *             if the peripheral has been closed.
     */
    @Override
    public int read() throws IOException, UnavailableDeviceException,
            ClosedDeviceException {
        ByteBuffer dst = ByteBuffer.allocateDirect(byteNum);
        transfer(null, 0, dst);
        return byteArray2int(dst);
    }

    /**
     * Reads a sequence of bytes from this slave device into the given buffer.
     * <p />
     * Dummy data will be sent to this slave device by the platform.
     * <p />
     * {@inheritDoc}
     *
     * @param dst
     *            The buffer into which bytes are to be transferred
     *
     * @return The number of bytes read, possibly zero, or {@code -1} if the device has reached end-of-stream
     *
     * @throws NullPointerException
     *             If {@code dst} is {@code null}.
     * @throws InvalidWordLengthException
     *             if the number of bytes to receive belies word length.
     * @throws UnavailableDeviceException
     *             if this peripheral is not currently available - such as it is locked by another application.
     * @throws ClosedDeviceException
     *             if the peripheral has been closed.
     * @throws IOException
     *             If some other I/O error occurs
     */
    @Override
    public int read(ByteBuffer dst) throws IOException,
            UnavailableDeviceException, ClosedDeviceException {
        return read(0, dst);
    }

    /**
     * Reads a sequence of bytes from this device into the given buffer, skipping the first {@code skip} bytes read.
     * <p />
     * Dummy data will be sent to this slave device by the platform.
     * <p />
     * Apart from skipping the first {@code skip} bytes, this method behaves identically to
     * {@link #read(java.nio.ByteBuffer)}.
     *
     * @param skip
     *            the number of read bytes that must be ignored/skipped before filling in the {@code dst} buffer.
     * @param dst
     *            The buffer into which bytes are to be transferred
     *
     * @return The number of bytes read, possibly zero, or {@code -1} if the device has reached end-of-stream
     *
     * @throws NullPointerException
     *             If {@code dst} is {@code null}.
     * @throws InvalidWordLengthException
     *             if the total number of bytes to receive belies word length.
     * @throws UnavailableDeviceException
     *             if this peripheral is not currently available - such as it is locked by another application.
     * @throws ClosedDeviceException
     *             if the peripheral has been closed.
     * @throws IOException
     *             If some other I/O error occurs
     */
    @Override
    public int read(int skip, ByteBuffer dst) throws IOException,
            UnavailableDeviceException, ClosedDeviceException {
        if (0 > skip) {
            throw new IllegalArgumentException();
        }
        checkBuffer(dst);
        return transfer(null, skip, dst);
    }

    @Override
    public int write(ByteBuffer src) throws IOException,
            UnavailableDeviceException, ClosedDeviceException {
        checkBuffer(src);
        return transfer(src, 0, null);
    }

    @Override
    public void write(int txData) throws IOException,
            UnavailableDeviceException, ClosedDeviceException {
        writeAndRead(txData);
    }

    @Override
    public int writeAndRead(ByteBuffer src, ByteBuffer dst)
            throws IOException, UnavailableDeviceException,
            ClosedDeviceException {
        return writeAndRead(src, 0, dst);
    }

    @Override
    public int writeAndRead(ByteBuffer src, int skip, ByteBuffer dst)
            throws IOException, UnavailableDeviceException,
            ClosedDeviceException {
        if (0 > skip) {
            throw new IllegalArgumentException();
        }
        checkBuffer(src);
        checkBuffer(dst);
        return transfer(src, skip, dst);
    }

    @Override
    public int writeAndRead(int txData) throws IOException, UnavailableDeviceException, ClosedDeviceException{
        ByteBuffer tx = int2byteArray(txData);
        ByteBuffer rx = tx.slice();
        transfer(tx, 0, rx);
        return byteArray2int(rx);
    }

    protected int getGrpID() {
        return getGrpID0();
    }

    @Override
    public ByteBuffer getInputBuffer() throws ClosedDeviceException,
            IOException {
        throw new java.lang.UnsupportedOperationException();
    }

    @Override
    public ByteBuffer getOutputBuffer() throws ClosedDeviceException,
            IOException {
        throw new java.lang.UnsupportedOperationException();
    }

    // checkWordLen ought to be called before checkBuffer to get byteNum is up to date
    void checkBuffer(ByteBuffer buffer) {
        Objects.requireNonNull(buffer, ExceptionMessage.format(ExceptionMessage.SPIBUS_NULL_BUFFER));

        if ((buffer.remaining() % byteNum) != 0) {
            throw new InvalidWordLengthException(
                ExceptionMessage.format(ExceptionMessage.SPIBUS_BYTE_NUMBER_BELIES_WORD_LENGTH)
            );
        }
    }

    private ByteBuffer int2byteArray(int intVal) {
        ByteBuffer retA = ByteBuffer.allocateDirect(byteNum);
        for (int i=0; i< byteNum ; i++) {
            retA.put((byte)((intVal >> (8*(byteNum-i-1))) & 0xff));
        }
        retA.flip();
        return retA;
    }

    private int byteArray2int(ByteBuffer byteA) {
        byteA.rewind();
        int retI = 0;
        int tmp;
        for (int i = 0; i< byteNum ;i++) {
            tmp = byteA.get();
            retI |= ((tmp & 0xff ) << (8*(byteNum - i - 1)));
        }
        return retI;
    }

    private int transfer(ByteBuffer src, int skip, ByteBuffer dst) throws IOException {
        return transfer(src, skip, dst, transaction);
    }

    /* Returns number of recevied bytes if dst is not NULL, or number of sent bytes otherwise */
    int transfer(ByteBuffer src, int skip, ByteBuffer dst, int transaction) throws IOException {

        synchronized(this) {
            checkPowerState();
        }

        int xfered = 0;
        final boolean count_recv = null != dst;
        final boolean combined = (0 != skip || (null != dst && null != src && dst.remaining() != src.remaining()));
        Vector<Runnable> localActions = null;

        /* synchronized allows to avoid IllegaStateException for the case when transfer()
           is called while previous operation is incomplete.
           sync on handle to avoid dead lock on close() since close is synchronized on this instance.
        */
        synchronized(handle){

        final boolean trStart = combined && transaction == Transaction.REGULAR_MESSAGE;

        if (trStart) {
            // can throw ISE
            transaction = beginTransaction();
            if (!(this instanceof Transactional)) {
                localActions = new Vector<>();
            }
        }

        try {
            do {
                // convert tries to align toSend and toRecv buffer length if src or dst are nondirect.
                ByteBuffer toRecv = convert(dst, src);
                ByteBuffer toSend = convert(src, toRecv);


                // if there is nothing to send, use recv buffer as dummy send data
                if (null == toSend) {
                    if (null == toRecv) {
                        // both empty buffers
                        return xfered;
                    }
                    toSend = toRecv.slice();
                }

                if (null != toRecv) {
                    // always align send and recv buffers len,
                    // or recv to NULL buffer
                    if (toSend.remaining() <= skip) {
                        toRecv = null;
                    }
                }


                try {
                    conditionalLock();
                    writeAndRead0(toSend, toRecv);
                } finally {
                    conditionalUnlock();
                }

                if (null != src) {

                    if (null != localActions) {
                        final ByteBuffer ref = toSend;
                        localActions.add(new Runnable() {
                                public void run() {
                                    // dummy action to retain object refence
                                    ref.remaining();
                                };
                            });
                    }

                    if (!count_recv) {
                        xfered += toSend.remaining();
                    }
                    shiftBufferPosition(src, src.position() + toSend.remaining());
                }

                if (skip > 0) {
                    if (null != toRecv) {
                        // ability to fit 'skip' bytes was checked above (see if (toSend.remaining() <= skip) )
                        toRecv.position(skip);
                        skip = 0;
                    } else {
                        skip-=toSend.remaining();
                    }
                }
                if (null != toRecv) {
                    xfered += toRecv.remaining();
                    // linux and similar accumulate packets and transfer them after endTrasaction call
                    // transaction requires postponed reverse copying
                    if (null != localActions) {
                        final ByteBuffer to = dst.slice();
                        final ByteBuffer from = toRecv.slice();
                        localActions.add(new Runnable() {
                                public void run() {
                                    to.put(from);
                                };
                            });

                        try {
                            dst.position(dst.position() + toRecv.remaining());
                        }catch (IllegalArgumentException e){
                            // the buffer was updated in parallel
                            Logging.reportWarning(ExceptionMessage.format(ExceptionMessage.BUFFER_IS_MODIFIED));
                            //
                            dst.position(dst.limit());
                        }

                    } else {
                        dst.put(toRecv);
                    }
                }

            } while ((null != dst && dst.hasRemaining()) || (null != src && src.hasRemaining()));

        } finally  {
            if (trStart) {
                try {
                    endTransaction(transaction);
                    if (null != localActions) {
                        for (Runnable toRun: localActions) {
                            toRun.run();
                        }
                    }
                } catch (IllegalStateException e) {
                    // intentionally skip
                }
            }
        }
        }//synchronized(handle)
        return xfered;
    }

    // transaction demarcator
    private static int trans_counter;

    protected int beginTransaction() throws IOException, IllegalStateException {
        // interapp lock
        conditionalLock();

        begin0();
        // it is enough to separate transactions in scope of current application enviroment.
        // the rest is cared by javacall
        synchronized(SPISlaveImpl.class) {
            return (transaction = trans_counter++);
        }
    }

    protected void endTransaction(int transaction) throws IllegalStateException {
        if (transaction != this.transaction) {
            throw new IllegalStateException();
        }

        end0();

        // interapp unlock
        conditionalUnlock();
    }

    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native int begin0() throws IOException, UnsupportedOperationException, IllegalStateException;
    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native int end0() throws IllegalStateException;

    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.unlock_func_ptr",
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.Handle.lock_func_ptr",
        "com.oracle.dio.impl.Handle.close_func_ptr",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native void openSPIDeviceByConfig0(int deviceNumber, int address,
                                              int csActive, int clockFrequency,
                                              int clockMode, int wordLen,
                                              int bitOrdering, boolean exclusive);

    /* PREREQUISITES: either dst.len must be equals to src.len or dst must null */
    @Local(DontRemoveFields = {
        "java.nio.Buffer.position",
        "java.nio.Buffer.limit",
        "java.nio.Buffer.data",
        "java.nio.Buffer.arrayOffset",
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native void writeAndRead0(ByteBuffer src, ByteBuffer dst) throws IOException;

    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native int getGrpID0();
    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native int getWordLength0();
    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native int getByteOrdering0();
}
