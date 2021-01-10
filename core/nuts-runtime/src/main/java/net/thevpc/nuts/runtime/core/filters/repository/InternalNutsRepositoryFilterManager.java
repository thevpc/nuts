package net.thevpc.nuts.runtime.core.filters.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterManager;
import net.thevpc.nuts.runtime.core.filters.InternalNutsTypedFilters;
import net.thevpc.nuts.runtime.core.filters.dependency.NutsDependencyFilterAnd;
import net.thevpc.nuts.runtime.core.filters.installstatus.NutsInstallStatusFilterParser;
import net.thevpc.nuts.runtime.core.filters.repository.DefaultNutsRepositoryFilter;
import net.thevpc.nuts.runtime.core.filters.repository.NutsRepositoryFilterFalse;
import net.thevpc.nuts.runtime.core.filters.repository.NutsRepositoryFilterNone;
import net.thevpc.nuts.runtime.core.filters.repository.NutsRepositoryFilterTrue;

import java.util.Arrays;
import java.util.List;

public class InternalNutsRepositoryFilterManager extends InternalNutsTypedFilters<NutsRepositoryFilter> implements NutsRepositoryFilterManager {
    private NutsRepositoryFilterTrue nutsRepositoryFilterTrue;
    private NutsRepositoryFilterFalse nutsRepositoryFilterFalse;

    public InternalNutsRepositoryFilterManager(DefaultNutsFilterManager defaultNutsFilterManager) {
        super(defaultNutsFilterManager,NutsRepositoryFilter.class);
    }

    @Override
    public NutsRepositoryFilter always() {
        if (nutsRepositoryFilterTrue == null) {
            nutsRepositoryFilterTrue = new NutsRepositoryFilterTrue(ws);
        }
        return nutsRepositoryFilterTrue;
    }

    @Override
    public NutsRepositoryFilter never() {
        if (nutsRepositoryFilterFalse == null) {
            nutsRepositoryFilterFalse = new NutsRepositoryFilterFalse(ws);
        }
        return nutsRepositoryFilterFalse;
    }

    @Override
    public NutsRepositoryFilter not(NutsFilter other) {
        return new NutsRepositoryFilterNone(ws, (NutsRepositoryFilter) other);
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

    @Override
    public NutsRepositoryFilter as(NutsFilter a) {
        if (a instanceof NutsRepositoryFilter) {
            return (NutsRepositoryFilter) a;
        }
        return null;
    }

    @Override
    public NutsRepositoryFilter from(NutsFilter a) {
        if (a == null) {
            return null;
        }
        NutsRepositoryFilter t = as(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(ws, "not a RepositoryFilter");
        }
        return t;
    }

    @Override
    public NutsRepositoryFilter all(NutsFilter... others) {
        List<NutsRepositoryFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsRepositoryFilterAnd(ws, all.toArray(new NutsRepositoryFilter[0]));
    }


    @Override
    public NutsRepositoryFilter any(NutsFilter... others) {
        List<NutsRepositoryFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsRepositoryFilterOr(ws, all.toArray(new NutsRepositoryFilter[0]));
    }

    @Override
    public NutsRepositoryFilter none(NutsFilter... others) {
        List<NutsRepositoryFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NutsRepositoryFilterNone(ws, all.toArray(new NutsRepositoryFilter[0]));
    }

    @Override
    public NutsRepositoryFilter parse(String expression) {
        return new NutsRepositoryFilterParser(expression, ws).parse();
    }
}
