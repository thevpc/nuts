package net.thevpc.nuts.runtime.standalone.util.filters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.dependency.filter.NDependencyFilterNone;
import net.thevpc.nuts.runtime.standalone.descriptor.filter.NDescriptorFilterNone;
import net.thevpc.nuts.runtime.standalone.id.filter.NIdFilterNone;
import net.thevpc.nuts.runtime.standalone.repository.filter.NRepositoryFilterNone;
import net.thevpc.nuts.runtime.standalone.version.filter.NVersionFilterNone;

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

    public <T extends NFilter> T nonnull(Class<T> type, NFilter filter, NSession session) {
        if (filter == null) {
            return always(type, session);
        }
        return filter.to(type);
    }

    public NTypedFilters resolveNutsTypedFilters(Class type, NSession session) {
        if (type == null) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("unable to detected Filter type"));
        }
        switch (type.getName()) {
            case "net.thevpc.nuts.NDependencyFilter": {
                return NDependencyFilters.of(session);
            }
            case "net.thevpc.nuts.NRepositoryFilter": {
                return NRepositoryFilters.of(session);
            }
            case "net.thevpc.nuts.NIdFilter": {
                return NIdFilters.of(session);
            }
            case "net.thevpc.nuts.NVersionFilter": {
                return NVersionFilters.of(session);
            }
            case "net.thevpc.nuts.NDescriptorFilter": {
                return NDescriptorFilters.of(session);
            }
            case "net.thevpc.nuts.NInstallStatusFilter": {
                return NInstallStatusFilters.of(session);
            }
        }
        throw new NIllegalArgumentException(session, NMsg.ofCstyle("unsupported filter type: %s", type));
    }

    public <T extends NFilter> T always(Class<T> type, NSession session) {
        return (T) resolveNutsTypedFilters(type, session).always();
    }

    public <T extends NFilter> T never(Class<T> type, NSession session) {
        return (T) resolveNutsTypedFilters(type, session).never();
    }

    public <T extends NFilter> T all(Class<T> type, NFilter[] others, NSession session) {
        others = expandAll(others).toArray(new NFilter[0]);
        if (type == null || type.equals(NFilter.class)) {
            List<NFilter> all = new ArrayList<>();
            all.addAll(Arrays.asList(others));
            type = detectType(all.toArray(new NFilter[0]), session);
            if (type == null) {
                throw new NIllegalArgumentException(session, NMsg.ofPlain("unable to detected Filter type"));
            }
        }
        return (T) resolveNutsTypedFilters(type, session).all(others);
    }

    public <T extends NFilter> T all(NFilter[] others, NSession session) {
        return all(null, others, session);
    }

    public <T extends NFilter> T any(Class<T> type, NFilter[] others, NSession session) {
        others = expandAny(others).toArray(new NFilter[0]);
        if (type == null || type.equals(NFilter.class)) {
            List<NFilter> all = new ArrayList<>();
            all.addAll(Arrays.asList(others));
            type = detectType(all.toArray(new NFilter[0]), session);
            if (type == null) {
                throw new NIllegalArgumentException(session, NMsg.ofPlain("unable to detected Filter type"));
            }
        }
        return (T) resolveNutsTypedFilters(type, session).any(others);
    }

    public <T extends NFilter> T not(NFilter other, NSession session) {
        return not(null, other, session);
    }

    public <T extends NFilter> T not(Class<T> type, NFilter other, NSession session) {
        if (type == null || type.equals(NFilter.class)) {
            type = (Class<T>) detectType(other, session);
            if (type == null) {
                throw new NIllegalArgumentException(session, NMsg.ofPlain("unable to detected Filter type"));
            }
        }
        return (T) resolveNutsTypedFilters(type, session).not(other);
    }

    public <T extends NFilter> T any(NFilter[] others, NSession session) {
        return any(null, others, session);
    }

    public <T extends NFilter> T none(Class<T> type, NFilter[] others, NSession session) {
        others = expandAll(others).toArray(new NFilter[0]);
        if (type == null || type.equals(NFilter.class)) {
            List<NFilter> all = new ArrayList<>();
            all.addAll(Arrays.asList(others));
            type = detectType(all.toArray(new NFilter[0]), session);
            if (type == null) {
                throw new NIllegalArgumentException(session, NMsg.ofPlain("unable to detected Filter type"));
            }
        }
        switch (type.getName()) {
            case "net.thevpc.nuts.NDependencyFilter": {
                List<NDependencyFilter> all = new ArrayList<>();
                for (NFilter other : others) {
                    NDependencyFilter a = NDependencyFilters.of(session).from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type, session);
                }
                return (T) new NDependencyFilterNone(session, all.toArray(new NDependencyFilter[0]));
            }
            case "net.thevpc.nuts.NRepositoryFilter": {
                List<NRepositoryFilter> all = new ArrayList<>();
                for (NFilter other : others) {
                    NRepositoryFilter a = NRepositoryFilters.of(session).from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type, session);
                }
                return (T) new NRepositoryFilterNone(session, all.toArray(new NRepositoryFilter[0]));
            }
            case "net.thevpc.nuts.NIdFilter": {
                List<NIdFilter> all = new ArrayList<>();
                for (NFilter other : others) {
                    NIdFilter a = NIdFilters.of(session).from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type, session);
                }
                return (T) new NIdFilterNone(session, all.toArray(new NIdFilter[0]));
            }
            case "net.thevpc.nuts.NVersionFilter": {
                List<NVersionFilter> all = new ArrayList<>();
                for (NFilter other : others) {
                    NVersionFilter a = NVersionFilters.of(session).from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type, session);
                }
                return (T) new NVersionFilterNone(session, all.toArray(new NVersionFilter[0]));
            }
            case "net.thevpc.nuts.NDescriptorFilter": {
                List<NDescriptorFilter> all = new ArrayList<>();
                for (NFilter other : others) {
                    NDescriptorFilter a = NDescriptorFilters.of(session).from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type, session);
                }
                return (T) new NDescriptorFilterNone(session, all.toArray(new NDescriptorFilter[0]));
            }
        }
        throw new NIllegalArgumentException(session, NMsg.ofCstyle("unsupported filter type: %s", type));
    }

    public <T extends NFilter> T none(NFilter[] others, NSession session) {
        return none(null, others, session);
    }

    public <T extends NFilter> T to(Class<T> toFilterInterface, NFilter filter, NSession session) {
        return (T) resolveNutsTypedFilters(toFilterInterface, session).from(filter);
    }

    public <T extends NFilter> T as(Class<T> toFilterInterface, NFilter filter, NSession session) {
        return (T) resolveNutsTypedFilters(toFilterInterface, session).as(filter);
    }

    public Class<? extends NFilter> detectType(NFilter nFilter, NSession session) {
        if (nFilter == null) {
            return null;
        }
        return detectType(nFilter.getClass(), session);
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

    public <T extends NFilter> Class<T> detectType(NFilter[] others, NSession session) {
        Class c = null;
        for (NFilter other : others) {
            if (other != null) {
                if (c == null) {
                    c = detectType(other.getClass(), session);
                } else {
                    c = detectType(c, other.getClass(), session);
                }
            }
        }
        if (c == null) {
            return null;
        }
        return c;
    }

    public <T extends NFilter> Class<T> detectType(Class<? extends NFilter> c1, NSession session) {
        if (c1 == null) {
            return null;
        }
        if (NVersionFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NVersionFilter.class;
        }
        if (NIdFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NIdFilter.class;
        }
        if (NDescriptorFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NDescriptorFilter.class;
        }
        if (NRepositoryFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NRepositoryFilter.class;
        }
        if (NDependencyFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NDependencyFilter.class;
        }
        if (NInstallStatusFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NInstallStatusFilter.class;
        }
        throw new NIllegalArgumentException(session, NMsg.ofCstyle("cannot detect filter type for %s", c1));
    }

    public <T extends NFilter> Class<T> detectType(Class<? extends NFilter> c1, Class<? extends NFilter> c2, NSession session) {
        if (NVersionFilter.class.isAssignableFrom(c1)) {
            if (NVersionFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NVersionFilter.class;
            }
            if (NIdFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NIdFilter.class;
            }
            if (NDescriptorFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NDescriptorFilter.class;
            }
            throw new NIllegalArgumentException(session, NMsg.ofCstyle("cannot detect common type for %s and %s", c1, c2));
        }
        if (NIdFilter.class.isAssignableFrom(c1)) {
            if (NVersionFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NIdFilter.class;
            }
            if (NIdFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NIdFilter.class;
            }
            if (NDescriptorFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NDescriptorFilter.class;
            }
            throw new NIllegalArgumentException(session, NMsg.ofCstyle("cannot detect common type for %s and %s",c1,c2));
        }
        if (NDescriptorFilter.class.isAssignableFrom(c1)) {
            if (NVersionFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NDescriptorFilter.class;
            }
            if (NIdFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NDescriptorFilter.class;
            }
            if (NDescriptorFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NDescriptorFilter.class;
            }
            throw new NIllegalArgumentException(session, NMsg.ofCstyle("cannot detect common type for %s and %s",c1,c2));
        }
        if (NDependencyFilter.class.isAssignableFrom(c1)) {
            if (NDependencyFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NDependencyFilter.class;
            }
            throw new NIllegalArgumentException(session, NMsg.ofCstyle("cannot detect common type for %s and %s",c1,c2));
        }
        if (NRepositoryFilter.class.isAssignableFrom(c1)) {
            if (NRepositoryFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NRepositoryFilter.class;
            }
            throw new NIllegalArgumentException(session, NMsg.ofCstyle("cannot detect common type for %s and %s",c1,c2));
        }
        if (NInstallStatusFilter.class.isAssignableFrom(c1)) {
            if (NInstallStatusFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NInstallStatusFilter.class;
            }
            throw new NIllegalArgumentException(session, NMsg.ofCstyle("cannot detect common type for %s and %s",c1,c2));
        }
        throw new NIllegalArgumentException(session, NMsg.ofCstyle("cannot detect common type for %s and %s",c1,c2));
    }

}
