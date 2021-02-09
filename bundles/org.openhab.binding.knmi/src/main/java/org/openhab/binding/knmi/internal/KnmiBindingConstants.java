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
    public static final ThingTypeUID THING_TYPE_OVERIJSSEL = new ThingTypeUID(BINDING_ID, "warnings-overijssel");
    public static final ThingTypeUID THING_TYPE_GELDERLAND = new ThingTypeUID(BINDING_ID, "warnings-gelderland");

    public static final HashMap<ThingTypeUID, String> THING_HEADER_MAP = new HashMap<ThingTypeUID, String>(Map
            .ofEntries(Map.entry(THING_TYPE_OVERIJSSEL, "Overijssel"), Map.entry(THING_TYPE_GELDERLAND, "Gelderland")));

    // List of all Channel ids
    public static final String CHANNEL_C1_DESCRIPTION = "current-1#description";
    public static final String CHANNEL_C2_DESCRIPTION = "current-2#description";
    public static final String CHANNEL_C3_DESCRIPTION = "current-3#description";

    public static final String CHANNEL_C1_START = "current-1#start_time";
    public static final String CHANNEL_C2_START = "current-2#start_time";
    public static final String CHANNEL_C3_START = "current-3#start_time";

    public static final String CHANNEL_C1_END = "current-1#end_time";
    public static final String CHANNEL_C2_END = "current-2#end_time";
    public static final String CHANNEL_C3_END = "current-3#end_time";

    public static final String CHANNEL_C1_LEVEL = "current-1#level";
    public static final String CHANNEL_C2_LEVEL = "current-2#level";
    public static final String CHANNEL_C3_LEVEL = "current-3#level";

}
