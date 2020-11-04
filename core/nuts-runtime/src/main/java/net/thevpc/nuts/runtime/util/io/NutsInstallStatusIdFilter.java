package net.thevpc.nuts.runtime.util.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.filters.CoreFilterUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class NutsInstallStatusIdFilter extends AbstractNutsFilter implements NutsIdFilter {
    private Set<NutsInstallStatus>[] installStatus;

    public NutsInstallStatusIdFilter(NutsWorkspace ws, NutsInstallStatus... installStatus) {
        this(ws,new Set[]{EnumSet.copyOf(Arrays.asList(installStatus))});
    }

    public NutsInstallStatusIdFilter(NutsWorkspace ws, Set<NutsInstallStatus>[] installStatus) {
        super(ws, NutsFilterOp.CUSTOM);
        this.installStatus = installStatus;
    }

    @Override
    public boolean acceptId(NutsId id, NutsSession session) {
        Set<NutsInstallStatus> is = NutsWorkspaceExt.of(session.getWorkspace()).getInstalledRepository().getInstallStatus(id, session);
        return accept(is);
    }

    @Override
    public boolean acceptSearchId(NutsSearchId sid, NutsSession session) {
        Set<NutsInstallStatus> is = NutsWorkspaceExt.of(session.getWorkspace()).getInstalledRepository().getInstallStatus(sid.getId(session), session);
        return accept(is);
    }

    private boolean accept(Set<NutsInstallStatus> status) {
        if (installStatus == null || installStatus.length==0) {
            return true;
        }
        for (Set<NutsInstallStatus> s : installStatus) {
            if (status.containsAll(s)){
                return true;
            }
        }
        return false;
    }

    public boolean containsUninstalled() {
        if(installStatus.length==0){
            return true;
        }
        for (Set<NutsInstallStatus> status : installStatus) {
            if(status.contains(NutsInstallStatus.NOT_INSTALLED)){
                return true;
            }
        }
        return false;
    }

    public boolean containsInstalled() {
        if(installStatus.length==0){
            return true;
        }
        for (Set<NutsInstallStatus> status : installStatus) {
            if(!status.contains(NutsInstallStatus.NOT_INSTALLED)){
                return true;
            }
        }
        return false;
    }

    public Set<Set<NutsInstallStatus>> getInstallStatus() {
        return new HashSet<>(Arrays.asList(installStatus));
    }

    public Set<Set<NutsInstallStatus>> getPossibilities() {
        Set<Set<NutsInstallStatus>> z=new HashSet<>();
        for (Set<NutsInstallStatus> s : CoreFilterUtils.getPossibleInstallStatuses()) {
            if(accept(s)){
                z.add(s);
            }
        }
        return z;
    }

    @Override
    public NutsFilter simplify() {
        return this;
    }

    @Override
    public String toString() {
        return
                "installStatusIncludesAny(" + Arrays.stream(installStatus).map(x->x.toString()).collect(Collectors.joining(",")) +
                ')';
    }
}
