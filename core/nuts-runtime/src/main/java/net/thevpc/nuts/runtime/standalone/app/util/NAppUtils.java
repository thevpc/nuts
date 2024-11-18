package net.thevpc.nuts.runtime.standalone.app.util;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NSession;

public class NAppUtils {
    public static boolean processHelpOptions(String[] args, NSession session) {
        if (isIncludesHelpOption(args)) {
            NCmdLine cmdLine = NCmdLine.of(args);
            while (cmdLine.hasNext()) {
                NArg a = cmdLine.peek().get();
                if (a.isOption()) {
                    switch(a.key()) {
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
