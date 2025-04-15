package net.thevpc.nuts.runtime.standalone.app.cmdline;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NCmdLineFormat;
import net.thevpc.nuts.cmdline.NCmdLineFormatStrategy;
import net.thevpc.nuts.cmdline.NCmdLines;
import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.xtra.shell.NShellHelper;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;

public class DefaultNCmdLineFormat extends DefaultFormatBase<NCmdLineFormat> implements NCmdLineFormat {

    private NCmdLine value;
    private NShellFamily formatFamily = NShellFamily.getCurrent();
    private NCmdLineFormatStrategy formatStrategy = NCmdLineFormatStrategy.DEFAULT;

    public DefaultNCmdLineFormat(NWorkspace workspace) {
        super("commandLine");
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
        return setValue(args == null ? null : NCmdLine.of(args));
    }

    @Override
    public NCmdLineFormat setValue(String args) {
        return setValue(args == null ? null : NCmdLines.of().parseCmdLine(args));
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
    public boolean configureFirst(NCmdLine cmdLine) {
        return false;
    }

    @Override
    public void print(NPrintStream out) {
        if (value != null) {
            String cmd =
                    NShellHelper.of(getShellFamily())
                            .escapeArguments(value.toStringArray(),
                                    new NCmdLineShellOptions()
                                            .setFormatStrategy(getFormatStrategy())
                                            .setExpectEnv(true)
                            );
            if (isNtf()) {
                out.print(
                        NText.ofCode("system", cmd)
                );
            } else {
                out.print(cmd);
            }
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
