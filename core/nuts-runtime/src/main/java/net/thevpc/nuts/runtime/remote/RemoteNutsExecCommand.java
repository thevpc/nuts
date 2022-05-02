package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNutsExecCommand;

public class RemoteNutsExecCommand extends AbstractNutsExecCommand {

    public RemoteNutsExecCommand(RemoteNutsWorkspace ws) {
        super(ws);
    }

    @Override
    protected RemoteNutsWorkspace getWorkspace() {
        return (RemoteNutsWorkspace) super.getWorkspace();
    }

    @Override
    public NutsExecutableInformation which() {
        NutsElements e = NutsElements.of(getSession());
        return getWorkspace().remoteCall(
                getWorkspace().createCall("workspace.which",
                        e.ofObject()
                                .build(), getSession()
                ),
                NutsExecutableInformation.class
        );
    }

    @Override
    public NutsExecCommand run() {
        NutsElements e = NutsElements.of(getSession());
        try {
            int r = getWorkspace().remoteCall(
                    getWorkspace().createCall("workspace.exec",
                            e.ofObject()
                                    .set("dry", dry)
                                    .set("failFast", failFast)
                                    .build(),
                            getSession()
                    ),
                    Integer.class
            );
        } catch (NutsExecutionException ex) {
            result = ex;
        } catch (Exception ex) {
            String p = getExtraErrorMessage();
            if (p != null) {
                result = new NutsExecutionException(session,
                        NutsMessage.cstyle("execution failed with code %d and message : %s", 244, p),
                        ex, 244);
            } else {
                result = new NutsExecutionException(session, NutsMessage.plain("remote command failed"), ex, 244);
            }
        }
        executed = true;
        if (result != null && failFast) {
            throw result;
        }
        return this;
    }
}
