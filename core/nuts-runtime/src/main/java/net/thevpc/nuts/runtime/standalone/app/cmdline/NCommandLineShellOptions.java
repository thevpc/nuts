package net.thevpc.nuts.runtime.standalone.app.cmdline;

import net.thevpc.nuts.cmdline.NCommandLineFormatStrategy;
import net.thevpc.nuts.NSession;

public class NCommandLineShellOptions implements Cloneable{
    private NCommandLineFormatStrategy formatStrategy;
    private boolean expectEnv;
    private boolean expectOption;
    private NSession session;

    public boolean isExpectOption() {
        return expectOption;
    }

    public NCommandLineShellOptions setExpectOption(boolean expectOption) {
        this.expectOption = expectOption;
        return this;
    }

    public NCommandLineFormatStrategy getFormatStrategy() {
        return formatStrategy;
    }

    public NCommandLineShellOptions setFormatStrategy(NCommandLineFormatStrategy formatStrategy) {
        this.formatStrategy = formatStrategy;
        return this;
    }

    public boolean isExpectEnv() {
        return expectEnv;
    }

    public NCommandLineShellOptions setExpectEnv(boolean expectEnv) {
        this.expectEnv = expectEnv;
        return this;
    }

    public NSession getSession() {
        return session;
    }

    public NCommandLineShellOptions setSession(NSession session) {
        this.session = session;
        return this;
    }
    public NCommandLineShellOptions copy(){
        try {
            return (NCommandLineShellOptions) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("unexpected clone unsupported");
        }
    }
}
