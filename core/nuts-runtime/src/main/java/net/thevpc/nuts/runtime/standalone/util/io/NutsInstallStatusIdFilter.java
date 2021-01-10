package net.thevpc.nuts.runtime.standalone.util.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.core.filters.CoreFilterUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NutsInstallStatusIdFilter extends AbstractNutsFilter implements NutsIdFilter {
    private Predicate<NutsInstallStatus> installStatus;

    public NutsInstallStatusIdFilter(NutsWorkspace ws, Predicate<NutsInstallStatus> installStatus) {
        super(ws, NutsFilterOp.CUSTOM);
        this.installStatus = installStatus;
    }

    @Override
    public boolean acceptId(NutsId id, NutsSession session) {
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
        return installStatus.test(status);
    }

    public Predicate<NutsInstallStatus> getInstallStatus() {
        return installStatus;
    }



    @Override
    public NutsFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        return
                "installStatus(" + installStatus +
                ')';
    }
}
