package net.thevpc.nuts.util;

public class NAssertException extends NIllegalArgumentException {

    public NAssertException(NMsg formattedMessage, Throwable cause) {
        super(formattedMessage, cause);
    }

}
