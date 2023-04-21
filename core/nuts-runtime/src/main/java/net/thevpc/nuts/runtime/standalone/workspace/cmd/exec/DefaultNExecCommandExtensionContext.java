package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.NExecCommandExtensionContext;
import net.thevpc.nuts.NExecInput;
import net.thevpc.nuts.NExecOutput;
import net.thevpc.nuts.NSession;

public class DefaultNExecCommandExtensionContext implements NExecCommandExtensionContext {
    private String host;
    private String[] command;
    private NSession session;
    private NExecInput in;
    private NExecOutput out;
    private NExecOutput err;

    public DefaultNExecCommandExtensionContext(String host, String[] command, NSession session,NExecInput in,NExecOutput out,NExecOutput err) {
        this.host = host;
        this.command = command;
        this.session = session;
        this.in = in;
        this.out = out;
        this.err = err;
    }

    public NExecInput getIn() {
        return in;
    }

    public NExecOutput getOut() {
        return out;
    }

    public NExecOutput getErr() {
        return err;
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
