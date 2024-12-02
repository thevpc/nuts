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

    public InternalNIdFilters(NWorkspace workspace) {
        super(workspace, NIdFilter.class);
    }

    @Override
    public NIdFilter byValue(NId id) {
        return new NIdIdFilter(id, getWorkspace());
    }

    @Override
    public NIdFilter always() {
        return new NIdFilterTrue(getWorkspace());
    }

    @Override
    public NIdFilter not(NFilter other) {
        return new NIdFilterNone(getWorkspace(), (NIdFilter) other);
    }

    @Override
    public NIdFilter never() {
        return new NIdFilterFalse(getWorkspace());
    }

    @Override
    public NIdFilter byDefaultVersion(Boolean defaultVersion) {
        if (defaultVersion == null) {
            return always();
        }
        return new NDefaultVersionIdFilter(getWorkspace(), defaultVersion);
    }

    @Override
    public NIdFilter byInstallStatus(NInstallStatusFilter installStatus) {
        return new NInstallStatusIdFilter(getWorkspace(), installStatus);
    }

    @Override
    public NIdFilter byName(String... names) {
        if (names == null || names.length == 0) {
            return always();
        }
        NIdFilter f = null;
        for (String wildcardId : names) {
            if (f == null) {
                f = new NPatternIdFilter(getWorkspace(), NId.get(wildcardId).get());
            } else {
                f = (NIdFilter) f.or(new NPatternIdFilter(getWorkspace(), NId.get(wildcardId).get()));
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
            return new NDescriptorIdFilter((NDescriptorFilter) a, getWorkspace());
        }
        if (a instanceof NVersionFilter) {
            return new NVersionIdFilter((NVersionFilter) a, getWorkspace());
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
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NIdFilterAnd(getWorkspace(), all.toArray(new NIdFilter[0]));
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
        return new NIdFilterOr(getWorkspace(), all.toArray(new NIdFilter[0]));
    }

    @Override
    public NIdFilter none(NFilter... others) {
        List<NIdFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NIdFilterNone(getWorkspace(), all.toArray(new NIdFilter[0]));
    }

    @Override
    public NIdFilter parse(String expression) {
        return new NIdFilterParser(expression, getWorkspace()).parse();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
