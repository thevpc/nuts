//package net.thevpc.nuts.runtime.standalone.definition.filter;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
//import net.thevpc.nuts.spi.base.AbstractIdFilter;
//import net.thevpc.nuts.util.NFilterOp;
//import net.thevpc.nuts.util.NSimplifiable;
//
//public class NInstallStatusDefinitionFilter extends AbstractDefinitionFilter implements NSimplifiable<NDefinitionFilter> {
//    private final NInstallStatusFilter installStatus;
//
//    public NInstallStatusDefinitionFilter(NInstallStatusFilter installStatus) {
//        super(NFilterOp.CUSTOM);
//        this.installStatus = installStatus;
//    }
//
//    @Override
//    public boolean acceptDefinition(NDefinition definition) {
//        NInstallStatus is = NWorkspaceExt.of().getInstalledRepository().getInstallStatus(definition.getId());
//        if (installStatus == null) {
//            return true;
//        }
//        return installStatus.acceptInstallStatus(is);
//    }
//
//
//    public NInstallStatusFilter getInstallStatus() {
//        return installStatus;
//    }
//
//    @Override
//    public NDefinitionFilter simplify() {
//        if (installStatus == null) {
//            return NDefinitionFilters.of().always();
//        }
//        return this;
//    }
//
//    @Override
//    public String toString() {
//        return
//                "installStatus(" + installStatus +
//                        ')';
//    }
//}
