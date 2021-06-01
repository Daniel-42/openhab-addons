/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.stecagrid.data;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class Data {
    private String timestamp = "";
    private int[] data = {}; // = new ArrayList<Integer>();

    public String getTimestamp() {
        return timestamp;
    }

    public int getValueCount() {
        return data.length;
    }

    public Integer getValue(int dataIndex) {
        return data[dataIndex];
    }

    @Override
    public String toString() {
        return String.format("timestamp: %s dataEntries: %d", timestamp, data.length);
    }
}
