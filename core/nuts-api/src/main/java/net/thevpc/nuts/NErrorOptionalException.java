package net.thevpc.nuts;

import net.thevpc.nuts.util.NMsg;

public class NErrorOptionalException extends NException {

    public NErrorOptionalException(NMsg formattedMessage,Throwable cause) {
        super(formattedMessage, cause);
    }

}
