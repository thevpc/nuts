package net.thevpc.nuts.runtime.core.app;

import net.thevpc.nuts.NutsCommandLineFormatStrategy;
import net.thevpc.nuts.NutsCommandlineFamily;
import net.thevpc.nuts.NutsSession;

public interface NutsCommandLineBashFamilySupport {
    NutsCommandLineBashFamilySupport BASH = new DefaultCommandLineBash();
    NutsCommandLineBashFamilySupport WINDOWS_CMD = new DefaultCommandLineWindowsCmd();

    static NutsCommandLineBashFamilySupport of(NutsCommandlineFamily f, NutsSession s) {
        if (f == null) {
            f = NutsCommandlineFamily.DEFAULT;
        }
        if (f == NutsCommandlineFamily.DEFAULT) {
            switch (s.getWorkspace().env().getOsFamily()) {
                case WINDOWS: {
                    return WINDOWS_CMD;
                }
                case LINUX:
                case MACOS:
                case UNIX: {
                    return BASH;
                }
            }
        }
        return BASH;
    }

    String escapeArgument(String arg, NutsCommandLineFormatStrategy s, NutsSession session) ;

    default String escapeArguments(String[] args, NutsCommandLineFormatStrategy s,NutsSession session) {
        if(s==null){
            s=NutsCommandLineFormatStrategy.DEFAULT;
        }
        if(s==NutsCommandLineFormatStrategy.DEFAULT){
            s=NutsCommandLineFormatStrategy.SUPPORT_QUOTES;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(escapeArgument(arg,s,session));
        }
        return sb.toString();
    }
}
