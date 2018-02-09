package net.vpc.app.nuts.extensions.filters;

import net.vpc.app.nuts.extensions.filters.id.JsNutsIdFilter;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.Simplifiable;

import java.util.Map;
import java.util.logging.Logger;
import net.vpc.app.nuts.extensions.filters.descriptor.JsNutsDescriptorFilter;
import net.vpc.app.nuts.extensions.filters.version.JsNutsVersionFilter;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

public class DefaultNutsIdMultiFilter implements NutsIdFilter, Simplifiable<NutsIdFilter>, JsNutsIdFilter {

    private static final Logger log = Logger.getLogger(DefaultNutsIdMultiFilter.class.getName());
    private final NutsIdFilter idFilter;

    private final NutsVersionFilter versionFilter;

    private final NutsDescriptorFilter descriptorFilter;
    private final NutsRepository repository;
    private final NutsSession session;

    public DefaultNutsIdMultiFilter(Map<String, String> map, NutsIdFilter idFilter, NutsVersionFilter versionFilter, NutsDescriptorFilter descriptorFilter,
            NutsRepository repository,
            NutsSession session
    ) {
        this.idFilter = CoreNutsUtils.simplify(idFilter);
        this.versionFilter = CoreNutsUtils.simplify(versionFilter);
        this.descriptorFilter = CoreNutsUtils.simplify(CoreNutsUtils.And(
                CoreNutsUtils.createNutsDescriptorFilter(map), descriptorFilter));
        this.repository = repository;
        this.session = session;
    }

    public NutsIdFilter getIdFilter() {
        return idFilter;
    }

    public NutsVersionFilter getVersionFilter() {
        return versionFilter;
    }

    public NutsDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    @Override
    public boolean accept(NutsId id) {
        if (idFilter != null) {
            if (!idFilter.accept(id)) {
                return false;
            }
        }
        if (versionFilter != null) {
            if (!versionFilter.accept(id.getVersion())) {
                return false;
            }
        }
        if (descriptorFilter != null) {
            NutsDescriptor descriptor = null;
            try {
                descriptor = repository.fetchDescriptor(id, session);
                if (!CoreNutsUtils.isEffectiveId(descriptor.getId())) {
                    NutsDescriptor nutsDescriptor = null;
                    try {
                        nutsDescriptor = repository.getWorkspace().resolveEffectiveDescriptor(descriptor, session);
                    } catch (Exception e) {
                        //throw new RuntimeException(e);
                    }
                    descriptor = nutsDescriptor;
                }
            } catch (Exception ex) {
                //suppose we cannot retrieve descriptor
                log.severe("Unable to fetch Descriptor for " + id + " : " + ex.toString());
                return false;
            }
            if (!descriptorFilter.accept(descriptor)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsIdFilter simplify() {
        NutsIdFilter idFilter2 = CoreNutsUtils.simplify(idFilter);

        NutsVersionFilter versionFilter2 = CoreNutsUtils.simplify(versionFilter);

        NutsDescriptorFilter descriptorFilter2 = CoreNutsUtils.simplify(descriptorFilter);
        if (idFilter2 == null && versionFilter2 == null && descriptorFilter2 == null) {
            return null;
        }
        if (idFilter2 != idFilter || versionFilter2 != versionFilter || descriptorFilter2 != descriptorFilter) {
            return new DefaultNutsIdMultiFilter(null, idFilter2, versionFilter2, descriptorFilter2, repository, session);
        }
        return this;
    }

    @Override
    public String toJsNutsIdFilterExpr() {
        class Item {

            Class type;
            Object value;

            public Item(Class type, Object value) {
                this.type = type;
                this.value = value;
            }

        }
        List<Item> all = new ArrayList<>();
        if (idFilter != null) {
            all.add(new Item(NutsIdFilter.class, idFilter));
        }
        if (descriptorFilter != null) {
            all.add(new Item(NutsDescriptorFilter.class, descriptorFilter));
        }
        if (versionFilter != null) {
            all.add(new Item(NutsVersionFilter.class, versionFilter));
        }
        StringBuilder sb = new StringBuilder();
        if (all.size() == 0) {
            return "true";
        }
        if (all.size() > 1) {
            sb.append("(");
        }
        for (Item id : all) {
            if (sb.length() > 0) {
                sb.append(" && ");
            }
            if (id.type.equals(NutsIdFilter.class)) {
                if (id.value instanceof JsNutsIdFilter) {
                    JsNutsIdFilter b = (JsNutsIdFilter) id.value;
                    String expr = b.toJsNutsIdFilterExpr();
                    if (CoreStringUtils.isEmpty(expr)) {
                        return null;
                    }
                    sb.append("(").append(expr).append("')");
                } else {
                    return null;
                }
            } else if (id.type.equals(NutsVersionFilter.class)) {
                if (id.value instanceof JsNutsVersionFilter) {
                    JsNutsVersionFilter b = (JsNutsVersionFilter) id.value;
                    String expr = b.toJsNutsVersionFilterExpr();
                    if (CoreStringUtils.isEmpty(expr)) {
                        return null;
                    }
                    sb.append("(").append(expr).append("')");
                } else {
                    return null;
                }
            } else if (id.type.equals(NutsDescriptorFilter.class)) {
                if (id.value instanceof JsNutsDescriptorFilter) {
                    JsNutsDescriptorFilter b = (JsNutsDescriptorFilter) id.value;
                    String expr = b.toJsNutsDescriptorFilterExpr();
                    if (CoreStringUtils.isEmpty(expr)) {
                        return null;
                    }
                    sb.append("(").append(expr).append("')");
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        if (all.size() > 0) {
            sb.append(")");
        }
        return sb.toString();
    }
}
