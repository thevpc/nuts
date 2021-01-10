package net.thevpc.nuts.runtime.core.filters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.dependency.*;
import net.thevpc.nuts.runtime.core.filters.descriptor.*;
import net.thevpc.nuts.runtime.core.filters.id.*;
import net.thevpc.nuts.runtime.core.filters.installstatus.InternalNutsInstallStatusFilterManager;
import net.thevpc.nuts.runtime.core.filters.repository.*;
import net.thevpc.nuts.runtime.core.filters.version.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DefaultNutsFilterManager implements NutsFilterManager {
    public NutsWorkspace ws;
    private final NutsIdFilterManager id;
    private final NutsDependencyFilterManager dependency;
    private final NutsRepositoryFilterManager repository;
    private final NutsVersionFilterManager version;
    private final NutsDescriptorFilterManager descriptor;
    private NutsInstallStatusFilterManager installStatus;

    public DefaultNutsFilterManager(NutsWorkspace ws) {
        this.ws = ws;
        id = new InternalNutsIdFilterManager(this);
        dependency = new InternalNutsDependencyFilterManager(this);
        repository = new InternalNutsRepositoryFilterManager(this);
        version = new InternalNutsVersionFilterManager(this);
        descriptor = new InternalNutsDescriptorFilterManager(this);
        installStatus = new InternalNutsInstallStatusFilterManager(this);
    }

    @Override
    public <T extends NutsFilter> T nonnull(Class<T> type, NutsFilter filter) {
        if (filter == null) {
            return always(type);
        }
        return filter.to(type);
    }

    public NutsTypedFilters resolveNutsTypedFilters(Class type) {
        if (type == null) {
            throw new NutsIllegalArgumentException(ws, "unable to detected Filter type");
        }
        switch (type.getName()) {
            case "net.thevpc.nuts.NutsDependencyFilter": {
                return dependency();
            }
            case "net.thevpc.nuts.NutsRepositoryFilter": {
                return repository();
            }
            case "net.thevpc.nuts.NutsIdFilter": {
                return id();
            }
            case "net.thevpc.nuts.NutsVersionFilter": {
                return version();
            }
            case "net.thevpc.nuts.NutsDescriptorFilter": {
                return descriptor();
            }
            case "net.thevpc.nuts.NutsInstallStatusFilter": {
                return installStatus();
            }
        }
        throw new NutsIllegalArgumentException(ws, "unsupported filter type: " + type);
    }

    public <T extends NutsFilter> T always(Class<T> type) {
        return (T)resolveNutsTypedFilters(type).always();
    }

    @Override
    public <T extends NutsFilter> T never(Class<T> type) {
        return (T)resolveNutsTypedFilters(type).never();
    }

    @Override
    public <T extends NutsFilter> T all(Class<T> type, NutsFilter... others) {
        others = expandAll(others).toArray(new NutsFilter[0]);
        if (type == null || type.equals(NutsFilter.class)) {
            List<NutsFilter> all = new ArrayList<>();
            all.addAll(Arrays.asList(others));
            type = detectType(all.toArray(new NutsFilter[0]));
            if (type == null) {
                throw new NutsIllegalArgumentException(ws, "unable to detected Filter type");
            }
        }
        return (T) resolveNutsTypedFilters(type).all(others);
    }

    @Override
    public <T extends NutsFilter> T all(NutsFilter... others) {
        return all(null, others);
    }

    @Override
    public <T extends NutsFilter> T any(Class<T> type, NutsFilter... others) {
        others = expandAny(others).toArray(new NutsFilter[0]);
        if (type == null || type.equals(NutsFilter.class)) {
            List<NutsFilter> all = new ArrayList<>();
            all.addAll(Arrays.asList(others));
            type = detectType(all.toArray(new NutsFilter[0]));
            if (type == null) {
                throw new NutsIllegalArgumentException(ws, "unable to detected Filter type");
            }
        }
        return (T) resolveNutsTypedFilters(type).any(others);
    }

    @Override
    public <T extends NutsFilter> T not(NutsFilter other) {
        return not(null, other);
    }

    @Override
    public <T extends NutsFilter> T not(Class<T> type, NutsFilter other) {
        if (type == null || type.equals(NutsFilter.class)) {
            type = (Class<T>) detectType(other);
            if (type == null) {
                throw new NutsIllegalArgumentException(ws, "unable to detected Filter type");
            }
        }
        return (T) resolveNutsTypedFilters(type).not(other);
    }

    @Override
    public <T extends NutsFilter> T any(NutsFilter... others) {
        return any(null, others);
    }

    @Override
    public <T extends NutsFilter> T none(Class<T> type, NutsFilter... others) {
        others = expandAll(others).toArray(new NutsFilter[0]);
        if (type == null || type.equals(NutsFilter.class)) {
            List<NutsFilter> all = new ArrayList<>();
            all.addAll(Arrays.asList(others));
            type = detectType(all.toArray(new NutsFilter[0]));
            if (type == null) {
                throw new NutsIllegalArgumentException(ws, "unable to detected Filter type");
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
                    return (T) always(type);
                }
                return (T) new NutsDependencyFilterNone(ws, all.toArray(new NutsDependencyFilter[0]));
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
                    return (T) always(type);
                }
                return (T) new NutsRepositoryFilterNone(ws, all.toArray(new NutsRepositoryFilter[0]));
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
                    return (T) always(type);
                }
                return (T) new NutsIdFilterNone(ws, all.toArray(new NutsIdFilter[0]));
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
                    return (T) always(type);
                }
                return (T) new NutsVersionFilterNone(ws, all.toArray(new NutsVersionFilter[0]));
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
                    return (T) always(type);
                }
                return (T) new NutsDescriptorFilterNone(ws, all.toArray(new NutsDescriptorFilter[0]));
            }
        }
        throw new NutsIllegalArgumentException(ws, "unsupported filter type: " + type);
    }

    @Override
    public <T extends NutsFilter> T none(NutsFilter... others) {
        return none(null, others);
    }

    @Override
    public <T extends NutsFilter> T to(Class<T> toFilterInterface, NutsFilter filter) {
        return (T) resolveNutsTypedFilters(toFilterInterface).from(filter);
    }

    public <T extends NutsFilter> T as(Class<T> toFilterInterface, NutsFilter filter) {
        return (T) resolveNutsTypedFilters(toFilterInterface).as(filter);
    }

    @Override
    public Class<? extends NutsFilter> detectType(NutsFilter nutsFilter) {
        if (nutsFilter == null) {
            return null;
        }
        return detectType(nutsFilter.getClass());
    }

    @Override
    public NutsIdFilterManager id() {
        return id;
    }

    @Override
    public NutsDependencyFilterManager dependency() {
        return dependency;
    }

    @Override
    public NutsRepositoryFilterManager repository() {
        return repository;
    }

    @Override
    public NutsVersionFilterManager version() {
        return version;
    }

    @Override
    public NutsDescriptorFilterManager descriptor() {
        return descriptor;
    }

    @Override
    public NutsInstallStatusFilterManager installStatus() {
        return installStatus;
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

    public <T extends NutsFilter> Class<T> detectType(NutsFilter... others) {
        Class c = null;
        for (NutsFilter other : others) {
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

    public <T extends NutsFilter> Class<T> detectType(Class<? extends NutsFilter> c1) {
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
        throw new NutsIllegalArgumentException(ws, "cannot detect filter type for " + c1);
    }

    public <T extends NutsFilter> Class<T> detectType(Class<? extends NutsFilter> c1, Class<? extends NutsFilter> c2) {
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
            throw new NutsIllegalArgumentException(ws, "cannot detect common type for " + c1 + " and " + c2);
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
            throw new NutsIllegalArgumentException(ws, "cannot detect common type for " + c1 + " and " + c2);
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
            throw new NutsIllegalArgumentException(ws, "cannot detect common type for " + c1 + " and " + c2);
        }
        if (NutsDependencyFilter.class.isAssignableFrom(c1)) {
            if (NutsDependencyFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsDependencyFilter.class;
            }
            throw new NutsIllegalArgumentException(ws, "cannot detect common type for " + c1 + " and " + c2);
        }
        if (NutsRepositoryFilter.class.isAssignableFrom(c1)) {
            if (NutsRepositoryFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsRepositoryFilter.class;
            }
            throw new NutsIllegalArgumentException(ws, "cannot detect common type for " + c1 + " and " + c2);
        }
        if (NutsInstallStatusFilter.class.isAssignableFrom(c1)) {
            if (NutsInstallStatusFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsInstallStatusFilter.class;
            }
            throw new NutsIllegalArgumentException(ws, "cannot detect common type for " + c1 + " and " + c2);
        }
        throw new NutsIllegalArgumentException(ws, "cannot detect common type for " + c1 + " and " + c2);
    }


}
