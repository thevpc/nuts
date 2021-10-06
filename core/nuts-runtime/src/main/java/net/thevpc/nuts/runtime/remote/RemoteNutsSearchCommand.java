package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.wscommands.search.AbstractNutsSearchCommand;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class RemoteNutsSearchCommand extends AbstractNutsSearchCommand {

    public RemoteNutsSearchCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    protected RemoteNutsWorkspace getWorkspace() {
        return (RemoteNutsWorkspace) super.getWorkspace();
    }

    @Override
    public NutsSearchCommand copy() {
        RemoteNutsSearchCommand b = new RemoteNutsSearchCommand(ws);
        b.copyFrom(this);
        return b;
    }

    @Override
    public NutsFetchCommand toFetch() {
        return null;
    }

    @Override
    protected Iterator<NutsId> getResultIdIteratorBase(Boolean forceInlineDependencies) {
        NutsSession ws = getSession();
        NutsElementFormat e = ws.elem().setSession(getSession());
        NutsObjectElementBuilder eb = e.forObject()
                .set("execType", getExecType())
                .set("defaultVersions", getDefaultVersions())
                .set("targetApiVersion", getTargetApiVersion().toString())
                .set("optional", getOptional())
                .set("arch", e.forArray().addAll(getArch()).build())
                .set("packaging", e.forArray().addAll(getPackaging()).build())
                .set("ids", e.forArray().addAll(Arrays.stream(getIds())
                        .map(Object::toString).toArray(String[]::new)).build());
        if (getIdFilter() != null) {
            eb.set("idFilter", e.toElement(getIdFilter()));
        }
        if (getDescriptorFilter() != null) {
            eb.set("descriptorFilter", ws.elem().toElement(getDescriptorFilter()));
        }
        if (getInstallStatus() != null) {
            eb.set("installStatus", e.forString(getInstallStatus().toString()));
        }
        if (getRepositoryFilter() != null) {
            eb.set("repositories", e.forString(getRepositoryFilter().toString()));
        }

        return getWorkspace().remoteCall(
                getWorkspace().createCall(
                        "workspace.searchIds",
                        eb.build(), getSession()
                ),
                List.class
        ).iterator();
    }


}
