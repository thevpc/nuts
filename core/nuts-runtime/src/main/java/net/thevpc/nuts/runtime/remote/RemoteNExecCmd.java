package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecCmd;
import net.thevpc.nuts.util.NMsg;

public class RemoteNExecCmd extends AbstractNExecCmd {

    public RemoteNExecCmd(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    protected RemoteNWorkspace getWorkspace() {
        return (RemoteNWorkspace) super.getWorkspace();
    }

    @Override
    public NExecutableInformation which() {
        NElements e = NElements.of();
        return getWorkspace().remoteCall(
                getWorkspace().createCall("workspace.which",
                        e.ofObjectBuilder()
                                .build()
                ),
                NExecutableInformation.class
        );
    }

    @Override
    public NExecCmd run() {
        NSession session=workspace.currentSession();
        NElements e = NElements.of();
        try {
            int r = getWorkspace().remoteCall(
                    getWorkspace().createCall("workspace.exec",
                            e.ofObjectBuilder()
                                    .set("dry", session.isDry())
                                    .set("failFast", failFast)
                                    .build()
                    ),
                    Integer.class
            );
        } catch (NExecutionException ex) {
            resultException = ex;
        } catch (Exception ex) {
            String p = getExtraErrorMessage();
            if (p != null) {
                resultException = new NExecutionException(
                        NMsg.ofC("execution failed with code %d and message : %s", NExecutionException.ERROR_255, p),
                        ex, NExecutionException.ERROR_255);
            } else {
                resultException = new NExecutionException(NMsg.ofPlain("remote command failed"), ex, NExecutionException.ERROR_255);
            }
        }
        executed = true;
        if (resultException != null && failFast) {
            throw resultException;
        }
        return this;
    }
}
