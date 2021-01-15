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
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link StecaGridBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class StecaGridBindingConstants {

    private static final String BINDING_ID = "stecagrid";

    // List of all Thing Type UIDs
    public static final ThingTypeUID STECA_GRID_INVERTER = new ThingTypeUID(BINDING_ID, "stecagrid_inverter");

    // List of all Channel ids
    public static final String CHANNEL_AC_VOLTAGE = "ac_voltage";

    // List of all properties
    public static final String PROPERTY_DEVICE_NAME = "deviceName";
}
