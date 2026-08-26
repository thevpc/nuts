package net.thevpc.nuts.boot;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private NBootCompleteRequest complete;

    public static NBootArguments of(String... args) {
        return ofOptionArgs(args);
    }

    public static NBootArguments ofFullArgs(String... args) {
        if (args.length > 0) {
            NBootCompleteRequest i = NBootCompleteRequest.parseOrNull(args[0]);
            if (i != null) {
                List<String> newArgs = new ArrayList<>(Arrays.asList(args));
                newArgs.remove(0);
                return new NBootArguments().optionArgs(newArgs.toArray(new String[0])).complete(i);
            }
        }
        return ofOptionArgs(args);
    }

    public static NBootArguments ofOptionArgs(String... args) {
        return new NBootArguments().optionArgs(args);
    }

    public static NBootArguments ofAppArgs(String... args) {
        return new NBootArguments().appArgs(args);
    }

    public String[] optionArgs() {
        return optionArgs;
    }

    public NBootCompleteRequest complete() {
        return complete;
    }

    public NBootArguments complete(NBootCompleteRequest complete) {
        this.complete = complete;
        return this;
    }

    public NBootArguments optionArgs(String[] args) {
        this.optionArgs = args;
        return this;
    }

    public String[] appArgs() {
        return appArgs;
    }

    public NBootArguments appArgs(String[] appArgs) {
        this.appArgs = appArgs;
        return this;
    }

    public Instant startTime() {
        return startTime;
    }

    public NBootArguments startTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    public boolean isSkipInherited() {
        return skipInherited;
    }

    public NBootArguments skipInherited(boolean skipInherited) {
        this.skipInherited = skipInherited;
        return this;
    }

    public InputStream in() {
        return in;
    }

    public NBootArguments in(InputStream in) {
        this.in = in;
        return this;
    }

    public PrintStream out() {
        return out;
    }

    public NBootArguments out(PrintStream out) {
        this.out = out;
        return this;
    }

    public PrintStream err() {
        return err;
    }

    public NBootArguments term(NWorkspaceTerminalOptions term) {
        if (term != null) {
            this.in = term.getIn();
            this.out = term.getOut();
            this.err = term.getErr();
        }
        return this;
    }

    public NBootArguments err(PrintStream err) {
        this.err = err;
        return this;
    }

    public Set<String> ooFlags() {
        return ioFlags;
    }

    public NBootArguments ioFlags(Set<String> ioFlags) {
        this.ioFlags = ioFlags;
        return this;
    }
}
