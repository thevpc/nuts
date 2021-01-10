package net.thevpc.nuts.runtime.core.filters.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterManager;
import net.thevpc.nuts.runtime.core.filters.InternalNutsTypedFilters;
import net.thevpc.nuts.runtime.core.filters.NutsPatternIdFilter;
import net.thevpc.nuts.runtime.core.filters.dependency.NutsDependencyFilterAnd;
import net.thevpc.nuts.runtime.core.filters.id.*;
import net.thevpc.nuts.runtime.core.filters.installstatus.NutsInstallStatusFilterParser;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.io.NutsInstallStatusIdFilter;

import java.util.List;
import java.util.function.Predicate;

public class InternalNutsIdFilterManager extends InternalNutsTypedFilters<NutsIdFilter> implements NutsIdFilterManager {
    private final DefaultNutsFilterManager defaultNutsFilterManager;
    private NutsIdFilterTrue nutsIdFilterTrue;
    private NutsIdFilterFalse nutsIdFilterFalse;

    public InternalNutsIdFilterManager(DefaultNutsFilterManager defaultNutsFilterManager) {
        super(defaultNutsFilterManager, NutsIdFilter.class);
        this.defaultNutsFilterManager = defaultNutsFilterManager;
    }

    @Override
    public NutsIdFilter always() {
        if (nutsIdFilterTrue == null) {
            nutsIdFilterTrue = new NutsIdFilterTrue(ws);
        }
        return nutsIdFilterTrue;
    }

    @Override
    public NutsIdFilter not(NutsFilter other) {
        return new NutsIdFilterNone(ws, (NutsIdFilter) other);
    }

    @Override
    public NutsIdFilter never() {
        if (nutsIdFilterFalse == null) {
            nutsIdFilterFalse = new NutsIdFilterFalse(ws);
        }
        return nutsIdFilterFalse;
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
    public NutsIdFilter byInstallStatus(NutsInstallStatusFilter installStatus) {
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

    @Override
    public NutsIdFilter as(NutsFilter a) {
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

    @Override
    public NutsIdFilter from(NutsFilter a) {
        if (a == null) {
            return null;
        }
        NutsIdFilter t = as(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(ws, "not a IdFilter");
        }
        return t;
    }

    @Override
    public NutsIdFilter all(NutsFilter... others) {
        List<NutsIdFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsIdFilterAnd(ws, all.toArray(new NutsIdFilter[0]));
    }

    @Override
    public NutsIdFilter any(NutsFilter... others) {
        List<NutsIdFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsIdFilterOr(ws, all.toArray(new NutsIdFilter[0]));
    }

    @Override
    public NutsIdFilter none(NutsFilter... others) {
        List<NutsIdFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NutsIdFilterNone(ws, all.toArray(new NutsIdFilter[0]));
    }

    @Override
    public NutsIdFilter parse(String expression) {
        return new NutsIdFilterParser(expression, ws).parse();
    }
}
