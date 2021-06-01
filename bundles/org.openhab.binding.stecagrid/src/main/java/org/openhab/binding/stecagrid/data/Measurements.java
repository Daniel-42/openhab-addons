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
package org.openhab.binding.stecagrid.data;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
@XStreamAlias("root")
public class Measurements {
    @XStreamAlias("Device")
    private Device device = new Device();

    public Device getDevice() {
        return device;
    }
}
