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

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knmi.data.Item;
import org.openhab.binding.knmi.data.UrgencyComparator;
import org.openhab.binding.knmi.data.Warning;
import org.openhab.binding.knmi.data.Warnings;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.InitializationException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * The {@link KnmiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class KnmiHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(KnmiHandler.class);
    private final UrgencyComparator urgencyComparator = new UrgencyComparator();
    private final Warning emptyWarning = new Warning();

    private @Nullable ScheduledFuture<?> pollingJob;

    private String thingItemTitle = "";
    private @Nullable XStream xstream;

    private KnmiConfiguration config = new KnmiConfiguration();

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
            logger.error("Unmapped ThingTypeUID {}", thing.getThingTypeUID());
            return;
        }

        try {
            xstream = new XStream(new DomDriver());
        } catch (InitializationException ie) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Failed to initialize XML parser.");
            return;
        }

        if (xstream != null) {
            configureXstream(xstream);
        }

        updateStatus(ThingStatus.UNKNOWN);
        pollingJob = scheduler.scheduleWithFixedDelay(this::refreshHandler, 0, config.refreshDelay, TimeUnit.SECONDS);
    }

    private void configureXstream(XStream xml) {
        XStream.setupDefaultSecurity(xml);
        xml.allowTypesByWildcard(new String[] { Warnings.class.getPackageName() + ".**" });
        xml.setClassLoader(Warnings.class.getClassLoader());
        xml.ignoreUnknownElements();
        xml.processAnnotations(Warnings.class);
    }

    /**
     * dispose: stop the poller
     */
    @Override
    public void dispose() {
        var job = pollingJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
        pollingJob = null;
    }

    /**
     * Handler for the periodic refreshes
     */
    private void refreshHandler() {
        final String result;

        try {
            result = HttpUtil.executeUrl("GET", "https://cdn.knmi.nl/knmi/xml/rss/rss_KNMIwaarschuwingen.xml", 2500);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to query wanings RSS");
            // todo shorter refresh interval?
            return;
        }

        if (result.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "KNMI RSS Feed returned empty feed.");
            return;
        }

        try {
            if (xstream != null) {
                Warnings warnings = (Warnings) xstream.fromXML(result);

                // If we get as far as this, we're online
                updateStatus(ThingStatus.ONLINE);

                for (Item i : warnings.getChannel().getItems()) {
                    if (i.getTitle().equals(thingItemTitle)) {

                        ArrayList<Warning> currentWarnings = new ArrayList<Warning>();
                        ArrayList<Warning> futureWarnings = new ArrayList<Warning>();

                        String des = i.getDescription();
                        String[] tokens = des.split("<br><br>");
                        for (String token : tokens) {
                            String[] subtokens = token.split("<p>|<br>| \\(van | uur\\)");
                            if (subtokens.length != 4) {
                                continue;
                            }

                            Warning w = new Warning();
                            w.setLevel(subtokens[0]);
                            w.setTitle(subtokens[1]);
                            w.setDescription(subtokens[2]);

                            String[] dateTokens = subtokens[3].split(" tot ");
                            if (dateTokens.length != 2) {
                                continue;
                            }

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                                    .withZone(ZoneId.of("UTC+1"));

                            // Times in the RSS feed are always one hour later than what is shown on the
                            // KNMI website (which does show local time) I've informed KNMI about this
                            ZonedDateTime start = ZonedDateTime.parse(dateTokens[0], formatter).minusHours(1);
                            ZonedDateTime end = ZonedDateTime.parse(dateTokens[1], formatter).minusHours(1);
                            w.setValidity(start, end);

                            if (w.isExpired()) {
                                continue;
                            }

                            if (w.isFuture()) {
                                futureWarnings.add(w);
                            } else if (w.isCurrent()) {
                                currentWarnings.add(w);
                            }
                        }

                        currentWarnings.sort(urgencyComparator);
                        updateWarningStates("current", currentWarnings);

                        futureWarnings.sort(urgencyComparator);
                        updateWarningStates("future", futureWarnings);

                    } else if (!i.getTitle().startsWith("Waarschuwingen")) {
                        // logger.warn("Summary: {}", i.getTitle());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("XML could not be parsed. {}", e);
        }
    }

    private void updateWarningStates(String channelHeader, ArrayList<Warning> warnings) {

        for (int i = 1; i < 4; i++) {
            Warning w = emptyWarning;
            if (i <= warnings.size()) {
                w = warnings.get(i - 1);
            }

            updateState(String.format(KnmiBindingConstants.CHANNEL_N_LEVEL, channelHeader, i),
                    new DecimalType(w.getLevel()));
            updateState(String.format(KnmiBindingConstants.CHANNEL_N_START, channelHeader, i),
                    new DateTimeType(w.getStartDateTime()));
            updateState(String.format(KnmiBindingConstants.CHANNEL_N_END, channelHeader, i),
                    new DateTimeType(w.getEndDateTime()));
            updateState(String.format(KnmiBindingConstants.CHANNEL_N_TITLE, channelHeader, i),
                    new StringType(w.getTitle()));
            updateState(String.format(KnmiBindingConstants.CHANNEL_N_DESCRIPTION, channelHeader, i),
                    new StringType(w.getDescription()));
        }
    }
}
