package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.shell;

import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

public class WinCmdShellWriter extends AbstractShellWriter {
    public WinCmdShellWriter() {
        super(NShellFamily.WIN_CMD);
    }

    @Override
    protected String lineCommentImpl(String anyString) {
        return "REM " + anyString;
    }

    @Override
    public NShellWriter echoOff() {
        printlnCommandImpl("@echo off");
        return this;
    }

    @Override
    public NShellWriter echoOn() {
        printlnCommandImpl("@echo on");
        return this;
    }

    @Override
    public NShellWriter printlnSetVarScriptPath(String varName) {
        printlnCommandImpl("SET " + varName + "=%~dp0");
        return this;
    }

    @Override
    public NShellWriter printlnSetVarFolderPath(String varName, String fromPathVarName) {
        printlnCommandImpl("SET " + varName + "=%" + fromPathVarName + ":~0,-1%");
        return this;
    }

    @Override
    public NShellWriter printlnSetVar(String varName, String varExpr) {
        printlnCommandImpl("SET " + varName + "=" + replaceDollarVar(varExpr.replace("/", "\\")));
        return this;
    }

    @Override
    public String varValue(String varName) {
        if (NStringUtils.isEmpty(varName)) {
            return "${NULL}";
        }
        switch (varName) {
            case "*": {
                return "%*";
            }
            default: {
                return "%" + varName + "%";
            }
        }
    }

}
