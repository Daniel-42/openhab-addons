package org.openhab.binding.sonybravia.simpleip;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This enum lists the supported commands.
 *
 * Sources:
 * - More official: https://pro-bravia.sony.net/develop/integrate/ssip/command-definitions/index.html
 * - More complete:
 * https://shop.kindermann.de/erp/KCO/avs/3/3005/3005000168/01_Anleitungen+Doku/Steuerungsprotokoll_1.pdf
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public enum SimpleIpCommand {
    // IRCC_CODE_SET("IRCC"), // not supporting the infrared commands yet
    POWER_STATUS("POWR"),
    TOGGLE_POWER_STATUS("TPOW"),
    AUDIO_VOLUME("VOLU"),
    AUDIO_MUTE("AMUT"),
    PICTURE_MUTE("PMUT"),
    TOGGLE_PICTURE_MUTE("TPMU"),
    CHANNEL("CHNN"),
    INPUT_SOURCE("ISRC"),
    INPUT("INPT"),
    PIP("PIPI"),
    TOGGLE_PIP("TPIP"),
    TOGGLE_PIP_POSITION("TPPP"),
    TRIPLET_CHANNEL("TCHN");

    private String command;

    /**
     * @return the simple ip command string
     */
    public String getCommandString() {
        return command;
    }

    /**
     * Find the enum by the bytes from the network
     *
     * @param command bytes read from the packet
     * @return enum for the command
     * @throws SimpleIpException Thrown if the bytes cannot be converted into a command
     */
    public static SimpleIpCommand getIpCommand(byte[] command) throws SimpleIpException {
        String commandString = new String(command);
        for (SimpleIpCommand candidate : values()) {
            if (candidate.getCommandString().equals(commandString)) {
                return candidate;
            }
        }
        throw new SimpleIpException(String.format("Unknown Simple IP command %s", commandString));
    }

    /**
     * Constructor
     *
     * @param command string representation of the command
     */
    private SimpleIpCommand(String command) {
        this.command = command;
    }
}
