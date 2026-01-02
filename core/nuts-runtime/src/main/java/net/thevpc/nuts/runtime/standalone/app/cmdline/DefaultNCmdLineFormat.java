package net.thevpc.nuts.runtime.standalone.app.cmdline;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.text.NCmdLineFormat;
import net.thevpc.nuts.cmdline.NCmdLineFormatStrategy;
import net.thevpc.nuts.cmdline.NCmdLines;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.xtra.shell.NShellHelper;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.text.NText;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNCmdLineFormat extends DefaultFormatBase<NCmdLineFormat> implements NCmdLineFormat {

    private NShellFamily formatFamily = NShellFamily.getCurrent();
    private NCmdLineFormatStrategy formatStrategy = NCmdLineFormatStrategy.DEFAULT;

    public DefaultNCmdLineFormat(NWorkspace workspace) {
        super("commandLine");
    }

    public NCmdLineFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
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
    public boolean configureFirst(NCmdLine cmdLine) {
        return false;
    }

    @Override
    public void print(Object aValue, NPrintStream out) {
        if (aValue != null) {
            String cmd =
                    NShellHelper.of(getShellFamily())
                            .escapeArguments(((NCmdLine)aValue).toStringArray(),
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

}
