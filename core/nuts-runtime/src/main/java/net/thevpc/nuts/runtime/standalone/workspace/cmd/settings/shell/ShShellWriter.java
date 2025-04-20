package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.shell;

import net.thevpc.nuts.NShellFamily;

public class ShShellWriter extends AbstractPosixShellWriter {
    public ShShellWriter() {
        super(NShellFamily.SH);
    }

    @Override
    public NShellWriter printlnSetVarFolderPath(String varName, String fromPathVarName) {
        printlnCommandImpl(varName + "=$(cd \"$(dirname \"$0\")\" && pwd)/$(basename \"$0\")");
        return this;
    }

}
