/*
 * Copyright (c) 2012, 2014, Oracle and/or its affiliates. All rights reserved.
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
import java.io.IOException;

import com.oracle.dio.utils.ExceptionMessage;

import jdk.dio.ClosedDeviceException;
import jdk.dio.DeviceDescriptor;
import jdk.dio.DeviceNotFoundException;
import jdk.dio.InvalidDeviceConfigException;
import jdk.dio.UnavailableDeviceException;
import jdk.dio.UnsupportedAccessModeException;
import jdk.dio.modem.ModemSignalEvent;
import jdk.dio.modem.ModemSignalListener;
import jdk.dio.modem.ModemSignalsControl;
import jdk.dio.uart.ModemUART;
import jdk.dio.uart.UART;

import romizer.*;

class ModemUARTImpl extends UARTImpl implements ModemUART, ModemSignalDispatcher.SerialSignalListener {

    private ModemSignalListener modemListener;

    ModemUARTImpl(DeviceDescriptor<UART> dscr, int mode)
                                throws DeviceNotFoundException, InvalidDeviceConfigException, UnsupportedAccessModeException{
        super(dscr, mode);
    }


    /**
     * Sets or clears the designated signal.
     */
    public synchronized void setSignalState(int signalID, boolean state) throws IOException,
                                UnavailableDeviceException, ClosedDeviceException{

        if (signalID != ModemSignalsControl.DTR_SIGNAL && signalID != ModemSignalsControl.RTS_SIGNAL) {

            throw new IllegalArgumentException(
                    ExceptionMessage.format(ExceptionMessage.UART_UNKNOWN_SIGNAL_ID)
            );
        }

        checkPowerState();
        setDTESignalState0( signalID, state);
    }
    /**
     * Gets the state of the designated signal.
     *
     */
    public synchronized boolean getSignalState(int signalID) throws IOException,
                                UnavailableDeviceException, ClosedDeviceException{

        if (signalID != ModemSignalsControl.CTS_SIGNAL && signalID != ModemSignalsControl.DCD_SIGNAL &&
            signalID != ModemSignalsControl.DSR_SIGNAL && signalID != ModemSignalsControl.RI_SIGNAL) {

            throw new IllegalArgumentException(
                ExceptionMessage.format(ExceptionMessage.UART_UNKNOWN_SIGNAL_ID)
            );
        }
        checkPowerState();
        return getDCESignalState0( signalID);
    };

    /**
     * Registers a {@link ModemSignalListener} instance which will get asynchronously notified when one of the
     * designated signals changes. Notification will automatically begin after registration completes.
     */
    public synchronized void setSignalChangeListener(ModemSignalListener<ModemUART> listener, int signals)
            throws IOException, UnavailableDeviceException, ClosedDeviceException{

        // valid IDs of signals to monitor: DCD_SIGNAL, DSR_SIGNAL, RI_SIGNAL or CTS_SIGNAL.
        int tmpS = ~(ModemSignalsControl.CTS_SIGNAL | ModemSignalsControl.DCD_SIGNAL |
                ModemSignalsControl.DSR_SIGNAL | ModemSignalsControl.RI_SIGNAL);

        if ( ( signals & tmpS ) != 0 ){

            throw new IllegalArgumentException(
                ExceptionMessage.format(ExceptionMessage.UART_SIGNALS_NOT_BITWISE_COMBINATION)
            );
        }

        checkPowerState();

        if ( (listener != null) && (modemListener != null) ){
            throw new IllegalStateException(
                ExceptionMessage.format(ExceptionMessage.UART_LISTENER_ALREADY_REGISTERED)
            );
        }

        if (listener != null) {
            if (modemListener == null) {
                ModemSignalDispatcher.getInstance().addListener(getHandle().getNativeHandle(), this);
            }
            modemListener = listener;
        } else {
            ModemSignalDispatcher.getInstance().removeListener(getHandle().getNativeHandle(), this);
            modemListener = null;
        }
    }

    public void signalChanged(int signalLine, boolean state) {
        ModemSignalListener l = modemListener;
        if (null != l) {
            ModemSignalEvent sce = new ModemSignalEvent(this, signalLine, state);
            try{
                l.signalStateChanged(sce);
            }catch(Exception e){
                //do nothing
            }
        }
    }

    @Override
    public synchronized void close() throws IOException{
        if (isOpen()) {
            if (modemListener != null){
                ModemSignalDispatcher.getInstance().removeListener(getHandle().getNativeHandle(), this);
                modemListener = null;
            }
            super.close();
        }
    }

    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native void setDTESignalState0( int signalID, boolean state);
    @Local(DontRemoveFields = {
        "com.oracle.dio.impl.Handle.native_handle",
        "com.oracle.dio.impl.AbstractPeripheral.handle",
    })
    private native boolean getDCESignalState0( int signalID);
};
