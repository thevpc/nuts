package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.search.AbstractNutsSearchCommand;

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
        NutsSession session = getSession();
        NutsElements e = NutsElements.of(getSession()).setSession(getSession());
        NutsObjectElementBuilder eb = e.ofObject()
                .set("execType", getExecType())
                .set("defaultVersions", getDefaultVersions())
                .set("targetApiVersion", getTargetApiVersion().toString())
                .set("optional", getOptional())
                .set("arch", e.ofArray().addAll(getArch()).build())
                .set("packaging", e.ofArray().addAll(getPackaging()).build())
                .set("ids", e.ofArray().addAll(Arrays.stream(getIds())
                        .map(Object::toString).toArray(String[]::new)).build());
        if (getIdFilter() != null) {
            eb.set("idFilter", e.toElement(getIdFilter()));
        }
        if (getDescriptorFilter() != null) {
            eb.set("descriptorFilter", NutsElements.of(getSession()).toElement(getDescriptorFilter()));
        }
        if (getInstallStatus() != null) {
            eb.set("installStatus", e.ofString(getInstallStatus().toString()));
        }
        if (getRepositoryFilter() != null) {
            eb.set("repositories", e.ofString(getRepositoryFilter().toString()));
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
