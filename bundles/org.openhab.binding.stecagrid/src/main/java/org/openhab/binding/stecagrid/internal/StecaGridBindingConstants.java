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
    public static final String CHANNEL_AC_VOLTAGE = "inverter#ac_voltage";
    public static final String CHANNEL_AC_CURRENT = "inverter#ac_current";
    public static final String CHANNEL_AC_POWER = "inverter#ac_power";
    public static final String CHANNEL_AC_POWER_FAST = "inverter#ac_power_fast";
    public static final String CHANNEL_AC_FREQUENCY = "inverter#ac_frequency";
    public static final String CHANNEL_DC_VOLTAGE = "inverter#dc_voltage";
    public static final String CHANNEL_DC_CURRENT = "inverter#dc_current";
    public static final String CHANNEL_LINK_VOLTAGE = "inverter#link_voltage";
    public static final String CHANNEL_DERATING = "inverter#derating";

    public static final String CHANNEL_GRID_POWER = "grid#grid_power";
    public static final String CHANNEL_GRID_CONSUMED_POWER = "grid#grid_consumed_power";
    public static final String CHANNEL_GRID_INJECTED_POWER = "grid#grid_injected_power";
    public static final String CHANNEL_OWN_CONSUMED_POWER = "grid#own_consumed_power";

    public static final String CHANNEL_YIELD_DAY_CURRENT = "yield#yield_day_current";
    public static final String CHANNEL_YIELD_DAY_PREVIOUS = "yield#yield_day_previous";

    // List of all properties
    public static final String PROPERTY_DEVICE_NAME = "deviceName";
    public static final String PROPERTY_DEVICE_TYPE = "deviceType";
    public static final String PROPERTY_DEVICE_NOMINAL_POWER = "deviceNominalPower";
}
