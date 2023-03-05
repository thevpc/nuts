package net.thevpc.nuts.toolbox.nsh.sys;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellContext;
import net.thevpc.nuts.toolbox.nsh.err.JShellException;

public class JShellNoExternalExecutor implements JShellExternalExecutor {
    @Override
    public int execExternalCommand(String[] command, JShellContext context) {
        throw new JShellException(context.getSession(), NMsg.ofC("not found %s", command[0]), 101);
    }
}
