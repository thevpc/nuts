//package net.thevpc.nuts.runtime.core.filters;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.runtime.core.filters.descriptor.JsNutsDescriptorFilter;
//import net.thevpc.nuts.runtime.core.filters.version.JsNutsVersionFilter;
//import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
//import net.thevpc.nuts.runtime.core.util.Simplifiable;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import net.thevpc.nuts.core.NutsWorkspaceExt;
//import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
//import net.thevpc.nuts.runtime.core.filters.id.NutsScriptAwareIdFilter;
//
//public class DefaultNutsIdMultiFilter implements NutsIdFilter, Simplifiable<NutsIdFilter>, NutsScriptAwareIdFilter {
//
//    private final NutsIdFilter idFilter;
//
//
//    private final NutsDescriptorFilter descriptorFilter;
//    private final NutsRepository repository;
//    private final NutsSession session;
//
//    public DefaultNutsIdMultiFilter(Map<String, String> map, NutsIdFilter idFilter, NutsDescriptorFilter descriptorFilter, NutsRepository repository, NutsSession session) {
//        this.idFilter = CoreNutsUtils.simplify(idFilter);
//        this.descriptorFilter = CoreNutsUtils.simplify(CoreFilterUtils.And(
//                CoreNutsUtils.createNutsDescriptorFilter(map), descriptorFilter));
//        this.repository = repository;
//        this.session = session;
//    }
//
//    public NutsIdFilter getIdFilter() {
//        return idFilter;
//    }
//
//    public NutsDescriptorFilter getDescriptorFilter() {
//        return descriptorFilter;
//    }
//
//    @Override
//    public boolean acceptSearchId(NutsSearchId sid, NutsSession session) {
//        if (idFilter != null) {
//            if (!idFilter.acceptSearchId(sid, ws, session)) {
//                return false;
//            }
//        }
//        if (descriptorFilter != null) {
//            if (!descriptorFilter.acceptSearchId(sid, ws, session)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public boolean accept(NutsId id, NutsSession session) {
//        if (idFilter != null) {
//            if (!idFilter.accept(id,ws, session)) {
//                return false;
//            }
//        }
//        if (descriptorFilter != null) {
//            NutsDescriptor descriptor = null;
//            try {
//                descriptor = repository.fetchDescriptor().setId(id).session(this.session).getResult();
//                if (!CoreNutsUtils.isEffectiveId(descriptor.getId())) {
//                    NutsDescriptor nutsDescriptor = null;
//                    try {
//                        //NutsWorkspace ws = repository.getWorkspace();
//                        nutsDescriptor = NutsWorkspaceExt.of(ws).resolveEffectiveDescriptor(descriptor, this.session);
//                    } catch (Exception e) {
//                        //throw new NutsException(e);
//                    }
//                    descriptor = nutsDescriptor;
//                }
//            } catch (Exception ex) {
//                //suppose we cannot retrieve descriptor
//                if (LOG.isLoggable(Level.FINER)) {
//                    LOG.log(Level.FINER, this.session.getFetchMode() + " Unable to fetch Descriptor for " + id + " from repository " + repository.config().getName() + " : " + CoreStringUtils.exceptionToString(ex));
//                }
//                return false;
//            }
//            if (!descriptorFilter.accept(descriptor, ws, session)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public NutsIdFilter simplify() {
//        NutsIdFilter idFilter2 = CoreNutsUtils.simplify(idFilter);
//
//
//        NutsDescriptorFilter descriptorFilter2 = CoreNutsUtils.simplify(descriptorFilter);
//        if (idFilter2 == null && descriptorFilter2 == null) {
//            return null;
//        }
//        if (idFilter2 != idFilter || descriptorFilter2 != descriptorFilter) {
//            return new DefaultNutsIdMultiFilter(null, idFilter2, descriptorFilter2, repository, session);
//        }
//        return this;
//    }
//
//    @Override
//    public String toJsNutsIdFilterExpr() {
//        class Item {
//
//            Class type;
//            Object value;
//
//            public Item(Class type, Object value) {
//                this.type = type;
//                this.value = value;
//            }
//
//        }
//        List<Item> all = new ArrayList<>();
//        if (idFilter != null) {
//            all.add(new Item(NutsIdFilter.class, idFilter));
//        }
//        if (descriptorFilter != null) {
//            all.add(new Item(NutsDescriptorFilter.class, descriptorFilter));
//        }
//        StringBuilder sb = new StringBuilder();
//        if (all.isEmpty()) {
//            return "true";
//        }
//        if (all.size() > 1) {
//            sb.append("(");
//        }
//        for (Item id : all) {
//            if (sb.length() > 0) {
//                sb.append(" && ");
//            }
//            if (id.type.equals(NutsIdFilter.class)) {
//                if (id.value instanceof NutsScriptAwareIdFilter) {
//                    NutsScriptAwareIdFilter b = (NutsScriptAwareIdFilter) id.value;
//                    String expr = b.toJsNutsIdFilterExpr();
//                    if (NutsBlankable.isBlank(expr)) {
//                        return null;
//                    }
//                    sb.append("(").append(expr).append("')");
//                } else {
//                    return null;
//                }
//            } else if (id.type.equals(NutsVersionFilter.class)) {
//                if (id.value instanceof JsNutsVersionFilter) {
//                    JsNutsVersionFilter b = (JsNutsVersionFilter) id.value;
//                    String expr = b.toJsNutsVersionFilterExpr();
//                    if (NutsBlankable.isBlank(expr)) {
//                        return null;
//                    }
//                    sb.append("(").append(expr).append("')");
//                } else {
//                    return null;
//                }
//            } else if (id.type.equals(NutsDescriptorFilter.class)) {
//                if (id.value instanceof JsNutsDescriptorFilter) {
//                    JsNutsDescriptorFilter b = (JsNutsDescriptorFilter) id.value;
//                    String expr = b.toJsNutsDescriptorFilterExpr();
//                    if (NutsBlankable.isBlank(expr)) {
//                        return null;
//                    }
//                    sb.append("(").append(expr).append("')");
//                } else {
//                    return null;
//                }
//            } else {
//                return null;
//            }
//        }
//        if (all.size() > 0) {
//            sb.append(")");
//        }
//        return sb.toString();
//    }
//
//    @Override
//    public String toString() {
//        return "DefaultNutsIdMultiFilter{" + "idFilter=" + idFilter + ", descriptorFilter=" + descriptorFilter + ", repository=" + (repository == null ? "" : repository.config().getName()) + '}';
//    }
//
//}
