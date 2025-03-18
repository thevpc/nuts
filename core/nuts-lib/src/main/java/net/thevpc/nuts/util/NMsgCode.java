package net.thevpc.nuts.util;

import net.thevpc.nuts.NDTO;

public class NMsgCode implements NDTO {
    private String code;
    private String[] params;

    public NMsgCode(String code, String... params) {
        this.code = code;
        this.params = params;
    }

    public String getCode() {
        return code;
    }

    public String[] getParams() {
        return params;
    }
}
