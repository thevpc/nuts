package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElementBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.search.AbstractNSearchCmd;
import net.thevpc.nuts.util.NIterator;

import java.util.Iterator;
import java.util.List;

public class RemoteNSearchCmd extends AbstractNSearchCmd {

    public RemoteNSearchCmd() {
        super();
    }

    @Override
    public NSearchCmd copy() {
        RemoteNSearchCmd b = new RemoteNSearchCmd();
        b.setAll(this);
        return b;
    }

    @Override
    public NFetchCmd toFetch() {
        return null;
    }

    @Override
    protected NIterator<NId> getResultIdIteratorBase(Boolean forceInlineDependencies) {
        NElements e = NElements.of();
        NObjectElementBuilder eb = e.ofObjectBuilder()
                .set("execType", getExecType())
                .set("defaultVersions", getDefaultVersions())
                .set("targetApiVersion", getTargetApiVersion().toString())
                .set("optional", getOptional())
                .set("arch", e.ofArrayBuilder().addAll(getArch().toArray(new String[0])).build())
                .set("packaging", e.ofArrayBuilder().addAll(getPackaging().toArray(new String[0])).build())
                .set("ids", e.ofArrayBuilder().addAll(getIds().stream()
                        .map(Object::toString).toArray(String[]::new)).build());
        if (getIdFilter() != null) {
            eb.set("idFilter", e.toElement(getIdFilter()));
        }
        if (getDescriptorFilter() != null) {
            eb.set("descriptorFilter", NElements.of().toElement(getDescriptorFilter()));
        }
        if (getInstallStatus() != null) {
            eb.set("installStatus", e.ofString(getInstallStatus().toString()));
        }
        if (getRepositoryFilter() != null) {
            eb.set("repositories", e.ofString(getRepositoryFilter().toString()));
        }

        RemoteNWorkspace ws=(RemoteNWorkspace)NWorkspace.get();
        return NIterator.of(
                (Iterator <NId>)
                        ws.remoteCall(
                                ws.createCall(
                                "workspace.searchIds",
                                eb.build()
                        ),
                        List.class
                ).iterator()
        ).withDesc(NEDesc.of("searchRemoteIds"));
    }


}
