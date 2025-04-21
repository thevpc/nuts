package net.thevpc.nuts.util;

import net.thevpc.nuts.NException;

public class NMsgCodeException extends NException implements NMsgCodeAware {
    private NMsgCode code;

    public NMsgCodeException(NMsg message, NMsgCode code) {
        super(message);
        this.code = NMsgCode.ofMessage(
                message.toString(),
                code == null ? null : code.getCode(), code == null ? new String[0] : code.getParams()
        );
    }

    public NMsgCodeException(NMsg message, NMsgCode code, Throwable cause) {
        super(message, cause);
        this.code = NMsgCode.ofMessage(
                message.toString(),
                code == null ? null : code.getCode(), code == null ? new String[0] : code.getParams()
        );
    }

    @Override
    public NMsgCode getMsgCode() {
        return code;
    }
}
