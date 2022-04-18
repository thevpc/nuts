package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNutsTypedFilters;

import java.util.List;

import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class InternalNutsVersionFilters extends InternalNutsTypedFilters<NutsVersionFilter> implements NutsVersionFilters {


    public InternalNutsVersionFilters(NutsSession session) {
        super(session, NutsVersionFilter.class);
    }

    public NutsOptional<NutsVersionFilter> byValue(String version) {
        checkSession();
        return DefaultNutsVersionFilter.parse(version, getSession());
    }

    @Override
    public NutsVersionFilter always() {
        checkSession();
        return new NutsVersionFilterTrue(getSession());
    }

    @Override
    public NutsVersionFilter never() {
        checkSession();
        return new NutsVersionFilterFalse(getSession());
    }

    @Override
    public NutsVersionFilter not(NutsFilter other) {
        checkSession();
        return new NutsVersionFilterNone(getSession(), (NutsVersionFilter) other);
    }

    @Override
    public NutsVersionFilter as(NutsFilter a) {
        checkSession();
        if (a instanceof NutsVersionFilter) {
            return (NutsVersionFilter) a;
        }
        return null;
    }

    @Override
    public NutsVersionFilter from(NutsFilter a) {
        checkSession();
        if (a == null) {
            return null;
        }
        NutsVersionFilter t = as(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("not a VersionFilter"));
        }
        return t;
    }

    @Override
    public NutsVersionFilter all(NutsFilter... others) {
        checkSession();
        List<NutsVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsVersionFilterAnd(getSession(), all.toArray(new NutsVersionFilter[0]));
    }

    @Override
    public NutsVersionFilter any(NutsFilter... others) {
        checkSession();
        List<NutsVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsVersionFilterOr(getSession(), all.toArray(new NutsVersionFilter[0]));
    }

    @Override
    public NutsVersionFilter none(NutsFilter... others) {
        checkSession();
        List<NutsVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NutsVersionFilterNone(getSession(), all.toArray(new NutsVersionFilter[0]));
    }

    @Override
    public NutsVersionFilter parse(String expression) {
        checkSession();
        return new NutsVersionFilterParser(expression, getSession()).parse();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
