package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecCmd;
import net.thevpc.nuts.util.NMsg;

public class RemoteNExecCmd extends AbstractNExecCmd {

    public RemoteNExecCmd(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public NExecutableInformation which() {
        RemoteNWorkspace ws=(RemoteNWorkspace)NWorkspace.get();
        return ws.remoteCall(
                ws.createCall("workspace.which",
                        NElement.ofObjectBuilder()
                                .build()
                ),
                NExecutableInformation.class
        );
    }

    @Override
    public NExecCmd run() {
        NSession session=NSession.of();
        RemoteNWorkspace ws=(RemoteNWorkspace)NWorkspace.get();
        try {
            int r = ws.remoteCall(
                    ws.createCall("workspace.exec",
                            NElement.ofObjectBuilder()
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
