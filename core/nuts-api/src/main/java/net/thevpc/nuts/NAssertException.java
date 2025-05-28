package net.thevpc.nuts;

import net.thevpc.nuts.util.NMsg;

public class NAssertException extends NIllegalArgumentException {

    public NAssertException(NMsg formattedMessage, Throwable cause) {
        super(formattedMessage, cause);
    }

}
