package net.thevpc.nuts.runtime.core.app;

import net.thevpc.nuts.NutsCommandLineFormatStrategy;
import net.thevpc.nuts.NutsSession;

public class NutsCommandLineShellOptions implements Cloneable{
    private NutsCommandLineFormatStrategy formatStrategy;
    private boolean expectEnv;
    private boolean expectOption;
    private NutsSession session;

    public boolean isExpectOption() {
        return expectOption;
    }

    public NutsCommandLineShellOptions setExpectOption(boolean expectOption) {
        this.expectOption = expectOption;
        return this;
    }

    public NutsCommandLineFormatStrategy getFormatStrategy() {
        return formatStrategy;
    }

    public NutsCommandLineShellOptions setFormatStrategy(NutsCommandLineFormatStrategy formatStrategy) {
        this.formatStrategy = formatStrategy;
        return this;
    }

    public boolean isExpectEnv() {
        return expectEnv;
    }

    public NutsCommandLineShellOptions setExpectEnv(boolean expectEnv) {
        this.expectEnv = expectEnv;
        return this;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsCommandLineShellOptions setSession(NutsSession session) {
        this.session = session;
        return this;
    }
    public NutsCommandLineShellOptions copy(){
        try {
            return (NutsCommandLineShellOptions) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("unexpected clone unsupported");
        }
    }
}
