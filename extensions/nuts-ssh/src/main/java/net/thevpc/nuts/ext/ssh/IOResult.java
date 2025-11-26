package net.thevpc.nuts.ext.ssh;

public class IOResult {
    private int code;
    private byte[] out;
    private byte[] err;

    public IOResult(int code, byte[] out, byte[] err) {
        this.code = code;
        this.out = out;
        this.err = err;
    }

    public int code() {
        return code;
    }

    public String outString() {
        return out == null ? "" : new String(out);
    }

    public String errString() {
        return err == null ? "" : new String(err);
    }

    public byte[] out() {
        return out;
    }

    public byte[] err() {
        return err;
    }
}
