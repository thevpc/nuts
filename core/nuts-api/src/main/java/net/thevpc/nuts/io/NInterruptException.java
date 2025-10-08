package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NException;
import net.thevpc.nuts.text.NMsg;

public class NInterruptException extends NException {
    public NInterruptException() {
        super(NMsg.ofPlain("interrupted"));
    }

    public NInterruptException(NMsg message) {
        super(message);
    }

    public NInterruptException(NMsg message, Throwable cause) {
        super(message, cause);
    }

    public NInterruptException(NMsg message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
