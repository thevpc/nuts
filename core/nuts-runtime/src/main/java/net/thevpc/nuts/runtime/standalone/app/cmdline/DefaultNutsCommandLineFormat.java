package net.thevpc.nuts.runtime.standalone.app.cmdline;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.cmdline.NutsCommandLineFormat;
import net.thevpc.nuts.cmdline.NutsCommandLineFormatStrategy;
import net.thevpc.nuts.cmdline.NutsCommandLines;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.shell.NutsShellHelper;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.text.NutsTexts;

public class DefaultNutsCommandLineFormat extends DefaultFormatBase<NutsCommandLineFormat> implements NutsCommandLineFormat {

    private NutsCommandLine value;
    private NutsShellFamily formatFamily = NutsShellFamily.getCurrent();
    private NutsCommandLineFormatStrategy formatStrategy = NutsCommandLineFormatStrategy.DEFAULT;

    public DefaultNutsCommandLineFormat(NutsSession session) {
        super(session, "commandLine");
    }

    public NutsCommandLineFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public NutsCommandLineFormat setValue(NutsCommandLine value) {
        this.value = value;
        return this;
    }

    @Override
    public NutsCommandLineFormat setValue(String[] args) {
        checkSession();
        return setValue(args == null ? null : NutsCommandLine.of(args));
    }

    @Override
    public NutsCommandLineFormat setValue(String args) {
        return setValue(args == null ? null : NutsCommandLines.of(getSession()).parseCommandline(args));
    }

    public NutsShellFamily getShellFamily() {
        return formatFamily;
    }

    public NutsCommandLineFormat setShellFamily(NutsShellFamily family) {
        this.formatFamily = family == null ? NutsShellFamily.getCurrent() : family;
        return this;
    }

    public NutsCommandLineFormatStrategy getFormatStrategy() {
        return formatStrategy;
    }

    public void setFormatStrategy(NutsCommandLineFormatStrategy formatStrategy) {
        this.formatStrategy = formatStrategy == null ? NutsCommandLineFormatStrategy.DEFAULT : formatStrategy;
    }

    @Override
    public NutsCommandLine getValue() {
        return value;
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return false;
    }

    @Override
    public void print(NutsPrintStream out) {
        checkSession();
        if (value != null) {
            String cmd =
                    NutsShellHelper.of(getShellFamily())
                            .escapeArguments(value.toStringArray(),
                                    new NutsCommandLineShellOptions()
                                            .setSession(getSession())
                                            .setFormatStrategy(getFormatStrategy())
                                            .setExpectEnv(true)
                            );
            if (isNtf()) {
                out.printf(
                        NutsTexts.of(getSession()).ofCode("system", cmd)
                );
            } else {
                out.print(cmd);
            }
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
