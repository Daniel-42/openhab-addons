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
package org.openhab.binding.knmi.data;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@NonNullByDefault
@XStreamAlias("channel")
public class Channel {

    private String title = "";

    @XStreamImplicit(itemFieldName = "item")
    private ArrayList<Item> items = new ArrayList<Item>();

    public String getTitle() {
        return title;
    }

    public ArrayList<Item> getItems() {
        return items;
    }
}
