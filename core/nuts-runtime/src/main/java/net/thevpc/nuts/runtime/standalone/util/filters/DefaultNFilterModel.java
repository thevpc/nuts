package net.thevpc.nuts.runtime.standalone.util.filters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.definition.filter.NDefinitionFilterNone;
import net.thevpc.nuts.runtime.standalone.dependency.filter.NDependencyFilterNone;
import net.thevpc.nuts.runtime.standalone.id.filter.NIdFilterNone;
import net.thevpc.nuts.runtime.standalone.repository.filter.NRepositoryFilterNone;
import net.thevpc.nuts.runtime.standalone.version.filter.NVersionFilterNone;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NMsg;

import java.util.*;
import java.util.function.Supplier;

public class DefaultNFilterModel {

    private NWorkspace workspace;
    private Map<String, Object> shared = new HashMap<String, Object>();

    public DefaultNFilterModel(NWorkspace ws) {
        this.workspace = ws;
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

    public <T> T getShared(Class<T> clz, Supplier<T> s) {
        return (T) shared.computeIfAbsent(clz.getName(), (String t) -> s.get());
    }

    public <T extends NFilter> T nonnull(Class<T> type, NFilter filter) {
        if (filter == null) {
            return always(type);
        }
        return filter.to(type);
    }

    public NTypedFilters resolveNutsTypedFilters(Class type) {
        if (type == null) {
            throw new NIllegalArgumentException(NMsg.ofPlain("unable to detected Filter type"));
        }
        switch (type.getName()) {
            case "net.thevpc.nuts.NDependencyFilter": {
                return NDependencyFilters.of();
            }
            case "net.thevpc.nuts.NDefinitionFilter": {
                return NDefinitionFilters.of();
            }
            case "net.thevpc.nuts.NRepositoryFilter": {
                return NRepositoryFilters.of();
            }
            case "net.thevpc.nuts.NIdFilter": {
                return NIdFilters.of();
            }
            case "net.thevpc.nuts.NVersionFilter": {
                return NVersionFilters.of();
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported filter type: %s", type));
    }

    public <T extends NFilter> T always(Class<T> type) {
        return (T) resolveNutsTypedFilters(type).always();
    }

    public <T extends NFilter> T never(Class<T> type) {
        return (T) resolveNutsTypedFilters(type).never();
    }

    public <T extends NFilter> T all(Class<T> type, NFilter[] others) {
        others = expandAll(others).toArray(new NFilter[0]);
        if (type == null || type.equals(NFilter.class)) {
            List<NFilter> all = new ArrayList<>();
            all.addAll(Arrays.asList(others));
            type = detectType(all.toArray(new NFilter[0]));
            if (type == null) {
                throw new NIllegalArgumentException(NMsg.ofPlain("unable to detected Filter type"));
            }
        }
        return (T) resolveNutsTypedFilters(type).all(others);
    }

    public <T extends NFilter> T all(NFilter[] others) {
        return all(null, others);
    }

    public <T extends NFilter> T any(Class<T> type, NFilter[] others) {
        others = expandAny(others).toArray(new NFilter[0]);
        if (type == null || type.equals(NFilter.class)) {
            List<NFilter> all = new ArrayList<>();
            all.addAll(Arrays.asList(others));
            type = detectType(all.toArray(new NFilter[0]));
            if (type == null) {
                throw new NIllegalArgumentException(NMsg.ofPlain("unable to detected Filter type"));
            }
        }
        return (T) resolveNutsTypedFilters(type).any(others);
    }

    public <T extends NFilter> T not(NFilter other) {
        return not(null, other);
    }

    public <T extends NFilter> T not(Class<T> type, NFilter other) {
        if (type == null || type.equals(NFilter.class)) {
            type = (Class<T>) detectType(other);
            if (type == null) {
                throw new NIllegalArgumentException(NMsg.ofPlain("unable to detected Filter type"));
            }
        }
        return (T) resolveNutsTypedFilters(type).not(other);
    }

    public <T extends NFilter> T any(NFilter[] others) {
        return any(null, others);
    }

    public <T extends NFilter> T none(Class<T> type, NFilter[] others) {
        others = expandAll(others).toArray(new NFilter[0]);
        if (type == null || type.equals(NFilter.class)) {
            List<NFilter> all = new ArrayList<>();
            all.addAll(Arrays.asList(others));
            type = detectType(all.toArray(new NFilter[0]));
            if (type == null) {
                throw new NIllegalArgumentException(NMsg.ofPlain("unable to detected Filter type"));
            }
        }
        switch (type.getName()) {
            case "net.thevpc.nuts.NDependencyFilter": {
                List<NDependencyFilter> all = new ArrayList<>();
                for (NFilter other : others) {
                    NDependencyFilter a = NDependencyFilters.of().from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                return (T) new NDependencyFilterNone(all.toArray(new NDependencyFilter[0]));
            }
            case "net.thevpc.nuts.NRepositoryFilter": {
                List<NRepositoryFilter> all = new ArrayList<>();
                for (NFilter other : others) {
                    NRepositoryFilter a = NRepositoryFilters.of().from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                return (T) new NRepositoryFilterNone(all.toArray(new NRepositoryFilter[0]));
            }
            case "net.thevpc.nuts.NIdFilter": {
                List<NIdFilter> all = new ArrayList<>();
                for (NFilter other : others) {
                    NIdFilter a = NIdFilters.of().from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                return (T) new NIdFilterNone(all.toArray(new NIdFilter[0]));
            }
            case "net.thevpc.nuts.NVersionFilter": {
                List<NVersionFilter> all = new ArrayList<>();
                for (NFilter other : others) {
                    NVersionFilter a = NVersionFilters.of().from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                return (T) new NVersionFilterNone(all.toArray(new NVersionFilter[0]));
            }
            case "net.thevpc.nuts.NDefinitionFilter": {
                List<NDefinitionFilter> all = new ArrayList<>();
                for (NFilter other : others) {
                    NDefinitionFilter a = NDefinitionFilters.of().from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                return (T) new NDefinitionFilterNone(all.toArray(new NDefinitionFilter[0]));
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported filter type: %s", type));
    }

    public <T extends NFilter> T none(NFilter[] others) {
        return none(null, others);
    }

    public <T extends NFilter> T to(Class<T> toFilterInterface, NFilter filter) {
        return (T) resolveNutsTypedFilters(toFilterInterface).from(filter);
    }

    public <T extends NFilter> T as(Class<T> toFilterInterface, NFilter filter) {
        return (T) resolveNutsTypedFilters(toFilterInterface).as(filter);
    }

    public Class<? extends NFilter> detectType(NFilter nFilter) {
        if (nFilter == null) {
            return null;
        }
        return detectType(nFilter.getClass());
    }

    private Collection<NFilter> expandAny(NFilter... others) {
        List<NFilter> ok = new ArrayList<>();
        if (others != null) {
            for (NFilter other : others) {
                if (other != null) {
                    if (other.getFilterOp() == NFilterOp.OR) {
                        ok.addAll(other.getSubFilters());
                    } else {
                        ok.addAll(Arrays.asList(other));
                    }
                }
            }
        }
        return ok;
    }

    private Collection<NFilter> expandAll(NFilter... others) {
        List<NFilter> ok = new ArrayList<>();
        if (others != null) {
            for (NFilter other : others) {
                if (other != null) {
                    if (other.getFilterOp() == NFilterOp.AND) {
                        ok.addAll(other.getSubFilters());
                    } else {
                        ok.addAll(Arrays.asList(other));
                    }
                }
            }
        }
        return ok;
    }

    private Collection<NFilter> expandNone(NFilter... others) {
        List<NFilter> ok = new ArrayList<>();
        if (others != null) {
            for (NFilter other : others) {
                if (other != null) {
                    if (other.getFilterOp() == NFilterOp.NOT) {
                        ok.addAll(other.getSubFilters());
                    } else {
                        ok.addAll(Arrays.asList(other));
                    }
                }
            }
        }
        return ok;
    }

    public <T extends NFilter> Class<T> detectType(NFilter[] others) {
        Class c = null;
        for (NFilter other : others) {
            if (other != null) {
                if (c == null) {
                    c = detectType(other.getClass());
                } else {
                    c = detectType(c, other.getClass());
                }
            }
        }
        if (c == null) {
            return null;
        }
        return c;
    }

    public <T extends NFilter> Class<T> detectType(Class<? extends NFilter> c1) {
        if (c1 == null) {
            return null;
        }
        if (NVersionFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NVersionFilter.class;
        }
        if (NIdFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NIdFilter.class;
        }
        if (NDefinitionFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NDefinitionFilter.class;
        }
        if (NRepositoryFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NRepositoryFilter.class;
        }
        if (NDependencyFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NDependencyFilter.class;
        }
        throw new NIllegalArgumentException(NMsg.ofC("cannot detect filter type for %s", c1));
    }

    public <T extends NFilter> Class<T> detectType(Class<? extends NFilter> c1, Class<? extends NFilter> c2) {
        if (NVersionFilter.class.isAssignableFrom(c1)) {
            if (NVersionFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NVersionFilter.class;
            }
            if (NIdFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NIdFilter.class;
            }
            if (NDefinitionFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NDefinitionFilter.class;
            }
            throw new NIllegalArgumentException(NMsg.ofC("cannot detect common type for %s and %s", c1, c2));
        }
        if (NIdFilter.class.isAssignableFrom(c1)) {
            if (NVersionFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NIdFilter.class;
            }
            if (NIdFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NIdFilter.class;
            }
            if (NDefinitionFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NDefinitionFilter.class;
            }
            throw new NIllegalArgumentException(NMsg.ofC("cannot detect common type for %s and %s", c1, c2));
        }
        if (NDefinitionFilter.class.isAssignableFrom(c1)) {
            if (NVersionFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NDefinitionFilter.class;
            }
            if (NIdFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NDefinitionFilter.class;
            }
            if (NDefinitionFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NDefinitionFilter.class;
            }
            throw new NIllegalArgumentException(NMsg.ofC("cannot detect common type for %s and %s", c1, c2));
        }
        if (NDependencyFilter.class.isAssignableFrom(c1)) {
            if (NDependencyFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NDependencyFilter.class;
            }
            throw new NIllegalArgumentException(NMsg.ofC("cannot detect common type for %s and %s", c1, c2));
        }
        if (NRepositoryFilter.class.isAssignableFrom(c1)) {
            if (NRepositoryFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NRepositoryFilter.class;
            }
            throw new NIllegalArgumentException(NMsg.ofC("cannot detect common type for %s and %s", c1, c2));
        }
        throw new NIllegalArgumentException(NMsg.ofC("cannot detect common type for %s and %s", c1, c2));
    }

}
