package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.io.progress.CProgressBar;


public class CoreTerminalUtils {
    public static CProgressBar resolveProgressBar(NutsSession s) {
        CProgressBar p = (CProgressBar) s.getProperties().get(CProgressBar.class.getName());
        if (p == null) {
            p = createProgressBar(s);
            s.getProperties().put(CProgressBar.class.getName(), p);
        }
        return p;

    }

    public static CProgressBar createProgressBar(NutsSession s) {
        return new CProgressBar(s).setSuffixMoveLineStart(true).setPrefixMoveLineStart(true);
    }
}
