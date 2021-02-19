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
public class Curves {
    private ArrayList<Dataset> datasets = new ArrayList<Dataset>();

    public int getDatasetCount() {
        return datasets.size();
    }

    public Dataset getDataset(int datasetIndex) {
        return datasets.get(datasetIndex);
    }

    public @Nullable Dataset getDatasetByType(String type) {
        for (var dataset : datasets) {
            if (dataset.getType().equals(type)) {
                return dataset;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("dataSets: %d", datasets.size());
    }
}
