package net.thevpc.nuts.runtime.standalone.app.cmdline;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.cmdline.NCommandLineFormat;
import net.thevpc.nuts.cmdline.NCommandLineFormatStrategy;
import net.thevpc.nuts.cmdline.NCommandLines;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.shell.NShellHelper;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTexts;

public class DefaultNCommandLineFormat extends DefaultFormatBase<NCommandLineFormat> implements NCommandLineFormat {

    private NCommandLine value;
    private NShellFamily formatFamily = NShellFamily.getCurrent();
    private NCommandLineFormatStrategy formatStrategy = NCommandLineFormatStrategy.DEFAULT;

    public DefaultNCommandLineFormat(NSession session) {
        super(session, "commandLine");
    }

    public NCommandLineFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public NCommandLineFormat setValue(NCommandLine value) {
        this.value = value;
        return this;
    }

    @Override
    public NCommandLineFormat setValue(String[] args) {
        checkSession();
        return setValue(args == null ? null : NCommandLine.of(args));
    }

    @Override
    public NCommandLineFormat setValue(String args) {
        return setValue(args == null ? null : NCommandLines.of(getSession()).parseCommandline(args));
    }

    public NShellFamily getShellFamily() {
        return formatFamily;
    }

    public NCommandLineFormat setShellFamily(NShellFamily family) {
        this.formatFamily = family == null ? NShellFamily.getCurrent() : family;
        return this;
    }

    public NCommandLineFormatStrategy getFormatStrategy() {
        return formatStrategy;
    }

    public void setFormatStrategy(NCommandLineFormatStrategy formatStrategy) {
        this.formatStrategy = formatStrategy == null ? NCommandLineFormatStrategy.DEFAULT : formatStrategy;
    }

    @Override
    public NCommandLine getValue() {
        return value;
    }

    @Override
    public boolean configureFirst(NCommandLine commandLine) {
        return false;
    }

    @Override
    public void print(NPrintStream out) {
        checkSession();
        if (value != null) {
            String cmd =
                    NShellHelper.of(getShellFamily())
                            .escapeArguments(value.toStringArray(),
                                    new NCommandLineShellOptions()
                                            .setSession(getSession())
                                            .setFormatStrategy(getFormatStrategy())
                                            .setExpectEnv(true)
                            );
            if (isNtf()) {
                out.println(
                        NTexts.of(getSession()).ofCode("system", cmd)
                );
            } else {
                out.print(cmd);
            }
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
