package net.thevpc.nuts.runtime.core.filters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.dependency.InternalNutsDependencyFilterManager;
import net.thevpc.nuts.runtime.core.filters.dependency.NutsDependencyFilterNone;
import net.thevpc.nuts.runtime.core.filters.descriptor.InternalNutsDescriptorFilterManager;
import net.thevpc.nuts.runtime.core.filters.descriptor.NutsDescriptorFilterNone;
import net.thevpc.nuts.runtime.core.filters.id.InternalNutsIdFilterManager;
import net.thevpc.nuts.runtime.core.filters.id.NutsIdFilterNone;
import net.thevpc.nuts.runtime.core.filters.installstatus.InternalNutsInstallStatusFilterManager;
import net.thevpc.nuts.runtime.core.filters.repository.InternalNutsRepositoryFilterManager;
import net.thevpc.nuts.runtime.core.filters.repository.NutsRepositoryFilterNone;
import net.thevpc.nuts.runtime.core.filters.version.InternalNutsVersionFilterManager;
import net.thevpc.nuts.runtime.core.filters.version.NutsVersionFilterNone;

import java.util.*;
import java.util.function.Supplier;

public class DefaultNutsFilterModel {

    private NutsWorkspace workspace;
    private Map<String, Object> shared = new HashMap<String, Object>();

    public DefaultNutsFilterModel(NutsWorkspace ws) {
        this.workspace = ws;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public <T> T getShared(Class<T> clz, Supplier<T> s) {
        return (T) shared.computeIfAbsent(clz.getName(), (String t) -> s.get());
    }

    public <T extends NutsFilter> T nonnull(Class<T> type, NutsFilter filter, NutsSession session) {
        if (filter == null) {
            return always(type, session);
        }
        return filter.to(type);
    }

    public NutsTypedFilters resolveNutsTypedFilters(Class type, NutsSession session) {
        if (type == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to detected Filter type"));
        }
        switch (type.getName()) {
            case "net.thevpc.nuts.NutsDependencyFilter": {
                return dependency().setSession(session);
            }
            case "net.thevpc.nuts.NutsRepositoryFilter": {
                return repository().setSession(session);
            }
            case "net.thevpc.nuts.NutsIdFilter": {
                return id().setSession(session);
            }
            case "net.thevpc.nuts.NutsVersionFilter": {
                return version().setSession(session);
            }
            case "net.thevpc.nuts.NutsDescriptorFilter": {
                return descriptor().setSession(session);
            }
            case "net.thevpc.nuts.NutsInstallStatusFilter": {
                return installStatus().setSession(session);
            }
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported filter type: %s", type));
    }

    public <T extends NutsFilter> T always(Class<T> type, NutsSession session) {
        return (T) resolveNutsTypedFilters(type, session).always();
    }

    public <T extends NutsFilter> T never(Class<T> type, NutsSession session) {
        return (T) resolveNutsTypedFilters(type, session).never();
    }

    public <T extends NutsFilter> T all(Class<T> type, NutsFilter[] others, NutsSession session) {
        others = expandAll(others).toArray(new NutsFilter[0]);
        if (type == null || type.equals(NutsFilter.class)) {
            List<NutsFilter> all = new ArrayList<>();
            all.addAll(Arrays.asList(others));
            type = detectType(all.toArray(new NutsFilter[0]), session);
            if (type == null) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to detected Filter type"));
            }
        }
        return (T) resolveNutsTypedFilters(type, session).all(others);
    }

    public <T extends NutsFilter> T all(NutsFilter[] others, NutsSession session) {
        return all(null, others, session);
    }

    public <T extends NutsFilter> T any(Class<T> type, NutsFilter[] others, NutsSession session) {
        others = expandAny(others).toArray(new NutsFilter[0]);
        if (type == null || type.equals(NutsFilter.class)) {
            List<NutsFilter> all = new ArrayList<>();
            all.addAll(Arrays.asList(others));
            type = detectType(all.toArray(new NutsFilter[0]), session);
            if (type == null) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to detected Filter type"));
            }
        }
        return (T) resolveNutsTypedFilters(type, session).any(others);
    }

