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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This interface defines interface to receive status updates from a Sony Bravia TV.
 *
 * @author Daniël van Os - Initial contribution (based upon OnkyoEventListener class by Pauli Anttila)
 */
@NonNullByDefault
public interface SimpleIpEventListener {

    /**
     * Callback for receiving a status update from a Sony Bravia TV.
     *
     * @param message Received data.
     */
    void statusUpdateReceived(String ip, SimpleIpMessage message);

    /**
     * Callback for receiving error events from a Sony Bravia TV.
     *
     * @param errorMessage Details of the error
     */
    void connectionError(String ip, String errorMessage);
}