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

package com.oracle.dio.uart.impl;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.AccessControlException;
import java.security.AccessController;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import com.oracle.dio.power.impl.PowerManagedBase;
import com.oracle.dio.utils.Configuration;
import com.oracle.dio.utils.Constants;
import com.oracle.dio.utils.ExceptionMessage;
import com.oracle.dio.utils.Logging;
import com.oracle.dio.utils.PrivilegeController;
import com.oracle.dio.utils.PrivilegedAction;

import jdk.dio.*;
import jdk.dio.uart.*;

import romizer.*;

class UARTImpl extends PowerManagedBase<UART> implements UART {
    private boolean isWriting;

    private Object synchReadLock = new Object();

    private ByteBuffer writeBuffers[] = new ByteBuffer[2];
    private int writeBuffersPositions[] = new int[2];

    private ByteBuffer readBuffers[] = new ByteBuffer[2];
    private int readBuffersPositions[] = new int[2];

    private int readBufferIdx = 0;
    private int writeBufferIdx = 0;

    private InputRoundListener<UART, ByteBuffer> inRoundListener;
    private OutputRoundListener<UART, ByteBuffer> outRoundListener;

    private Hashtable<Integer, UARTEventListener> eventListeners;

    private int receiveTriggerLevel;
    private int inputTimeout = Integer.MAX_VALUE;//timeout is disabled


    UARTImpl(DeviceDescriptor<UART> dscr, int mode)
                                throws DeviceNotFoundException, InvalidDeviceConfigException, UnsupportedAccessModeException{
        super(dscr, mode);

        String deviceName;
        byte[] devName; // UTF-8 device name

        if( mode != DeviceManager.EXCLUSIVE){
            throw new UnsupportedAccessModeException();
        }

        UARTConfig cfg = dscr.getConfiguration();

        //deviceName = uart device prefix + device number
        deviceName = getSecurityName();

        if (deviceName == null){
            throw new DeviceNotFoundException(
                ExceptionMessage.format(ExceptionMessage.UART_CANT_GET_PORT_NAME)
            );
        }

        UARTPermission permission = new UARTPermission(deviceName);
        AccessController.checkPermission(permission);

        try{
            devName = deviceName.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new DeviceNotFoundException(
                ExceptionMessage.format(ExceptionMessage.UART_UTF8_UNCONVERTIBLE_DEVNAME)
            );
        }

        openUARTByConfig0(devName, cfg.getBaudRate(), cfg.getStopBits(), cfg.getFlowControlMode(),
                                   cfg.getDataBits(), cfg.getParity(), mode == DeviceManager.EXCLUSIVE);

        isWriting = false;
        eventListeners = new Hashtable<Integer, UARTEventListener>();
        initPowerManagement();
    }

    private InputRoundListener getLocalInputRoundListener(){
        return new InputRoundListener<UART, ByteBuffer>() {
                @Override
                public void inputRoundCompleted(RoundCompletionEvent<UART, ByteBuffer> event) {
                    synchronized(synchReadLock){
                        synchReadLock.notifyAll();
                    }
                }

                @Override
                public void failed(Throwable ex, UART arg1) {
                    synchronized(synchReadLock){
                        synchReadLock.notifyAll();
                    }
                }
            };
    }


    private boolean isAlphaNumerical(char ch) {
        if ((('a' <= ch && ch <= 'z') ||
             ('A' <= ch && ch <= 'Z') ||
             ('0' <= ch && ch <= '9'))) {
            return true;
        }
        return false;
    }