    public <T extends NutsFilter> T not(NutsFilter other, NutsSession session) {
        return not(null, other, session);
    }

    public <T extends NutsFilter> T not(Class<T> type, NutsFilter other, NutsSession session) {
        if (type == null || type.equals(NutsFilter.class)) {
            type = (Class<T>) detectType(other, session);
            if (type == null) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to detected Filter type"));
            }
        }
        return (T) resolveNutsTypedFilters(type, session).not(other);
    }

    public <T extends NutsFilter> T any(NutsFilter[] others, NutsSession session) {
        return any(null, others, session);
    }

    public <T extends NutsFilter> T none(Class<T> type, NutsFilter[] others, NutsSession session) {
        others = expandAll(others).toArray(new NutsFilter[0]);
        if (type == null || type.equals(NutsFilter.class)) {
            List<NutsFilter> all = new ArrayList<>();
            all.addAll(Arrays.asList(others));
            type = detectType(all.toArray(new NutsFilter[0]), session);
            if (type == null) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to detected Filter type"));
            }
        }
        switch (type.getName()) {
            case "net.thevpc.nuts.NutsDependencyFilter": {
                List<NutsDependencyFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsDependencyFilter a = dependency().from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type, session);
                }
                return (T) new NutsDependencyFilterNone(session, all.toArray(new NutsDependencyFilter[0]));
            }
            case "net.thevpc.nuts.NutsRepositoryFilter": {
                List<NutsRepositoryFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsRepositoryFilter a = repository().from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type, session);
                }
                return (T) new NutsRepositoryFilterNone(session, all.toArray(new NutsRepositoryFilter[0]));
            }
            case "net.thevpc.nuts.NutsIdFilter": {
                List<NutsIdFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsIdFilter a = id().from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type, session);
                }
                return (T) new NutsIdFilterNone(session, all.toArray(new NutsIdFilter[0]));
            }
            case "net.thevpc.nuts.NutsVersionFilter": {
                List<NutsVersionFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsVersionFilter a = version().from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type, session);
                }
                return (T) new NutsVersionFilterNone(session, all.toArray(new NutsVersionFilter[0]));
            }
            case "net.thevpc.nuts.NutsDescriptorFilter": {
                List<NutsDescriptorFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsDescriptorFilter a = descriptor().from(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type, session);
                }
                return (T) new NutsDescriptorFilterNone(session, all.toArray(new NutsDescriptorFilter[0]));
            }
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported filter type: %s", type));
    }

    public <T extends NutsFilter> T none(NutsFilter[] others, NutsSession session) {
        return none(null, others, session);
    }

    public <T extends NutsFilter> T to(Class<T> toFilterInterface, NutsFilter filter, NutsSession session) {
        return (T) resolveNutsTypedFilters(toFilterInterface, session).from(filter);
    }

    public <T extends NutsFilter> T as(Class<T> toFilterInterface, NutsFilter filter, NutsSession session) {
        return (T) resolveNutsTypedFilters(toFilterInterface, session).as(filter);
    }

    public Class<? extends NutsFilter> detectType(NutsFilter nutsFilter, NutsSession session) {
        if (nutsFilter == null) {
            return null;
        }
        return detectType(nutsFilter.getClass(), session);
    }

    public NutsIdFilterManager id() {
        return new InternalNutsIdFilterManager(this);
    }

    public NutsDependencyFilterManager dependency() {
        return new InternalNutsDependencyFilterManager(this);
    }

    public NutsRepositoryFilterManager repository() {
        return new InternalNutsRepositoryFilterManager(this);
    }

    public NutsVersionFilterManager version() {
        return new InternalNutsVersionFilterManager(this);
    }

    public NutsDescriptorFilterManager descriptor() {
        return new InternalNutsDescriptorFilterManager(this);
    }

    public NutsInstallStatusFilterManager installStatus() {
        return new InternalNutsInstallStatusFilterManager(this);
    }

    private Collection<NutsFilter> expandAny(NutsFilter... others) {
        List<NutsFilter> ok = new ArrayList<>();
        if (others != null) {
            for (NutsFilter other : others) {
                if (other != null) {
                    if (other.getFilterOp() == NutsFilterOp.OR) {
                        ok.addAll(Arrays.asList(other.getSubFilters()));
                    } else {
                        ok.addAll(Arrays.asList(other));
                    }
                }
            }
        }
        return ok;
    }

    private Collection<NutsFilter> expandAll(NutsFilter... others) {
        List<NutsFilter> ok = new ArrayList<>();
        if (others != null) {
            for (NutsFilter other : others) {
                if (other != null) {
                    if (other.getFilterOp() == NutsFilterOp.AND) {
                        ok.addAll(Arrays.asList(other.getSubFilters()));
                    } else {
                        ok.addAll(Arrays.asList(other));
                    }
                }
            }
        }
        return ok;
    }

    private Collection<NutsFilter> expandNone(NutsFilter... others) {
        List<NutsFilter> ok = new ArrayList<>();
        if (others != null) {
            for (NutsFilter other : others) {
                if (other != null) {
                    if (other.getFilterOp() == NutsFilterOp.NOT) {
                        ok.addAll(Arrays.asList(other.getSubFilters()));
                    } else {
                        ok.addAll(Arrays.asList(other));
                    }
                }
            }
        }
        return ok;
    }

    public <T extends NutsFilter> Class<T> detectType(NutsFilter[] others, NutsSession session) {
        Class c = null;
        for (NutsFilter other : others) {
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

    public <T extends NutsFilter> Class<T> detectType(Class<? extends NutsFilter> c1, NutsSession session) {
        if (c1 == null) {
            return null;
        }
        if (NutsVersionFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NutsVersionFilter.class;
        }
        if (NutsIdFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NutsIdFilter.class;
        }
        if (NutsDescriptorFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NutsDescriptorFilter.class;
        }
        if (NutsRepositoryFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NutsRepositoryFilter.class;
        }
        if (NutsDependencyFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NutsDependencyFilter.class;
        }
        if (NutsInstallStatusFilter.class.isAssignableFrom(c1)) {
            return (Class<T>) NutsInstallStatusFilter.class;
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cannot detect filter type for %s", c1));
    }

    public <T extends NutsFilter> Class<T> detectType(Class<? extends NutsFilter> c1, Class<? extends NutsFilter> c2, NutsSession session) {
        if (NutsVersionFilter.class.isAssignableFrom(c1)) {
            if (NutsVersionFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsVersionFilter.class;
            }
            if (NutsIdFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsIdFilter.class;
            }
            if (NutsDescriptorFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsDescriptorFilter.class;
            }
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cannot detect common type for %s and %s", c1, c2));
        }
        if (NutsIdFilter.class.isAssignableFrom(c1)) {
            if (NutsVersionFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsIdFilter.class;
            }
            if (NutsIdFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsIdFilter.class;
            }
            if (NutsDescriptorFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsDescriptorFilter.class;
            }
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cannot detect common type for %s and %s",c1,c2));
        }
        if (NutsDescriptorFilter.class.isAssignableFrom(c1)) {
            if (NutsVersionFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsDescriptorFilter.class;
            }
            if (NutsIdFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsDescriptorFilter.class;
            }
            if (NutsDescriptorFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsDescriptorFilter.class;
            }
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cannot detect common type for %s and %s",c1,c2));
        }
        if (NutsDependencyFilter.class.isAssignableFrom(c1)) {
            if (NutsDependencyFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsDependencyFilter.class;
            }
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cannot detect common type for %s and %s",c1,c2));
        }
        if (NutsRepositoryFilter.class.isAssignableFrom(c1)) {
            if (NutsRepositoryFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsRepositoryFilter.class;
            }
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cannot detect common type for %s and %s",c1,c2));
        }
        if (NutsInstallStatusFilter.class.isAssignableFrom(c1)) {
            if (NutsInstallStatusFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsInstallStatusFilter.class;
            }
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cannot detect common type for %s and %s",c1,c2));
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cannot detect common type for %s and %s",c1,c2));
    }

}
