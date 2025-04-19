package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.shell;

import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.util.NOptional;

public interface NShellWriter {
    static NOptional<NShellWriter> of(NShellFamily family) {
        if (family == null) {
            family = NShellFamily.getCurrent();
        }
        switch (family) {
            case BASH: {
                return NOptional.of(new BashShellWriter());
            }
            case ZSH: {
                return NOptional.of(new ZshShellWriter());
            }
            case WIN_CMD: {
                return NOptional.of(new WinCmdShellWriter());
            }
        }
        return NOptional.ofNamedEmpty(family.id() + " shell writer");
    }

    boolean isCommentsMode();

    NShellWriter setCommentsMode(boolean commentsMode);

    NShellWriter printlnSetVarScriptPath(String varName);

    NShellWriter printlnSetVarFolderPath(String varName, String fromPathVarName);

    NShellWriter printlnComment(String comment);

    NShellWriter printlnSetVar(String varName, String varExpr);

    NShellWriter printlnSetAppendVar(String varName, String varExpr);

    NShellWriter println();

    NShellWriter echoOff();

    NShellWriter echoOn();

    NShellWriter printlnCommand(String any);

    String build();
}
