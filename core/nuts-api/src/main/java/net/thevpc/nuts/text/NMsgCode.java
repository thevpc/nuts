package net.thevpc.nuts.text;

import net.thevpc.nuts.util.NDTO;
import net.thevpc.nuts.util.NImmutable;

/**
 * NMsgCode class.
 *
 * @author thevpc
 * @since 0.8.0
 */
@NImmutable
public class NMsgCode implements NDTO{
    private String code;
    private String[] params;
    private String message;

    /**
     * Creates a new instance of of code.
     *
     * @param code code
     * @param params params
     * @return of code result
     */
    public static NMsgCode ofCode(String code, String... params) {
        return new NMsgCode(code, code, params);
    }

    /**
     * Creates a new instance of of message.
     *
     * @param message message
     * @param code code
     * @param params params
     * @return of message result
     */
    public static NMsgCode ofMessage(String message, String code, String... params) {
        return new NMsgCode(message, code, params);
    }

    /**
     * N msg code.
     *
     * @param message message
     * @param code code
     * @param params params
     * @return n msg code result
     */
    public NMsgCode(String message, String code, String[] params) {
        this.message = message;
        this.code = code;
        this.params = params;
    }

    /**
     * Message.
     *
     * @return message result
     */
    public String message() {
        return message;
    }

    /**
     * Code.
     *
     * @return code result
     */
    public String code() {
        return code;
    }

    /**
     * Params.
     *
     * @return params result
     */
    public String[] params() {
        return params;
    }
}
