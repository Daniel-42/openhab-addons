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

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.stecagrid.data.DailyYields;
import org.openhab.binding.stecagrid.data.Device;
import org.openhab.binding.stecagrid.data.Measurements;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.thoughtworks.xstream.InitializationException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * The {@link StecaGridHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class StecaGridHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(StecaGridHandler.class);

    private StecaGridConfiguration config = new StecaGridConfiguration();

    private @Nullable ScheduledFuture<?> refreshMeasurements;
    private @Nullable ScheduledFuture<?> refreshYields;

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    private @Nullable XStream xstream;

    private String stecaHost = "";

    private String deviceName = "N/A";
    private String deviceType = "N/A";
    private String deviceNominalPower = "N/A";

    /**
     * Constructor
     *
     * @param thing The thing to handle
     */
    public StecaGridHandler(Thing thing) {
        super(thing);
    }

    /**
     * Not listening to any commands.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * If a host has been specified start polling it
     */
    @Override
    public void initialize() {
        try {
            xstream = new XStream(new DomDriver());
        } catch (InitializationException ie) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Failed to initialize XML parser");
            return;
        }

        if (xstream != null) {
            configureXstream(xstream);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Did not get an XML parser");
            return;
        }

        config = getConfigAs(StecaGridConfiguration.class);
        if (configure()) {
            refreshMeasurements = scheduler.scheduleWithFixedDelay(this::pollMeasurements, 0,
                    config.measurementInterval, TimeUnit.SECONDS);
            refreshYields = scheduler.scheduleWithFixedDelay(this::pollYields, 0, config.yieldInterval,
                    TimeUnit.SECONDS);
        }
    }

    private void configureXstream(XStream xstream) {
        XStream.setupDefaultSecurity(xstream);
        xstream.allowTypesByWildcard(new String[] { Measurements.class.getPackageName() + ".**" }); // todo fix
        xstream.setClassLoader(Measurements.class.getClassLoader());
        xstream.processAnnotations(Measurements.class);
    }

    /**
     * Check the current configuration
     *
     * @return true if the configuration is ok to start polling, false otherwise
     */
    private boolean configure() {
        if (config.ipAddress.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing ipAddress/host configuration");
            return false;
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            stecaHost = config.ipAddress.trim();
            return true;
        }
    }

    /**
     * Stop the poller unconditionally
     */
    @Override
    public void dispose() {
        var job = refreshMeasurements;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
        refreshMeasurements = null;

        job = refreshYields;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
        refreshYields = null;
    }

    /**
     * polls the measurement xml data
     */
    private void pollMeasurements() {
        final String measurementsURL = String.format("http://%s/measurements.xml", stecaHost);

        var xstr = xstream;
        if (xstr == null) {
            return;
        }

        try {
            String result = HttpUtil.executeUrl("GET", measurementsURL, 1000);

            if (result.trim().isEmpty()) {
                logger.warn("Empty Measurement data at {} ", measurementsURL);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Inverter returned empty measurements");
                return;
            }

            updateStatus(ThingStatus.ONLINE);

            Measurements measurements;
            try {
                measurements = (Measurements) xstr.fromXML(result);
            } catch (Exception ex) {
                logger.warn("xstream failed on measurements.xml: {}", ex);
                return;
            }

            Device d = measurements.getDevice();
            if (!deviceName.equals(d.getName())) {
                deviceName = d.getName();
                updateProperty(StecaGridBindingConstants.PROPERTY_DEVICE_NAME, deviceName);
            }

            if (!deviceNominalPower.equals(d.getNominalPower())) {
                deviceNominalPower = d.getNominalPower();
                updateProperty(StecaGridBindingConstants.PROPERTY_DEVICE_NOMINAL_POWER, deviceNominalPower);
            }

            if (!deviceType.equals(d.getType())) {
                deviceType = d.getType();
                updateProperty(StecaGridBindingConstants.PROPERTY_DEVICE_TYPE, deviceType);
            }

            updateVolts(StecaGridBindingConstants.CHANNEL_AC_VOLTAGE, d.getAcVoltage());
            updateAmps(StecaGridBindingConstants.CHANNEL_AC_CURRENT, d.getAcCurrent());
            updateWatts(StecaGridBindingConstants.CHANNEL_AC_POWER, d.getAcPower());
            updateWatts(StecaGridBindingConstants.CHANNEL_AC_POWER_FAST, d.getAcPowerFast());
            updateState(StecaGridBindingConstants.CHANNEL_AC_FREQUENCY,
                    new QuantityType<>(d.getAcFrequency(), Units.HERTZ));
            updateVolts(StecaGridBindingConstants.CHANNEL_DC_VOLTAGE, d.getDcVoltage());
            updateAmps(StecaGridBindingConstants.CHANNEL_DC_CURRENT, d.getDcCurrent());
            updateVolts(StecaGridBindingConstants.CHANNEL_LINK_VOLTAGE, d.getLinkVoltage());
            updateState(StecaGridBindingConstants.CHANNEL_DERATING, new QuantityType<>(d.getDerating(), Units.ONE));
            updateWatts(StecaGridBindingConstants.CHANNEL_GRID_POWER, d.getGridPower());
            updateWatts(StecaGridBindingConstants.CHANNEL_GRID_CONSUMED_POWER, d.getGridConsumedPower());
            updateWatts(StecaGridBindingConstants.CHANNEL_GRID_INJECTED_POWER, d.getGridInjectedPower());
            updateWatts(StecaGridBindingConstants.CHANNEL_OWN_CONSUMED_POWER, d.getOwnConsumedPower());
        } catch (IOException e) {
            logger.warn("Measurement not found at {}", measurementsURL);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to query inverter");
        }
    }

    /**
     * polls the yield json data
     */
    private void pollYields() {
        final String yieldMonthURL = String.format("http://%s/yields.json?month=1", stecaHost);
        String result;

        try {
            result = HttpUtil.executeUrl("GET", yieldMonthURL, 2000);

            if (result.trim().isEmpty()) {
                logger.warn("Empty yield data at {} ", yieldMonthURL);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Inverter returned empty yields");
                return;
            }
        } catch (IOException e) {
            logger.warn("Monthly Yields not found at {}", yieldMonthURL);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to query inverter");
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        try {
            DailyYields daily = gson.fromJson(result, DailyYields.class);
            if (daily != null) {
                updateKWhs(StecaGridBindingConstants.CHANNEL_YIELD_DAY_CURRENT, daily.getTodaysYieldKWh());
                updateKWhs(StecaGridBindingConstants.CHANNEL_YIELD_DAY_PREVIOUS,daily.getYesterdaysYieldKWh());
                updateKWhs(StecaGridBindingConstants.CHANNEL_YIELD_LAST_30_DAYS, daily.getYieldLast30Days());
            }
        } catch (JsonSyntaxException jse) {
            logger.warn("Unable to parse Yield Json");
        }
    }

    private void updateWatts(String channel, double value) {
        updateState(channel, new QuantityType<>(value, Units.WATT));
    }

    private void updateVolts(String channel, double value) {
        updateState(channel, new QuantityType<>(value, Units.VOLT));
    }

    private void updateAmps(String channel, double value) {
        updateState(channel, new QuantityType<>(value, Units.AMPERE));
    }

    private void updateKWhs(String channel, double value) {
        updateState(channel, new QuantityType<>(value, Units.KILOWATT_HOUR));
    }
}
