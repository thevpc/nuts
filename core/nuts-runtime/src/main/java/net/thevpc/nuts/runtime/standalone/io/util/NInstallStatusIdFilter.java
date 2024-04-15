package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.base.AbstractIdFilter;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.NFilterOp;

public class NInstallStatusIdFilter extends AbstractIdFilter {
    private final NInstallStatusFilter installStatus;

    public NInstallStatusIdFilter(NSession session, NInstallStatusFilter installStatus) {
        super(session, NFilterOp.CUSTOM);
        this.installStatus = installStatus;
    }

    @Override
    public boolean acceptId(NId id, NSession session) {
        NInstallStatus is = NWorkspaceExt.of(session.getWorkspace()).getInstalledRepository().getInstallStatus(id, session);
        if (installStatus == null) {
            return true;
        }
        return installStatus.acceptInstallStatus(is,session);
    }

    @Override
    public boolean acceptSearchId(NSearchId sid, NSession session) {
        NInstallStatus is = NWorkspaceExt.of(session.getWorkspace()).getInstalledRepository().getInstallStatus(sid.getId(session), session);
        if (installStatus == null) {
            return true;
        }
        return installStatus.acceptInstallStatus(is,session);
    }

    public NInstallStatusFilter getInstallStatus() {
        return installStatus;
    }

    @Override
    public NIdFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        return
                "installStatus(" + installStatus +
                ')';
    }
}
