package net.thevpc.nuts.ext.ssh;

import java.io.InputStream;
import java.io.OutputStream;

public class IOBindings {
    public final OutputStream out;
    public final OutputStream err;
    public final InputStream in;

    public IOBindings(InputStream in, OutputStream out, OutputStream err) {
        this.out = out;
        this.err = err;
        this.in = in;
    }

    public OutputStream out() {
        return out;
    }

    public OutputStream err() {
        return err;
    }

    public InputStream in() {
        return in;
    }
}
