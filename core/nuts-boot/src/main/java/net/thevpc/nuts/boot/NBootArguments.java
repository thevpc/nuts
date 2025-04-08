package net.thevpc.nuts.boot;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.Set;

public class NBootArguments {
    private String[] optionArgs;
    private String[] appArgs;
    private Instant startTime = Instant.now();
    private boolean skipInherited;
    private InputStream in;
    private PrintStream out;
    private PrintStream err;
    private Set<String> ioFlags;

    public static NBootArguments of(String... args) {
        return ofOptionArgs(args);
    }

    public static NBootArguments ofOptionArgs(String... args) {
        return new NBootArguments().setOptionArgs(args);
    }

    public static NBootArguments ofAppArgs(String... args) {
        return new NBootArguments().setAppArgs(args);
    }

    public String[] getOptionArgs() {
        return optionArgs;
    }

    public NBootArguments setOptionArgs(String[] args) {
        this.optionArgs = args;
        return this;
    }

    public String[] getAppArgs() {
        return appArgs;
    }

    public NBootArguments setAppArgs(String[] appArgs) {
        this.appArgs = appArgs;
        return this;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public NBootArguments setStartTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    public boolean isSkipInherited() {
        return skipInherited;
    }

    public NBootArguments setSkipInherited(boolean skipInherited) {
        this.skipInherited = skipInherited;
        return this;
    }

    public InputStream getIn() {
        return in;
    }

    public NBootArguments setIn(InputStream in) {
        this.in = in;
        return this;
    }

    public PrintStream getOut() {
        return out;
    }

    public NBootArguments setOut(PrintStream out) {
        this.out = out;
        return this;
    }

    public PrintStream getErr() {
        return err;
    }

    public NBootArguments setTerm(NWorkspaceTerminalOptions term) {
        if (term != null) {
            this.in = term.getIn();
            this.out = term.getOut();
            this.err = term.getErr();
        }
        return this;
    }

    public NBootArguments setErr(PrintStream err) {
        this.err = err;
        return this;
    }

    public Set<String> getIoFlags() {
        return ioFlags;
    }

    public NBootArguments setIoFlags(Set<String> ioFlags) {
        this.ioFlags = ioFlags;
        return this;
    }
}
