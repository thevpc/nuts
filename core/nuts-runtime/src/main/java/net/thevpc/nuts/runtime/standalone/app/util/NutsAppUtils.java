package net.thevpc.nuts.runtime.standalone.app.util;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsSession;

public class NutsAppUtils {
    public static boolean processHelpOptions(String[] args, NutsSession session) {
        if (isIncludesHelpOption(args)) {
            NutsCommandLine cmdLine = NutsCommandLine.of(args, session);
            while (cmdLine.hasNext()) {
                NutsArgument a = cmdLine.peek();
                if (a.isOption()) {
                    switch (a.getKey().getString()) {
                        case "--help": {
                            cmdLine.skip();
                            break;
                        }
                        default: {
                            session.configureLast(cmdLine);
                        }
                    }
                } else {
                    cmdLine.skip();
                    cmdLine.skipAll();
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isIncludesHelpOption(String[] cmd) {
        if (cmd != null) {
            for (String c : cmd) {
                if (!c.startsWith("-")) {
                    break;
                }
                if ("--help".equals(c)) {
                    return true;
                }
            }
        }
        return false;
    }
}
