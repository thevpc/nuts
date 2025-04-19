package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.shell;

import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.util.NMsg;

public class ZshShellWriter extends AbstractPosixShellWriter {
    public ZshShellWriter() {
        super(NShellFamily.ZSH);
    }

}
