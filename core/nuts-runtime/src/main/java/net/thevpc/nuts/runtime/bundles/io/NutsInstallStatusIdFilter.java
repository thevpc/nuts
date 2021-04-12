package net.thevpc.nuts.runtime.bundles.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.filters.id.AbstractIdFilter;

public class NutsInstallStatusIdFilter extends AbstractIdFilter {
    private NutsInstallStatusFilter installStatus;

    public NutsInstallStatusIdFilter(NutsSession ws, NutsInstallStatusFilter installStatus) {
        super(ws, NutsFilterOp.CUSTOM);
        this.installStatus = installStatus;
    }

    @Override
    public boolean acceptId(NutsId id, NutsSession session) {
        NutsInstallStatus is = NutsWorkspaceExt.of(session.getWorkspace()).getInstalledRepository().getInstallStatus(id, session);
        if (installStatus == null) {
            return true;
        }
        return installStatus.acceptInstallStatus(is,session);
    }

    @Override
    public boolean acceptSearchId(NutsSearchId sid, NutsSession session) {
        NutsInstallStatus is = NutsWorkspaceExt.of(session.getWorkspace()).getInstalledRepository().getInstallStatus(sid.getId(session), session);
        if (installStatus == null) {
            return true;
        }
        return installStatus.acceptInstallStatus(is,session);
    }

    public NutsInstallStatusFilter getInstallStatus() {
        return installStatus;
    }

    @Override
    public NutsIdFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        return
                "installStatus(" + installStatus +
                ')';
    }
}
