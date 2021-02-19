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

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class Dataset {
    private String type = "";
    private ArrayList<Data> data = new ArrayList<Data>();

    public String getType() {
        return type;
    }

    public int getDataCount() {
        return data.size();
    }

    public Data getData(int dataIndex) {
        return data.get(dataIndex);
    }

    public @Nullable Data getDataByTimestamp(String timestamp) {
        for (var entry : data) {
            if (entry.getTimestamp().equals(timestamp)) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("type: %s dataEntries: %d", type, data.size());
    }
}
