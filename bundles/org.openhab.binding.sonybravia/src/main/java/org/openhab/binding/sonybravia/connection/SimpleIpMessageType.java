package org.openhab.binding.sonybravia.connection;

public enum SimpleIpMessageType {
    CONTROL("C"),
    ENQUIRY("E"),
    ANSWER("A"),
    NOTIFY("N");

    private String typeChar = "";

    public String getTypeChar() {
        return typeChar;
    }

    public static SimpleIpMessageType getIpMessageType(byte[] type) throws SimpleIpException {
        String typeString = new String(type);
        for (SimpleIpMessageType candidate : values()) {
            if (candidate.getTypeChar().equals(typeString)) {
                return candidate;
            }
        }
        throw new SimpleIpException(String.format("Unknown Simple IP message type %s", typeString));
    }

    private SimpleIpMessageType(String typeChar) {
        this.typeChar = typeChar;
    }
}
