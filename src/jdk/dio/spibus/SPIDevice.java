/*
 * Copyright (c) 2013, 2015, Oracle and/or its affiliates. All rights reserved.
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

package jdk.dio.spibus;

import jdk.dio.BufferAccess;
import jdk.dio.Device;
import jdk.dio.DeviceManager;
import jdk.dio.ClosedDeviceException;
import jdk.dio.UnavailableDeviceException;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.ByteChannel;
import romizer.WeakDontRenameClass;

/**
 * The {@code SPIDevice} interface provides methods for transmitting and receiving data to/from an SPI slave device.
 * <p />
 * An SPI slave device may be identified by the numeric ID and by the name (if any defined) that correspond to its
 * registered configuration. An {@code SPIDevice} instance can be opened by a call to one of the
 * {@link DeviceManager#open(int) DeviceManager.open(id,...)} methods using its ID or by a call to one of the
 * {@link DeviceManager#open(java.lang.String, java.lang.Class, java.lang.String[])
 * DeviceManager.open(name,...)} methods using its name. When an {@code SPIDevice} instance is opened with an ad-hoc
 * {@link SPIDeviceConfig} configuration (which includes its hardware addressing information) using one of the
 * {@link DeviceManager#open(jdk.dio.DeviceConfig) DeviceManager.open(config,...)} it is not
 * assigned any ID nor name.
 * <p />
 * On an SPI bus, data is transferred between the SPI master device and an SPI slave device in full duplex. That is,
 * data is transmitted by the SPI master to the SPI slave device at the same time data is received from the SPI slave
 * device by the SPI master.
 * <p />
 * To perform such a bidirectional exchange of data with an SPI slave device, an application may use one of the
 * {@link #writeAndRead(ByteBuffer, ByteBuffer) writeAndRead} methods. <br />
 * When an application only wants to send data to or receive data from an SPI slave device, it may use the
 * {@link #write(ByteBuffer) write} or the {@link #read(ByteBuffer) read} method, respectively. When writing only, the
 * data received from the SPI slave device will be ignored/discarded. When reading only, dummy data will be sent to the
 * slave.
 * <p/>
 * A data exchange consists of words of a certain length which may vary from SPI slave device to SPI slave device. <br />
 * Words in the sending and receiving byte buffers are not packed (bit-wise) and must be byte-aligned. The most
 * significant bits of a word are stored at the lower index (that is first). If a word's length is not a multiple of 8
 * (the byte length in bits) then the most significant bits will be undefined when receiving or unused when sending. If
 * the designated portion of a sending or receiving byte buffer cannot contain a (positive) integral number of words
 * then an {@link InvalidWordLengthException} will be thrown. For example, if the word length is 16bits and the
 * designated portion of buffer is only 1-byte long or is 3-byte long an {@link InvalidWordLengthException} will be
 * thrown. <br />
 * Assuming a word length <em>w</em>, the length <em>l</em> of the designated portion of the sending or receiving byte
 * buffer must be such that: <br />
 * <em>((l % (((w - 1) / 8) + 1)) == 0)</em>
 * <p />
 * Since the SPI master device controls the serial transmission clock read and write operations are never blocked nor delayed by
 * unresponsive slave devices. Not though that a read or write operation may be blocked if another read or write operation
 * is concurrently being performed on the same {@code SPIDevice} instance.
 * <p />
 * When the data exchange is over, an application should call the {@link #close SPIDevice.close} method to close the
 * SPI slave device. Any further attempt to transmit/write or receive/read to/from an SPI slave device which has been
 * closed will result in a {@link ClosedDeviceException} been thrown.
 * <h3>SPI Transactions</h3>
 * Depending on the underlying platform and driver capabilities an {@code SPIDevice} instance may additionally implement
 * the {@link jdk.dio.Transactional Transactional} interface to indicate that it supports SPI transactions. In such a case the {@link jdk.dio.Transactional#begin Transactional.begin}
 * and {@link jdk.dio.Transactional#end Transactional.end} methods may be used to demarcate the beginning and the end of an SPI transaction.
 * This typically results in the SPI slave's Select line (SS) remaining asserted during the whole sequence of read and write operations performed within the demarcated
 * transaction. It is the responsibility of the application to appropriately control the timing between a call to the {@code begin} method which
 * may assert the Chip Select (depending on the configuration, see {@link SPIDeviceConfig#CS_NOT_CONTROLLED}) and
 * subsequent calls to the {@link #read read}, {@link #write write} and {@link #writeAndRead writeAndRead} methods.
 * <p />
 * An application can alternatively perform a sequence of reads and/or writes - from/to one or several slaves, that
 * are guaranteed to be performed as a single transaction using an {@link SPICompositeMessage} object.
 * <h3>Device Probing Limitations</h3>
 * Opening an {@code SPIDevice} instance with hardware addressing information and configuration
 * may be subject to <a href="{@docRoot}/jdk/dio/DeviceManager.html#probing">device probing limitations </a>.
 *
 * @see jdk.dio.Transactional
 * @see SPIPermission
 * @see SPICompositeMessage
 * @see InvalidWordLengthException
 * @see ClosedDeviceException
 * @since 1.0
 */
