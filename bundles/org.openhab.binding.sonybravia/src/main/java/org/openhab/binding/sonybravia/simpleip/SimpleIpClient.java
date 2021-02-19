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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class opens a TCP/IP connection to a Sony Bravia TV.
 *
 * @author Daniël van Os - Initial contribution (based upon OnkyoConnection class by Pauli Anttila)
 */
@NonNullByDefault
public class SimpleIpClient {

    private final Logger logger = LoggerFactory.getLogger(SimpleIpClient.class);

    /** default Simple IP port. **/
    public static final int DEFAULT_SIMPLE_IP_PORT = 20060;

    /** Connection timeout in milliseconds **/
    private static final int CONNECTION_TIMEOUT = 5000;

    /** Connection test interval in milliseconds **/
    private static final int CONNECTION_TEST_INTERVAL = 60000;

    /** Socket timeout in milliseconds **/
    private static final int SOCKET_TIMEOUT = CONNECTION_TEST_INTERVAL + 10000;

    /** Connection retry count on error situations **/
    private static final int FAST_CONNECTION_RETRY_COUNT = 3;

    /** Connection retry delays in milliseconds **/
    private static final int FAST_CONNECTION_RETRY_DELAY = 1000;
    private static final int SLOW_CONNECTION_RETRY_DELAY = 60000;

    private final Socket simpleIpSocket = new Socket();
    private final ConnectionSupervisor supervisor = new ConnectionSupervisor(CONNECTION_TEST_INTERVAL);

    private boolean connected = false;
    private int retryCount = 1;

    private String ip;
    private int port;
    private List<SimpleIpEventListener> listeners = new ArrayList<>();

    private @Nullable DataListener dataListener;
    private @Nullable DataOutputStream outStream;
    private @Nullable DataInputStream inStream;

    public SimpleIpClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * Open connection to the Bravia TV.
     */
    public void openConnection() {
        connectSocket();
    }

    /**
     * Closes the connection to the Bravia TV.
     */
    public void closeConnection() {
        closeSocket();
    }

    public void addEventListener(SimpleIpEventListener listener) {
        this.listeners.add(listener);
    }

