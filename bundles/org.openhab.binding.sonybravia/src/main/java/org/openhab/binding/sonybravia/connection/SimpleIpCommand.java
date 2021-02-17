package org.openhab.binding.sonybravia.connection;

public enum SimpleIpCommand {
    // Based upon: https://pro-bravia.sony.net/develop/integrate/ssip/command-definitions/index.html

    // IRCC_CODE_SET("IRCC"),

    POWER_STATUS_GET(SimpleIpMessageType.ENQUIRY, "POWR", Constants.NO_PARAMETERS),
    POWER_STATUS_SET(SimpleIpMessageType.CONTROL, "POWR", "%016d"),
    POWER_STATUS_ANSWER(SimpleIpMessageType.ANSWER, "POWR", "%016d"),
    POWER_STATUS_TOGGLE(SimpleIpMessageType.CONTROL, "TPOW", Constants.NO_PARAMETERS),
    FIRE_POWER_CHANGE(SimpleIpMessageType.NOTIFY, "POWR", "%016d"),

    AUDIO_VOLUME_GET(SimpleIpMessageType.ENQUIRY, "VOLU", Constants.NO_PARAMETERS),
    AUDIO_VOLUME_SET(SimpleIpMessageType.CONTROL, "VOLU", "%016d"),
    AUDIO_VOLUME_ANSWER(SimpleIpMessageType.ANSWER, "VOLU", "%016d"),
    FIRE_VOLUME_CHANGE(SimpleIpMessageType.NOTIFY, "VOLU", "%016d"),

    AUDIO_MUTE_GET(SimpleIpMessageType.ENQUIRY, "AMUT", Constants.NO_PARAMETERS),
    AUDIO_MUTE_SET(SimpleIpMessageType.CONTROL, "AMUT", "%016d"),
    AUDIO_MUTE_ANSWER(SimpleIpMessageType.ANSWER, "AMUT", "%016d"),
    FIRE_MUTE_CHANGE(SimpleIpMessageType.NOTIFY, "AMUT", "%016d"),

    INPUT_GET(SimpleIpMessageType.ENQUIRY, "INPT", Constants.NO_PARAMETERS),
    INPUT_SET(SimpleIpMessageType.CONTROL, "INPT", "%08d%08d"),
    INPUT_ANSWER(SimpleIpMessageType.ANSWER, "INPT", "%08d%08d"),
    FIRE_INPUT_CHANGE(SimpleIpMessageType.NOTIFY, "INPT", "%08d%08d");

    private SimpleIpMessageType type;
    private String command;
    private String value;

    /**
     * @return the simple ip type
     */
    public SimpleIpMessageType getType() {
        return type;
    }

    /**
     * @return the simple ip command string
     */
    public String getCommand() {
        return command;
    }

    /**
     * @return the simple ip value string
     */
    public String getValue() {
        return value;
    }

    public static SimpleIpCommand getIpCommand(SimpleIpMessageType type, byte[] command) throws SimpleIpException {
        String commandString = new String(command);
        for (SimpleIpCommand candidate : values()) {
            if (candidate.getType() == type && candidate.getCommand().equals(commandString)) {
                return candidate;
            }
        }
        throw new SimpleIpException(String.format("Unknown Simple IP command %s:%s", type.getTypeChar(), command));
    }

    private SimpleIpCommand(SimpleIpMessageType type, String command, String value) {
        this.type = type;
        this.command = command;
        this.value = value;
    }

    public static class Constants {
        public static final String NO_PARAMETERS = "################";
        public static final String ERROR_PARAMETER = "FFFFFFFFFFFFFFFF";
        public static final String NOT_FOUND = "NNNNNNNNNNNNNNNN";
        public static final String SUCCESS_PARAMETER = "0000000000000000";
        public static final String INPUT_HDMI = "00000001";
        public static final String INPUT_COMPONENT = "00000004";
        public static final String INPUT_SCREEN_MIRRORING = "00000005";
    }
}
