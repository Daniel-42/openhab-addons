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

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class MonthlyYields {
    private Curves yearCurves = new Curves();

    public Curves getCurves() {
        return yearCurves;
    }

    private int getDateYield(String timestamp, int monthIndex) {
        Dataset produced = yearCurves.getDatasetByType("Produced");
        if (produced == null) {
            return 0;
        }

        Data producedYear = produced.getDataByTimestamp(timestamp);
        if (producedYear == null) {
            return 0;
        }

        if (monthIndex < producedYear.getValueCount()) {
            return producedYear.getValue(monthIndex);
        } else {
            return 0;
        }
    }

    public double getThisMonthsYieldKWh() {
        ZonedDateTime zdt = ZonedDateTime.now();
        String timestamp = String.format("%04d", zdt.getYear());
        return getDateYield(timestamp, zdt.getMonthValue() - 1) / 1000.0;
    }

    public double getLastMonthsYieldKWh() {
        ZonedDateTime zdt = ZonedDateTime.now().minusMonths(1);
        String timestamp = String.format("%04d", zdt.getYear());
        return getDateYield(timestamp, zdt.getMonthValue() - 1) / 1000.0;
    }

    /**
     * Get the yield for the current year
     *
     * @return summed yield
     */
    public double getYieldCurrentYearKWh() {
        ZonedDateTime zdt = ZonedDateTime.now();
        double yield = 0;
        String timestamp = String.format("%04d", zdt.getYear());
        for (int m = 0; m < 12; m++) {
            yield += getDateYield(timestamp, m);
        }
        return yield / 1000.0;
    }
}
