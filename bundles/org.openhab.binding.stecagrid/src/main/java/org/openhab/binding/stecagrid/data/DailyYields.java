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
public class DailyYields {
    private Curves monthCurves = new Curves();

    public Curves getCurves() {
        return monthCurves;
    }

    private int getDateYield(String timestamp, int index) {
        Dataset produced = monthCurves.getDatasetByType("Produced");
        if (produced == null) {
            return 0;
        }

        Data producedMonth = produced.getDataByTimestamp(timestamp);
        if (producedMonth == null) {
            return 0;
        }

        if (index < producedMonth.getValueCount()) {
            return producedMonth.getValue(index);
        } else {
            return 0;
        }
    }

    public double getTodaysYieldKWh() {
        ZonedDateTime zdt = ZonedDateTime.now();
        String timestamp = String.format("%04d-%02d", zdt.getYear(), zdt.getMonthValue());
        return getDateYield(timestamp, zdt.getDayOfMonth() - 1) / 1000.0;
    }

    public double getYesterdaysYieldKWh() {
        ZonedDateTime zdt = ZonedDateTime.now().minusDays(1);
        String timestamp = String.format("%04d-%02d", zdt.getYear(), zdt.getMonthValue());
        return getDateYield(timestamp, zdt.getDayOfMonth() - 1) / 1000.0;
    }

    /**
     * Sum the yields of the previous 30 days
     *
     * @return summed yield
     */
    public double getYieldLast30Days() {
        ZonedDateTime zdt = ZonedDateTime.now();
        double yield = 0;
        for (int d = 0; d < 30; d++) {
            zdt = zdt.minusDays(1);
            String timestamp = String.format("%04d-%02d", zdt.getYear(), zdt.getMonthValue());
            yield += getDateYield(timestamp, zdt.getDayOfMonth() - 1);
        }
        return yield / 1000.0;
    }
}
