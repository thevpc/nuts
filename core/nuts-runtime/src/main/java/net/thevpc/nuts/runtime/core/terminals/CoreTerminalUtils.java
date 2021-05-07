package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.console.CProgressBar;


public class CoreTerminalUtils {
    public static CProgressBar createProgressBar(NutsSession s){
        return new CProgressBar(s).setSuffixMoveLineStart(true).setPrefixMoveLineStart(true);
    }
}
