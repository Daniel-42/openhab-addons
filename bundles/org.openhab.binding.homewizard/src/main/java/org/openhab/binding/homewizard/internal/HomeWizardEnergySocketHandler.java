/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.homewizard.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link HomeWizardEnergySocketHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class HomeWizardEnergySocketHandler extends HomeWizardDeviceHandler {

    /**
     * Constructor
     *
     * @param thing The thing to handle
     */
    public HomeWizardEnergySocketHandler(Thing thing) {
        super(thing, true);
    }

    /**
     * Not listening to any commands yet.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getIdWithoutGroup()) {
            case HomeWizardBindingConstants.CHANNEL_RING_BRIGHTNESS: {
                int b = (Integer.valueOf(command.toFullString()) * 255) / 100;
                InputStream is = new ByteArrayInputStream(String.format("{\"brightness\": %d}", b).getBytes());
                try {
                    HttpUtil.executeUrl("PUT", stateURL, is, "application/json", 30000);
                } catch (IOException e) {
                }
                break;
            }
            case HomeWizardBindingConstants.CHANNEL_POWER_SWITCH: {
                boolean onOff = command.equals(OnOffType.ON);
                InputStream is = new ByteArrayInputStream(String.format("{\"power_on\": %b}", onOff).getBytes());
                try {
                    HttpUtil.executeUrl("PUT", stateURL, is, "application/json", 30000);
                } catch (IOException e) {
                }
                break;
            }
            case HomeWizardBindingConstants.CHANNEL_POWER_LOCK: {
                boolean onOff = command.equals(OnOffType.ON);
                InputStream is = new ByteArrayInputStream(String.format("{\"switch_lock\": %b}", onOff).getBytes());
                try {
                    HttpUtil.executeUrl("PUT", stateURL, is, "application/json", 30000);
                } catch (IOException e) {
                }
                break;
            }
            default:
                logger.warn("Should handle {} {}", channelUID.getIdWithoutGroup(), command);
                break;
        }
    }

    /**
     * Device specific handling of the returned payload.
     *
     * @param payload The data parsed from the Json file
     */
    @Override
    protected void handleDataPayload(DataPayload payload) {
        updateState(HomeWizardBindingConstants.CHANNEL_ENERGY_IMPORT_T1,
                new QuantityType<>(payload.getTotalEnergyImportT1Kwh(), Units.KILOWATT_HOUR));
        updateState(HomeWizardBindingConstants.CHANNEL_ENERGY_EXPORT_T1,
                new QuantityType<>(payload.getTotalEnergyExportT1Kwh(), Units.KILOWATT_HOUR));

        updateState(HomeWizardBindingConstants.CHANNEL_ACTIVE_POWER,
                new QuantityType<>(payload.getActivePowerW(), Units.WATT));
    }

    @Override
    protected void handleStatePayload(StatePayload payload) {
        updateState(HomeWizardBindingConstants.CHANNEL_POWER_SWITCH, OnOffType.from(payload.getPowerOn()));
        updateState(HomeWizardBindingConstants.CHANNEL_POWER_LOCK, OnOffType.from(payload.getSwitchLock()));
        updateState(HomeWizardBindingConstants.CHANNEL_RING_BRIGHTNESS,
                new PercentType(100 * payload.getBrightness() / 255));
    }
}