    public void removeEventListener(SimpleIpEventListener listener) {
        this.listeners.remove(listener);
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Sends a request message to a Bravia TV.
     *
     * @param cmd Simple Ip command to send
     */
    public void enquire(SimpleIpCommand cmd) {
        try {
            sendCommand(
                    new SimpleIpMessage.MessageBuilder().messageType(SimpleIpMessageType.ENQUIRY).command(cmd).build());
        } catch (Exception e) {
            logger.warn("Could not send request to device on {}:{}: ", ip, port, e);
        }
    }

    /**
     * Sends a control message to a Bravia TV.
     *
     * @param cmd Simple Ip command to send
     * @param parameters Parameters to send with the command
     */
    public void control(SimpleIpCommand cmd, SimpleIpParameter parameters) {
        try {
            sendCommand(new SimpleIpMessage.MessageBuilder().messageType(SimpleIpMessageType.CONTROL).command(cmd)
                    .parameter(parameters).build());
        } catch (Exception e) {
            logger.warn("Could not send request to device on {}:{}: ", ip, port, e);
        }
    }

    private void sendCommand(SimpleIpMessage msg) {
        logger.debug("Send command: {} to {}:{} ({})", msg.toString(), ip, port, simpleIpSocket);
        sendCommand(msg, retryCount);
    }

    /**
     * Sends to command to the receiver.
     *
     * @param msg the message to send
     * @param retry retry count when connection fails.
     */
    private void sendCommand(SimpleIpMessage msg, int retry) {
        if (connectSocket()) {
            try {
                var os = outStream;
                if (os != null) {
                    os.writeBytes(SimpleIpProtocol.createPdu(msg));
                    os.flush();
                }
            } catch (IOException ioException) {
                if (retry > 0) {
                    closeSocket();
                    sendCommand(msg, retry - 1);
                } else {
                    sendConnectionErrorEvent(ioException.getMessage());
                }
            }
        }
    }

    /**
     * Connects to the receiver by opening a socket connection through the
     * IP and port.
     */
    private synchronized boolean connectSocket() {

        if (!connected || !simpleIpSocket.isConnected()) {
            try {
                supervisor.startConnectionTest();
                simpleIpSocket.connect(new InetSocketAddress(ip, port), CONNECTION_TIMEOUT);

                // Get Input and Output streams
                outStream = new DataOutputStream(simpleIpSocket.getOutputStream());
                outStream.flush();

                InputStream is = simpleIpSocket.getInputStream();
                if (is != null) {
                    inStream = new DataInputStream(is);
                } else {
                    throw new SimpleIpException("Unable to get Input Stream");
                }

                simpleIpSocket.setSoTimeout(SOCKET_TIMEOUT);
                connected = true;

                // start status update listener
                if (dataListener == null) {
                    dataListener = new DataListener();
                    dataListener.start();
                }
            } catch (UnknownHostException uhe) {
                sendConnectionErrorEvent(uhe.getMessage());
            } catch (IOException ioe) {
                sendConnectionErrorEvent(ioe.getMessage());
            } catch (SimpleIpException sie) {
                sendConnectionErrorEvent(sie.getMessage());
            }
        }

        return connected;
    }

    /**
     * Closes the socket connection.
     *
     * @return true if the closed successfully
     */
    private boolean closeSocket() {
        try {
            if (dataListener != null) {
                dataListener.setInterrupted(true);
                dataListener = null;
            }

            supervisor.stopConnectionTester();

            if (inStream != null) {
                IOUtils.closeQuietly(inStream);
                inStream = null;
            }
            if (outStream != null) {
                IOUtils.closeQuietly(outStream);
                outStream = null;
            }
            IOUtils.closeQuietly(simpleIpSocket);
            connected = false;
        } catch (Exception e) {
            logger.debug("Closing connection throws an exception, {}", e.getMessage());
        }

        return connected;
    }

    /**
     * This method wait any state messages form receiver.
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws EiscpException
     */
    private void waitStateMessages()
            throws NumberFormatException, IOException, InterruptedException, SimpleIpException {
        if (connected) {
            while (true) {
                if (inStream != null) { // yikes -> busy wait possible if null
                    SimpleIpMessage message = SimpleIpProtocol.getNextMessage(inStream);
                    sendMessageEvent(message);
                }
            }
        } else {
            throw new IOException("Not Connected to Receiver");
        }
    }

    private class DataListener extends Thread {
        private boolean interrupted = false;

        DataListener() {
        }

        public void setInterrupted(boolean interrupted) {
            this.interrupted = interrupted;
            this.interrupt();
        }

        @Override
        public void run() {
            boolean restartConnection = false;
            long connectionAttempts = 0;

            // as long as no interrupt is requested, continue running
            while (!interrupted) {
                try {
                    waitStateMessages();
                    connectionAttempts = 0;
                } catch (SimpleIpException e) {
                    logger.warn("Error occurred during message waiting: {}", e.getMessage());
                } catch (SocketTimeoutException e) {
                    logger.warn("No data received during supervision interval ({} ms)!", SOCKET_TIMEOUT);
                    restartConnection = true;
                } catch (Exception e) {
                    if (!interrupted && !this.isInterrupted()) {
                        logger.warn("Error occurred during message waiting: {}", e.getMessage());
                        restartConnection = true;

                        // sleep a while, to prevent fast looping if error situation is permanent
                        if (++connectionAttempts < FAST_CONNECTION_RETRY_COUNT) {
                            mysleep(FAST_CONNECTION_RETRY_DELAY);
                        } else {
                            mysleep(SLOW_CONNECTION_RETRY_DELAY);
                        }
                    }
                }

                if (restartConnection) {
                    restartConnection = false;

                    try {
                        connected = false;
                        connectSocket();
                        enquire(SimpleIpCommand.POWER_STATUS);
                    } catch (Exception ex) {
                        sendConnectionErrorEvent(ex.getMessage());
                    }
                }
            }
        }

        private void mysleep(long milli) {
            try {
                sleep(milli);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
    }

    /**
     * This class periodically requests data, which is used
     * to see if the connection is still up.
     */
    private class ConnectionSupervisor {
        private final int timeOut;

        private @Nullable Timer timer;

        public ConnectionSupervisor(int timeOut) {
            this.timeOut = timeOut;
        }

        public void startConnectionTest() {
            timer = new Timer();
            timer.schedule(new Task(), 5000, timeOut);
        }

        public void stopConnectionTester() {
            if (timer != null) {
                timer.cancel();
            }
        }

        class Task extends TimerTask {
            @Override
            public void run() {
                enquire(SimpleIpCommand.POWER_STATUS);
            }
        }
    }

    private void sendConnectionErrorEvent(@Nullable String errorMsg) {
        // send message to event listeners
        String messageToSend = (errorMsg != null) ? errorMsg : "Error";
        try {
            for (SimpleIpEventListener listener : listeners) {
                listener.connectionError(ip, messageToSend);
            }
        } catch (Exception ex) {
            logger.debug("Event listener invoking error: {}", ex.getMessage());
        }
    }

    private void sendMessageEvent(SimpleIpMessage message) {
        // send message to event listeners
        try {
            for (SimpleIpEventListener listener : listeners) {
                listener.statusUpdateReceived(ip, message);
            }
        } catch (Exception e) {
            logger.warn("Event listener invoking error: {}", e.getMessage());
        }
    }
}