package org.openhab.binding.sonybravia.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SimpleIpMessage {

    private SimpleIpMessageType messageType;
    private SimpleIpCommand command;
    private String value = "";

    private SimpleIpMessage(MessageBuilder messageBuilder) {
        this.messageType = messageBuilder.messageType;
        this.command = messageBuilder.command;
        this.value = messageBuilder.value;
    }

    public SimpleIpMessageType getMessageType() {
        return messageType;
    }

    public SimpleIpCommand getCommand() {
        return command;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("Type: {%s} Command: {%s} Value: {%s}", messageType.getTypeChar(), command.getCommand(),
                value);
    }

    public static class MessageBuilder {
        private SimpleIpMessageType messageType = SimpleIpMessageType.ENQUIRY;
        private SimpleIpCommand command = SimpleIpCommand.POWER_STATUS_GET;
        private String value = "";

        public MessageBuilder messageType(SimpleIpMessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        public MessageBuilder command(SimpleIpCommand command) {
            this.command = command;
            return this;
        }

        public MessageBuilder value(String value) {
            this.value = value;
            return this;
        }

        public MessageBuilder value(String format, Object... args) {
            this.value = String.format(format, args);
            return this;
        }

        public SimpleIpMessage build() {
            return new SimpleIpMessage(this);
        }
    }
}
