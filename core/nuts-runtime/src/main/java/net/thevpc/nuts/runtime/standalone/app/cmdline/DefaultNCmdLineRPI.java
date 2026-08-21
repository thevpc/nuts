package net.thevpc.nuts.runtime.standalone.app.cmdline;

import net.thevpc.nuts.cmdline.DefaultNCmdLine;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.internal.rpi.NCmdLineRPI;

import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.runtime.standalone.xtra.shell.NShellHelper;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNCmdLineRPI implements NCmdLineRPI {

//    private NShellFamily family = NShellFamily.current();
//    private boolean lenient;

    public DefaultNCmdLineRPI() {
    }

    public NOptional<NCmdLine> parseCmdLine(String line, NShellFamily family, boolean lenient) {
        try {
            return NOptional.of(new DefaultNCmdLine(parseCmdLineArr(line,family,lenient),family));
        } catch (Exception e) {
            return NOptional.ofNamedError(NMsg.ofC("%s", e));
        }
    }

    @Override
    public NCmdLine createCmdLineByArgs(String[] args, NShellFamily family) {
        return new DefaultNCmdLine(args,family);
    }

    private String[] parseCmdLineArr(String line,NShellFamily f,boolean lenient) {
        if (f == null) {
            f = NEnv.of().shellFamily();
        }
        if (f == null) {
            f = NShellFamily.current();
        }
        return NShellHelper.of(f).parseCmdLineArr(line, lenient);
    }

}
