package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNTypedFilters;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InternalNDependencyFilters extends InternalNTypedFilters<NDependencyFilter>
        implements NDependencyFilters {

    public InternalNDependencyFilters(NSession session) {
        super(session, NDependencyFilter.class);
//        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
    }

    @Override
    public NDependencyFilter always() {
        checkSession();
        return new NDependencyFilterTrue(getSession());
    }

    @Override
    public NDependencyFilter never() {
        checkSession();
        return new NDependencyFilterFalse(getSession());
    }

    @Override
    public NDependencyFilter all(NFilter... others) {
        checkSession();
        List<NDependencyFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NDependencyFilterAnd(getSession(), all.toArray(new NDependencyFilter[0]));
    }

    @Override
    public NDependencyFilter any(NFilter... others) {
        checkSession();
        List<NDependencyFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NDependencyFilterOr(getSession(), all.toArray(new NDependencyFilter[0]));
    }

    @Override
    public NDependencyFilter not(NFilter other) {
        checkSession();
        return new NDependencyFilterNone(getSession(), (NDependencyFilter) other);
    }

    @Override
    public NDependencyFilter none(NFilter... others) {
        checkSession();
        List<NDependencyFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NDependencyFilterNone(getSession(), all.toArray(new NDependencyFilter[0]));
    }

    @Override
    public NDependencyFilter from(NFilter a) {
        checkSession();
        if (a == null) {
            return null;
        }
        NDependencyFilter t = as(a);
        NSession session = getSession();
        NAssert.requireNonNull(t, "InstallDependencyFilter", session);
        return t;
    }

    @Override
    public NDependencyFilter as(NFilter a) {
        checkSession();
        if (a instanceof NDependencyFilter) {
            return (NDependencyFilter) a;
        }
        return null;
    }

    @Override
    public NDependencyFilter parse(String expression) {
        checkSession();
        return new NDependencyFilterParser(expression, getSession()).parse();
    }

    @Override
    public NDependencyFilter nonnull(NFilter filter) {
        checkSession();
        return super.nonnull(filter);
    }

    @Override
    public NDependencyFilter byScope(NDependencyScopePattern scope) {
        checkSession();
        if (scope == null) {
            return always();
        }
        return new ScopeNDependencyFilter(getSession(), scope);
    }

    @Override
    public NDependencyFilter byScope(NDependencyScope scope) {
        checkSession();
        if (scope == null) {
            return always();
        }
        return new NDependencyScopeFilter(getSession()).add(Arrays.asList(scope));
    }

    @Override
    public NDependencyFilter byScope(NDependencyScope... scopes) {
        checkSession();
        if (scopes == null) {
            return always();
        }
        return new NDependencyScopeFilter(getSession()).add(Arrays.asList(scopes));
    }

    @Override
    public NDependencyFilter byScope(Collection<NDependencyScope> scopes) {
        checkSession();
        if (scopes == null) {
            return always();
        }
        return new NDependencyScopeFilter(getSession()).add(scopes);
    }

    @Override
    public NDependencyFilter byOptional(Boolean optional) {
        checkSession();
        if (optional == null) {
            return always();
        }
        return new NDependencyOptionFilter(getSession(), optional);
    }

    @Override
    public NDependencyFilter byExclude(NDependencyFilter filter, String[] exclusions) {
        checkSession();
        return new NExclusionDependencyFilter(getSession(), filter, Arrays.stream(exclusions).map(x -> NId.of(x).get( getSession())).toArray(NId[]::new));
    }

    @Override
    public NDependencyFilter byArch(Collection<NArchFamily> archs) {
        checkSession();
        if (archs == null) {
            return always();
        }
        return new NDependencyArchFamilyFilter(getSession()).add(archs);
    }

    @Override
    public NDependencyFilter byArch(NArchFamily arch) {
        checkSession();
        if (arch == null) {
            return always();
        }
        return new NDependencyArchFamilyFilter(getSession()).add(Arrays.asList(arch));
    }

    @Override
    public NDependencyFilter byArch(NArchFamily... archs) {
        checkSession();
        if (archs == null) {
            return always();
        }
        return new NDependencyArchFamilyFilter(getSession()).add(Arrays.asList(archs));
    }

    @Override
    public NDependencyFilter byArch(String arch) {
        checkSession();
        if (arch == null) {
            return always();
        }
        return new NDependencyArchFamilyFilter(getSession(), arch);
    }

    @Override
    public NDependencyFilter byOs(Collection<NOsFamily> os) {
        checkSession();
        if (os == null) {
            return always();
        }
        return new NDependencyOsFilter(getSession()).add(os);
    }

    @Override
    public NDependencyFilter byCurrentDesktop() {
        checkSession();
        return byDesktop(NEnvs.of(getSession()).getDesktopEnvironmentFamilies());
    }

    public NDependencyFilter byCurrentArch() {
        checkSession();
        return byArch(NEnvs.of(getSession()).getArchFamily());
    }

    @Override
    public NDependencyFilter byCurrentOs() {
        checkSession();
        return byOs(NEnvs.of(getSession()).getOsFamily());
    }

    @Override
    public NDependencyFilter byRegularType() {
        return byType(null).or(byType("jar"));
    }

    public NDependencyFilter byCurrentEnv() {
        return byCurrentOs()
                .and(byCurrentArch())
                .and(byCurrentDesktop())
                ;
    }

    @Override
    public NDependencyFilter byRunnable(boolean optional) {
        return byScope(NDependencyScopePattern.RUN)
                .and(byOptional(optional?null:false))
                .and(byRegularType())
                .and(byCurrentEnv());
    }

    @Override
    public NDependencyFilter byRunnable() {
        return byRunnable(false);
    }

    @Override
    public NDependencyFilter byOs(NOsFamily os) {
        checkSession();
        if (os == null) {
            return always();
        }
        return new NDependencyOsFilter(getSession()).add(Arrays.asList(os));
    }

    @Override
    public NDependencyFilter byOs(NOsFamily... os) {
        checkSession();
        checkSession();
        if (os == null) {
            return always();
        }
        return new NDependencyOsFilter(getSession()).add(Arrays.asList(os));
    }

    @Override
    public NDependencyFilter byDesktop(NDesktopEnvironmentFamily de) {
        checkSession();
        if (de == null) {
            return always();
        }
        return new NDependencyDEFilter(getSession()).add(Arrays.asList(de));
    }

    @Override
    public NDependencyFilter byDesktop(NDesktopEnvironmentFamily... de) {
        checkSession();
        checkSession();
        if (de == null) {
            return always();
        }
        return new NDependencyDEFilter(getSession()).add(Arrays.asList(de));
    }

    @Override
    public NDependencyFilter byDesktop(Collection<NDesktopEnvironmentFamily> de) {
        return byDesktop(de.toArray(new NDesktopEnvironmentFamily[0]));
    }

    @Override
    public NDependencyFilter byType(String type) {
        return new NDependencyTypeFilter(getSession(), type);
    }

    @Override
    public NDependencyFilter byOs(String os) {
        checkSession();
        if (os == null) {
            return always();
        }
        return new NDependencyOsFilter(getSession(), os);
    }

    @Override
    public NDependencyFilter byOsDist(String osDist) {
        checkSession();
        if (osDist == null) {
            return always();
        }
        return new NDependencyOsDistIdFilter(getSession()).add(Collections.singletonList(NId.of(osDist).get( getSession())));
    }

    @Override
    public NDependencyFilter byOsDist(String... osDists) {
        checkSession();
        if (osDists == null || osDists.length==0) {
            return always();
        }
        return new NDependencyOsDistIdFilter(getSession()).add(
                Arrays.stream(osDists).map(x-> NId.of(x).get(getSession()))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public NDependencyFilter byOsDist(Collection<String> osDists) {
        checkSession();
        if (osDists == null || osDists.isEmpty()) {
            return always();
        }
        return new NDependencyOsDistIdFilter(getSession()).add(
                osDists.stream().map(x-> NId.of(x).get(getSession()))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public NDependencyFilter byPlatform(NPlatformFamily... pf) {
        checkSession();
        if (pf == null || pf.length==0) {
            return always();
        }
        return new NDependencyPlatformFamilyFilter(getSession()).add(Arrays.asList(pf));
    }

    @Override
    public NDependencyFilter byPlatform(String... pf) {
        checkSession();
        if (pf == null || pf.length==0) {
            return always();
        }
        return new NDependencyPlatformIdFilter(getSession()).add(
                Arrays.stream(pf).map(x-> NId.of(x).get(getSession()))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
