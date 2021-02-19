package org.openhab.binding.sonybravia.simpleip;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception class for exceptions from the Simple IP client
 * 
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class SimpleIpException extends Exception {
    private static final long serialVersionUID = -2903625738969270568L;

    public SimpleIpException(String message) {
        super(message);
    }
}
