package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal;

import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.spi.NComponent;

public interface NInternalCommand extends NComponent {
    String getName();

    int execute(String[] args, NExecCmd execCommand);
}
