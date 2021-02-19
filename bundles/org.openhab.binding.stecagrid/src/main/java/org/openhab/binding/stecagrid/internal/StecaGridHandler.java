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
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    private final Lock pollingJobLock = new ReentrantLock();
    private @Nullable ScheduledFuture<?> pollingJob;
    private int currentRefreshDelay = 0;

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    private final Lock configurationLock = new ReentrantLock(); // to protected fields accessed in init/dispose AND the
                                                                // poller
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
                    "Failed to initialize XML parsern");
            return;
        }

        if (xstream != null) {
            configureXstream(xstream);
        }

        config = getConfigAs(StecaGridConfiguration.class);

        if (configure()) {
            stopPolling(true);
            startPolling();
        }

        updateStatus(ThingStatus.ONLINE);
    }

    private void configureXstream(XStream xstream) {
        XStream.setupDefaultSecurity(xstream);
        xstream.allowTypesByWildcard(new String[] { Measurements.class.getPackageName() + ".**" });
        xstream.setClassLoader(Measurements.class.getClassLoader());
        xstream.processAnnotations(Measurements.class);
    }

    /**
     * Handle updates to the configuration gracefully
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        Object cfgObject = configurationParameters.get("refreshDelay");
        if (cfgObject != null) {
            BigDecimal bd = (BigDecimal) cfgObject;
            config.refreshDelay = bd.intValue();
        }

        cfgObject = configurationParameters.get("ipAddress");
        if (cfgObject != null) {
            config.ipAddress = (String) cfgObject;
        }

        if (configure()) {
            // If the new configuration is proper, stop the poller if needed
            stopPolling(true);
            // Then start it again if it was stopped
            startPolling();
        } else {
            // Stop polling if the new config is invalid
            stopPolling(false);
        }
    }

    /**
     * Check the current configuration
     *
     * @return true if the configuration is ok to start polling, false otherwise
     */
    private boolean configure() {
        if (config.ipAddress.trim().isEmpty()) {
            // since it is marked as required, initialize() should not be called if this field is empty
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing ipAddress/host configuration");
            return false;
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            try {
                configurationLock.lock();
                stecaHost = config.ipAddress.trim();
            } finally {
                configurationLock.unlock();
            }
            return true;
        }
    }

    /**
     * Stop the poller unconditionally
     */
    @Override
    public void dispose() {
        stopPolling(false);
    }

    /**
     * Stop the polling job
     *
     * @param onlyIfNeeded if true polling will only actually stop if a new refresh interval should be set
     */
    private void stopPolling(boolean onlyIfNeeded) {
        try {
            pollingJobLock.lock();
            if (pollingJob != null && !pollingJob.isCancelled()) {
                if (!onlyIfNeeded || config.refreshDelay != currentRefreshDelay) {
                    if (pollingJob != null) {
                        pollingJob.cancel(true);
                    }
                    pollingJob = null;
                }
            }
        } finally {
            pollingJobLock.unlock();
        }
    }

    /**
     * Start a polling job if it is not already running.
     */
    private void startPolling() {
        try {
            pollingJobLock.lock();
            boolean startPoller = false;
            if (pollingJob != null) {
                startPoller = pollingJob.isCancelled();
            } else {
                startPoller = true;
            }

            if (startPoller) {
                currentRefreshDelay = config.refreshDelay;
                pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, currentRefreshDelay,
                        TimeUnit.SECONDS);
            }
        } finally {
            pollingJobLock.unlock();
        }
    }

    /**
     * The actual polling loop
     */
    private void pollingCode() {

        // start with creating a local copy of the required configuration for this run
        String measurementsURL = "";
        String yieldMonthURL = "";
        // String yieldYearURL = "";

        try {
            configurationLock.lock();
            measurementsURL = String.format("http://%s/measurements.xml", stecaHost);
            yieldMonthURL = String.format("http://%s/yields.json?month=1", stecaHost);
            // yieldYearURL = String.format("http://%s/yields.json?year=1", stecaHost);
        } finally {
            configurationLock.unlock();
        }

        String result;

        // first get the current measurements
        try {
            result = HttpUtil.executeUrl("GET", measurementsURL, 1000);

            if (result.trim().isEmpty()) {
                logger.warn("Empty Measurement data at {} ", measurementsURL);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Inverter returned empty measurements");
                return;
            }

            updateStatus(ThingStatus.ONLINE);

            try {
                if (xstream != null) {
                    Measurements measurements = (Measurements) xstream.fromXML(result);

                    Device d = measurements.getDevice();

                    if (deviceName != d.getName()) {
                        deviceName = d.getName();
                        updateProperty(StecaGridBindingConstants.PROPERTY_DEVICE_NAME, deviceName);
                    }

                    if (deviceNominalPower != d.getNominalPower()) {
                        deviceNominalPower = d.getNominalPower();
                        updateProperty(StecaGridBindingConstants.PROPERTY_DEVICE_NOMINAL_POWER, deviceNominalPower);
                    }

                    if (deviceType != d.getType()) {
                        deviceType = d.getType();
                        updateProperty(StecaGridBindingConstants.PROPERTY_DEVICE_TYPE, deviceType);
                    }

                    // There's no sense, it's all Volta, Ampère and Ohm
                    // Earth to Moon, it's the same as London-Rome

                    updateState(StecaGridBindingConstants.CHANNEL_AC_VOLTAGE,
                            new QuantityType<>(d.getAcVoltage(), Units.VOLT));

                    updateState(StecaGridBindingConstants.CHANNEL_AC_CURRENT,
                            new QuantityType<>(d.getAcCurrent(), Units.AMPERE));

                    updateState(StecaGridBindingConstants.CHANNEL_AC_POWER,
                            new QuantityType<>(d.getAcPower(), Units.WATT));

                    updateState(StecaGridBindingConstants.CHANNEL_AC_POWER_FAST,
                            new QuantityType<>(d.getAcPowerFast(), Units.WATT));

                    updateState(StecaGridBindingConstants.CHANNEL_AC_FREQUENCY,
                            new QuantityType<>(d.getAcFrequency(), Units.HERTZ));

                    updateState(StecaGridBindingConstants.CHANNEL_DC_VOLTAGE,
                            new QuantityType<>(d.getDcVoltage(), Units.VOLT));

                    updateState(StecaGridBindingConstants.CHANNEL_DC_CURRENT,
                            new QuantityType<>(d.getDcCurrent(), Units.AMPERE));

                    updateState(StecaGridBindingConstants.CHANNEL_LINK_VOLTAGE,
                            new QuantityType<>(d.getLinkVoltage(), Units.VOLT));

                    updateState(StecaGridBindingConstants.CHANNEL_DERATING,
                            new QuantityType<>(d.getDerating(), Units.ONE));

                    updateState(StecaGridBindingConstants.CHANNEL_GRID_POWER,
                            new QuantityType<>(d.getGridPower(), Units.WATT));
                    updateState(StecaGridBindingConstants.CHANNEL_GRID_CONSUMED_POWER,
                            new QuantityType<>(d.getGridConsumedPower(), Units.WATT));
                    updateState(StecaGridBindingConstants.CHANNEL_GRID_INJECTED_POWER,
                            new QuantityType<>(d.getGridInjectedPower(), Units.WATT));
                    updateState(StecaGridBindingConstants.CHANNEL_OWN_CONSUMED_POWER,
                            new QuantityType<>(d.getOwnConsumedPower(), Units.WATT));
                }
            } catch (Exception ex) {
                logger.warn("woopy", ex);
            }
        } catch (IOException e) {
            logger.warn("Measurement not found at {}", measurementsURL);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to query inverter");
        }

        try {
            result = HttpUtil.executeUrl("GET", yieldMonthURL, 2000);

            if (result.trim().isEmpty()) {
                logger.warn("Empty yield data at {} ", yieldMonthURL);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Inverter returned empty yields");
                return;
            }

            updateStatus(ThingStatus.ONLINE);

            DailyYields daily = gson.fromJson(result, DailyYields.class);
            if (daily != null) {
                updateState(StecaGridBindingConstants.CHANNEL_YIELD_DAY_CURRENT,
                        new QuantityType<>(daily.getTodaysYieldKWh(), Units.KILOWATT_HOUR));
                updateState(StecaGridBindingConstants.CHANNEL_YIELD_DAY_PREVIOUS,
                        new QuantityType<>(daily.getYesterdaysYieldKWh(), Units.KILOWATT_HOUR));
            }
        } catch (IOException e) {
            logger.warn("Monthly Yields not found at {}", yieldMonthURL);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Unable to query inverter");
            return;
        }
    }
}
