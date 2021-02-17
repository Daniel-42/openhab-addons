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
package org.openhab.binding.sonybravia.internal;

import static org.openhab.binding.sonybravia.internal.SonyBraviaBindingConstants.CHANNEL_POWER;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonybravia.connection.SimpleIpClient;
import org.openhab.binding.sonybravia.connection.SimpleIpCommand;
import org.openhab.binding.sonybravia.connection.SimpleIpEventListener;
import org.openhab.binding.sonybravia.connection.SimpleIpMessage;
import org.openhab.binding.sonybravia.connection.SimpleIpMessageType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonyBraviaHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class SonyBraviaHandler extends BaseThingHandler implements SimpleIpEventListener {

    private final Logger logger = LoggerFactory.getLogger(SonyBraviaHandler.class);

    private @Nullable SonyBraviaConfiguration config;
    private @Nullable SimpleIpClient connection;

    public SonyBraviaHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_POWER.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                // TODO: handle data refresh
            }

            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information:
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        // WILL BE USED FOR IP AND PORT
        config = getConfigAs(SonyBraviaConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        SimpleIpClient connection = new SimpleIpClient("10.42.1.51", 20060);
        connection.addEventListener(this);
        connection.openConnection();
        this.connection = connection;
        if (connection.isConnected()) {
            updateStatus(ThingStatus.ONLINE);

            connection.send(SimpleIpCommand.AUDIO_VOLUME_GET, SimpleIpCommand.AUDIO_VOLUME_GET.getValue());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (connection != null) {
            var cnx = connection;
            cnx.removeEventListener(this);
            cnx.closeConnection();
        }
    }

    @Override
    public void statusUpdateReceived(String ip, SimpleIpMessage data) {
        logger.warn("Received: {}", data.toString());

        SimpleIpMessageType type = data.getMessageType();
        switch (type) {
            case NOTIFY:
                handleNotification(data);
                break;
            case ANSWER:
                handleAnswer(data);
                break;
            default:
            case CONTROL:
            case ENQUIRY:
                // ignore, should not happen
                break;
        }
    }

    private void handleAnswer(SimpleIpMessage data) {
        SimpleIpCommand command = data.getCommand();
        switch (command) {
            case POWER_STATUS_ANSWER:
                if (data.getValue().equals(SimpleIpCommand.Constants.ERROR_PARAMETER)) {
                    logger.warn("Answer: Power Status = ERROR");
                } else {
                    // tricky... answer 0 to set == success, answer 0 to get == off
                    boolean isOn = Integer.parseInt(data.getValue()) == 1 ? true : false;
                    logger.warn("Answer: Power Status = {}", isOn);
                    updateState(SonyBraviaBindingConstants.CHANNEL_POWER, OnOffType.from(isOn));
                }
                break;
            case AUDIO_VOLUME_ANSWER:
                if (data.getValue().equals(SimpleIpCommand.Constants.ERROR_PARAMETER)) {
                    logger.warn("Answer: Audio Volume = ERROR");
                } else {
                    int volume = Integer.parseInt(data.getValue());
                    logger.warn("Answer: Audio Volume = {}", volume);
                    updateState(SonyBraviaBindingConstants.CHANNEL_VOLUME, new PercentType(volume));
                }
                break;
            case AUDIO_MUTE_ANSWER:
                if (data.getValue().equals(SimpleIpCommand.Constants.ERROR_PARAMETER)) {
                    logger.warn("Answer: Audio Mute = ERROR");
                } else {
                    boolean isOn = Integer.parseInt(data.getValue()) == 1 ? true : false;
                    logger.warn("Answer: Audio Mute = {}", isOn);
                    updateState(SonyBraviaBindingConstants.CHANNEL_MUTE, OnOffType.from(isOn));
                }
                break;
            case INPUT_ANSWER:
                if (data.getValue().equals(SimpleIpCommand.Constants.ERROR_PARAMETER)) {
                    logger.warn("Answer: Input = ERROR");
                } else if (data.getValue().equals(SimpleIpCommand.Constants.NOT_FOUND)) {
                    logger.warn("Answer: Input = NOT FOUND");
                } else {
                    int input = Integer.parseInt(data.getValue().substring(0, 8));
                    int subInput = Integer.parseInt(data.getValue().substring(8, 16));
                    logger.warn("Answer: Input = {}/{}", input, subInput);
                }
                break;
            default:
                break;
        }
    }

    private void handleNotification(SimpleIpMessage data) {
        SimpleIpCommand command = data.getCommand();
        switch (command) {
            case FIRE_POWER_CHANGE:
                logger.warn("Notification: Power Status = {}", Integer.parseInt(data.getValue()));
                break;
            case FIRE_VOLUME_CHANGE:
                logger.warn("Notification: Audio Volume = {}", Integer.parseInt(data.getValue()));
                break;
            case FIRE_MUTE_CHANGE:
                logger.warn("Notification: Audio Mute = {}", Integer.parseInt(data.getValue()));
                break;
            case FIRE_INPUT_CHANGE:
                int input = Integer.parseInt(data.getValue().substring(0, 8));
                int subInput = Integer.parseInt(data.getValue().substring(8, 16));
                logger.warn("Notification: Input = {}/{}", input, subInput);
                break;
            default:
                break;
        }
    }

    /**
     * Handler for connection errors.
     *
     * @param errorMsg Reason for error.
     */
    @Override
    public void connectionError(String ip, String errorMsg) {
        logger.warn("Connection Error: {}", errorMsg);
        updateStatus(ThingStatus.OFFLINE);
    }

}