    private String getSecurityName() {
        UARTConfig cfg = dscr.getConfiguration();
        int devNum = cfg.getControllerNumber();
        String securityName = null;

        if (null != cfg.getControllerName()) {
            securityName = cfg.getControllerName();
            for (int i = 0; i < securityName.length(); i++) {
                if(!isAlphaNumerical(securityName.charAt(i))) {
                    // for security reason to prohibit usage of "../"
                    // and to align with MEEP spec
                    Logging.reportError("Unacceptable device name:", securityName);
                    return null;
                }
            }

        } else {
            if (devNum == DeviceConfig.DEFAULT) {
                devNum = 0;
            }
            // first port in list is DEFAULT port
            try {
                String ports = Configuration.getProperty("microedition.commports");
                if (null != ports) {
                    StringTokenizer t = new StringTokenizer(ports, ",");
                    while(devNum-- > 0 && t.hasMoreTokens()) {
                        t.nextToken();
                    }
                    // if no more tokens - returns null
                    if (t.hasMoreTokens()) {
                        securityName = t.nextToken();
                    }
                }
            } catch (AccessControlException  e) {
                // SE app must not be aware about ME property
            }

        }

        return securityName;
    }

    protected void checkPowerPermission(){
        AccessController.checkPermission(new UARTPermission(getSecurityName(), DevicePermission.POWER_MANAGE));
    }

    protected synchronized void processEvent(int event, int bytesProcessed){
        UARTEventListener listener = eventListeners.get(event);
        if (listener != null){
            try{
                UARTEvent uartEvent = new UARTEvent(this, event);
                listener.eventDispatched(uartEvent);
            } catch(Throwable e){
                //do nothing
            }
        }

        switch(event){
        case UARTEvent.INPUT_DATA_AVAILABLE:
            if (inRoundListener != null){
                ByteBuffer buffer = readBuffers[readBufferIdx];
                if (null == buffer) {
                    try{
                        inRoundListener.failed(new Exception("Event processing error. Read buffer is null"), this);
                    }catch(Exception e){
                        //do nothing
                    }
                    return;
                }
                /*
                    read0 is designed to copy available data from the javacall buffer to java buffer,
                    because of that no slice() call is necessary, the following is redundand:

                    int bytesReaden = read0(buffer.slice());

                */
                int tmpPos = buffer.position();
                int bytesRead = read0(buffer);
                try{
                    shiftBufferPosition(buffer, tmpPos + bytesRead);
                }catch(IOException e){

                }

                if(!buffer.hasRemaining() || ( receiveTriggerLevel !=0 && (buffer.position() - readBuffersPositions[readBufferIdx]) >= receiveTriggerLevel) || (-1 == bytesProcessed)){
                    RoundCompletionEvent<UART,ByteBuffer> rcEvent =
                        new RoundCompletionEvent(this, buffer, buffer.position() - readBuffersPositions[readBufferIdx]);

                    if (null != readBuffers[1]) {
                        //2 buffers schema
                        //switch buffers, than notify user
                        readBufferIdx = readBufferIdx == 0 ? 1 : 0;
                        buffer = readBuffers[readBufferIdx];
                        readBuffersPositions[readBufferIdx] = buffer.position();

                        //notify user
                        try{
                            inRoundListener.inputRoundCompleted(rcEvent);
                        }catch(Exception e){
                            //do nothing, listener should not throw an exception
                        }
                    }else{
                        //1 buffer
                        //notify the user first, then keep reading
                        try{
                            inRoundListener.inputRoundCompleted(rcEvent);
                            readBuffersPositions[readBufferIdx] = buffer.position();
                        }catch(Exception e){
                            //do nothing, listener should not throw an exception
                        }
                    }//end of else 1 buffer
                }
            }
            break;

        case UARTEvent.INPUT_BUFFER_OVERRUN:
            break;
        case UARTEvent.OUTPUT_BUFFER_EMPTY:
            if (outRoundListener != null){
                ByteBuffer buffer = writeBuffers[writeBufferIdx];

                if (null == buffer) {
                    try{
                        outRoundListener.failed(new Exception("Event processing error. Write buffer is null"), this);
                    }catch(Exception e){
                        //do nothing, listener should not throw an exception
                    }
                    return;
                }
                buffer.position(buffer.position() + bytesProcessed);

                if (!buffer.hasRemaining()) {
                    RoundCompletionEvent<UART,ByteBuffer> rcEvent = new RoundCompletionEvent(this, buffer, buffer.position() - writeBuffersPositions[writeBufferIdx]);

                    if (null != writeBuffers[1]) {
                        //2 byffers
                        //switch buffers, than notify user
                        writeBufferIdx = writeBufferIdx == 0 ? 1 : 0;
                        buffer = writeBuffers[writeBufferIdx];
                        //keep writing from the second buffer before user notice
                        if(isWriting){
                            if (buffer.hasRemaining()){
                                writeAsynch0(buffer);
                            }
                        }
                        //notify user
                        try{
                            outRoundListener.outputRoundCompleted(rcEvent);
                        }catch(Exception e){
                            //do nothing, listener should not throw an exception
                        }
                    }else{
                        //1 buffer
                        //notify user first, then keep writing
                        try{
                            outRoundListener.outputRoundCompleted(rcEvent);
                        }catch(Exception e){
                            //do nothing, listener should not throw an exception
                        }
                        if(isWriting){
                            if (buffer.hasRemaining()){
                                writeAsynch0(buffer);
                            }
                        }
                    }//end of else 1 buffer
                }else{ //buffer has remaining, keep writing
                    if(isWriting){
                        write0(buffer);
                    }
                }
            }//if (outRoundListener != null)
            break;
        }//switch(event)
    }

