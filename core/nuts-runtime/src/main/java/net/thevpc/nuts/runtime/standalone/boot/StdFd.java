package net.thevpc.nuts.runtime.standalone.boot;

import java.io.InputStream;
import java.io.PrintStream;

public class StdFd {
    public final InputStream in;
    public final PrintStream out;
    public final PrintStream err;
    public final boolean ansiSupport;

    public StdFd(InputStream in,PrintStream out, PrintStream err, boolean ansiSupport) {
        this.in = in;
        this.out = out;
        this.err = err;
        this.ansiSupport = ansiSupport;
    }
}
