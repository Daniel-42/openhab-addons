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
package org.openhab.binding.knmi.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link KnmiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class KnmiBindingConstants {

    private static final String BINDING_ID = "knmi";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WADDENEILANDEN = new ThingTypeUID(BINDING_ID,
            "warnings-waddeneilanden");
    public static final ThingTypeUID THING_TYPE_GRONINGEN = new ThingTypeUID(BINDING_ID, "knmi-warnings-groningen");
    public static final ThingTypeUID THING_TYPE_FRIESLAND = new ThingTypeUID(BINDING_ID, "knmi-warnings-friesland");
    public static final ThingTypeUID THING_TYPE_DRENTHE = new ThingTypeUID(BINDING_ID, "knmi-warnings-drenthe");
    public static final ThingTypeUID THING_TYPE_NOORDHOLLAND = new ThingTypeUID(BINDING_ID,
            "knmi-warnings-noordholland");
    public static final ThingTypeUID THING_TYPE_FLEVOLAND = new ThingTypeUID(BINDING_ID, "knmi-warnings-flevoland");
    public static final ThingTypeUID THING_TYPE_OVERIJSSEL = new ThingTypeUID(BINDING_ID, "knmi-warnings-overijssel");
    public static final ThingTypeUID THING_TYPE_GELDERLAND = new ThingTypeUID(BINDING_ID, "knmi-warnings-gelderland");
    public static final ThingTypeUID THING_TYPE_UTRECHT = new ThingTypeUID(BINDING_ID, "knmi-warnings-utrecht");
    public static final ThingTypeUID THING_TYPE_ZUIDHOLLAND = new ThingTypeUID(BINDING_ID, "knmi-warnings-zuidholland");
    public static final ThingTypeUID THING_TYPE_ZEELAND = new ThingTypeUID(BINDING_ID, "knmi-warnings-zeeland");
    public static final ThingTypeUID THING_TYPE_NOORDBRABANT = new ThingTypeUID(BINDING_ID,
            "knmi-warnings-noordbrabant");
    public static final ThingTypeUID THING_TYPE_LIMBURG = new ThingTypeUID(BINDING_ID, "knmi-warnings-limburg");
    public static final ThingTypeUID THING_TYPE_WADDENZEE = new ThingTypeUID(BINDING_ID, "knmi-warnings-waddenzee");
    public static final ThingTypeUID THING_TYPE_IJSSELMEERGEBIED = new ThingTypeUID(BINDING_ID,
            "knmi-warnings-ijsselmeergebied");

    public static final HashMap<ThingTypeUID, String> THING_HEADER_MAP = new HashMap<ThingTypeUID, String>(
            Map.ofEntries(Map.entry(THING_TYPE_WADDENEILANDEN, "Waddeneilanden"),
                    Map.entry(THING_TYPE_GRONINGEN, "Groningen"), Map.entry(THING_TYPE_FRIESLAND, "Friesland"),
                    Map.entry(THING_TYPE_DRENTHE, "Drenthe"), Map.entry(THING_TYPE_NOORDHOLLAND, "Noord-Holland"),
                    Map.entry(THING_TYPE_FLEVOLAND, "Flevoland"), Map.entry(THING_TYPE_OVERIJSSEL, "Overijssel"),
                    Map.entry(THING_TYPE_GELDERLAND, "Gelderland"), Map.entry(THING_TYPE_UTRECHT, "Utrecht"),
                    Map.entry(THING_TYPE_ZUIDHOLLAND, "Zuid-Holland"), Map.entry(THING_TYPE_ZEELAND, "Zeeland"),
                    Map.entry(THING_TYPE_NOORDBRABANT, "Noord-Brabant"), Map.entry(THING_TYPE_LIMBURG, "Limburg"),
                    Map.entry(THING_TYPE_WADDENZEE, "Waddenzee"),
                    Map.entry(THING_TYPE_IJSSELMEERGEBIED, "IJsselmeergebied")));

    // List of all Channel ids
    public static final String CHANNEL_N_START = "%s-%d#start_time";
    public static final String CHANNEL_N_END = "%s-%d#end_time";
    public static final String CHANNEL_N_LEVEL = "%s-%d#level";
    public static final String CHANNEL_N_TITLE = "%s-%d#title";
    public static final String CHANNEL_N_DESCRIPTION = "%s-%d#description";
}
