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

package jdk.dio.dac;

import jdk.dio.OutputRoundListener;
import jdk.dio.RoundCompletionEvent;
import java.nio.IntBuffer;

/**
 * The {@code GenerationRoundListener} interface defines methods for getting notified of the
 * completion of the conversion of a set of raw output values and that more output values to be
 * converted may be provided. <br />
 * This interface also indirectly extends the {@link jdk.dio.AsyncErrorHandler
 * AsyncErrorHandler} interface for getting notified of asynchronous I/O errors.
 * <p />
 * A {@code GenerationRoundListener} can be registered using one of the {@link DACChannel#startGeneration DACChannel.startGeneration}
 * methods.
 *
 * @see DACChannel#startGeneration DACChannel.startGeneration
 * @since 1.0
 */
@apimarker.API("device-io_1.1_dac")
public interface GenerationRoundListener extends OutputRoundListener<DACChannel, IntBuffer> {

    /**
     * Invoked when a buffer of DAC output values has been converted and the buffer is available for
     * copying more output values to convert, or when an output underrun error occurred.
     *
     * @param event
     *            the event that occurred.
     */
    @Override
    void outputRoundCompleted(RoundCompletionEvent<DACChannel, IntBuffer> event);

    /**
     * Invoked when an I/O operation fails.
     *
     * @param exception
     *            The exception to indicate why the I/O operation failed
     * @param source
     *            The {@link DACChannel} instance that generated the error.
     */
    @Override
    void failed(Throwable exception, DACChannel source);
}
