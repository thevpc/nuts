package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.shell;

import net.thevpc.nuts.NShellFamily;

public class BashShellWriter extends AbstractPosixShellWriter {
    public BashShellWriter() {
        super(NShellFamily.BASH);
    }

}
