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
package org.openhab.binding.stecagrid.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link StecaGridConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class StecaGridConfiguration {

    /**
     * IP Address or host for the P1 Meter
     */
    public String ipAddress = "";

    /**
     * Measurement refresh interval in seconds
     */
    public Integer measurementInterval = 5;

    /**
     * Yield refresh interval in seconds
     */
    public Integer yieldInterval = 300;

}
