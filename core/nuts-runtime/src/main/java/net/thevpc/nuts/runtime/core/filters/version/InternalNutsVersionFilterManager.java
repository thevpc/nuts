package net.thevpc.nuts.runtime.core.filters.version;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterManager;
import net.thevpc.nuts.runtime.core.filters.InternalNutsTypedFilters;
import net.thevpc.nuts.runtime.core.filters.dependency.NutsDependencyFilterAnd;
import net.thevpc.nuts.runtime.core.filters.installstatus.NutsInstallStatusFilterParser;
import net.thevpc.nuts.runtime.core.filters.version.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.List;

public class InternalNutsVersionFilterManager extends InternalNutsTypedFilters<NutsVersionFilter> implements NutsVersionFilterManager {
    private final DefaultNutsFilterManager defaultNutsFilterManager;
    private NutsVersionFilterTrue nutsVersionFilterTrue;
    private NutsVersionFilterFalse nutsVersionFilterFalse;

    public InternalNutsVersionFilterManager(DefaultNutsFilterManager defaultNutsFilterManager) {
        super(defaultNutsFilterManager, NutsVersionFilter.class);
        this.defaultNutsFilterManager = defaultNutsFilterManager;
    }

    public NutsVersionFilter byValue(String version) {
        return DefaultNutsVersionFilter.parse(version, ws);
    }

    @Override
    public NutsVersionFilter always() {
        if (nutsVersionFilterTrue == null) {
            nutsVersionFilterTrue = new NutsVersionFilterTrue(ws);
        }
        return nutsVersionFilterTrue;
    }

    @Override
    public NutsVersionFilter never() {
        if (nutsVersionFilterFalse == null) {
            nutsVersionFilterFalse = new NutsVersionFilterFalse(ws);
        }
        return nutsVersionFilterFalse;
    }

    @Override
    public NutsVersionFilter not(NutsFilter other) {
        return new NutsVersionFilterNone(ws, (NutsVersionFilter) other);
    }


    @Override
    public NutsVersionFilter byExpression(String expression) {
        if (CoreStringUtils.isBlank(expression)) {
            return always();
        }
        return NutsVersionJavascriptFilter.valueOf(expression, ws);
    }

    @Override
    public NutsVersionFilter as(NutsFilter a) {
        if (a instanceof NutsVersionFilter) {
            return (NutsVersionFilter) a;
        }
        return null;
    }

    @Override
    public NutsVersionFilter from(NutsFilter a) {
        if (a == null) {
            return null;
        }
        NutsVersionFilter t = as(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(ws, "not a VersionFilter");
        }
        return t;
    }

    @Override
    public NutsVersionFilter all(NutsFilter... others) {
        List<NutsVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsVersionFilterAnd(ws, all.toArray(new NutsVersionFilter[0]));
    }

    @Override
    public NutsVersionFilter any(NutsFilter... others) {
        List<NutsVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsVersionFilterOr(ws, all.toArray(new NutsVersionFilter[0]));
    }

    @Override
    public NutsVersionFilter none(NutsFilter... others) {
        List<NutsVersionFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NutsVersionFilterNone(ws, all.toArray(new NutsVersionFilter[0]));
    }

    @Override
    public NutsVersionFilter parse(String expression) {
        return new NutsVersionFilterParser(expression, ws).parse();
    }
}
