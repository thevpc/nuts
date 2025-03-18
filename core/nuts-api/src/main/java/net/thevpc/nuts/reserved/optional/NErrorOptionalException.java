package net.thevpc.nuts.reserved.optional;

import net.thevpc.nuts.NException;
import net.thevpc.nuts.util.NMsg;

public class NErrorOptionalException extends NException {

    public NErrorOptionalException(NMsg formattedMessage,Throwable cause) {
        super(formattedMessage, cause);
    }

}
