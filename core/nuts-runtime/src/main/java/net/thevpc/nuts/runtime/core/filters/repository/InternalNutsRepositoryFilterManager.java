package net.thevpc.nuts.runtime.core.filters.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.InternalNutsTypedFilters;

import java.util.Arrays;
import java.util.List;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterModel;

public class InternalNutsRepositoryFilterManager extends InternalNutsTypedFilters<NutsRepositoryFilter> implements NutsRepositoryFilterManager {

    public InternalNutsRepositoryFilterManager(DefaultNutsFilterModel model) {
        super(model, NutsRepositoryFilter.class);
    }

    @Override
    public InternalNutsRepositoryFilterManager setSession(NutsSession session) {
        super.setSession(session);
        return this;
    }

    @Override
    public NutsRepositoryFilter always() {
        checkSession();
        return new NutsRepositoryFilterTrue(getSession());
    }

    @Override
    public NutsRepositoryFilter never() {
        checkSession();
        return new NutsRepositoryFilterFalse(getSession());
    }

    @Override
    public NutsRepositoryFilter not(NutsFilter other) {
        checkSession();
        return new NutsRepositoryFilterNone(getSession(), (NutsRepositoryFilter) other);
    }

    @Override
    public NutsRepositoryFilter byName(String[] names) {
        checkSession();
        if (names == null || names.length == 0) {
            return always();
        }
        return new DefaultNutsRepositoryFilter(getSession(), Arrays.asList(names));
    }

    @Override
    public NutsRepositoryFilter byUuid(String... uuids) {
        checkSession();
        if (uuids == null || uuids.length == 0) {
            return always();
        }
        //TODO should create another class for uuids!
        return new DefaultNutsRepositoryFilter(getSession(), Arrays.asList(uuids));
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
        checkSession();
        if (a == null) {
            return null;
        }
        NutsRepositoryFilter t = as(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(getSession(), "not a RepositoryFilter");
        }
        return t;
    }

    @Override
    public NutsRepositoryFilter all(NutsFilter... others) {
        checkSession();
        List<NutsRepositoryFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsRepositoryFilterAnd(getSession(), all.toArray(new NutsRepositoryFilter[0]));
    }

    @Override
    public NutsRepositoryFilter any(NutsFilter... others) {
        checkSession();
        List<NutsRepositoryFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsRepositoryFilterOr(getSession(), all.toArray(new NutsRepositoryFilter[0]));
    }

    @Override
    public NutsRepositoryFilter none(NutsFilter... others) {
        checkSession();
        List<NutsRepositoryFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NutsRepositoryFilterNone(getSession(), all.toArray(new NutsRepositoryFilter[0]));
    }

    @Override
    public NutsRepositoryFilter parse(String expression) {
        checkSession();
        return new NutsRepositoryFilterParser(expression, getSession()).parse();
    }
}
