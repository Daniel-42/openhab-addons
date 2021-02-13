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

import static org.openhab.binding.knmi.internal.KnmiBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link KnmiHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.knmi", service = ThingHandlerFactory.class)
public class KnmiHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_GRONINGEN,
            THING_TYPE_FRIESLAND, THING_TYPE_DRENTHE, THING_TYPE_NOORDHOLLAND, THING_TYPE_FLEVOLAND,
            THING_TYPE_OVERIJSSEL, THING_TYPE_GELDERLAND, THING_TYPE_UTRECHT, THING_TYPE_ZUIDHOLLAND,
            THING_TYPE_ZEELAND, THING_TYPE_NOORDBRABANT, THING_TYPE_LIMBURG, THING_TYPE_WADDENZEE,
            THING_TYPE_IJSSELMEERGEBIED);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new KnmiHandler(thing);
        }

        return null;
    }
}
