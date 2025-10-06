package net.thevpc.nuts.util;

public class NErrorOptionalException extends NException {

    public NErrorOptionalException(NMsg formattedMessage,Throwable cause) {
        super(formattedMessage, cause);
    }

}
