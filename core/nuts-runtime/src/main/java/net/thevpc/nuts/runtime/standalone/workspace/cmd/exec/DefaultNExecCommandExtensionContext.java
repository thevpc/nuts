package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.NExecCommandExtensionContext;
import net.thevpc.nuts.NSession;

public class DefaultNExecCommandExtensionContext implements NExecCommandExtensionContext {
    private String host;
    private String[] command;
    private NSession session;

    public DefaultNExecCommandExtensionContext(String host, String[] command, NSession session) {
        this.host = host;
        this.command = command;
        this.session = session;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String[] getCommand() {
        return command;
    }

    @Override
    public NSession getSession() {
        return session;
    }
}
