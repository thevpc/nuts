package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.shell;

import net.thevpc.nuts.platform.NShellFamily;

public class ZshShellWriter extends AbstractPosixShellWriter {
    public ZshShellWriter() {
        super(NShellFamily.ZSH);
    }

}
