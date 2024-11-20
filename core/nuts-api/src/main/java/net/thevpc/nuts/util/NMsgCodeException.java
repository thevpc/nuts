package net.thevpc.nuts.util;

import net.thevpc.nuts.NException;

public class NMsgCodeException extends NException implements NMsgCodeAware {
    private NMsgCode code;

    public NMsgCodeException(NMsgCode code, NMsg message) {
        super(message);
        this.code = code;
    }

    public NMsgCodeException(NMsgCode code, NMsg message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    @Override
    public NMsgCode getNMsgCode() {
        return code;
    }
}
