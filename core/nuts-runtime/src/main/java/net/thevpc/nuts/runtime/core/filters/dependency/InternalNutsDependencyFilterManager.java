package net.thevpc.nuts.runtime.core.filters.dependency;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterManager;
import net.thevpc.nuts.runtime.core.filters.InternalNutsTypedFilters;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class InternalNutsDependencyFilterManager extends InternalNutsTypedFilters<NutsDependencyFilter> implements NutsDependencyFilterManager {
    private final DefaultNutsFilterManager defaultNutsFilterManager;
    private NutsDependencyFilterFalse nutsDependencyFilterFalse;
    private NutsDependencyFilterTrue nutsDependencyFilterTrue;

    public InternalNutsDependencyFilterManager(DefaultNutsFilterManager defaultNutsFilterManager) {
        super(defaultNutsFilterManager, NutsDependencyFilter.class);
        this.defaultNutsFilterManager = defaultNutsFilterManager;
    }

    @Override
    public NutsDependencyFilter always() {
        if (nutsDependencyFilterTrue == null) {
            nutsDependencyFilterTrue = new NutsDependencyFilterTrue(ws);
        }
        return nutsDependencyFilterTrue;
    }

    @Override
    public NutsDependencyFilter not(NutsFilter other) {
        return new NutsDependencyFilterNone(ws, (NutsDependencyFilter) other);
    }

    @Override
    public NutsDependencyFilter never() {
        if (nutsDependencyFilterFalse == null) {
            nutsDependencyFilterFalse = new NutsDependencyFilterFalse(ws);
        }
        return nutsDependencyFilterFalse;
    }

    @Override
    public NutsDependencyFilter nonnull(NutsFilter filter) {
        return super.nonnull(filter);
    }

    @Override
    public NutsDependencyFilter byScope(NutsDependencyScopePattern scope) {
        if (scope == null) {
            return always();
        }
        return new ScopeNutsDependencyFilter(ws, scope);
    }

    @Override
    public NutsDependencyFilter byScope(NutsDependencyScope scope) {
        if (scope == null) {
            return always();
        }
        return new NutsDependencyScopeFilter(ws).addScopes(Arrays.asList(scope));
    }

    @Override
    public NutsDependencyFilter byScope(NutsDependencyScope... scope) {
        if (scope == null) {
            return always();
        }
        return new NutsDependencyScopeFilter(ws).addScopes(Arrays.asList(scope));
    }

    @Override
    public NutsDependencyFilter byScope(Collection<NutsDependencyScope> scope) {
        if (scope == null) {
            return always();
        }
        return new NutsDependencyScopeFilter(ws).addScopes(scope);
    }

    @Override
    public NutsDependencyFilter byOptional(Boolean optional) {
        if (optional == null) {
            return always();
        }
        return new NutsDependencyOptionFilter(ws, optional);
    }

    @Override
    public NutsDependencyFilter byExpression(String expression) {
        if (CoreStringUtils.isBlank(expression)) {
            return always();
        }
        return NutsDependencyJavascriptFilter.valueOf(expression, ws);
    }

    @Override
    public NutsDependencyFilter byExclude(NutsDependencyFilter filter, String[] exclusions) {
        return new NutsExclusionDependencyFilter(ws, filter, Arrays.stream(exclusions).map(x -> ws.id().parser().setLenient(false).parse(x)).toArray(NutsId[]::new));
    }

    @Override
    public NutsDependencyFilter as(NutsFilter a) {
        if (a instanceof NutsDependencyFilter) {
            return (NutsDependencyFilter) a;
        }
        return null;
    }

    @Override
    public NutsDependencyFilter from(NutsFilter a) {
        if (a == null) {
            return null;
        }
        NutsDependencyFilter t = as(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(ws, "not a NutsDependencyFilter");
        }
        return t;
    }

    @Override
    public NutsDependencyFilter all(NutsFilter... others) {
        List<NutsDependencyFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsDependencyFilterAnd(ws, all.toArray(new NutsDependencyFilter[0]));
    }

    @Override
    public NutsDependencyFilter any(NutsFilter... others) {
        List<NutsDependencyFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsDependencyFilterOr(ws, all.toArray(new NutsDependencyFilter[0]));
    }

    @Override
    public NutsDependencyFilter none(NutsFilter... others) {
        List<NutsDependencyFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NutsDependencyFilterNone(ws, all.toArray(new NutsDependencyFilter[0]));
    }

    @Override
    public NutsDependencyFilter parse(String expression) {
        return new NutsDependencyFilterParser(expression, ws).parse();
    }
}
