package net.thevpc.nuts.boot;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.Set;

public class NBootArguments {
    private String[] args;
    private String[] appArgs;
    private Instant startTime;
    private boolean inherited;
    private InputStream in;
    private PrintStream out;
    private PrintStream err;
    private Set<String> ioFlags;

    public String[] getArgs() {
        return args;
    }

    public NBootArguments setArgs(String[] args) {
        this.args = args;
        return this;
    }

    public String[] getAppArgs() {
        return appArgs;
    }

    public void setAppArgs(String[] appArgs) {
        this.appArgs = appArgs;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public NBootArguments setStartTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    public boolean isInherited() {
        return inherited;
    }

    public NBootArguments setInherited(boolean inherited) {
        this.inherited = inherited;
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
