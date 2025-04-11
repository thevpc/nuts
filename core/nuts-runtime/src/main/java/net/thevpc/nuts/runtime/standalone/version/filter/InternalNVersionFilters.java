package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNTypedFilters;

import java.util.List;

import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

public class InternalNVersionFilters extends InternalNTypedFilters<NVersionFilter> implements NVersionFilters {


    public InternalNVersionFilters() {
        super(NVersionFilter.class);
    }

    public NOptional<NVersionFilter> byValue(String version) {
        return DefaultNVersionFilter.parse(version);
    }

    @Override
    public NVersionFilter always() {
        return new NVersionFilterTrue();
    }

    @Override
    public NVersionFilter never() {
        return new NVersionFilterFalse();
    }

    @Override
    public NVersionFilter not(NFilter other) {
        return new NVersionFilterNone((NVersionFilter) other);
    }

    @Override
    public NVersionFilter as(NFilter a) {
        if (a instanceof NVersionFilter) {
            return (NVersionFilter) a;
        }
        return null;
    }

    @Override
    public NVersionFilter from(NFilter a) {
        if (a == null) {
            return null;
        }
        NVersionFilter t = as(a);
        if (t == null) {
            throw new NIllegalArgumentException(NMsg.ofPlain("not a VersionFilter"));
        }
        return t;
    }

    @Override
    public NVersionFilter all(NFilter... others) {
        List<NVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NVersionFilterAnd(all.toArray(new NVersionFilter[0]));
    }

    @Override
    public NVersionFilter any(NFilter... others) {
        List<NVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NVersionFilterOr(all.toArray(new NVersionFilter[0]));
    }

    @Override
    public NVersionFilter none(NFilter... others) {
        List<NVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NVersionFilterNone(all.toArray(new NVersionFilter[0]));
    }

    @Override
    public NVersionFilter parse(String expression) {
        return new NVersionFilterParser(expression).parse();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
