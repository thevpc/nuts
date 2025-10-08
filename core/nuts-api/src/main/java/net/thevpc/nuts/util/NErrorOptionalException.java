package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

public class NErrorOptionalException extends NException {

    public NErrorOptionalException(NMsg formattedMessage, Throwable cause) {
        super(formattedMessage, cause);
    }

}
