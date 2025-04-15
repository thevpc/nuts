package net.thevpc.nuts.runtime.standalone.xtra.shell;

import net.thevpc.nuts.NOsFamily;
import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCmdLineShellOptions;

public interface NShellHelper {
    static NShellHelper of(NShellFamily shellFamily) {
        if (shellFamily == null) {
            shellFamily = NShellFamily.UNKNOWN;
        }
        switch (shellFamily) {
            case SH:
                return ShNShellHelper.SH;
            case BASH:
                return BashNShellHelper.BASH;
            case CSH:
                return CshNShellHelper.CSH;
            case ZSH:
                return ZshNShellHelper.ZSH;
            case KSH:
                return KshNShellHelper.KSH;
            case FISH:
                return FishNShellHelper.FISH;
            case WIN_CMD:
                return WinCmdNShellHelper.WIN_CMD;
            case WIN_POWER_SHELL:
                return WinPowerShellNShellHelper.WIN_POWER_SHELL;
            default: {
                switch (NOsFamily.getCurrent()) {
                    case WINDOWS:
                        return of(NShellFamily.WIN_CMD);
                    default:
                        return of(NShellFamily.SH);
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

    String[] parseCmdLineArr(String line) ;

    String escapeArgument(String arg, NCmdLineShellOptions options) ;

    String escapeArguments(String[] args, NCmdLineShellOptions options);
}
