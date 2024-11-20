package net.thevpc.nuts.runtime.standalone.app.cmdline;

import net.thevpc.nuts.cmdline.NCmdLineFormatStrategy;
import net.thevpc.nuts.NSession;

public class NCmdLineShellOptions implements Cloneable{
    private NCmdLineFormatStrategy formatStrategy;
    private boolean expectEnv;
    private boolean expectOption;

    public boolean isExpectOption() {
        return expectOption;
    }

    public NCmdLineShellOptions setExpectOption(boolean expectOption) {
        this.expectOption = expectOption;
        return this;
    }

    public NCmdLineFormatStrategy getFormatStrategy() {
        return formatStrategy;
    }

    public NCmdLineShellOptions setFormatStrategy(NCmdLineFormatStrategy formatStrategy) {
        this.formatStrategy = formatStrategy;
        return this;
    }

    public boolean isExpectEnv() {
        return expectEnv;
    }

    public NCmdLineShellOptions setExpectEnv(boolean expectEnv) {
        this.expectEnv = expectEnv;
        return this;
    }


    public NCmdLineShellOptions copy(){
        try {
            return (NCmdLineShellOptions) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("unexpected clone unsupported");
        }
    }
}
