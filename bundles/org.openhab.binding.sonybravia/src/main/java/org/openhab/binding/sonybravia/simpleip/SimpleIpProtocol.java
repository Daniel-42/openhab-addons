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
package org.openhab.binding.sonybravia.simpleip;

import java.io.DataInputStream;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle Sony Bravia Simple IP protocol.
 *
 * @author Daniël van Os - Initial contribution (loosely based upon EiscpProtocol class by Pauli Anttila)
 */
@NonNullByDefault
public class SimpleIpProtocol {

    private static final Logger logger = LoggerFactory.getLogger(SimpleIpProtocol.class);

    /**
     * Wraps a command in a Simple IP data message (data characters).
     *
     * @param msg Simple IP command/parameter
     * @return String holding the full Simple IP message packet
     */
    public static String createPdu(SimpleIpMessage msg) {
        StringBuilder sb = new StringBuilder();

        sb.append("*S");
        sb.append(msg.getMessageType().getTypeString());
        sb.append(msg.getCommand().getCommandString());
        sb.append(msg.getParameter().getParameterString());
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

        boolean previousMessageOk = true; // used to prevent log flooding

        while (true) {
            // 1st 3 chars are the lead in
            byte firstByte = stream.readByte();
            if (firstByte != '*') {
                if (previousMessageOk) {
                    logger.warn("Simple IP Message did not start with *, ignoring.");
                    previousMessageOk = false;
                }
                continue;
            }

            if (stream.readByte() != 'S') {
                if (previousMessageOk) {
                    logger.warn("Simple IP Message did not start with *S, ignoring.");
                    previousMessageOk = false;
                }
                continue;
            }

            // If we get this far, I want to see the next failure in the logging
            previousMessageOk = true;

            SimpleIpMessageType messageType = SimpleIpMessageType.getIpMessageType(stream.readNBytes(1));
            SimpleIpCommand command = SimpleIpCommand.getIpCommand(stream.readNBytes(4));
            SimpleIpParameter parameter = new SimpleIpParameter(new String(stream.readNBytes(16)));

            if (stream.readByte() != 0x0a) {
                logger.warn("Simple IP Message did not end with 0x0a, ignoring.");
                continue;
            }

            return new SimpleIpMessage.MessageBuilder().messageType(messageType).command(command).parameter(parameter)
                    .build();
        }
    }
}