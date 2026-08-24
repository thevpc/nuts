package net.thevpc.nuts.text;

import net.thevpc.nuts.util.NException;

/**
 * NMsgCodeException class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NMsgCodeException extends NException implements NMsgCodeAware {
    private NMsgCode code;

    /**
     * N msg code exception.
     *
     * @param message message
     * @param code code
     * @return n msg code exception result
     */
    public NMsgCodeException(NMsg message, NMsgCode code) {
      /**
       * Super.
       *
       * @param message message
       */
        super(message);
        this.code = NMsgCode.ofMessage(
                message.toString(),
                code == null ? null : code.code(), code == null ? new String[0] : code.params()
        );
    }

    /**
     * N msg code exception.
     *
     * @param message message
     * @param code code
     * @param cause cause
     * @return n msg code exception result
     */
    public NMsgCodeException(NMsg message, NMsgCode code, Throwable cause) {
      /**
       * Super.
       *
       * @param message message
       * @param cause cause
       */
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
