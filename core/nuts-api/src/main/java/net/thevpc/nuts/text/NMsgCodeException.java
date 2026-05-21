package net.thevpc.nuts.text;

import net.thevpc.nuts.util.NException;

public class NMsgCodeException extends NException implements NMsgCodeAware {
    private NMsgCode code;

    public NMsgCodeException(NMsg message, NMsgCode code) {
        super(message);
        this.code = NMsgCode.ofMessage(
                message.toString(),
                code == null ? null : code.code(), code == null ? new String[0] : code.params()
        );
    }

    public NMsgCodeException(NMsg message, NMsgCode code, Throwable cause) {
        super(message, cause);
        this.code = NMsgCode.ofMessage(
                message.toString(),
                code == null ? null : code.code(), code == null ? new String[0] : code.params()
        );
    }

    @Override
    public NMsgCode msgCode() {
        return code;
    }
}
