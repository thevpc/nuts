package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNTypedFilters;
import net.thevpc.nuts.runtime.standalone.io.util.NInstallStatusIdFilter;

import java.util.List;

import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NFilter;

public class InternalNIdFilters extends InternalNTypedFilters<NIdFilter> implements NIdFilters {

    public InternalNIdFilters() {
        super(NIdFilter.class);
    }

    @Override
    public NIdFilter byValue(NId id) {
        return new NIdIdFilter(id);
    }

    @Override
    public NIdFilter always() {
        return new NIdFilterTrue();
    }

    @Override
    public NIdFilter not(NFilter other) {
        return new NIdFilterNone((NIdFilter) other);
    }

    @Override
    public NIdFilter never() {
        return new NIdFilterFalse();
    }

    @Override
    public NIdFilter byDefaultVersion(Boolean defaultVersion) {
        if (defaultVersion == null) {
            return always();
        }
        return new NDefaultVersionIdFilter(defaultVersion);
    }

    @Override
    public NIdFilter byInstallStatus(NInstallStatusFilter installStatus) {
        return new NInstallStatusIdFilter(installStatus);
    }

    @Override
    public NIdFilter byName(String... names) {
        if (names == null || names.length == 0) {
            return always();
        }
        NIdFilter f = null;
        for (String wildcardId : names) {
            if (f == null) {
                f = new NPatternIdFilter(NId.get(wildcardId).get());
            } else {
                f = (NIdFilter) f.or(new NPatternIdFilter(NId.get(wildcardId).get()));
            }
        }
        return f;
    }

    @Override
    public NIdFilter as(NFilter a) {
        if (a instanceof NIdFilter) {
            return (NIdFilter) a;
        }
        if (a instanceof NDescriptorFilter) {
            return new NDescriptorIdFilter((NDescriptorFilter) a);
        }
        if (a instanceof NVersionFilter) {
            return new NVersionIdFilter((NVersionFilter) a);
        }
        return null;
    }

    @Override
    public NIdFilter from(NFilter a) {
        if (a == null) {
            return null;
        }
        NIdFilter t = as(a);
        NAssert.requireNonNull(t, "IdFilter");
        return t;
    }

    @Override
    public NIdFilter all(NFilter... others) {
        List<NIdFilter> all = convertList(others);
        for (int i = all.size()-1; i >=0; i--) {
            NIdFilter c = (NIdFilter) all.get(i).simplify();
            if(c!=null) {
                if (c.equals(always())) {
                    if (all.size() > 1) {
                        all.remove(i);
                    }
                } else if (c.equals(never())) {
                    return never();
                }
            }else{
                all.remove(i);
            }
        }
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NIdFilterAnd(all.toArray(new NIdFilter[0]));
    }

    @Override
    public NIdFilter any(NFilter... others) {
        List<NIdFilter> all = convertList(others);
        for (int i = all.size()-1; i >=0; i--) {
            NIdFilter c = (NIdFilter) all.get(i).simplify();
            if(c!=null) {
                if (c.equals(never())) {
                    if (all.size() > 1) {
                        all.remove(i);
                    }
                } else if (c.equals(always())) {
                    return always();
                }
            }else{
                all.remove(i);
            }
        }
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NIdFilterOr(all.toArray(new NIdFilter[0]));
    }

    @Override
    public NIdFilter none(NFilter... others) {
        List<NIdFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NIdFilterNone(all.toArray(new NIdFilter[0]));
    }

    @Override
    public NIdFilter parse(String expression) {
        return new NIdFilterParser(expression).parse();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