    /**
     * Gets the current baud rate. If the baud rate was not set previously using {@link #setBaudRate(int)} the
     * peripheral configuration-specific default value is returned.
     *
     * @return the current baud rate.
     */
    @Override
    public synchronized int getBaudRate() throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkPowerState();
        return getBaudRate0();

    }

    /**
     * Gets the current number of bits per character.
     *
     */
    @Override
    public synchronized int getDataBits() throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkPowerState();
        return getDataBits0();
    }

    @Override
    public int getFlowControlMode() throws IOException, UnavailableDeviceException, ClosedDeviceException {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the current parity.
     *
     */
    @Override
    public synchronized int getParity() throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkPowerState();
        return getParity0();
    }

    /**
     * Gets the current number of stop bits per character.
     *
     */
    @Override
    public synchronized int getStopBits() throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkPowerState();
        return getStopBits0();
    }

    /**
     * Sets the baud rate.
     *
     */
    @Override
    public synchronized void setBaudRate(int baudRate) throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkPowerState();
        if(baudRate <= 0){
            throw new java.lang.UnsupportedOperationException();
        }
        setBaudRate0( baudRate);
    }

    /**
     * Sets the number of bits per character.
     *
     */
    @Override
    public synchronized void setDataBits(int dataBits) throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkPowerState();
        setDataBits0( dataBits);
    }

    /**
     * Registers a {@link UARTEventListener} instance to monitor input data availability, input buffer overrun or
     * empty output buffer conditions. While the listener can be triggered by hardware interrupts, there are no
     * real-time guarantees of when the listener will be called.
     */
    @Override
    public synchronized void setEventListener(int eventId, UARTEventListener listener) throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkOpen();

        if (eventId != UARTEvent.INPUT_BUFFER_OVERRUN && eventId != UARTEvent.INPUT_DATA_AVAILABLE &&
            eventId != UARTEvent.OUTPUT_BUFFER_EMPTY && eventId != UARTEvent.BREAK_INTERRUPT &&
            eventId != UARTEvent.PARITY_ERROR &&eventId != UARTEvent.FRAMING_ERROR){
                throw new IllegalArgumentException();
        }
        UARTEventListener registeredListener = eventListeners.get(eventId);

        if (listener != null && registeredListener != null){
            //got listener for the eventId
            throw new IllegalStateException();
        }

        if (listener == null){
            //remove listener for the eventId
             eventListeners.remove(eventId);
             unsubscribe(eventId);
             // remove handlers
        }else{
             eventListeners.put(eventId, listener);
             subscribe(eventId);
        }
    }

    private void subscribe(int eventId){
        UARTEventHandler.getInstance().addEventListener(eventId, this);
        setEventListener0(eventId);
    }

    private void unsubscribe(int eventId){
        /*
         * 2 listeners work withINPUT_DATA_AVAILABLE :
         * user defined + internal for filling buffers
         */
        if(eventId == UARTEvent.INPUT_DATA_AVAILABLE){
            if(inRoundListener != null || eventListeners.get(eventId) != null){
                return;
            }
        }

        UARTEventHandler.getInstance().removeEventListener(eventId, this);
        removeEventListener0(eventId);
    }

    @Override
    public void setFlowControlMode(int flowcontrol) throws IOException, UnavailableDeviceException, ClosedDeviceException {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the parity.
     */
    @Override
    public synchronized void setParity(int parity) throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkPowerState();
        setParity0(parity);
    }

    /**
     * Sets the number of stop bits per character.
     *
     */
    @Override
    public synchronized void setStopBits(int stopBits) throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkPowerState();
        setStopBits0(stopBits);
    }

    /**
     * Starts asynchronous writing in sucessive rounds - initially writing the data remaining in the provided
     * buffer. Additional rounds are asynchronously fetched by notifying the provided {@link OutputRoundListener}
     * instance once the initial data have been written. The initial data to be written
     * is retrieved from the provided buffer; the data to write during the subsequent rounds is retrieved
     * from that very same buffer upon invocation od the provided {@link OutputRoundListener} instance.
     */
    @Override
    public synchronized void startWriting(ByteBuffer src, OutputRoundListener<UART, ByteBuffer> listener) throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkPowerState();
        checkWrite();

        if(src == null || listener == null){
            throw new NullPointerException(
                ExceptionMessage.format(ExceptionMessage.UART_NULL_SRC_OR_LISTENER)
            );
        }

        writeBuffers[0] = src;
        writeBuffersPositions[0] = src.position();
        writeBufferIdx = 0;
        outRoundListener = listener;
        subscribe(UARTEvent.OUTPUT_BUFFER_EMPTY);
        writeAsynch0(src);
        isWriting = true;
    }

    /**
     * Starts asynchronous writing in successive rounds.
     */
    @Override
    public synchronized void startWriting(ByteBuffer src1, ByteBuffer src2, OutputRoundListener<UART, ByteBuffer> listener) throws IOException,
        UnavailableDeviceException, ClosedDeviceException{

        if(src1 == null || src2 == null || listener == null){
            throw new NullPointerException(
                ExceptionMessage.format(ExceptionMessage.UART_NULL_SRC1_OR_SRC2_OR_LISTENER)
            );
        }

        writeBuffers[1] = src2;
        writeBuffersPositions[1] = src2.position();
        startWriting(src1, listener);
    }

    /**
     * Stops (cancels) the currently active writing session.
     */
    @Override
    public synchronized void stopWriting() throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkOpen();
        if (isWriting){
            outRoundListener = null;
            unsubscribe(UARTEvent.OUTPUT_BUFFER_EMPTY);
            writeBuffers[0] = writeBuffers[1] = null;
            stopWriting0();
            isWriting = false;
        }
    }

    /**
     * Starts asynchronous reading in sucessive rounds - reading data into the provided
     * buffer.
     */
    @Override
    public synchronized void startReading(ByteBuffer src, InputRoundListener<UART, ByteBuffer> listener) throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkPowerState();
        checkRead();

        if(src == null || listener == null){
            throw new NullPointerException(
                ExceptionMessage.format(ExceptionMessage.UART_NULL_SRC_OR_LISTENER)
            );
        }

        inRoundListener = listener;
        readBuffers[0] = src;
        readBuffersPositions[0] = src.position();
        readBufferIdx = 0;
        /*
                        subscribe calls set_event_listener, in case of INPUT_DATA_AVAILABLE
                        the native function checks if data available in the internal
                        buffer and generates INPUT_DATA_AVAILABLE event if so.
                */
        subscribe(UARTEvent.INPUT_DATA_AVAILABLE);
    }

    /**
     * Starts asynchronous reading in sucessive rounds.
     */
    @Override
    public synchronized void startReading(ByteBuffer src1, ByteBuffer src2, InputRoundListener<UART, ByteBuffer> listener) throws IOException,
            UnavailableDeviceException, ClosedDeviceException{

        if(src1 == null || src2 == null || listener == null){
            throw new NullPointerException(
                ExceptionMessage.format(ExceptionMessage.UART_NULL_SRC1_OR_SRC2_OR_LISTENER)
            );
        }
        readBuffers[1] = src2;
        readBuffersPositions[1] = src2.position();
        startReading(src1, listener);
    }

    /**
     * Stops (cancels) the currently active reading session.
      */
    @Override
    public synchronized void stopReading() throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkOpen();
        if (inRoundListener != null){
            inRoundListener = null;
            unsubscribe(UARTEvent.INPUT_DATA_AVAILABLE);
            readBuffers[0] = readBuffers[1] = null;
            stopReading0();
        }
    }

    /**
     * Generates a break condition for the specified duration.
     */
    @Override
    public synchronized void generateBreak(int duration) throws IOException, UnavailableDeviceException, ClosedDeviceException{
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the receive trigger level
     */
    @Override
    public synchronized void setReceiveTriggerLevel(int level) throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkPowerState();
        if(level < 0){
            throw new IllegalArgumentException();
        }
        receiveTriggerLevel = level;
    }

    /**
     * Gets the current receive trigger level.
     *
     */
    @Override
    public synchronized int getReceiveTriggerLevel() throws IOException, UnavailableDeviceException, ClosedDeviceException{
        checkPowerState();
        return receiveTriggerLevel;
    }

    /**
     * Reads a sequence of bytes from this UART into the given buffer.
     */
    @Override
    public int read(ByteBuffer dst) throws IOException,
            UnavailableDeviceException, ClosedDeviceException{
        int ret;

        synchronized(this){
            checkRead();
        }

        synchronized(handle){

            if (dst == null){
                throw new NullPointerException(
                    ExceptionMessage.format(ExceptionMessage.UART_NULL_DST)
                );
            }

            if(!dst.hasRemaining()){
                ret = 0;
            }else{
                ret = dst.position();
                /*read all data available*/
                int readRes = read0(dst);
                shiftBufferPosition(dst, ret + readRes);
                if (dst.hasRemaining()) {
                    if (!UARTEventHandler.getInstance().isDispatchThread()) {
                        /*
                         * the user calls read() from the event callback, or inputTimeout is 0
                         * exit immediatelly,
                         * else read with timeout
                         */
                        startReading(dst, getLocalInputRoundListener());
                        synchronized(synchReadLock){
                            try{
                                if(inputTimeout == Integer.MAX_VALUE){
                                    //timeout disabled, wait forever or till the buffer is fullfilled
                                    synchReadLock.wait();
                                }else if (inputTimeout > 0) {
                                    synchReadLock.wait(inputTimeout);
                                }
                            }catch(InterruptedException iE){
                                throw new IOException();
                            }
                        } // synch
                        stopReading();
                    }
                } // if !event thread
                ret = dst.position() - ret;
            } // if has Remaining
        } // synch handle
        return ret;
    }

    /**
     * Writes a sequence of bytes to this UART from the given buffer.
     */
    @Override
    public int write(ByteBuffer src) throws IOException,
            UnavailableDeviceException, ClosedDeviceException{
        if (src == null){
            throw new NullPointerException(
                ExceptionMessage.format(ExceptionMessage.UART_NULL_SRC)
            );
        }

        synchronized (this) {
            checkPowerState();
            checkWrite();
        }

        int ret = 0;
        try {
            isWriting = true;
            /*
             * synchronous write0 returns number of written bytes
             * slice is needed to avoid memory corruption because src buffer modification
             * might happen during write0
             */
            ret = write0(src.slice());
            try{
                src.position(src.position() + ret);
            }catch(IllegalArgumentException e){
                //IAE happens if src.position() + ret < 0 (not expected) or src.position() + ret > limit
                src.position(src.limit());
            }
        } finally {
            isWriting = false;
            return ret;
        }
    }

    @Override
    public synchronized void close() throws IOException{
        if (isOpen()) {
            synchronized(synchReadLock){
                synchReadLock.notifyAll();
            }
            stopWriting();
            stopReading();
            super.close();
        }
    }

    @Override
    public synchronized int getReceiveTimeout() throws IOException, UnavailableDeviceException, ClosedDeviceException {
        checkPowerState();
        return inputTimeout;
    }

    @Override
    public synchronized void setReceiveTimeout(int timeout) throws IOException, UnavailableDeviceException, ClosedDeviceException {
        checkPowerState();
        if(timeout < 0 ){
            throw new UnsupportedOperationException(
                    ExceptionMessage.format(ExceptionMessage.UART_NEGATIVE_TIMEOUT)
            );
        }
        inputTimeout = timeout;
    }


    private void checkRead(){
        if (inRoundListener != null){
            throw new IllegalStateException(
                ExceptionMessage.format(ExceptionMessage.UART_ACTIVE_READ_OPERATION)
            );
        }
    }

    private void checkWrite(){
        if (isWriting){
            throw new IllegalStateException(
                ExceptionMessage.format(ExceptionMessage.UART_ACTIVE_WRITE_OPERATION)
            );
        }
    }


    protected synchronized int getGrpID() {
        return getUartId0();
    }

    public synchronized ByteBuffer getInputBuffer() throws ClosedDeviceException,
            IOException {
        throw new java.lang.UnsupportedOperationException();
    }

    public synchronized ByteBuffer getOutputBuffer() throws ClosedDeviceException,
            IOException {
        throw new java.lang.UnsupportedOperationException();
    }

    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native void removeEventListener0(int eventId);
    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native void setEventListener0(int eventId);

    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.unlock_func_ptr",
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.Handle.lock_func_ptr",
        "com.oracle.dio.impl.Handle.close_func_ptr",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native void openUARTByConfig0(byte[] devName, int baudrate, int stopBits, int flowControl, int bitsPerChar, int parity, boolean exclusive);
    /*
     * write0 does not shift the buffer's position, it only returns bytes wrote, in case of asynch operation it must return 0.
     */
    @Local(DontRemoveFields = {
        "java.nio.Buffer.position",
        "java.nio.Buffer.limit",
        "java.nio.Buffer.data",
        "java.nio.Buffer.arrayOffset",
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native int write0(ByteBuffer src);

    /*
     * starts asynch write session
     */
    @Local(DontRemoveFields = {
        "java.nio.Buffer.position",
        "java.nio.Buffer.limit",
        "java.nio.Buffer.data",
        "java.nio.Buffer.arrayOffset",
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native void writeAsynch0(ByteBuffer src);
    /*
     * read0 does not shift the buffer's position, it only returns bytes read.
     */
    @Local(DontRemoveFields = {
        "java.nio.Buffer.position",
        "java.nio.Buffer.limit",
        "java.nio.Buffer.data",
        "java.nio.Buffer.arrayOffset",
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native int read0(ByteBuffer src);

    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native int getBaudRate0();
    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native void setBaudRate0(int baudRate);

    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native int getDataBits0();
    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native void setDataBits0(int dataBits);

    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native int getParity0();
    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native int setParity0(int parity);

    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native int getStopBits0();
    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native int setStopBits0(int stopBits);

    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native void stopWriting0();
    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native void stopReading0();

    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native int getUartId0();
}
