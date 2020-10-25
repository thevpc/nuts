package net.vpc.app.nuts.runtime.filters;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.NutsPatternIdFilter;
import net.vpc.app.nuts.runtime.filters.dependency.*;
import net.vpc.app.nuts.runtime.filters.descriptor.*;
import net.vpc.app.nuts.runtime.filters.id.*;
import net.vpc.app.nuts.runtime.filters.repository.*;
import net.vpc.app.nuts.runtime.filters.version.*;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.io.NutsInstallStatusIdFilter;

import java.util.*;

public class DefaultNutsFilterManager implements NutsFilterManager {
    private NutsWorkspace ws;
    private NutsIdFilterManager id = new InternalNutsIdFilterManager();
    private NutsDependencyFilterManager dependency = new InternalNutsDependencyFilterManager();
    private NutsRepositoryFilterManager repository = new InternalNutsRepositoryFilterManager();
    private NutsVersionFilterManager version = new InternalNutsVersionFilterManager();
    private NutsDescriptorFilterManager descriptor = new InternalNutsDescriptorFilterManager();
    private NutsDependencyFilterTrue nutsDependencyFilterTrue;
    private NutsRepositoryFilterTrue nutsRepositoryFilterTrue;
    private NutsIdFilterTrue nutsIdFilterTrue;
    private NutsVersionFilterTrue nutsVersionFilterTrue;
    private NutsDescriptorFilterTrue nutsDescriptorFilterTrue;
    private NutsDependencyFilterFalse nutsDependencyFilterFalse;
    private NutsRepositoryFilterFalse nutsRepositoryFilterFalse;
    private NutsIdFilterFalse nutsIdFilterFalse;
    private NutsVersionFilterFalse nutsVersionFilterFalse;
    private NutsDescriptorFilterFalse nutsDescriptorFilterFalse;

    public DefaultNutsFilterManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public <T extends NutsFilter> T nonnull(Class<T> type, NutsFilter filter) {
        if (filter == null) {
            return always(type);
        }
        return filter.to(type);
    }

    @Override
    public <T extends NutsFilter> T always(Class<T> type) {
        if (type == null) {
            throw new NutsIllegalArgumentException(ws, "Unable to detected Filter type");
        }
        switch (type.getName()) {
            case "net.vpc.app.nuts.NutsDependencyFilter": {
                if (nutsDependencyFilterTrue == null) {
                    nutsDependencyFilterTrue = new NutsDependencyFilterTrue(ws);
                }
                return (T) nutsDependencyFilterTrue;
            }
            case "net.vpc.app.nuts.NutsRepositoryFilter": {
                if (nutsRepositoryFilterTrue == null) {
                    nutsRepositoryFilterTrue = new NutsRepositoryFilterTrue(ws);
                }
                return (T) nutsRepositoryFilterTrue;
            }
            case "net.vpc.app.nuts.NutsIdFilter": {
                if (nutsIdFilterTrue == null) {
                    nutsIdFilterTrue = new NutsIdFilterTrue(ws);
                }
                return (T) nutsIdFilterTrue;
            }
            case "net.vpc.app.nuts.NutsVersionFilter": {
                if (nutsVersionFilterTrue == null) {
                    nutsVersionFilterTrue = new NutsVersionFilterTrue(ws);
                }
                return (T) nutsVersionFilterTrue;
            }
            case "net.vpc.app.nuts.NutsDescriptorFilter": {
                if (nutsDescriptorFilterTrue == null) {
                    nutsDescriptorFilterTrue = new NutsDescriptorFilterTrue(ws);
                }
                return (T) nutsDescriptorFilterTrue;
            }
        }
        throw new NutsIllegalArgumentException(ws, "Unsupported Filter type: " + type);
    }

