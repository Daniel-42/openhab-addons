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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link KnmiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class KnmiHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(KnmiHandler.class);

    private String thingItemTitle = "";

    private @Nullable KnmiConfiguration config;

    public KnmiHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(KnmiConfiguration.class);

        if (KnmiBindingConstants.THING_HEADER_MAP.containsKey(thing.getThingTypeUID())) {
            thingItemTitle = "Waarschuwingen " + KnmiBindingConstants.THING_HEADER_MAP.get(thing.getThingTypeUID());
        } else {
            logger.warn("Unmapped ThingTypeUID {}", thing.getThingTypeUID());
        }

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.ONLINE);

        updateState(KnmiBindingConstants.CHANNEL_C1_LEVEL, new DecimalType(0));
    }
}
