/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.dio.registry;

import java.io.IOException;
import java.security.AccessController;
import java.util.Hashtable;
import java.util.Iterator;

import jdk.dio.Device;
import jdk.dio.DeviceAlreadyExistsException;
import jdk.dio.DeviceConfig;
import jdk.dio.DeviceDescriptor;
import jdk.dio.DeviceManager;
import jdk.dio.DeviceMgmtPermission;
import jdk.dio.DeviceNotFoundException;
import jdk.dio.InvalidDeviceConfigException;
import jdk.dio.UnsupportedDeviceTypeException;
/**
 * Device configuration registry.
 * <br/>
 * Maintains database that keeps peripheral configuration data
 * for particular ID/Name.
 *
 */
public abstract class Registry <T extends Device> {

    public static Registry getInstance() {
        return new RegistryImpl();
    }

    /**
     * Indicates if registration of a new device is possible.
     *         false  if "register" operation is not supported by platform (i.e. closed topology)
     *         true   otherwise
     */
    public static final boolean canRegister = RegistryImpl.canRegister;

    /**
     *
     * @return DeviceConfig instance
     *
     * @throw  DeviceNotFoundException if no entry with given
     *         {@code id} is found
     * @see
     *      jdk.dio.DeviceManager#open(int,Class)
     *
     */
    public  abstract DeviceDescriptor<? super T> get(int id);


    /**
     *
     * @return DeviceConfig instance
     *
     * @throw  DeviceNotFoundException if no entry with given
     *         {@code id} is found or config is not for {@code intf}
     *         peripheral type.
     * @see
     *      jdk.dio.DeviceManager#open(String,Class,String...)
     *
     */
    public abstract Iterator<DeviceDescriptor<? super T>> get(String name, Class<T> intf, String... properties);

    /**
     *
     * @return new Periprheal ID
     * @throw  UnsupportedOperationException if operation is not
     *         supported by platform
     * @see jdk.dio.DeviceManager#register(int,
     *      Class, DeviceConfig, String, String...)
     */
    public abstract void register(int id, Class<T> intf,
                                                        DeviceConfig<? super T> config,
                                                        String name,
                                                        String... properties)
    throws UnsupportedOperationException, IOException;

    /**
     * @see
     *      jdk.dio.DeviceManager#unregister(int)
     */
    public abstract DeviceDescriptor unregister(int id);


    /**
     * Checks if the application is authorized to perform one of
     * DeviceMgmtPermission actions
     *
     * @param d      ID and NAME holder
     * @param action <code>DeviceMgmtPermission</code> action
     */
    public static void checkPermission(DeviceDescriptor d, String action) {
        String perm = (DeviceManager.UNSPECIFIED_ID == d.getID()) ? "" : ":"+d.getID();
        perm = (null == d.getName()) ? perm : d.getName() + perm;
        AccessController.checkPermission(new DeviceMgmtPermission(perm, action));
    }

    /**
     *
     * @see jdk.dio.DeviceManager.list(Class)
     */
    public abstract Iterator<DeviceDescriptor<? super T>> list(Class<T> type);
}
