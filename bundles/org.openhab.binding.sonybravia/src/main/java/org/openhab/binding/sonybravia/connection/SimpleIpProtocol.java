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
package org.openhab.binding.sonybravia.connection;

import java.io.DataInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle Sony Bravia Simple IP protocol.
 *
 * @author Daniël van Os - Initial contribution (based upon EiscpProtocol class by Pauli Anttila)
 */
public class SimpleIpProtocol {

    private static final Logger logger = LoggerFactory.getLogger(SimpleIpProtocol.class);

    /**
     * Wraps a command in a Simple IP data message (data characters).
     *
     * @param msg Simple IP command/parameter
     * @return String holding the full Simple IP message packet
     */
    public static String createPdu(SimpleIpMessage msg) {
        String data = msg.getMessageType().getTypeChar() + msg.getCommand().getCommand() + msg.getValue();
        StringBuilder sb = new StringBuilder();

        sb.append("*S");
        sb.append(data);
        sb.append((char) 0x0a);

        if (sb.length() != 24) {
            logger.warn("Constructed Simple IP message length is not 24 but {}: {}", sb.length(), sb.toString());
        }

        return sb.toString();
    }

    /**
     * Method to read eISCP message from input stream.
     *
     * @return message
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws SimpleIpException Thrown if the network packet can't be converted into a SimpleIpMessage
     */
    public static SimpleIpMessage getNextMessage(DataInputStream stream)
            throws IOException, InterruptedException, SimpleIpException {
        while (true) {
            // 1st 3 chars are the lead in
            byte firstByte = stream.readByte();
            if (firstByte != '*') {
                logger.warn("Simple IP Message did not start with *, ignoring");
                continue;
            }

            if (stream.readByte() != 'S') {
                logger.warn("Simple IP Message 2 is not S, ignoring");
                continue;
            }

            SimpleIpMessageType messageType = SimpleIpMessageType.getIpMessageType(stream.readNBytes(1));
            SimpleIpCommand command = SimpleIpCommand.getIpCommand(messageType, stream.readNBytes(4));
            String value = new String(stream.readNBytes(16));

            if (stream.readByte() != 0x0a) {
                logger.warn("Simple IP Message no \\n");
                continue;
            }

            return new SimpleIpMessage.MessageBuilder().messageType(messageType).command(command).value(value).build();
        }
    }
}