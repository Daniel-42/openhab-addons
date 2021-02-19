package org.openhab.binding.sonybravia.simpleip;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SimpleIpParameter {
    public static final String PARAMETER_NONE = "################";
    public static final String PARAMETER_SUCCESS = "0000000000000000";
    public static final String PARAMETER_ERROR = "FFFFFFFFFFFFFFFF";
    public static final String PARAMETER_NOT_FOUND = "NNNNNNNNNNNNNNNN";
    public static final String PARAMETER_OFF = "0000000000000000";
    public static final String PARAMETER_ON = "0000000000000001";

    public static final String INPUT_HDMI = "00000001";
    public static final String INPUT_COMPONENT = "00000004";
    public static final String INPUT_SCREEN_MIRRORING = "00000005";

    private String parameter = "";

    public SimpleIpParameter() {
        this.parameter = PARAMETER_NONE;
    }

    public SimpleIpParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getParameterString() {
        return parameter;
    }

    public boolean isError() {
        return PARAMETER_ERROR.equals(parameter);
    }

    public boolean isNotFound() {
        return PARAMETER_NOT_FOUND.equals(parameter);
    }

    public boolean isSuccess() {
        return PARAMETER_SUCCESS.equals(parameter);
    }

    public boolean isOn() {
        return PARAMETER_ON.equals(parameter);
    }

    public boolean isOff() {
        return PARAMETER_OFF.equals(parameter);
    }

    public int asVolume() throws NumberFormatException {
        return Integer.parseInt(parameter);
    }

    public int asInputType() throws NumberFormatException {
        return Integer.parseInt(parameter.substring(0, 8));
    }

    public int asInputSequence() throws NumberFormatException {
        return Integer.parseInt(parameter.substring(8, 16));
    }

}
