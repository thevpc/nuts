package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.console.CProgressBar;

import java.util.Stack;

public class SearchTraceHelper {
    public static CProgressBar createProgressBar(NutsSession s){
        return new CProgressBar(s).setSuffixMoveLineStart(true).setPrefixMoveLineStart(true);
    }

    public static CProgressBar resolveCProgressBar(NutsSession s) {
        Stack<CProgressBar> tt = resolve(s);
        if (tt.isEmpty()) {
            CProgressBar p = createProgressBar(s);
            tt.push(p);
            return p;
        } else {
            return tt.peek();
        }
    }

    public static Stack<CProgressBar> resolve(NutsSession s) {
        Stack<CProgressBar> tt = (Stack) s.getProperty("SearchTraceHelper");
        if (tt == null) {
            tt = new Stack<>();
            s.setProperty("SearchTraceHelper", tt);
        }
        return tt;
    }

    public static void start(NutsSession s) {
        resolve(s).push(new CProgressBar(s));
    }

    public static void progress(int percent, String msg, NutsSession s) {
        if (isProgress(s)) {
            resolveCProgressBar(s).printProgress(percent, msg,s.out());
        }
    }

    public static void progressIndeterminate(String msg, NutsSession s) {
        progress(-1, msg, s);
    }

    public static boolean isProgress(NutsSession s) {
        if (!s.isPlainOut()) {
            return false;
        }
        if (s.isBot() || NutsWorkspaceUtils.parseProgressOptions(s).contains("false")) {
            return false;
        }
        return true;
    }

    public static void end(NutsSession s) {
        Stack<CProgressBar> tt = resolve(s);
        if (!tt.isEmpty()) {
            resolve(s).pop();
            if (s.isTrace()) {
//                s.out().print("`" + FPrintCommands.MOVE_LINE_START + "`");
//                s.out().print(CoreStringUtils.fillString(' ', 80));
//                s.out().print("`" + FPrintCommands.MOVE_LINE_START + "`");
//                s.out().flush();
            }
        }
    }
}
