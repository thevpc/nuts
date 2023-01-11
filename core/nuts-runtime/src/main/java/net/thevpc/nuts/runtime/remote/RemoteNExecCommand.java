package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecCommand;

public class RemoteNExecCommand extends AbstractNExecCommand {

    public RemoteNExecCommand(NSession ws) {
        super(ws);
    }

    @Override
    protected RemoteNWorkspace getWorkspace() {
        return (RemoteNWorkspace) super.getWorkspace();
    }

    @Override
    public NExecutableInformation which() {
        NElements e = NElements.of(getSession());
        return getWorkspace().remoteCall(
                getWorkspace().createCall("workspace.which",
                        e.ofObject()
                                .build(), getSession()
                ),
                NExecutableInformation.class
        );
    }

    @Override
    public NExecCommand run() {
        NElements e = NElements.of(getSession());
        try {
            int r = getWorkspace().remoteCall(
                    getWorkspace().createCall("workspace.exec",
                            e.ofObject()
                                    .set("dry", session.isDry())
                                    .set("failFast", failFast)
                                    .build(),
                            getSession()
                    ),
                    Integer.class
            );
        } catch (NExecutionException ex) {
            result = ex;
        } catch (Exception ex) {
            String p = getExtraErrorMessage();
            if (p != null) {
                result = new NExecutionException(session,
                        NMsg.ofC("execution failed with code %d and message : %s", 244, p),
                        ex, 244);
            } else {
                result = new NExecutionException(session, NMsg.ofPlain("remote command failed"), ex, 244);
            }
        }
        executed = true;
        if (result != null && failFast) {
            throw result;
        }
        return this;
    }
}
