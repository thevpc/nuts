package net.thevpc.nuts.runtime.standalone.boot;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class StdFd {
    public final InputStream in;
    public final PrintStream out;
    public final PrintStream err;
    public final boolean ansi;
    public final Set<String> flags;

    public StdFd(InputStream in, PrintStream out, PrintStream err, boolean ansi, String... flags) {
        this.in = in;
        this.out = out;
        this.err = err;
        this.ansi = ansi;
        this.flags = new LinkedHashSet<>(Arrays.asList(flags));
    }
}
