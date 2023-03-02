package net.thevpc.nuts.runtime.standalone.app.cmdline;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineFormat;
import net.thevpc.nuts.cmdline.NCmdLineFormatStrategy;
import net.thevpc.nuts.cmdline.NCmdLines;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.shell.NShellHelper;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTexts;

public class DefaultNCmdLineFormat extends DefaultFormatBase<NCmdLineFormat> implements NCmdLineFormat {

    private NCmdLine value;
    private NShellFamily formatFamily = NShellFamily.getCurrent();
    private NCmdLineFormatStrategy formatStrategy = NCmdLineFormatStrategy.DEFAULT;

    public DefaultNCmdLineFormat(NSession session) {
        super(session, "commandLine");
    }

    public NCmdLineFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public NCmdLineFormat setValue(NCmdLine value) {
        this.value = value;
        return this;
    }

    @Override
    public NCmdLineFormat setValue(String[] args) {
        checkSession();
        return setValue(args == null ? null : NCmdLine.of(args));
    }

    @Override
    public NCmdLineFormat setValue(String args) {
        return setValue(args == null ? null : NCmdLines.of(getSession()).parseCommandline(args));
    }

    public NShellFamily getShellFamily() {
        return formatFamily;
    }

    public NCmdLineFormat setShellFamily(NShellFamily family) {
        this.formatFamily = family == null ? NShellFamily.getCurrent() : family;
        return this;
    }

    public NCmdLineFormatStrategy getFormatStrategy() {
        return formatStrategy;
    }

    public void setFormatStrategy(NCmdLineFormatStrategy formatStrategy) {
        this.formatStrategy = formatStrategy == null ? NCmdLineFormatStrategy.DEFAULT : formatStrategy;
    }

    @Override
    public NCmdLine getValue() {
        return value;
    }

    @Override
    public boolean configureFirst(NCmdLine commandLine) {
        return false;
    }

    @Override
    public void print(NPrintStream out) {
        checkSession();
        if (value != null) {
            String cmd =
                    NShellHelper.of(getShellFamily())
                            .escapeArguments(value.toStringArray(),
                                    new NCmdLineShellOptions()
                                            .setSession(getSession())
                                            .setFormatStrategy(getFormatStrategy())
                                            .setExpectEnv(true)
                            );
            if (isNtf()) {
                out.print(
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
