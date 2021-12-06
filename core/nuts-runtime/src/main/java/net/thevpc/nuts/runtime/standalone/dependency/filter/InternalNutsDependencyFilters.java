package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNutsTypedFilters;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InternalNutsDependencyFilters extends InternalNutsTypedFilters<NutsDependencyFilter>
        implements NutsDependencyFilters {

    public InternalNutsDependencyFilters(NutsSession session) {
        super(session, NutsDependencyFilter.class);
//        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
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
    public NutsDependencyFilter not(NutsFilter other) {
        checkSession();
        return new NutsDependencyFilterNone(getSession(), (NutsDependencyFilter) other);
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
    public NutsDependencyFilter as(NutsFilter a) {
        checkSession();
        if (a instanceof NutsDependencyFilter) {
            return (NutsDependencyFilter) a;
        }
        return null;
    }

    @Override
    public NutsDependencyFilter parse(String expression) {
        checkSession();
        return new NutsDependencyFilterParser(expression, getSession()).parse();
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
    public NutsDependencyFilter byScope(NutsDependencyScope... scopes) {
        checkSession();
        if (scopes == null) {
            return always();
        }
        return new NutsDependencyScopeFilter(getSession()).add(Arrays.asList(scopes));
    }

    @Override
    public NutsDependencyFilter byScope(Collection<NutsDependencyScope> scopes) {
        checkSession();
        if (scopes == null) {
            return always();
        }
        return new NutsDependencyScopeFilter(getSession()).add(scopes);
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
    public NutsDependencyFilter byExclude(NutsDependencyFilter filter, String[] exclusions) {
        checkSession();
        return new NutsExclusionDependencyFilter(getSession(), filter, Arrays.stream(exclusions).map(x -> NutsId.of(x, getSession())).toArray(NutsId[]::new));
    }

    @Override
    public NutsDependencyFilter byArch(Collection<NutsArchFamily> archs) {
        checkSession();
        if (archs == null) {
            return always();
        }
        return new NutsDependencyArchFamilyFilter(getSession()).add(archs);
    }

    @Override
    public NutsDependencyFilter byArch(NutsArchFamily arch) {
        checkSession();
        if (arch == null) {
            return always();
        }
        return new NutsDependencyArchFamilyFilter(getSession()).add(Arrays.asList(arch));
    }

    @Override
    public NutsDependencyFilter byArch(NutsArchFamily... archs) {
        checkSession();
        if (archs == null) {
            return always();
        }
        return new NutsDependencyArchFamilyFilter(getSession()).add(Arrays.asList(archs));
    }

    @Override
    public NutsDependencyFilter byArch(String arch) {
        checkSession();
        if (arch == null) {
            return always();
        }
        return new NutsDependencyArchFamilyFilter(getSession(), arch);
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
    public NutsDependencyFilter byCurrentDesktop() {
        checkSession();
        return byDesktop(getSession().env().getDesktopEnvironmentFamilies());
    }

    public NutsDependencyFilter byCurrentArch() {
        checkSession();
        return byArch(getSession().env().getArchFamily());
    }

    @Override
    public NutsDependencyFilter byCurrentOs() {
        checkSession();
        return byOs(getSession().env().getOsFamily());
    }

    @Override
    public NutsDependencyFilter byRegularType() {
        return byType(null).or(byType("jar"));
    }

    public NutsDependencyFilter byCurrentEnv() {
        return byCurrentOs()
                .and(byCurrentArch())
                .and(byCurrentDesktop())
                ;
    }

    @Override
    public NutsDependencyFilter byRunnable(boolean optional) {
        return byScope(NutsDependencyScopePattern.RUN)
                .and(byOptional(optional?null:false))
                .and(byRegularType())
                .and(byCurrentEnv());
    }

    @Override
    public NutsDependencyFilter byRunnable() {
        return byRunnable(false);
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
    public NutsDependencyFilter byDesktop(NutsDesktopEnvironmentFamily de) {
        checkSession();
        if (de == null) {
            return always();
        }
        return new NutsDependencyDEFilter(getSession()).add(Arrays.asList(de));
    }

    @Override
    public NutsDependencyFilter byDesktop(NutsDesktopEnvironmentFamily... de) {
        checkSession();
        checkSession();
        if (de == null) {
            return always();
        }
        return new NutsDependencyDEFilter(getSession()).add(Arrays.asList(de));
    }

    @Override
    public NutsDependencyFilter byType(String type) {
        return new NutsDependencyTypeFilter(getSession(), type);
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
    public NutsDependencyFilter byOsDist(String osDist) {
        checkSession();
        if (osDist == null) {
            return always();
        }
        return new NutsDependencyOsDistIdFilter(getSession()).add(Collections.singletonList(NutsId.of(osDist, getSession())));
    }

    @Override
    public NutsDependencyFilter byOsDist(String... osDists) {
        checkSession();
        if (osDists == null || osDists.length==0) {
            return always();
        }
        return new NutsDependencyOsDistIdFilter(getSession()).add(
                Arrays.stream(osDists).map(x->NutsId.of(x,getSession()))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public NutsDependencyFilter byOsDist(Collection<String> osDists) {
        checkSession();
        if (osDists == null || osDists.isEmpty()) {
            return always();
        }
        return new NutsDependencyOsDistIdFilter(getSession()).add(
                osDists.stream().map(x->NutsId.of(x,getSession()))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public NutsDependencyFilter byPlatform(NutsPlatformFamily... pf) {
        checkSession();
        if (pf == null || pf.length==0) {
            return always();
        }
        return new NutsDependencyPlatformFamilyFilter(getSession()).add(Arrays.asList(pf));
    }

    @Override
    public NutsDependencyFilter byPlatform(String... pf) {
        checkSession();
        if (pf == null || pf.length==0) {
            return always();
        }
        return new NutsDependencyPlatformIdFilter(getSession()).add(
                Arrays.stream(pf).map(x->NutsId.of(x,getSession()))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
