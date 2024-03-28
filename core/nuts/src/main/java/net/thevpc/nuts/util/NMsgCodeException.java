package net.thevpc.nuts.util;

import net.thevpc.nuts.NException;
import net.thevpc.nuts.NSession;

public class NMsgCodeException extends NException implements NMsgCodeAware {
    private NMsgCode code;

    public NMsgCodeException(NSession session, NMsgCode code, NMsg message) {
        super(session, message);
        this.code = code;
    }

    public NMsgCodeException(NSession session, NMsgCode code, NMsg message, Throwable cause) {
        super(session, message, cause);
        this.code = code;
    }

    @Override
    public NMsgCode getNMsgCode() {
        return code;
    }
}