    @Override
    public <T extends NutsFilter> T never(Class<T> type) {
        if (type == null) {
            throw new NutsIllegalArgumentException(ws, "Unable to detected Filter type");
        }
        switch (type.getName()) {
            case "net.vpc.app.nuts.NutsDependencyFilter": {
                if (nutsDependencyFilterFalse == null) {
                    nutsDependencyFilterFalse = new NutsDependencyFilterFalse(ws);
                }
                return (T) nutsDependencyFilterFalse;
            }
            case "net.vpc.app.nuts.NutsRepositoryFilter": {
                if (nutsRepositoryFilterFalse == null) {
                    nutsRepositoryFilterFalse = new NutsRepositoryFilterFalse(ws);
                }
                return (T) nutsRepositoryFilterFalse;
            }
            case "net.vpc.app.nuts.NutsIdFilter": {
                if (nutsIdFilterFalse == null) {
                    nutsIdFilterFalse = new NutsIdFilterFalse(ws);
                }
                return (T) nutsIdFilterFalse;
            }
            case "net.vpc.app.nuts.NutsVersionFilter": {
                if (nutsVersionFilterFalse == null) {
                    nutsVersionFilterFalse = new NutsVersionFilterFalse(ws);
                }
                return (T) nutsVersionFilterFalse;
            }
            case "net.vpc.app.nuts.NutsDescriptorFilter": {
                if (nutsDescriptorFilterFalse == null) {
                    nutsDescriptorFilterFalse = new NutsDescriptorFilterFalse(ws);
                }
                return (T) nutsDescriptorFilterFalse;
            }
        }
        throw new NutsIllegalArgumentException(ws, "Unsupported Filter type: " + type);
    }

