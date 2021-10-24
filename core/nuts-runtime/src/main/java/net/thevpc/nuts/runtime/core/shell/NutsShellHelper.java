package net.thevpc.nuts.runtime.core.shell;

import net.thevpc.nuts.NutsOsFamily;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsShellFamily;
import net.thevpc.nuts.runtime.core.app.NutsCommandLineShellOptions;

public interface NutsShellHelper {
    static NutsShellHelper of(NutsShellFamily shellFamily) {
        if (shellFamily == null) {
            shellFamily = NutsShellFamily.UNKNOWN;
        }
        switch (shellFamily) {
            case SH:
                return ShNutsShellHelper.SH;
            case BASH:
                return BashNutsShellHelper.BASH;
            case CSH:
                return CshNutsShellHelper.CSH;
            case ZSH:
                return ZshNutsShellHelper.ZSH;
            case KSH:
                return KshNutsShellHelper.KSH;
            case FISH:
                return FishNutsShellHelper.FISH;
            case WIN_CMD:
                return WinCmdNutsShellHelper.WIN_CMD;
            case WIN_POWER_SHELL:
                return WinPowerShellNutsShellHelper.WIN_POWER_SHELL;
            default: {
                switch (NutsOsFamily.getCurrent()) {
                    case WINDOWS:
                        return of(NutsShellFamily.WIN_CMD);
                    default:
                        return of(NutsShellFamily.SH);
                }
            }
        }
    }

    String newlineString() ;

    String getExportCommand(String[] names);

    String getSetVarCommand(String name, String value);

    String getSetVarStaticCommand(String name, String value);

    String getCallScriptCommand(String path, String... args);

    boolean isComments(String line);

    String toCommentLine(String line);

    ReplaceString getShebanSh();

    String varRef(String v);

    String trimComments(String line);

    String getPathVarSep();

    String getSysRcName();

    String[] parseCommandLineArr(String line, NutsSession session) ;

    String escapeArgument(String arg, NutsCommandLineShellOptions options) ;

    String escapeArguments(String[] args, NutsCommandLineShellOptions options);
}
