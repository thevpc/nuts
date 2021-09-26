package net.thevpc.nuts.runtime.core.filters.dependency;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.InternalNutsTypedFilters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsFilterModel;

public class InternalNutsDependencyFilterManager extends InternalNutsTypedFilters<NutsDependencyFilter> implements NutsDependencyFilterManager {

//    private static class LocalModel {
//
//        private NutsDependencyFilterFalse nutsDependencyFilterFalse;
//        private NutsDependencyFilterTrue nutsDependencyFilterTrue;
//        private NutsWorkspace ws;
//
//        public LocalModel(NutsWorkspace ws) {
//            this.ws = ws;
//        }
//
//        public NutsDependencyFilter always() {
//            if (nutsDependencyFilterTrue == null) {
//                nutsDependencyFilterTrue = new NutsDependencyFilterTrue(ws);
//            }
//            return nutsDependencyFilterTrue;
//        }
//
//        public NutsDependencyFilter never() {
//            if (nutsDependencyFilterFalse == null) {
//                nutsDependencyFilterFalse = new NutsDependencyFilterFalse(ws);
//            }
//            return nutsDependencyFilterFalse;
//        }
//
//    }
//    private final LocalModel localModel;
    public InternalNutsDependencyFilterManager(DefaultNutsFilterModel model) {
        super(model, NutsDependencyFilter.class);
//        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
    }

    @Override
    public InternalNutsDependencyFilterManager setSession(NutsSession session) {
        super.setSession(session);
        return this;
    }

    @Override
    public NutsDependencyFilter not(NutsFilter other) {
        checkSession();
        return new NutsDependencyFilterNone(getSession(), (NutsDependencyFilter) other);
    }

    @Override
    public NutsDependencyFilter always() {
        checkSession();
        return new NutsDependencyFilterTrue(getSession());
    }

    @Override
    public NutsDependencyFilter never() {
        checkSession();
        return new NutsDependencyFilterFalse(getSession());
    }

    @Override
    public NutsDependencyFilter nonnull(NutsFilter filter) {
        checkSession();
        return super.nonnull(filter);
    }

    @Override
    public NutsDependencyFilter byScope(NutsDependencyScopePattern scope) {
        checkSession();
        if (scope == null) {
            return always();
        }
        return new ScopeNutsDependencyFilter(getSession(), scope);
    }

    @Override
    public NutsDependencyFilter byScope(NutsDependencyScope scope) {
        checkSession();
        if (scope == null) {
            return always();
        }
        return new NutsDependencyScopeFilter(getSession()).add(Arrays.asList(scope));
    }

    @Override
    public NutsDependencyFilter byScope(NutsDependencyScope... scope) {
        checkSession();
        if (scope == null) {
            return always();
        }
        return new NutsDependencyScopeFilter(getSession()).add(Arrays.asList(scope));
    }

    @Override
    public NutsDependencyFilter byScope(Collection<NutsDependencyScope> scope) {
        checkSession();
        if (scope == null) {
            return always();
        }
        return new NutsDependencyScopeFilter(getSession()).add(scope);
    }

    @Override
    public NutsDependencyFilter byOs(String os) {
        checkSession();
        if (os == null) {
            return always();
        }
        return new NutsDependencyOsFilter(getSession(), os);
    }

    @Override
    public NutsDependencyFilter byType(String type) {
        return new NutsDependencyTypeFilter(getSession(), type);
    }
    
    

    @Override
    public NutsDependencyFilter byOs(NutsOsFamily os) {
        checkSession();
        if (os == null) {
            return always();
        }
        return new NutsDependencyOsFilter(getSession()).add(Arrays.asList(os));
    }

    @Override
    public NutsDependencyFilter byOs(NutsOsFamily... os) {
        checkSession();
        checkSession();
        if (os == null) {
            return always();
        }
        return new NutsDependencyOsFilter(getSession()).add(Arrays.asList(os));
    }

    @Override
    public NutsDependencyFilter byOs(Collection<NutsOsFamily> os) {
        checkSession();
        if (os == null) {
            return always();
        }
        return new NutsDependencyOsFilter(getSession()).add(os);
    }

    @Override
    public NutsDependencyFilter byArch(NutsArchFamily os) {
        checkSession();
        if (os == null) {
            return always();
        }
        return new NutsDependencyArchFilter(getSession()).add(Arrays.asList(os));
    }

    @Override
    public NutsDependencyFilter byArch(NutsArchFamily... arch) {
        checkSession();
        if (arch == null) {
            return always();
        }
        return new NutsDependencyArchFilter(getSession()).add(Arrays.asList(arch));
    }

    @Override
    public NutsDependencyFilter byArch(Collection<NutsArchFamily> arch) {
        checkSession();
        if (arch == null) {
            return always();
        }
        return new NutsDependencyArchFilter(getSession()).add(arch);
    }

    @Override
    public NutsDependencyFilter byArch(String arch) {
        checkSession();
        if (arch == null) {
            return always();
        }
        return new NutsDependencyArchFilter(getSession(), arch);
    }

    @Override
    public NutsDependencyFilter byOptional(Boolean optional) {
        checkSession();
        if (optional == null) {
            return always();
        }
        return new NutsDependencyOptionFilter(getSession(), optional);
    }

    @Override
    public NutsDependencyFilter byExpression(String expression) {
        checkSession();
        if (NutsBlankable.isBlank(expression)) {
            return always();
        }
        return NutsDependencyJavascriptFilter.valueOf(expression, getSession());
    }

    @Override
    public NutsDependencyFilter byExclude(NutsDependencyFilter filter, String[] exclusions) {
        checkSession();
        return new NutsExclusionDependencyFilter(getSession(), filter, Arrays.stream(exclusions).map(x -> ws.id().parser().setLenient(false).parse(x)).toArray(NutsId[]::new));
    }

    @Override
    public NutsDependencyFilter as(NutsFilter a) {
        checkSession();
        if (a instanceof NutsDependencyFilter) {
            return (NutsDependencyFilter) a;
        }
        return null;
    }

    @Override
    public NutsDependencyFilter from(NutsFilter a) {
        checkSession();
        if (a == null) {
            return null;
        }
        NutsDependencyFilter t = as(a);
        if (t == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("not a NutsDependencyFilter"));
        }
        return t;
    }

    @Override
    public NutsDependencyFilter all(NutsFilter... others) {
        checkSession();
        List<NutsDependencyFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsDependencyFilterAnd(getSession(), all.toArray(new NutsDependencyFilter[0]));
    }

    @Override
    public NutsDependencyFilter any(NutsFilter... others) {
        checkSession();
        List<NutsDependencyFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NutsDependencyFilterOr(getSession(), all.toArray(new NutsDependencyFilter[0]));
    }

    @Override
    public NutsDependencyFilter none(NutsFilter... others) {
        checkSession();
        List<NutsDependencyFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NutsDependencyFilterNone(getSession(), all.toArray(new NutsDependencyFilter[0]));
    }

    @Override
    public NutsDependencyFilter parse(String expression) {
        checkSession();
        return new NutsDependencyFilterParser(expression, getSession()).parse();
    }
}
