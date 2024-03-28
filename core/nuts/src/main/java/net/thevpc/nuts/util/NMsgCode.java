package net.thevpc.nuts.util;

public class NMsgCode {
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
