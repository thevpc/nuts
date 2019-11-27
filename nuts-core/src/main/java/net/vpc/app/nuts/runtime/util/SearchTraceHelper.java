package net.vpc.app.nuts.runtime.util;

import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.console.CProgressBar;
import net.vpc.app.nuts.runtime.util.fprint.FPrintCommands;

import java.util.Stack;

public class SearchTraceHelper {

    public static CProgressBar resolveCProgressBar(NutsSession s) {
        Stack<CProgressBar> tt = resolve(s);
        if (tt.isEmpty()) {
            CProgressBar p = new CProgressBar(s.getWorkspace()).setSuffixMoveLineStart();
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
        resolve(s).push(new CProgressBar(s.getWorkspace()));
    }

    public static void progress(int percent, String msg, NutsSession s) {
        if (isProgress(s)) {
//        TT t = resolve(s).peek();
            CProgressBar bar = resolveCProgressBar(s);
            String p = bar.progress(percent, msg);
            if(p==null|| p.isEmpty()){
                return;
            }
            s.out().print(p);
            s.out().flush();
        }
    }

    public static void progressIndeterminate(String msg, NutsSession s) {
        progress(-1, msg, s);
    }

    public static boolean isProgress(NutsSession s) {
        if (!s.isPlainOut()) {
            return false;
        }
        Object p = s.getProperty("traceMonitor");
        if (p instanceof Boolean && ((Boolean) p).booleanValue()) {
            return true;
        }
        if (p instanceof String && Boolean.parseBoolean(p.toString())) {
            return true;
        }
        return false;
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
