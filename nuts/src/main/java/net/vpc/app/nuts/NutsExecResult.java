package net.vpc.app.nuts;

import java.io.Serializable;

public class NutsExecResult implements Serializable{
    private static final long serialVersionUID=1L;
    private final int code;
    private final String output;

    public NutsExecResult(int code, String output) {
        this.code = code;
        this.output = output;
    }

    public int getCode() {
        return code;
    }

    public String getOutput() {
        return output;
    }
}
