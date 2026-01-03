package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

public class NNonCopiableException extends NIllegalArgumentException {

    public NNonCopiableException(NMsg formattedMessage, Throwable cause) {
        super(formattedMessage, cause);
    }
    public NNonCopiableException(NMsg formattedMessage) {
        super(formattedMessage);
    }

}
