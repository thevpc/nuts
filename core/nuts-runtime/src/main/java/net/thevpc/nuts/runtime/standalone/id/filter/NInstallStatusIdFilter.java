//package net.thevpc.nuts.runtime.standalone.id.filter;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.spi.base.AbstractIdFilter;
//import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
//import net.thevpc.nuts.util.NFilterOp;
//
//public class NInstallStatusIdFilter extends AbstractIdFilter {
//    private final NInstallStatusFilter installStatus;
//
//    public NInstallStatusIdFilter(NInstallStatusFilter installStatus) {
//        super(NFilterOp.CUSTOM);
//        this.installStatus = installStatus;
//    }
//
//    @Override
//    public boolean acceptId(NId id) {
//        NInstallStatus is = NWorkspaceExt.of().getInstalledRepository().getInstallStatus(id);
//        if (installStatus == null) {
//            return true;
//        }
//        return installStatus.acceptInstallStatus(is);
//    }
//
//    @Override
//    public boolean acceptSearchId(NSearchId sid) {
//        NInstallStatus is = NWorkspaceExt.of().getInstalledRepository().getInstallStatus(sid.getId());
//        if (installStatus == null) {
//            return true;
//        }
//        return installStatus.acceptInstallStatus(is);
//    }
//
//    public NInstallStatusFilter getInstallStatus() {
//        return installStatus;
//    }
//
//    @Override
//    public NIdFilter simplify() {
//        return this;
//    }
//
//    @Override
//    public String toString() {
//        return
//                "installStatus(" + installStatus +
//                ')';
//    }
//}
