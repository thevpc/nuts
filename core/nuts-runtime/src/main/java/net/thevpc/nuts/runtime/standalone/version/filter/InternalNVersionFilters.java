package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNTypedFilters;

import java.util.List;

import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

public class InternalNVersionFilters extends InternalNTypedFilters<NVersionFilter> implements NVersionFilters {


    public InternalNVersionFilters(NSession session) {
        super(session, NVersionFilter.class);
    }

    public NOptional<NVersionFilter> byValue(String version) {
        checkSession();
        return DefaultNVersionFilter.parse(version, getSession());
    }

    @Override
    public NVersionFilter always() {
        checkSession();
        return new NVersionFilterTrue(getSession());
    }

    @Override
    public NVersionFilter never() {
        checkSession();
        return new NVersionFilterFalse(getSession());
    }

    @Override
    public NVersionFilter not(NFilter other) {
        checkSession();
        return new NVersionFilterNone(getSession(), (NVersionFilter) other);
    }

    @Override
    public NVersionFilter as(NFilter a) {
        checkSession();
        if (a instanceof NVersionFilter) {
            return (NVersionFilter) a;
        }
        return null;
    }

    @Override
    public NVersionFilter from(NFilter a) {
        checkSession();
        if (a == null) {
            return null;
        }
        NVersionFilter t = as(a);
        if (t == null) {
            throw new NIllegalArgumentException(getSession(), NMsg.ofPlain("not a VersionFilter"));
        }
        return t;
    }

    @Override
    public NVersionFilter all(NFilter... others) {
        checkSession();
        List<NVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NVersionFilterAnd(getSession(), all.toArray(new NVersionFilter[0]));
    }

    @Override
    public NVersionFilter any(NFilter... others) {
        checkSession();
        List<NVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NVersionFilterOr(getSession(), all.toArray(new NVersionFilter[0]));
    }

    @Override
    public NVersionFilter none(NFilter... others) {
        checkSession();
        List<NVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NVersionFilterNone(getSession(), all.toArray(new NVersionFilter[0]));
    }

    @Override
    public NVersionFilter parse(String expression) {
        checkSession();
        return new NVersionFilterParser(expression, getSession()).parse();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