@apimarker.API("device-io_1.1_spibus")
@WeakDontRenameClass
public interface SPIDevice extends Device<SPIDevice>, ByteChannel, BufferAccess<ByteBuffer> {

    /**
     * Creates a new {@code SPICompositeMessage} instance targeting this slave
     * device.
     *
     * @return a new {@code SPICompositeMessage} instance.
     * @since 1.0.1
     */
    SPICompositeMessage createCompositeMessage();

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
     *             if this device is not currently available - such as it is locked by another application.
     * @throws ClosedDeviceException
     *             if the device has been closed.
     */
    int getWordLength() throws IOException, UnavailableDeviceException, ClosedDeviceException;

    /**
     * Reads one data word of up to 32 bits from this slave device.
     * <p />
     * This method may be invoked at any time. If another thread has already initiated a read or write
     * operation upon this slave device, however, then an invocation of this method will block until
     * the first operation is complete.
     *
     * @return the data word read.
     * @throws IOException
     *             if some other I/O error occurs.
     * @throws InvalidWordLengthException
     *             if the number of bytes to receive belies word length; that is this slave's word length is bigger than
     *             32 bits.
     * @throws UnavailableDeviceException
     *             if this device is not currently available - such as it is locked by another application.
     * @throws ClosedDeviceException
     *             if the device has been closed.
     */
    int read() throws IOException, UnavailableDeviceException, ClosedDeviceException;

    /**
     * Reads a sequence of bytes from this slave device into the given buffer.
     * <p />
     * Dummy data will be sent to this slave device by the platform.
     * <p />
     * An attempt is made to read up to <i>r</i> bytes from the device, where <i>r</i> is the number
     * of bytes remaining in the buffer, that is, {@code dst.remaining()}, at the moment this method
     * is invoked.
     * <p />
     * Suppose that a byte sequence of length <i>n</i> is read, where <i>{@code 0 <= n <= r}</i>.
     * This byte sequence will be transferred into the buffer so that the first byte in the sequence
     * is at index <i>p</i> and the last byte is at index <i>{@code p + n - 1}</i>, where <i>p</i>
     * is the buffer's position at the moment this method is invoked. Upon return the buffer's
     * position will be equal to <i>{@code p + n}</i>; its limit will not have changed.
     * <p />
     * A read operation will block until the requested <i>r</i> bytes are read.
     * <p />
     * This method may be invoked at any time. If another thread has already initiated a read or write
     * operation upon this slave device, however, then an invocation of this method will block until
     * the first operation is complete.
     *
     * @param dst
     *            the buffer into which bytes are to be transferred
     *
     * @return the number of bytes read into {@code dst}, possibly zero.
     *
     * @throws NullPointerException
     *             if {@code dst} is {@code null}.
     * @throws InvalidWordLengthException
     *             if the number of bytes to receive belies word length.
     * @throws UnavailableDeviceException
     *             if this device is not currently available - such as it is locked by another application.
     * @throws ClosedDeviceException
     *             if the device has been closed.
     * @throws IOException
     *             if some other I/O error occurs.
     */
    @Override
    int read(ByteBuffer dst) throws IOException, UnavailableDeviceException, ClosedDeviceException;

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
     *            the buffer into which bytes are to be transferred
     *
     * @return The number of bytes read into {@code dst}, possibly zero.
     *
     * @throws NullPointerException
     *             if {@code dst} is {@code null}.
     * @throws IllegalArgumentException
     *              if {@code skip} is negative.
     * @throws InvalidWordLengthException
     *             if the total number of bytes to receive belies word length.
     * @throws UnavailableDeviceException
     *             if this device is not currently available - such as it is locked by another application.
     * @throws ClosedDeviceException
     *             if the device has been closed.
     * @throws IOException
     *             if some other I/O error occurs.
     */
    int read(int skip, ByteBuffer dst) throws IOException, UnavailableDeviceException, ClosedDeviceException;

    /**
     * Writes a sequence of bytes to this slave device from the given buffer.
     * <p />
     * An attempt is made to write up to <i>r</i> bytes to the device, where <i>r</i> is the number
     * of bytes remaining in the buffer, that is, {@code src.remaining()}, at the moment this method
     * is invoked.
     * <p />
     * Suppose that a byte sequence of length <i>n</i> is written, where <i>{@code 0 <= n <= r}</i>.
     * This byte sequence will be transferred from the buffer starting at index <i>p</i>, where
     * <i>p</i> is the buffer's position at the moment this method is invoked; the index of the last
     * byte written will be <i>{@code p + n - 1}</i>. Upon return the buffer's position will be
     * equal to <i>{@code p + n}</i>; its limit will not have changed.
     * <p />
     * A write operation will return only after writing all of the <i>r</i> requested bytes.
     * <p />
     * This method may be invoked at any time. If another thread has already initiated a read or write
     * operation upon this slave device, however, then an invocation of this method will block until
     * the first operation is complete.
     *
     * @param src
     *            the buffer from which bytes are to be retrieved.
     * @return the number of bytes written from {@code src}, possibly zero.
     * @throws InvalidWordLengthException
     *             if the number of bytes to send belies word length.
     * @throws NullPointerException
     *             if {@code src} is {@code null}.
     * @throws UnavailableDeviceException
     *             if this device is not currently available - such as it is locked by another application.
     * @throws ClosedDeviceException
     *             if the device has been closed.
     * @throws IOException
     *             if some other I/O error occurs.
     */
    @Override
    int write(ByteBuffer src) throws IOException, UnavailableDeviceException, ClosedDeviceException;

