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
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@NonNullByDefault
@XStreamAlias("Measurement")
public class Measurement {
    @XStreamAlias("Value")
    @XStreamAsAttribute
    private String value = "";
    @XStreamAlias("Unit")
    @XStreamAsAttribute
    private String unit = "";
    @XStreamAlias("Type")
    @XStreamAsAttribute
    private String type = "";

    /**
     * Getter for Value
     *
     * @return Value
     */
    public String getValue() {
        return value;
    }

    /**
     * Getter for unit
     *
     * @return unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Getter for type
     *
     * @return type
     */
    public String getType() {
        return type;
    }
}
