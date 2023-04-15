package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElementBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.search.AbstractNSearchCommand;
import net.thevpc.nuts.util.NIterator;

import java.util.List;

public class RemoteNSearchCommand extends AbstractNSearchCommand {

    public RemoteNSearchCommand(NSession session) {
        super(session);
    }

    @Override
    protected RemoteNWorkspace getWorkspace() {
        return (RemoteNWorkspace) super.getWorkspace();
    }

    @Override
    public NSearchCommand copy() {
        RemoteNSearchCommand b = new RemoteNSearchCommand(getSession());
        b.setAll(this);
        return b;
    }

    @Override
    public NFetchCommand toFetch() {
        return null;
    }

    @Override
    protected NIterator<NId> getResultIdIteratorBase(Boolean forceInlineDependencies) {
        NElements e = NElements.of(getSession()).setSession(getSession());
        NObjectElementBuilder eb = e.ofObject()
                .set("execType", getExecType())
                .set("defaultVersions", getDefaultVersions())
                .set("targetApiVersion", getTargetApiVersion().toString())
                .set("optional", getOptional())
                .set("arch", e.ofArray().addAll(getArch().toArray(new String[0])).build())
                .set("packaging", e.ofArray().addAll(getPackaging().toArray(new String[0])).build())
                .set("ids", e.ofArray().addAll(getIds().stream()
                        .map(Object::toString).toArray(String[]::new)).build());
        if (getIdFilter() != null) {
            eb.set("idFilter", e.toElement(getIdFilter()));
        }
        if (getDescriptorFilter() != null) {
            eb.set("descriptorFilter", NElements.of(getSession()).toElement(getDescriptorFilter()));
        }
        if (getInstallStatus() != null) {
            eb.set("installStatus", e.ofString(getInstallStatus().toString()));
        }
        if (getRepositoryFilter() != null) {
            eb.set("repositories", e.ofString(getRepositoryFilter().toString()));
        }

        return NIterator.of(
                getWorkspace().remoteCall(
                        getWorkspace().createCall(
                                "workspace.searchIds",
                                eb.build(), getSession()
                        ),
                        List.class
                ).iterator(), "searchRemoteIds");
    }


}
