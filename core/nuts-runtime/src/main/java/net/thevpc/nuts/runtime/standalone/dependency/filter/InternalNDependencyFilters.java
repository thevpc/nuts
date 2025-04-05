package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.runtime.standalone.util.filters.InternalNTypedFilters;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NFilter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InternalNDependencyFilters extends InternalNTypedFilters<NDependencyFilter>
        implements NDependencyFilters {

    public InternalNDependencyFilters(NWorkspace workspace) {
        super(NDependencyFilter.class);
//        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
    }

    @Override
    public NDependencyFilter always() {
        return new NDependencyFilterTrue();
    }

    @Override
    public NDependencyFilter never() {
        return new NDependencyFilterFalse();
    }

    @Override
    public NDependencyFilter all(NFilter... others) {
        List<NDependencyFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NDependencyFilterAnd(all.toArray(new NDependencyFilter[0])).simplify();
    }

    @Override
    public NDependencyFilter any(NFilter... others) {
        List<NDependencyFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new NDependencyFilterOr(all.toArray(new NDependencyFilter[0])).simplify();
    }

    @Override
    public NDependencyFilter not(NFilter other) {
        return new NDependencyFilterNone((NDependencyFilter) other);
    }

    @Override
    public NDependencyFilter none(NFilter... others) {
        List<NDependencyFilter> all = convertList(others);
        if (all.isEmpty()) {
            return always();
        }
        return new NDependencyFilterNone(all.toArray(new NDependencyFilter[0])).simplify();
    }

    @Override
    public NDependencyFilter from(NFilter a) {
        if (a == null) {
            return null;
        }
        NDependencyFilter t = as(a);
        NAssert.requireNonNull(t, "InstallDependencyFilter");
        return t;
    }

    @Override
    public NDependencyFilter as(NFilter a) {
        if (a instanceof NDependencyFilter) {
            return (NDependencyFilter) a;
        }
        return null;
    }

    @Override
    public NDependencyFilter parse(String expression) {
        return new NDependencyFilterParser(expression).parse();
    }

    @Override
    public NDependencyFilter nonnull(NFilter filter) {
        return super.nonnull(filter);
    }

    @Override
    public NDependencyFilter byScope(NDependencyScopePattern ... scopes) {
        if (scopes == null || scopes.length == 0) {
            return always();
        }
        return new ScopeNDependencyFilter(scopes).simplify();
    }

    @Override
    public NDependencyFilter byScope(NDependencyScope scope) {
        if (scope == null) {
            return always();
        }
        return new NDependencyScopeFilter().add(Arrays.asList(scope)).simplify();
    }

    @Override
    public NDependencyFilter byScope(NDependencyScope... scopes) {
        if (scopes == null) {
            return always();
        }
        return new NDependencyScopeFilter().add(Arrays.asList(scopes)).simplify();
    }

    @Override
    public NDependencyFilter byScope(Collection<NDependencyScope> scopes) {
        if (scopes == null) {
            return always();
        }
        return new NDependencyScopeFilter().add(scopes).simplify();
    }

    @Override
    public NDependencyFilter byOptional(Boolean optional) {
        if (optional == null) {
            return always();
        }
        return new NDependencyOptionFilter(optional).simplify();
    }

    @Override
    public NDependencyFilter byExclude(NDependencyFilter filter, String[] exclusions) {
        return new NExclusionDependencyFilter(filter, Arrays.stream(exclusions).map(x -> NId.get(x).get()).toArray(NId[]::new)).simplify();
    }

    @Override
    public NDependencyFilter byArch(Collection<NArchFamily> archs) {
        if (archs == null) {
            return always();
        }
        return new NDependencyArchFamilyFilter().add(archs).simplify();
    }

    @Override
    public NDependencyFilter byArch(NArchFamily arch) {
        if (arch == null) {
            return always();
        }
        return new NDependencyArchFamilyFilter().add(Arrays.asList(arch)).simplify();
    }

    @Override
    public NDependencyFilter byArch(NArchFamily... archs) {
        if (archs == null) {
            return always();
        }
        return new NDependencyArchFamilyFilter().add(Arrays.asList(archs)).simplify();
    }

    @Override
    public NDependencyFilter byArch(String arch) {
        if (arch == null) {
            return always();
        }
        return new NDependencyArchFamilyFilter(arch);
    }

    @Override
    public NDependencyFilter byOs(Collection<NOsFamily> os) {
        if (os == null) {
            return always();
        }
        return new NDependencyOsFilter().add(os).simplify();
    }

    @Override
    public NDependencyFilter byCurrentDesktop() {
        return byDesktop(NWorkspace.of().getDesktopEnvironmentFamilies());
    }

    public NDependencyFilter byCurrentArch() {
        return byArch(NWorkspace.of().getArchFamily());
    }

    @Override
    public NDependencyFilter byCurrentOs() {
        return byOs(NWorkspace.of().getOsFamily());
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
    public NDependencyFilter byRunnable(boolean optional,boolean anyEnv) {
        return byScope(NDependencyScopePattern.RUN)
                .and(byOptional(optional?null:false))
                .and(byRegularType())
                .and(anyEnv?null:byCurrentEnv());
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
        if (os == null) {
            return always();
        }
        return new NDependencyOsFilter().add(Arrays.asList(os)).simplify();
    }

    @Override
    public NDependencyFilter byOs(NOsFamily... os) {
        if (os == null) {
            return always();
        }
        return new NDependencyOsFilter().add(Arrays.asList(os)).simplify();
    }

    @Override
    public NDependencyFilter byDesktop(NDesktopEnvironmentFamily de) {
        if (de == null) {
            return always();
        }
        return new NDependencyDEFilter().add(Arrays.asList(de)).simplify();
    }

    @Override
    public NDependencyFilter byDesktop(NDesktopEnvironmentFamily... de) {
        if (de == null) {
            return always();
        }
        return new NDependencyDEFilter().add(Arrays.asList(de)).simplify();
    }

    @Override
    public NDependencyFilter byDesktop(Collection<NDesktopEnvironmentFamily> de) {
        return byDesktop(de.toArray(new NDesktopEnvironmentFamily[0]));
    }

    @Override
    public NDependencyFilter byType(String type) {
        return new NDependencyTypeFilter(type).simplify();
    }

    @Override
    public NDependencyFilter byOs(String os) {
        if (os == null) {
            return always();
        }
        return new NDependencyOsFilter(os).simplify();
    }

    @Override
    public NDependencyFilter byOsDist(String osDist) {
        if (osDist == null) {
            return always();
        }
        return new NDependencyOsDistIdFilter().add(Collections.singletonList(NId.get(osDist).get())).simplify();
    }

    @Override
    public NDependencyFilter byOsDist(String... osDists) {
        if (osDists == null || osDists.length==0) {
            return always();
        }
        return new NDependencyOsDistIdFilter().add(
                Arrays.stream(osDists).map(x-> NId.get(x).get())
                        .collect(Collectors.toList())
        ).simplify();
    }

    @Override
    public NDependencyFilter byOsDist(Collection<String> osDists) {
        if (osDists == null || osDists.isEmpty()) {
            return always();
        }
        return new NDependencyOsDistIdFilter().add(
                osDists.stream().map(x-> NId.get(x).get())
                        .collect(Collectors.toList())
        ).simplify();
    }

    @Override
    public NDependencyFilter byPlatform(NPlatformFamily... pf) {
        if (pf == null || pf.length==0) {
            return always();
        }
        return new NDependencyPlatformFamilyFilter().add(Arrays.asList(pf)).simplify();
    }

    @Override
    public NDependencyFilter byPlatform(String... pf) {
        if (pf == null || pf.length==0) {
            return always();
        }
        return new NDependencyPlatformIdFilter().add(
                Arrays.stream(pf).map(x-> NId.get(x).get())
                        .collect(Collectors.toList())
        ).simplify();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
