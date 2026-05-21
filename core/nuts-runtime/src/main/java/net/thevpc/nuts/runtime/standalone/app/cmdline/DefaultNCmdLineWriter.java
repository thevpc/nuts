package net.thevpc.nuts.runtime.standalone.app.cmdline;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.format.DefaultObjectWriterBase;
import net.thevpc.nuts.text.NCmdLineWriter;
import net.thevpc.nuts.cmdline.NCmdLineFormatStrategy;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.xtra.shell.NShellHelper;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.text.NText;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNCmdLineWriter extends DefaultObjectWriterBase<NCmdLineWriter> implements NCmdLineWriter {

    private NShellFamily formatFamily = NShellFamily.current();
    private NCmdLineFormatStrategy formatStrategy = NCmdLineFormatStrategy.DEFAULT;

    public DefaultNCmdLineWriter() {
        super("commandLine");
    }

    public NCmdLineWriter ntf(boolean ntf) {
        super.ntf(ntf);
        return this;
    }

    public NShellFamily shellFamily() {
        return formatFamily;
    }

    public NCmdLineWriter shellFamily(NShellFamily family) {
        this.formatFamily = family == null ? NShellFamily.current() : family;
        return this;
    }

    public NCmdLineFormatStrategy formatStrategy() {
        return formatStrategy;
    }

    public void formatStrategy(NCmdLineFormatStrategy formatStrategy) {
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
                    NShellHelper.of(shellFamily())
                            .escapeArguments(((NCmdLine)aValue).toStringArray(),
                                    new NCmdLineShellOptions()
                                            .setFormatStrategy(formatStrategy())
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
