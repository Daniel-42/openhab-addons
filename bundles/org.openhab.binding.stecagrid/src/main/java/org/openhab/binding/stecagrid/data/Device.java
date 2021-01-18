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

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@NonNullByDefault
public class Device {

    @XStreamAlias("Name")
    @XStreamAsAttribute
    private String name = "";
    @XStreamAlias("Type")
    @XStreamAsAttribute
    private String type = "";
    @XStreamAlias("Platform")
    @XStreamAsAttribute
    private String platform = "";
    @XStreamAlias("HmiPlatform")
    @XStreamAsAttribute
    private String hmiPlatform = "";
    @XStreamAlias("NominalPower")
    @XStreamAsAttribute
    private String nominalPower = "";
    @XStreamAlias("UserPowerLimit")
    @XStreamAsAttribute
    private String userPowerLimit = "";
    @XStreamAlias("CountryPowerLimit")
    @XStreamAsAttribute
    private String countryPowerLimit = "";
    @XStreamAlias("Serial")
    @XStreamAsAttribute
    private String serial = "";
    @XStreamAlias("OEMSerial")
    @XStreamAsAttribute
    private String oemSerial = "";
    @XStreamAlias("BusAddress")
    @XStreamAsAttribute
    private String busAddress = "";
    @XStreamAlias("NetBiosName")
    @XStreamAsAttribute
    private String netBiosName = "";
    @XStreamAlias("WebPortal")
    @XStreamAsAttribute
    private String webPortal = "";
    @XStreamAlias("ManufacturerURL")
    @XStreamAsAttribute
    private String manufacturerURL = "";
    @XStreamAlias("IpAddress")
    @XStreamAsAttribute
    private String ipAddress = "";
    @XStreamAlias("DateTime")
    @XStreamAsAttribute
    private String dateTime = "";
    @XStreamAlias("MilliSeconds")
    @XStreamAsAttribute
    private String milliSeconds = "";

    @XStreamAlias("Measurements")
    private ArrayList<Measurement> measurements = new ArrayList<Measurement>();

    public Device() {
        measurements.add(new Measurement());
        measurements.add(new Measurement());
    }

    /**
     * Getter for Name
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for Type
     *
     * @return Type
     */
    public String getType() {
        return type;
    }

    /**
     * Getter for Platform
     *
     * @return Platform
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Getter for HmiPlatform
     *
     * @return HmiPlatform
     */
    public String getHmiPlatform() {
        return hmiPlatform;
    }

    /**
     * Getter for NominalPower
     *
     * @return NominalPower
     */
    public String getNominalPower() {
        return nominalPower;
    }

    /**
     * Getter for UserPowerLimit
     *
     * @return UserPowerLimit
     */
    public String getUserPowerLimit() {
        return userPowerLimit;
    }

    /**
     * Getter for CountryPowerLimit
     *
     * @return CountryPowerLimit
     */
    public String getCountryPowerLimit() {
        return countryPowerLimit;
    }

    /**
     * Getter for Serial
     *
     * @return Serial
     */
    public String getSerial() {
        return serial;
    }

    /**
     * Getter for OemSerial
     *
     * @return OemSerial
     */
    public String getOemSerial() {
        return oemSerial;
    }

    /**
     * Getter for BusAddress
     *
     * @return BusAddress
     */
    public String getBusAddress() {
        return busAddress;
    }

    /**
     * Getter for NetBiosName
     *
     * @return NetBiosName
     */
    public String getNetBiosName() {
        return netBiosName;
    }

    /**
     * Getter for WebPortal
     *
     * @return WebPortal
     */
    public String getWebPortal() {
        return webPortal;
    }

    /**
     * Getter for ManufacturerURL
     *
     * @return ManufacturerURL
     */
    public String getManufacturerURL() {
        return manufacturerURL;
    }

    /**
     * Getter for IpAddress
     *
     * @return IpAddress
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Getter for DateTime
     *
     * @return DateTime
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * Getter for MilliSeconds
     *
     * @return MilliSeconds
     */
    public String getMilliSeconds() {
        return milliSeconds;
    }

    public double getAcVoltage() {
        return getNamedDouble("AC_Voltage");
    }

    public double getAcCurrent() {
        return getNamedDouble("AC_Current");
    }

    public double getAcPower() {
        return getNamedDouble("AC_Power_fast");
    }

    public double getAcPowerFast() {
        return getNamedDouble("AC_Power_fast");
    }

    public double getDcCurrent() {
        return getNamedDouble("DC_Current");
    }

    public double getDcVoltage() {
        return getNamedDouble("DC_Voltage");
    }

    public double getAcFrequency() {
        return getNamedDouble("AC_Frequency");
    }

    public double getDerating() {
        return getNamedDouble("Derating");
    }

    public double getLinkVoltage() {
        return getNamedDouble("Link_Voltage");
    }

    private double getNamedDouble(String name) {
        for (Measurement m : measurements) {
            if (m.getType().equals(name)) {
                return m.getValue();
            }
        }
        return 0.0;
    }

}
