package org.openhab.binding.sonybravia.simpleip;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class to wrap / construct Simple IP messages
 *
 * @author Daniël van Os
 */
@NonNullByDefault
public class SimpleIpMessage {

    private SimpleIpMessageType messageType;
    private SimpleIpCommand command;
    private SimpleIpParameter parameter;

    private SimpleIpMessage(MessageBuilder messageBuilder) {
        this.messageType = messageBuilder.messageType;
        this.command = messageBuilder.command;
        this.parameter = messageBuilder.parameter;
    }

    /**
     * Accessor for message type
     *
     * @return message type object
     */
    public SimpleIpMessageType getMessageType() {
        return messageType;
    }

    /**
     * Accessor for message command
     *
     * @return command enum
     */
    public SimpleIpCommand getCommand() {
        return command;
    }

    /**
     * Accessor for message parameter
     *
     * @return parameter object
     */
    public SimpleIpParameter getParameter() {
        return parameter;
    }

    /**
     * Mostly for debugging
     */
    @Override
    public String toString() {
        return String.format("Type: {%s} Command: {%s} Parameter: {%s}", messageType.getTypeString(),
                command.getCommandString(), parameter.getParameterString());
    }

    /**
     * Static builder class to help constructing a message
     *
     * By default a Power status request message will be constructed.
     *
     * @author Daniël van Os
     *
     */
    public static class MessageBuilder {
        private SimpleIpCommand command = SimpleIpCommand.POWER_STATUS;
        private SimpleIpMessageType messageType = SimpleIpMessageType.ENQUIRY;
        private SimpleIpParameter parameter = new SimpleIpParameter();

        /**
         * Setter for the message type
         *
         * @param messageType The type of message to build
         * @return updated message builder
         */
        public MessageBuilder messageType(SimpleIpMessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        /**
         * Setter for the message command
         *
         * @param command The command to put into the message
         * @return updated message builder
         */
        public MessageBuilder command(SimpleIpCommand command) {
            this.command = command;
            return this;
        }

        /**
         * Setter for the message parameter
         *
         * @param parameter The parameters for the message
         * @return updated message builder
         */
        public MessageBuilder parameter(SimpleIpParameter parameter) {
            this.parameter = parameter;
            return this;
        }

        /**
         * Builder
         *
         * @return A Simple IP message object with the previously set options.
         */
        public SimpleIpMessage build() {
            return new SimpleIpMessage(this);
        }
    }
}
