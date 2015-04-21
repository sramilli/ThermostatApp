/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.dio.utils;

import java.util.StringTokenizer;
import jdk.dio.DevicePermission;

/**
 * Utility class for device permission class
 */
public class ActionFactory {
    /**
     * Checks if <code>actions1</code> list "implies"
     * <code>actions2</code> list
     *
     * @param actions1 comma-separated list of verified and valid
     *                 actions
     * @param actions1 comma-separated list of verified and valid
     *                 actions
     *
     * @return true <code>actions2</code> list is implied by
     *         <code>actions1</code> list
     */
    public static boolean implies(String actions1, String actions2) {
        int index = actions2.indexOf(",");
        if (index == -1) {
            return isIncluded(actions2, actions1);
        } else {
            return implies(actions1, actions2.substring(0, index)) &&
                   implies(actions1, actions2.substring(index+1));
        }
    }

    /**
     * Checks if given <code>what</code> is included to
     * <code>where</code> list
     *
     * @param what   string to compare
     * @param where  coma-separated string list to search at
     *
     * @return <code>true</code> if <code>what</code> contains in
     *         the list
     */
    private static boolean isIncluded(String what, String where) {
        StringTokenizer tokens = new StringTokenizer(where, ",");
        while (tokens.hasMoreElements()) {
            if (tokens.nextToken().equals(what)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true for permission specified actions
     *
     * @param action for validation
     * @param normalizedList allowed actions list
     *
     * @return true for valid
     */
    private static boolean isValidAction(String actions, String normalizedList) {
        StringTokenizer tokez = new StringTokenizer(actions, ",", true);
        // detect first empty token
        boolean lastTokenIsComma = true;
        while (tokez.hasMoreElements()) {
            String action = tokez.nextToken();
            // special case for empty actions that are not returned by StringTokenizer.nextToken() by default
            if (action.equals(",")) {
                if (lastTokenIsComma) {
                    return false;
                } else {
                    lastTokenIsComma = true;
                    continue;
                }
            } else {
                lastTokenIsComma = false;
            }
            if (!isIncluded(action, normalizedList)) {
                return false;
            }
        }
        // detects last empty token as well
        return !lastTokenIsComma;
    }

    /**
     * Returns action list in spec required order
     *
     * @param actions unordered and unverified list
     * @param normalizedList allowed actions list in normalized form
     *
     * @return ordered list
     */
    public static String verifyAndOrderActions(String actions, String normalizedList) {
        if(actions == null){
            throw new IllegalArgumentException(
                ExceptionMessage.format(ExceptionMessage.DEVICE_NULL_ACTIONS)
            );
        }
        if(actions.length() == 0){
            throw new IllegalArgumentException(
                ExceptionMessage.format(ExceptionMessage.DEVICE_EMPTY_ACTIONS)
            );
        }

        if (!isValidAction(actions, normalizedList)) {
            throw new IllegalArgumentException(actions);
        }

        boolean comma = false;
        StringBuilder sb = new StringBuilder(30);
        StringTokenizer tokez = new StringTokenizer(normalizedList, ",");
        while (tokez.hasMoreElements()) {
            String validAction = tokez.nextToken();
            if (isIncluded(validAction, actions)) {
                if (comma) {
                    sb.append(',');
                }
                sb.append(validAction);
                comma = true;
            }
        }

        return sb.toString();
    }

    /**
     * Verifies and order given <code>actions</code> against
     * DevicePermission actions list
     *
     * @param actions unordered and unverified list
     *
     * @return ordered and verified list
     */
    public static String verifyAndOrderDeviceActions(String actions) {
        return verifyAndOrderActions(actions, DevicePermission.OPEN + "," + DevicePermission.POWER_MANAGE);
    }
}
