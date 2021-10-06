package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

public class MemResult {
    private String out;
    private String err;
    private int exitCode;

    public MemResult(String out, String err, int exitCode) {
        this.out = out;
        this.err = err;
        this.exitCode = exitCode;
    }

    public String out() {
        return out;
    }

    public String err() {
        return err;
    }

    public int exitCode() {
        return exitCode;
    }
}
