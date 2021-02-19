package org.openhab.binding.sonybravia.simpleip;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This enum lists the supported message types.
 *
 * Sources:
 * - More official: https://pro-bravia.sony.net/develop/integrate/ssip/command-definitions/index.html
 * - More complete:
 * https://shop.kindermann.de/erp/KCO/avs/3/3005/3005000168/01_Anleitungen+Doku/Steuerungsprotokoll_1.pdf
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public enum SimpleIpMessageType {
    CONTROL("C"),
    ENQUIRY("E"),
    ANSWER("A"),
    NOTIFY("N");

    private String typeString = "";

    public String getTypeString() {
        return typeString;
    }

    public static SimpleIpMessageType getIpMessageType(byte[] type) throws SimpleIpException {
        String typeString = new String(type);
        for (SimpleIpMessageType candidate : values()) {
            if (candidate.getTypeString().equals(typeString)) {
                return candidate;
            }
        }
        throw new SimpleIpException(String.format("Unknown Simple IP message type %s", typeString));
    }

    private SimpleIpMessageType(String typeString) {
        this.typeString = typeString;
    }
}
