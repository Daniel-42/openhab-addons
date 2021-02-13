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

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class UrgencyComparator implements Comparator<Warning> {

    @Override
    public int compare(Warning w1, Warning w2) {
        if (w1.getLevel() == w2.getLevel()) {
            if (w1.getStartDateTime().isEqual(w2.getStartDateTime())) {
                return 0;
            }
            return w1.getStartDateTime().isBefore(w2.getStartDateTime()) ? -1 : 1;
        } else {
            return (w1.getLevel() - w2.getLevel());
        }
    }
}
