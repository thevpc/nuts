package net.thevpc.nuts.runtime.core.app;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsShellFamily;

public interface NutsCommandLineShellSupport {
    NutsCommandLineShellSupport BASH = new DefaultCommandLineBash();
    NutsCommandLineShellSupport WINDOWS_CMD = new DefaultCommandLineWindowsCmd();

    static NutsCommandLineShellSupport of(NutsShellFamily f, NutsSession s) {
        if (f == null) {
            f = NutsShellFamily.getCurrent();
        }
        switch (f){
            case BASH:
            case SH:
            case CSH:
            case FISH:
            case KSH:
            case ZSH:{
                return BASH;
            }
            case WIN_CMD:
            case WIN_POWER_SHELL:{
                return WINDOWS_CMD;
            }
        }
        return BASH;
    }

    String escapeArgument(String arg, NutsCommandLineShellOptions options) ;

    default String escapeArguments(String[] args, NutsCommandLineShellOptions options) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(escapeArgument(arg,options));
        }
        return sb.toString();
    }
}
