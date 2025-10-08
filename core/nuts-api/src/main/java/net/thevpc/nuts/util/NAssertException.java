package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

public class NAssertException extends NIllegalArgumentException {

    public NAssertException(NMsg formattedMessage, Throwable cause) {
        super(formattedMessage, cause);
    }

}
