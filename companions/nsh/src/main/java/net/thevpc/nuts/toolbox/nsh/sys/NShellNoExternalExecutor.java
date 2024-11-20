package net.thevpc.nuts.toolbox.nsh.sys;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;
import net.thevpc.nuts.toolbox.nsh.err.NShellException;

public class NShellNoExternalExecutor implements NShellExternalExecutor {
    @Override
    public int execExternalCommand(String[] command, NShellContext context) {
        throw new NShellException(NMsg.ofC("not found %s", command[0]), 101);
    }
}
