package org.openhab.binding.sonybravia.connection;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class SimpleIpException extends Exception {
    private static final long serialVersionUID = -2903625738969270568L;

    public SimpleIpException(String message) {
        super(message);
    }
}
