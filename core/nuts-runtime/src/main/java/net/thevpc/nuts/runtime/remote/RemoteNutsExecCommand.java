package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.NutsElementFormat;
import net.thevpc.nuts.NutsExecCommand;
import net.thevpc.nuts.NutsExecutableInformation;
import net.thevpc.nuts.NutsExecutionException;
import net.thevpc.nuts.runtime.standalone.wscommands.AbstractNutsExecCommand;

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
        RemoteNutsWorkspace ws = getWorkspace();
        NutsElementFormat e = ws.formats().element().setSession(getSession());
        return getWorkspace().remoteCall(
                getWorkspace().createCall("workspace.which",
                        e.forObject()
                                .build(),getSession()
                ),
                 NutsExecutableInformation.class
        );
    }

    @Override
    public NutsExecCommand run() {
        RemoteNutsWorkspace ws = getWorkspace();
        NutsElementFormat e = ws.formats().element().setSession(getSession());
        try {
            int r = getWorkspace().remoteCall(
                    getWorkspace().createCall("workspace.exec",
                            e.forObject()
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
                result = new NutsExecutionException(ws,
                        "execution failed with code " + 244 + " and message : " + p,
                        ex, 244);
            } else {
                result = new NutsExecutionException(ws, ex, 244);
            }
        }
        executed = true;
        if (result != null && failFast) {
            throw result;
        }
        return this;
    }
}
