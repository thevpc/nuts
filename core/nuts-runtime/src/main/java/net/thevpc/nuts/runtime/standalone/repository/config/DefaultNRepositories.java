//package net.thevpc.nuts.runtime.standalone.repository.config;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.NConstants;
//import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
//import net.thevpc.nuts.spi.NComponentScope;
//import net.thevpc.nuts.spi.NScopeType;
//import net.thevpc.nuts.spi.NSupportLevelContext;
//import net.thevpc.nuts.util.NOptional;
//
//@NComponentScope(NScopeType.SESSION)
//public class DefaultNRepositories implements NRepositories {
//
//    public DefaultNRepositories() {
//    }
//
//
//    @Override
//    public int getSupportLevel(NSupportLevelContext context) {
//        return NConstants.Support.DEFAULT_SUPPORT;
//    }
//
//    @Override
//    public NRepositoryFilters filter() {
//        return NRepositoryFilters.of();
//    }
//
////    private NRepository toSessionAwareRepo(NRepository x) {
////        return NRepositorySessionAwareImpl.of(x, model.getWorkspace(), workspace);
////    }
//
////    private NOptional<NRepository> toSessionAwareRepoOptional(NOptional<NRepository> x) {
////        return x.map(r->NRepositorySessionAwareImpl.of(r, model.getWorkspace(), workspace));
////    }
//
//
//}
