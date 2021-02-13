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
package org.openhab.binding.knmi.data;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class Warning {

    private ZonedDateTime startDateTime = ZonedDateTime.now();
    private ZonedDateTime endDateTime = ZonedDateTime.now();
    private int level = 0;
    private String title = "";
    private String description = "";

    public void setValidity(ZonedDateTime start, ZonedDateTime end) {
        startDateTime = start;
        endDateTime = end;
    }

    public ZonedDateTime getStartDateTime() {
        return startDateTime;
    }

    public ZonedDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setLevel(String levelText) {
        switch (levelText.toLowerCase().substring(5, 8)) {
            case "gro":
            default:
                level = 0;
                break;
            case "gee":
                level = 1;
                break;
            case "ora":
                level = 2;
                break;
            case "roo":
                level = 3;
                break;
        }
    }

    public int getLevel() {
        return level;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCurrent() {
        ZonedDateTime dzt = ZonedDateTime.now();
        return startDateTime.isBefore(dzt) && endDateTime.isAfter(dzt);
    }

    public boolean isExpired() {
        return endDateTime.isBefore(ZonedDateTime.now());
    }

    public boolean isFuture() {
        return startDateTime.isAfter(ZonedDateTime.now());
    }

    @Override
    public String toString() {
        return String.format("%s-%s %d %s", startDateTime, endDateTime, level, title);
    }
}
