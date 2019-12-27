package net.vpc.app.nuts.runtime.util.io;

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsSearchId;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.core.NutsWorkspaceExt;
import net.vpc.app.nuts.NutsInstallStatus;

public class NutsIdFilterTopInstalled implements NutsIdFilter {
    private NutsInstallStatus installStatus;

    public NutsIdFilterTopInstalled(NutsInstallStatus installStatus) {
        this.installStatus = installStatus;
    }

    @Override
    public boolean accept(NutsId id, NutsSession session) {
        NutsInstallStatus is = NutsWorkspaceExt.of(session.getWorkspace()).getInstalledRepository().getInstallStatus(id, session);
        return accept(is);
    }

    @Override
    public boolean acceptSearchId(NutsSearchId sid, NutsSession session) {
        NutsInstallStatus is = NutsWorkspaceExt.of(session.getWorkspace()).getInstalledRepository().getInstallStatus(sid.getId(session), session);
        return accept(is);
    }

    private boolean accept(NutsInstallStatus status) {
        if (installStatus == null) {
            return true;
        }
        switch (installStatus) {
            case INSTALLED_OR_INCLUDED: {
                return status == NutsInstallStatus.INSTALLED || status == NutsInstallStatus.INCLUDED;
            }
        }
        return status==installStatus;
    }

    public NutsInstallStatus getInstallStatus() {
        return installStatus;
    }
}
