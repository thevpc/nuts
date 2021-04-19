package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsCollectionResult;
import net.thevpc.nuts.runtime.standalone.wscommands.AbstractNutsSearchCommand;

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
    protected NutsCollectionResult<NutsId> getResultIdsBase(boolean print, boolean sort) {
        return buildNutsCollectionSearchResult(getResultIdsBaseIterator(sort), print);
    }

    protected Iterator<NutsId> getResultIdsBaseIterator(boolean sort) {
        RemoteNutsWorkspace ws = getWorkspace();
        NutsElementFormat e = ws.formats().element().setSession(getSession());
        NutsObjectElementBuilder eb = e.forObject()
                .set("execType", getExecType())
                .set("defaultVersions", getDefaultVersions())
                .set("targetApiVersion", getTargetApiVersion())
                .set("optional", getOptional())
                .set("arch", e.forArray().addAll(getArch()).build())
                .set("packaging", e.forArray().addAll(getPackaging()).build())
                .set("repositories", e.forArray().addAll(getRepositories()).build())
                .set("ids", e.forArray().addAll(Arrays.stream(getIds())
                        .map(Object::toString).toArray(String[]::new)).build());
        if (getIdFilter() != null) {
            eb.set("idFilter", e.convertToElement(getIdFilter()));
        }
        if (getDescriptorFilter() != null) {
            eb.set("descriptorFilter", ws.formats().element().convertToElement(getDescriptorFilter()));
        }
        if (getInstallStatus() != null) {
            eb.set("installStatus", e.forString(getInstallStatus().toString()));
        }

        return getWorkspace().remoteCall(
                getWorkspace().createCall(
                        "workspace.searchIds",
                        eb.build(),getSession()
                ),
                 List.class
        ).iterator();
    }
    protected Iterator<NutsDependency> getResultIdsBaseIterator2(boolean sort) {
        RemoteNutsWorkspace ws = getWorkspace();
        NutsElementFormat e = ws.formats().element();
        NutsObjectElementBuilder eb = e.forObject()
                .set("execType", getExecType())
                .set("defaultVersions", getDefaultVersions())
                .set("targetApiVersion", getTargetApiVersion())
                .set("optional", getOptional())
                .set("arch", e.forArray().addAll(getArch()).build())
                .set("packaging", e.forArray().addAll(getPackaging()).build())
                .set("repositories", e.forArray().addAll(getRepositories()).build())
                .set("ids", e.forArray().addAll(Arrays.stream(getIds())
                        .map(Object::toString).toArray(String[]::new)).build());
        if (getIdFilter() != null) {
            eb.set("idFilter", ws.formats().element().convertToElement(getIdFilter()));
        }
        if (getDescriptorFilter() != null) {
            eb.set("descriptorFilter", ws.formats().element().convertToElement(getDescriptorFilter()));
        }
        if (getInstallStatus() != null) {
            eb.set("installStatus", e.forString(getInstallStatus().toString()));
        }

        return getWorkspace().remoteCall(
                getWorkspace().createCall(
                        "workspace.searchDependencies",
                        eb.build(),getSession()
                ),
                 List.class
        ).iterator();
    }

    @Override
    protected NutsCollectionResult<NutsDependency> getResultDependenciesBase(boolean print, boolean sort) {
        return buildNutsCollectionSearchResult(getResultIdsBaseIterator2(sort), print);

    }

}
