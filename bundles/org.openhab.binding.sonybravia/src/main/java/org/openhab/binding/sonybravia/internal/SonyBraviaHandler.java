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
import org.openhab.binding.sonybravia.simpleip.SimpleIpClient;
import org.openhab.binding.sonybravia.simpleip.SimpleIpCommand;
import org.openhab.binding.sonybravia.simpleip.SimpleIpEventListener;
import org.openhab.binding.sonybravia.simpleip.SimpleIpMessage;
import org.openhab.binding.sonybravia.simpleip.SimpleIpMessageType;
import org.openhab.binding.sonybravia.simpleip.SimpleIpParameter;
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

            connection.enquire(SimpleIpCommand.AUDIO_VOLUME);
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

    private void handleAnswer(SimpleIpMessage message) {
        SimpleIpCommand command = message.getCommand();
        SimpleIpParameter parameter = message.getParameter();
        switch (command) {
            case POWER_STATUS:
                if (parameter.isError()) {
                    logger.warn("Answer: Power Status = ERROR");
                } else {
                    // tricky... answer 0 to set == success, answer 0 to get == off
                    logger.warn("Answer: Power Status = {}", parameter.isOn());
                    updateState(SonyBraviaBindingConstants.CHANNEL_POWER, OnOffType.from(parameter.isOn()));
                }
                break;
            case AUDIO_VOLUME:
                if (parameter.isError()) {
                    logger.warn("Answer: Audio Volume = ERROR");
                } else {
                    logger.warn("Answer: Audio Volume = {}", parameter.asVolume());
                    updateState(SonyBraviaBindingConstants.CHANNEL_VOLUME, new PercentType(parameter.asVolume()));
                }
                break;
            case AUDIO_MUTE:
                if (parameter.isError()) {
                    logger.warn("Answer: Audio Mute = ERROR");
                } else {
                    logger.warn("Answer: Audio Mute = {}", parameter.isOn());
                    updateState(SonyBraviaBindingConstants.CHANNEL_MUTE, OnOffType.from(parameter.isOn()));
                }
                break;
            case INPUT:
                if (parameter.isError()) {
                    logger.warn("Answer: Input = ERROR");
                } else if (parameter.isNotFound()) {
                    logger.warn("Answer: Input = NOT FOUND");
                } else {
                    logger.warn("Answer: Input = {}/{}", parameter.asInputType(), parameter.asInputSequence());
                }
                break;
            default:
                break;
        }
    }

    private void handleNotification(SimpleIpMessage message) {
        SimpleIpCommand command = message.getCommand();
        SimpleIpParameter parameter = message.getParameter();

        switch (command) {
            case POWER_STATUS:
                logger.warn("Notification: Power Status = {}", parameter.isOn());
                break;
            case AUDIO_VOLUME:
                logger.warn("Notification: Audio Volume = {}", parameter.asVolume());
                break;
            case AUDIO_MUTE:
                logger.warn("Notification: Audio Mute = {}", parameter.isOn());
                break;
            case INPUT:
                logger.warn("Notification: Input = {}/{}", parameter.asInputType(), parameter.asInputSequence());
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
