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

package jdk.dio.atcmd;

import java.nio.channels.ByteChannel;

/**
 * The {@code DataConnection} interface provides methods for reading and writing to a data
 * connection opened by issuing an AT command (e.g. {@code ATD}).
 * @since 1.0
 */
@apimarker.API("device-io_1.1_atcmd")
public interface DataConnection extends ByteChannel {

    /**
     * Returns the {@code ATDevice} this data connection has been opened with.
     * <p />
     * The returned object may be an instance of {@link ATModem ATModem},
     * allowing for controlling the modem signals; in case of a virtual
     * channel open over a multiplexing protocol, this {@code ATModem} instance may
     * be specific to that channel and may allow
     * for controlling the virtual modem signals associated to that virtual channel.
     *
     * @return the {@code ATDevice} this data connection has been opened with.
     */
    ATDevice getDevice();
}
