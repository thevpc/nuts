package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNTypedFilters;
import net.thevpc.nuts.runtime.standalone.io.util.NInstallStatusIdFilter;

import java.util.List;

import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;

public class InternalNIdFilters extends InternalNTypedFilters<NIdFilter> implements NIdFilters {

    public InternalNIdFilters(NSession session) {
        super(session, NIdFilter.class);
    }

    @Override
    public NIdFilter byValue(NId id) {
        return new NIdIdFilter(id,getSession());
    }

    @Override
    public NIdFilter always() {
        checkSession();
        return new NIdFilterTrue(getSession());
    }

    @Override
    public NIdFilter not(NFilter other) {
        return new NIdFilterNone(getSession(), (NIdFilter) other);
    }

    @Override
    public NIdFilter never() {
        checkSession();
        return new NIdFilterFalse(getSession());
    }

    @Override
    public NIdFilter byDefaultVersion(Boolean defaultVersion) {
        if (defaultVersion == null) {
            return always();
        }
        return new NDefaultVersionIdFilter(getSession(), defaultVersion);
    }

    @Override
    public NIdFilter byInstallStatus(NInstallStatusFilter installStatus) {
        return new NInstallStatusIdFilter(getSession(), installStatus);
    }

    @Override
    public NIdFilter byName(String... names) {
        checkSession();
        if (names == null || names.length == 0) {
            return always();
        }
        NIdFilter f = null;
        for (String wildcardId : names) {
            if (f == null) {
                f = new NPatternIdFilter(getSession(), NId.of(wildcardId).get(getSession()));
            } else {
                f = (NIdFilter) f.or(new NPatternIdFilter(getSession(), NId.of(wildcardId).get(getSession())));
            }
        }
        return f;
    }

    @Override
    public NIdFilter as(NFilter a) {
        checkSession();
        if (a instanceof NIdFilter) {
            return (NIdFilter) a;
        }
        if (a instanceof NDescriptorFilter) {
            return new NDescriptorIdFilter((NDescriptorFilter) a,getSession());
        }
        if (a instanceof NVersionFilter) {
            return new NVersionIdFilter((NVersionFilter) a,getSession());
        }
        return null;
    }

    @Override
    public NIdFilter from(NFilter a) {
        checkSession();
        if (a == null) {
            return null;
        }
        NIdFilter t = as(a);
        NSession session = getSession();
        NAssert.requireNonNull(t, "IdFilter", session);
        return t;
    }

    @Override
    public NIdFilter all(NFilter... others) {
        List<NIdFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NIdFilterAnd(getSession(), all.toArray(new NIdFilter[0]));
    }

    @Override
    public NIdFilter any(NFilter... others) {
        List<NIdFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NIdFilterOr(getSession(), all.toArray(new NIdFilter[0]));
    }

    @Override
    public NIdFilter none(NFilter... others) {
        List<NIdFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NIdFilterNone(getSession(), all.toArray(new NIdFilter[0]));
    }

    @Override
    public NIdFilter parse(String expression) {
        return new NIdFilterParser(expression, getSession()).parse();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
