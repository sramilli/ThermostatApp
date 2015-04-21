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

package jdk.dio;

/**
 * The {@code DeviceEventListener} interface is a tagging interface that all event listeners
 * must implement. Event listeners provide methods for notifying applications of the occurrence of
 * events (hardware interrupts or software signals) on devices.
 * <p />
 * An event listener should implement the following requirements:
 * <ul>
 * <li>it should be implemented to be as fast as possible; for example it should not call any
 * operations that may block, nor pause the current thread.</li>
 * <li>it should not throw any unchecked exception.</li>
 * </ul>
 * A compliant implementation of this specification MUST catch unchecked exceptions that may be
 * thrown by a listener.
 *
 * @since 1.0
 */
@apimarker.API("device-io_1.1")
public interface DeviceEventListener {
}
