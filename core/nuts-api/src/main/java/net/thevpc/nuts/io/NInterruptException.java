package net.thevpc.nuts.io;

import net.thevpc.nuts.NException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;

public class NInterruptException extends NException {
    public NInterruptException(NSession session) {
        super(session, NMsg.ofPlain("interrupted"));
    }

    public NInterruptException(NSession session, NMsg message) {
        super(session, message);
    }

    public NInterruptException(NSession session, NMsg message, Throwable cause) {
        super(session, message, cause);
    }

    public NInterruptException(NSession session, NMsg message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(session, message, cause, enableSuppression, writableStackTrace);
    }
}
