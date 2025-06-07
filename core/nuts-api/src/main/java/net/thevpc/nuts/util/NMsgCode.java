package net.thevpc.nuts.util;

import net.thevpc.nuts.NDTO;

public class NMsgCode implements NDTO, NImmutable {
    private String code;
    private String[] params;
    private String message;

    public static NMsgCode ofCode(String code, String... params) {
        return new NMsgCode(code, code, params);
    }

    public static NMsgCode ofMessage(String message, String code, String... params) {
        return new NMsgCode(message, code, params);
    }

    public NMsgCode(String message, String code, String[] params) {
        this.message = message;
        this.code = code;
        this.params = params;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public String[] getParams() {
        return params;
    }
}
