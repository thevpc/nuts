package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecCmd;
import net.thevpc.nuts.util.NMsg;

public class RemoteNExecCmd extends AbstractNExecCmd {

    public RemoteNExecCmd(NSession session) {
        super(session);
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
    public NExecCmd run() {
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
            resultException = ex;
        } catch (Exception ex) {
            String p = getExtraErrorMessage();
            if (p != null) {
                resultException = new NExecutionException(session,
                        NMsg.ofC("execution failed with code %d and message : %s", NExecutionException.ERROR_255, p),
                        ex, NExecutionException.ERROR_255);
            } else {
                resultException = new NExecutionException(session, NMsg.ofPlain("remote command failed"), ex, NExecutionException.ERROR_255);
            }
        }
        executed = true;
        if (resultException != null && failFast) {
            throw resultException;
        }
        return this;
    }
}
