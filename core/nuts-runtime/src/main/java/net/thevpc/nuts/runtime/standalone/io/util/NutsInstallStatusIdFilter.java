package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.AbstractIdFilter;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;

public class NutsInstallStatusIdFilter extends AbstractIdFilter {
    private final NutsInstallStatusFilter installStatus;

    public NutsInstallStatusIdFilter(NutsSession session, NutsInstallStatusFilter installStatus) {
        super(session, NutsFilterOp.CUSTOM);
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