    @Override
    public <T extends NutsFilter> T all(Class<T> type, NutsFilter... others) {
        others = expandAll(others).toArray(new NutsFilter[0]);
        if (type == null || type.equals(NutsFilter.class)) {
            List<NutsFilter> all = new ArrayList<>();
            all.addAll(Arrays.asList(others));
            type = detectType(all.toArray(new NutsFilter[0]));
            if (type == null) {
                throw new NutsIllegalArgumentException(ws, "Unable to detected Filter type");
            }
        }
        switch (type.getName()) {
            case "net.vpc.app.nuts.NutsDependencyFilter": {
                List<NutsDependencyFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsDependencyFilter a = toDependencyFilter(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                if (all.size() == 1) {
                    return (T) all.get(0);
                }
                return (T) new NutsDependencyFilterAnd(ws, all.toArray(new NutsDependencyFilter[0]));
            }
            case "net.vpc.app.nuts.NutsRepositoryFilter": {
                List<NutsRepositoryFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsRepositoryFilter a = toRepositoryFilter(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                if (all.size() == 1) {
                    return (T) all.get(0);
                }
                return (T) new NutsRepositoryFilterAnd(ws, all.toArray(new NutsRepositoryFilter[0]));
            }
            case "net.vpc.app.nuts.NutsIdFilter": {
                List<NutsIdFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsIdFilter a = toIdFilter(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                if (all.size() == 1) {
                    return (T) all.get(0);
                }
                return (T) new NutsIdFilterAnd(ws, all.toArray(new NutsIdFilter[0]));
            }
            case "net.vpc.app.nuts.NutsVersionFilter": {
                List<NutsVersionFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsVersionFilter a = toVersionFilter(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                if (all.size() == 1) {
                    return (T) all.get(0);
                }
                return (T) new NutsVersionFilterAnd(ws, all.toArray(new NutsVersionFilter[0]));
            }
            case "net.vpc.app.nuts.NutsDescriptorFilter": {
                List<NutsDescriptorFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsDescriptorFilter a = toDescriptorFilter(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                if (all.size() == 1) {
                    return (T) all.get(0);
                }
                return (T) new NutsDescriptorFilterAnd(ws, all.toArray(new NutsDescriptorFilter[0]));
            }
        }
        throw new NutsIllegalArgumentException(ws, "Unsupported Filter type: " + type);
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
                throw new NutsIllegalArgumentException(ws, "Unable to detected Filter type");
            }
        }
        switch (type.getName()) {
            case "net.vpc.app.nuts.NutsDependencyFilter": {
                List<NutsDependencyFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsDependencyFilter a = toDependencyFilter(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                if (all.size() == 1) {
                    return (T) all.get(0);
                }
                return (T) new NutsDependencyFilterOr(ws, all.toArray(new NutsDependencyFilter[0]));
            }
            case "net.vpc.app.nuts.NutsRepositoryFilter": {
                List<NutsRepositoryFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsRepositoryFilter a = toRepositoryFilter(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                if (all.size() == 1) {
                    return (T) all.get(0);
                }
                return (T) new NutsRepositoryFilterOr(ws, all.toArray(new NutsRepositoryFilter[0]));
            }
            case "net.vpc.app.nuts.NutsIdFilter": {
                List<NutsIdFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsIdFilter a = toIdFilter(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                if (all.size() == 1) {
                    return (T) all.get(0);
                }
                return (T) new NutsIdFilterOr(ws, all.toArray(new NutsIdFilter[0]));
            }
            case "net.vpc.app.nuts.NutsVersionFilter": {
                List<NutsVersionFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsVersionFilter a = toVersionFilter(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                if (all.size() == 1) {
                    return (T) all.get(0);
                }
                return (T) new NutsVersionFilterOr(ws, all.toArray(new NutsVersionFilter[0]));
            }
            case "net.vpc.app.nuts.NutsDescriptorFilter": {
                List<NutsDescriptorFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsDescriptorFilter a = toDescriptorFilter(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                if (all.size() == 1) {
                    return (T) all.get(0);
                }
                return (T) new NutsDescriptorFilterOr(ws, all.toArray(new NutsDescriptorFilter[0]));
            }
        }
        throw new NutsIllegalArgumentException(ws, "Unsupported Filter type: " + type);
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
                throw new NutsIllegalArgumentException(ws, "Unable to detected Filter type");
            }
        }
        switch (type.getName()) {
            case "net.vpc.app.nuts.NutsDependencyFilter": {
                return (T) new NutsDependencyFilterNone(ws, (NutsDependencyFilter) other);
            }
            case "net.vpc.app.nuts.NutsRepositoryFilter": {
                return (T) new NutsRepositoryFilterNone(ws, (NutsRepositoryFilter) other);
            }
            case "net.vpc.app.nuts.NutsIdFilter": {
                return (T) new NutsIdFilterNone(ws, (NutsIdFilter) other);
            }
            case "net.vpc.app.nuts.NutsVersionFilter": {
                return (T) new NutsVersionFilterNone(ws, (NutsVersionFilter) other);
            }
            case "net.vpc.app.nuts.NutsDescriptorFilter": {
                return (T) new NutsDescriptorFilterNone(ws, (NutsDescriptorFilter) other);
            }
        }
        throw new NutsIllegalArgumentException(ws, "Unsupported Filter type: " + type);
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
                throw new NutsIllegalArgumentException(ws, "Unable to detected Filter type");
            }
        }
        switch (type.getName()) {
            case "net.vpc.app.nuts.NutsDependencyFilter": {
                List<NutsDependencyFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsDependencyFilter a = toDependencyFilter(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                return (T) new NutsDependencyFilterNone(ws, all.toArray(new NutsDependencyFilter[0]));
            }
            case "net.vpc.app.nuts.NutsRepositoryFilter": {
                List<NutsRepositoryFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsRepositoryFilter a = toRepositoryFilter(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                return (T) new NutsRepositoryFilterNone(ws, all.toArray(new NutsRepositoryFilter[0]));
            }
            case "net.vpc.app.nuts.NutsIdFilter": {
                List<NutsIdFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsIdFilter a = toIdFilter(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                return (T) new NutsIdFilterNone(ws, all.toArray(new NutsIdFilter[0]));
            }
            case "net.vpc.app.nuts.NutsVersionFilter": {
                List<NutsVersionFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsVersionFilter a = toVersionFilter(other);
                    if (a != null) {
                        all.add(a);
                    }
                }
                if (all.isEmpty()) {
                    return (T) always(type);
                }
                return (T) new NutsVersionFilterNone(ws, all.toArray(new NutsVersionFilter[0]));
            }
            case "net.vpc.app.nuts.NutsDescriptorFilter": {
                List<NutsDescriptorFilter> all = new ArrayList<>();
                for (NutsFilter other : others) {
                    NutsDescriptorFilter a = toDescriptorFilter(other);
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
        throw new NutsIllegalArgumentException(ws, "Unsupported Filter type: " + type);
    }

    @Override
    public <T extends NutsFilter> T none(NutsFilter... others) {
        return none(null, others);
    }

    @Override
    public <T extends NutsFilter> T to(Class<T> toFilterInterface, NutsFilter filter) {
        switch (toFilterInterface.getName()) {
            case "net.vpc.app.nuts.NutsDependencyFilter": {
                return (T) toDependencyFilter(filter);
            }
            case "net.vpc.app.nuts.NutsRepositoryFilter": {
                return (T) toRepositoryFilter(filter);
            }
            case "net.vpc.app.nuts.NutsIdFilter": {
                return (T) toIdFilter(filter);
            }
            case "net.vpc.app.nuts.NutsVersionFilter": {
                return (T) toVersionFilter(filter);
            }
            case "net.vpc.app.nuts.NutsDescriptorFilter": {
                return (T) toDescriptorFilter(filter);
            }
        }
        throw new NutsIllegalArgumentException(ws, "Unsupported Filter type: " + toFilterInterface);
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

    public <T extends NutsFilter> T as(Class<T> toFilterInterface, NutsFilter filter) {
        switch (toFilterInterface.getName()) {
            case "net.vpc.app.nuts.NutsDependencyFilter": {
                return (T) asDependencyFilter(filter);
            }
            case "net.vpc.app.nuts.NutsRepositoryFilter": {
                return (T) asRepositoryFilter(filter);
            }
            case "net.vpc.app.nuts.NutsIdFilter": {
                return (T) asIdFilter(filter);
            }
            case "net.vpc.app.nuts.NutsVersionFilter": {
                return (T) asVersionFilter(filter);
            }
            case "net.vpc.app.nuts.NutsDescriptorFilter": {
                return (T) asDescriptorFilter(filter);
            }
        }
        throw new NutsIllegalArgumentException(ws, "Unsupported Filter type: " + toFilterInterface);
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
        throw new NutsIllegalArgumentException(ws, "Cannot detect filter type for " + c1);
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
            throw new NutsIllegalArgumentException(ws, "Cannot detect common type for " + c1 + " and " + c2);
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
            throw new NutsIllegalArgumentException(ws, "Cannot detect common type for " + c1 + " and " + c2);
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
            throw new NutsIllegalArgumentException(ws, "Cannot detect common type for " + c1 + " and " + c2);
        }
        if (NutsDependencyFilter.class.isAssignableFrom(c1)) {
            if (NutsDependencyFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsDependencyFilter.class;
            }
            throw new NutsIllegalArgumentException(ws, "Cannot detect common type for " + c1 + " and " + c2);
        }
        if (NutsRepositoryFilter.class.isAssignableFrom(c1)) {
            if (NutsRepositoryFilter.class.isAssignableFrom(c2)) {
                return (Class<T>) NutsRepositoryFilter.class;
            }
            throw new NutsIllegalArgumentException(ws, "Cannot detect common type for " + c1 + " and " + c2);
        }
        throw new NutsIllegalArgumentException(ws, "Cannot detect common type for " + c1 + " and " + c2);
    }

    public NutsRepositoryFilter asRepositoryFilter(NutsFilter a) {
        if (a instanceof NutsRepositoryFilter) {
            return (NutsRepositoryFilter) a;
        }
        return null;
    }

    public NutsDependencyFilter asDependencyFilter(NutsFilter a) {
        if (a instanceof NutsDependencyFilter) {
            return (NutsDependencyFilter) a;
        }
        return null;
    }

    public NutsIdFilter asIdFilter(NutsFilter a) {
        if (a instanceof NutsIdFilter) {
            return (NutsIdFilter) a;
        }
        if (a instanceof NutsDescriptorFilter) {
            return new NutsDescriptorIdFilter((NutsDescriptorFilter) a);
        }
        if (a instanceof NutsVersionFilter) {
            return new NutstVersionIdFilter((NutsVersionFilter) a);
        }
        return null;
    }

    public NutsVersionFilter asVersionFilter(NutsFilter a) {
        if (a instanceof NutsVersionFilter) {
            return (NutsVersionFilter) a;
        }
        return null;
    }

    public NutsDescriptorFilter asDescriptorFilter(NutsFilter a) {
        if (a instanceof NutsDescriptorFilter) {
            return (NutsDescriptorFilter) a;
        }
        if (a instanceof NutsIdFilter) {
            return new NutsDescriptorFilterById((NutsIdFilter) a);
        }
        return null;
    }

    public NutsRepositoryFilter toRepositoryFilter(NutsFilter a) {
        if (a == null) {
            return null;
        }
        NutsRepositoryFilter t = asRepositoryFilter(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(ws, "Not a RepositoryFilter");
        }
        return t;
    }

    public NutsDependencyFilter toDependencyFilter(NutsFilter a) {
        if (a == null) {
            return null;
        }
        NutsDependencyFilter t = asDependencyFilter(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(ws, "Not a NutsDependencyFilter");
        }
        return t;
    }

    public NutsIdFilter toIdFilter(NutsFilter a) {
        if (a == null) {
            return null;
        }
        NutsIdFilter t = asIdFilter(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(ws, "Not a IdFilter");
        }
        return t;
    }

    public NutsVersionFilter toVersionFilter(NutsFilter a) {
        if (a == null) {
            return null;
        }
        NutsVersionFilter t = asVersionFilter(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(ws, "Not a VersionFilter");
        }
        return t;
    }

    public NutsDescriptorFilter toDescriptorFilter(NutsFilter a) {
        if (a == null) {
            return null;
        }
        NutsDescriptorFilter t = asDescriptorFilter(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(ws, "Not a DescriptorFilter");
        }
        return t;
    }

    private class InternalNutsIdFilterManager extends InternalNutsTypedFilters<NutsIdFilter> implements NutsIdFilterManager {
        public InternalNutsIdFilterManager() {
            super(NutsIdFilter.class);
        }

        @Override
        public NutsIdFilter byExpression(String expression) {
            if (CoreStringUtils.isBlank(expression)) {
                return always();
            }
            return NutsJavascriptIdFilter.valueOf(expression, ws);
        }

        @Override
        public NutsIdFilter byDefaultVersion(Boolean defaultVersion) {
            if (defaultVersion == null) {
                return always();
            }
            return new NutsDefaultVersionIdFilter(ws, defaultVersion);
        }

        @Override
        public NutsIdFilter byInstallStatus(NutsInstallStatus... installStatus) {
            return new NutsInstallStatusIdFilter(ws, installStatus == null ? null : new Set[]{EnumSet.copyOf(Arrays.asList(installStatus))});
        }

        @Override
        public NutsIdFilter byInstallStatus(Set<NutsInstallStatus>... installStatus) {
            return new NutsInstallStatusIdFilter(ws, installStatus);
        }

        @Override
        public NutsIdFilter byName(String... names) {
            if (names == null || names.length == 0) {
                return always();
            }
            NutsIdFilter f = null;
            for (String wildcardId : names) {
                if (f == null) {
                    f = new NutsPatternIdFilter(ws, ws.id().parser().parse(wildcardId));
                } else {
                    f = (NutsIdFilter) f.or(new NutsPatternIdFilter(ws, ws.id().parser().parse(wildcardId)));
                }
            }
            return f;
        }
    }

    private class InternalNutsDescriptorFilterManager extends InternalNutsTypedFilters<NutsDescriptorFilter> implements NutsDescriptorFilterManager {
        public InternalNutsDescriptorFilterManager() {
            super(NutsDescriptorFilter.class);
        }

        @Override
        public NutsDescriptorFilter byExpression(String expression) {
            if (CoreStringUtils.isBlank(expression)) {
                return always();
            }
            return NutsDescriptorJavascriptFilter.valueOf(expression, ws);
        }

        @Override
        public NutsDescriptorFilter byPackaging(String... values) {
            if (values == null || values.length == 0) {
                return always();
            }
            List<NutsDescriptorFilter> packs = new ArrayList<>();
            for (String v : values) {
                packs.add(new NutsDescriptorFilterPackaging(ws, v));
            }
            if (packs.size() == 1) {
                return packs.get(0);
            }
            return all(packs.toArray(new NutsDescriptorFilter[0]));
        }

        @Override
        public NutsDescriptorFilter byArch(String... values) {
            if (values == null || values.length == 0) {
                return always();
            }
            List<NutsDescriptorFilter> packs = new ArrayList<>();
            for (String v : values) {
                packs.add(new NutsDescriptorFilterArch(ws, v));
            }
            if (packs.size() == 1) {
                return packs.get(0);
            }
            return all(packs.toArray(new NutsDescriptorFilter[0]));
        }

        @Override
        public NutsDescriptorFilter byOsdist(String... values) {
            if (values == null || values.length == 0) {
                return always();
            }
            List<NutsDescriptorFilter> packs = new ArrayList<>();
            for (String v : values) {
                packs.add(new NutsDescriptorFilterOsdist(ws, v));
            }
            if (packs.size() == 1) {
                return packs.get(0);
            }
            return all(packs.toArray(new NutsDescriptorFilter[0]));
        }

        @Override
        public NutsDescriptorFilter byPlatform(String... values) {
            if (values == null || values.length == 0) {
                return always();
            }
            List<NutsDescriptorFilter> packs = new ArrayList<>();
            for (String v : values) {
                packs.add(new NutsDescriptorFilterPlatform(ws, v));
            }
            if (packs.size() == 1) {
                return packs.get(0);
            }
            return all(packs.toArray(new NutsDescriptorFilter[0]));
        }

        @Override
        public NutsDescriptorFilter byExec(Boolean value) {
            if (value == null) {
                return always();
            }
            return new NutsExecStatusIdFilter(ws, value, null);
        }

        @Override
        public NutsDescriptorFilter byApp(Boolean value) {
            if (value == null) {
                return always();
            }
            return new NutsExecStatusIdFilter(ws, null, value);
        }

        @Override
        public NutsDescriptorFilter byExtension(String targetApiVersion) {
            return new NutsExecExtensionFilter(ws,
                    targetApiVersion == null ? null : ws.id().parser().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build()
            );
        }

        @Override
        public NutsDescriptorFilter byRuntime(String targetApiVersion) {
            return new NutsExecRuntimeFilter(ws,
                    targetApiVersion == null ? null : ws.id().parser().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build(),
                    false
            );
        }

        @Override
        public NutsDescriptorFilter byCompanion(String targetApiVersion) {
            return new NutsExecCompanionFilter(ws,
                    targetApiVersion == null ? null : ws.id().parser().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(targetApiVersion).build(),
                    ws.companionIds().toArray(new String[0])
            );
        }

        @Override
        public NutsDescriptorFilter byApiVersion(String apiVersion) {
            if (apiVersion == null) {
                apiVersion = ws.getApiVersion();
            }
            return new BootAPINutsDescriptorFilter(
                    ws,
                    ws.id().parser().parse(NutsConstants.Ids.NUTS_API).builder().setVersion(apiVersion).build().getVersion()
            );
        }

        @Override
        public NutsDescriptorFilter byLockedIds(String... ids) {
            return new NutsLockedIdExtensionFilter(ws,
                    Arrays.stream(ids).map(x -> ws.id().parser().setLenient(false).parse(x)).toArray(NutsId[]::new)
            );
        }
    }

    private class InternalNutsVersionFilterManager extends InternalNutsTypedFilters<NutsVersionFilter> implements NutsVersionFilterManager {
        public InternalNutsVersionFilterManager() {
            super(NutsVersionFilter.class);
        }

        public NutsVersionFilter byValue(String version) {
            return DefaultNutsVersionFilter.parse(version, ws);
        }

        @Override
        public NutsVersionFilter byExpression(String expression) {
            if (CoreStringUtils.isBlank(expression)) {
                return always();
            }
            return NutsVersionJavascriptFilter.valueOf(expression, ws);
        }
    }

    private class InternalNutsDependencyFilterManager extends InternalNutsTypedFilters<NutsDependencyFilter> implements NutsDependencyFilterManager {
        public InternalNutsDependencyFilterManager() {
            super(NutsDependencyFilter.class);
        }

        @Override
        public NutsDependencyFilter byScope(NutsDependencyScopePattern scope) {
            if (scope == null) {
                return always();
            }
            return new ScopeNutsDependencyFilter(ws, scope);
        }

        @Override
        public NutsDependencyFilter byScope(NutsDependencyScope scope) {
            if (scope == null) {
                return always();
            }
            return new NutsDependencyScopeFilter(ws).addScopes(Arrays.asList(scope));
        }

        @Override
        public NutsDependencyFilter byScope(NutsDependencyScope... scope) {
            if (scope == null) {
                return always();
            }
            return new NutsDependencyScopeFilter(ws).addScopes(Arrays.asList(scope));
        }

        @Override
        public NutsDependencyFilter byScope(Collection<NutsDependencyScope> scope) {
            if (scope == null) {
                return always();
            }
            return new NutsDependencyScopeFilter(ws).addScopes(scope);
        }

        @Override
        public NutsDependencyFilter byOptional(Boolean optional) {
            if (optional == null) {
                return always();
            }
            return new NutsDependencyOptionFilter(ws, optional);
        }

        @Override
        public NutsDependencyFilter byExpression(String expression) {
            if (CoreStringUtils.isBlank(expression)) {
                return always();
            }
            return NutsDependencyJavascriptFilter.valueOf(expression, ws);
        }

        @Override
        public NutsDependencyFilter byExclude(NutsDependencyFilter filter, String[] exclusions) {
            return new NutsExclusionDependencyFilter(ws, filter, Arrays.stream(exclusions).map(x -> ws.id().parser().setLenient(false).parse(x)).toArray(NutsId[]::new));
        }
    }


    private class InternalNutsRepositoryFilterManager extends InternalNutsTypedFilters<NutsRepositoryFilter> implements NutsRepositoryFilterManager {
        public InternalNutsRepositoryFilterManager() {
            super(NutsRepositoryFilter.class);
        }

        @Override
        public NutsRepositoryFilter byName(String[] names) {
            if (names == null || names.length == 0) {
                return always();
            }
            return new DefaultNutsRepositoryFilter(ws, Arrays.asList(names));
        }

        @Override
        public NutsRepositoryFilter byUuid(String... uuids) {
            if (uuids == null || uuids.length == 0) {
                return always();
            }
            //TODO should create another class for uuids!
            return new DefaultNutsRepositoryFilter(ws, Arrays.asList(uuids));
        }
    }

    private class InternalNutsTypedFilters<T extends NutsFilter> implements NutsTypedFilters<T> {
        private Class<T> type;

        public InternalNutsTypedFilters(Class<T> type) {
            this.type = type;
        }

        @Override
        public T nonnull(NutsFilter filter) {
            return DefaultNutsFilterManager.this.nonnull(type, filter);
        }

        @Override
        public T always() {
            return DefaultNutsFilterManager.this.always(type);
        }

        @Override
        public T never() {
            return DefaultNutsFilterManager.this.never(type);
        }

        @Override
        public T all(NutsFilter... others) {
            return DefaultNutsFilterManager.this.all(type, others);
        }

        @Override
        public T any(NutsFilter... others) {
            return DefaultNutsFilterManager.this.any(type, others);
        }

        @Override
        public T not(NutsFilter other) {
            return DefaultNutsFilterManager.this.not(type, other);
        }

        @Override
        public T none(NutsFilter... others) {
            return DefaultNutsFilterManager.this.none(type, others);
        }

        @Override
        public T from(NutsFilter a) {
            return DefaultNutsFilterManager.this.to(type, a);
        }

        @Override
        public T fromOrNull(NutsFilter a) {
            return DefaultNutsFilterManager.this.as(type, a);
        }
    }
}