    /**
     * Writes one data word of up to 32 bits to this slave device.
     * <p />
     * This method may be invoked at any time. If another thread has already initiated a read or write
     * operation upon this slave device, however, then an invocation of this method will block until
     * the first operation is complete.
     *
     * @param txData
     *            the data word to be written.
     * @throws IOException
     *             if an I/O error occurred.
     * @throws InvalidWordLengthException
     *             if the number of bytes to send belies word length; that is this slave's word length is bigger than 32
     *             bits.
     * @throws UnavailableDeviceException
     *             if this device is not currently available - such as it is locked by another application.
     * @throws ClosedDeviceException
     *             if the device has been closed.
     */
    void write(int txData) throws IOException, UnavailableDeviceException, ClosedDeviceException;

    /**
     * Exchanges (transmits and receives) data with this slave device.
     * <p />
     * The designated portions of the sending and receiving byte buffers may not have the same length. When sending more
     * than is being received the extra received bytes are ignored/discarded. Conversely, when sending less than is
     * being received extra dummy data will be sent.
     * <p />
     * This method behaves as a combined {@link SPIDevice#write(java.nio.ByteBuffer)} and
     * {@link SPIDevice#read(java.nio.ByteBuffer)}.
     *
     * @param src
     *            the buffer from which bytes are to be retrieved.
     * @param dst
     *            the buffer into which bytes are to be transferred.
     * @return The number of bytes read into {@code dst}, possibly zero.
     * @throws NullPointerException
     *             if {@code src} or {@code dst} is {@code null}.
     * @throws InvalidWordLengthException
     *             if the number of bytes to receive or send belies word length.
     * @throws UnavailableDeviceException
     *             if this device is not currently available - such as it is locked by another application.
     * @throws ClosedDeviceException
     *             if the device has been closed.
     * @throws IOException
     *             if some other I/O error occurs.
     */
    int writeAndRead(ByteBuffer src, ByteBuffer dst) throws IOException, UnavailableDeviceException, ClosedDeviceException;

    /**
     * Exchanges (transmits and receives) data with this slave device skipping the specified number of bytes received.
     * <p />
     * The designated portions of the sending and receiving byte buffers may not have the same length. When sending more
     * than is being received the extra received bytes are ignored/discarded. Conversely, when sending less than is
     * being received extra dummy data will be sent.
     * <p />
     * This method behaves as a combined {@link SPIDevice#write(java.nio.ByteBuffer)} and
     * {@link SPIDevice#read(java.nio.ByteBuffer)}.
     *
     * @param src
     *            the buffer from which bytes are to be retrieved.
     * @param skip
     *            the number of received bytes that must be ignored/skipped before filling in the {@code dst} buffer.
     * @param dst
     *            the buffer into which bytes are to be transferred.
     * @return the number of bytes read into {@code dst}, possibly zero.
     * @throws NullPointerException
     *             if {@code src} or {@code dst} is {@code null}.
     * @throws IllegalArgumentException
     *              if {@code skip} is negative.
     * @throws InvalidWordLengthException
     *             if the total number of bytes to receive or send belies word length.
     * @throws UnavailableDeviceException
     *             if this device is not currently available - such as it is locked by another application.
     * @throws ClosedDeviceException
     *             if the device has been closed.
     * @throws IOException
     *             if some other I/O error occurs.
     */
    int writeAndRead(ByteBuffer src, int skip, ByteBuffer dst) throws IOException, UnavailableDeviceException, ClosedDeviceException;

    /**
     * Exchanges (transmits and receives) one data word of up to 32 bits with this slave device.
     * <p />
     * This method may be invoked at any time. If another thread has already initiated a read or write
     * operation upon this slave device, however, then an invocation of this method will block until
     * the first operation is complete.
     *
     * @param txData
     *            the word to send.
     * @return the word received.
     * @throws IOException
     *             if some other I/O error occurs.
     * @throws InvalidWordLengthException
     *             if the numbers of bytes to send or to receive bely word length; that is this slave's word length is
     *             bigger than 32 bits.
     * @throws UnavailableDeviceException
     *             if this device is not currently available - such as it is locked by another application.
     * @throws ClosedDeviceException
     *             if the device has been closed.
     */
    int writeAndRead(int txData) throws IOException, UnavailableDeviceException, ClosedDeviceException;
}
