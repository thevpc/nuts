/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.runtime.standalone.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.dependency.NutsDependencyScopes;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @param <T> Type
 * @author thevpc
 */
public abstract class DefaultNutsQueryBaseOptions<T extends NutsWorkspaceCommand> extends NutsWorkspaceCommandBase<T> {

//    private final List<String> repos = new ArrayList<>();
    protected NutsDependencyFilter dependencyFilter;
    private boolean failFast = false;
    private Boolean optional = null;
    private Set<NutsDependencyScope> scope = EnumSet.noneOf(NutsDependencyScope.class);
    private boolean content = false;
    private boolean inlineDependencies = false;
    private boolean dependencies = false;
    private boolean effective = false;
    private Path location = null;
    private NutsFetchDisplayOptions displayOptions;
    private NutsRepositoryFilter repositoryFilter;

    //    private Boolean transitive = true;
//    private Boolean cached = true;
//    private Boolean indexed = null;
//    private NutsFetchStrategy fetchStrategy = null;
    public DefaultNutsQueryBaseOptions(NutsWorkspace ws, String name) {
        super(ws, name);
//        this.session=ws.createSession();
        displayOptions = new NutsFetchDisplayOptions(NutsSessionUtils.defaultSession(ws));
    }

    //@Override
    public T copyFromDefaultNutsQueryBaseOptions(DefaultNutsQueryBaseOptions other) {
        if (other != null) {
            super.copyFromWorkspaceCommandBase(other);
            this.optional = other.getOptional();
            this.failFast = other.isFailFast();
//            this.fetchStrategy = other.getFetchStrategy();
//            this.indexed = other.getIndexed();
//            this.transitive = other.isTransitive();
//            this.cached = other.isCached();
            this.content = other.isContent();
            this.inlineDependencies = other.isInlineDependencies();
            this.dependencies = other.isDependencies();
            this.effective = other.isEffective();
            this.scope = EnumSet.copyOf(other.getScope());
            this.location = other.getLocation();
//            this.repos.clear();
//            this.repos.addAll(Arrays.asList(other.getRepositories()));
            this.dependencyFilter = other.getDependencyFilter();
            this.repositoryFilter = other.getRepositoryFilter();

        }
        return (T) this;
    }

    //@Override
    public Boolean getOptional() {
        return optional;
    }

    //@Override
    public T setOptional(Boolean acceptOptional) {
        this.optional = acceptOptional;
        return (T) this;
    }

    //    //@Override
//    public T scopes(Collection<NutsDependencyScope> scope) {
//        return DefaultNutsQueryBaseOptions.this.addScopes(scope);
//    }
    //@Override
    public T clearScopes() {
        this.scope = EnumSet.noneOf(NutsDependencyScope.class);
        return (T) this;
    }

    //@Override
    public T addScope(NutsDependencyScope scope) {
        this.scope = NutsDependencyScopes.add(this.scope, scope);
        return (T) this;
    }

    public T addScope(NutsDependencyScopePattern scope) {
        this.scope = NutsDependencyScopes.add(this.scope, scope);
        return (T) this;
    }

    //@Override
    public T addScopes(NutsDependencyScope... scope) {
        this.scope = NutsDependencyScopes.add(this.scope, scope);
        return (T) this;
    }

    public T addScopes(NutsDependencyScopePattern... scope) {
        this.scope = NutsDependencyScopes.add(this.scope, scope);
        return (T) this;
    }

    //@Override
    public T removeScopes(NutsDependencyScope... scope) {
        this.scope = NutsDependencyScopes.remove(this.scope, scope);
        return (T) this;
    }

    public T removeScopes(NutsDependencyScopePattern... scope) {
        this.scope = NutsDependencyScopes.remove(this.scope, scope);
        return (T) this;
    }

    //@Override
    public T removeScope(NutsDependencyScope scope) {
        this.scope = NutsDependencyScopes.remove(this.scope, scope);
        return (T) this;
    }

    public T removeScope(NutsDependencyScopePattern scope) {
        this.scope = NutsDependencyScopes.remove(this.scope, scope);
        return (T) this;
    }

    //@Override
    public Set<NutsDependencyScope> getScope() {
        return scope;
    }

    //@Override
    public T setScope(NutsDependencyScope scope) {
        return setScope(scope == null ? null : EnumSet.of(scope));
    }

    //@Override
    public T setScope(NutsDependencyScope... scope) {
        return setScope(scope == null ? null : EnumSet.<NutsDependencyScope>copyOf(Arrays.asList(scope)));
    }

    //@Override
    public T setScope(Collection<NutsDependencyScope> scope) {
        this.scope = scope == null ? EnumSet.noneOf(NutsDependencyScope.class) : EnumSet.<NutsDependencyScope>copyOf(scope);
        return (T) this;
    }

    //@Override
    public boolean isContent() {
        return content;
    }

    //@Override
    public T setContent(boolean includeContent) {
        this.content = includeContent;
        return (T) this;
    }

    //@Override
    public boolean isEffective() {
        return effective;
    }

    //@Override
    public T setEffective(boolean includeEffectiveDesc) {
        this.effective = includeEffectiveDesc;
        return (T) this;
    }

    //@Override
    public boolean isInlineDependencies() {
        return inlineDependencies;
    }

    //@Override
    public T setInlineDependencies(boolean include) {
        inlineDependencies = include;
        return (T) this;
    }

    public boolean isDependencies() {
        return dependencies;
    }

    //@Override
    public T setDependencies(boolean include) {
        dependencies = include;
        return (T) this;
    }

    //@Override
    public Path getLocation() {
        return location;
    }

    //@Override
    public T setLocation(Path location) {
        this.location = location;
        return (T) this;
    }

    //@Override
    public T setDefaultLocation() {
        this.location = null;
        return (T) this;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public T setFailFast(boolean enable) {
        this.failFast = enable;
        return (T) this;
    }

//    public T addRepositories(Collection<String> values) {
//        if (values != null) {
//            addRepositories(values.toArray(new String[0]));
//        }
//        return (T) this;
//    }

//    public T removeRepository(String value) {
//        repos.remove(value);
//        return (T) this;
//    }
//
//    public T addRepositories(String... values) {
//        if (values != null) {
//            repos.addAll(Arrays.asList(values));
//        }
//        return (T) this;
//    }
//
//    public T clearRepositories() {
//        repos.clear();
//        return (T) this;
//    }
//
//    public T addRepository(String value) {
//        repos.add(value);
//        return (T) this;
//    }
//
//    public String[] getRepositories() {
//        return repos.toArray(new String[0]);
//    }

    public NutsFetchDisplayOptions getDisplayOptions() {
        return displayOptions;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        if (super.configureFirst(cmdLine)) {
            return true;
        }
        if (getDisplayOptions().configureFirst(cmdLine)) {
            return true;
        }
        NutsArgument a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        switch(a.key()) {
            case "--failfast": {
                cmdLine.withNextBoolean((v,r,s)->this.setFailFast(v));
                return true;
            }
            case "-r":
            case "--repository": {
                cmdLine.withNextString((v,r,s)->addRepositoryFilter(NutsRepositoryFilters.of(getSession()).byName(v)));
                return true;
            }
            case "--dependencies": {
                cmdLine.withNextBoolean((v,r,s)->this.setDependencies(v));
                return true;
            }
            case "--scope": {
                cmdLine.withNextString((v,r,s)->this.addScope(NutsDependencyScopePattern.parse(v).orElse(NutsDependencyScopePattern.API)));
                return true;
            }

//            case "-i":
//            case "--installed":
//            {
//                cmdLine.skip();
//                this.setFetchStrategy(NutsFetchStrategy.INSTALLED);
//                return true;
//            }
            case "--optional": {
                cmdLine.withNextValue((v,r,s)->this.setOptional(
                        NutsValue.of(v.asString().get(session)).asBoolean()
                                .orNull()));
                return true;
            }
            case "--effective": {
                cmdLine.withNextBoolean((v,r,s)->this.setEffective(v));
                return true;
            }
            case "--content": {
                cmdLine.withNextBoolean((v,r,s)->this.setContent(v));
                return true;
            }
            case "--location": {
                cmdLine.withNextString((v,r,s)->this.setLocation(NutsBlankable.isBlank(v) ? null : Paths.get(v)));
                return true;
            }
        }
        return false;
    }

    //    @Override
    public NutsRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

//    @Override
    public T setRepositoryFilter(String filter) {
        checkSession();
        if(NutsBlankable.isBlank(filter)){
            this.repositoryFilter = null;
        }else {
            this.repositoryFilter = getSession().repos().filter().byName(filter);
        }
        return (T) this;
    }
//    @Override
    public T setRepositoryFilter(NutsRepositoryFilter filter) {
        this.repositoryFilter = filter;
        return (T) this;
    }

//    @Override
    public T addRepositoryFilter(NutsRepositoryFilter filter) {
        if (filter != null) {
            if (this.repositoryFilter == null) {
                this.repositoryFilter = filter;
            } else {
                this.repositoryFilter = this.repositoryFilter.and(filter);
            }
        }
        return (T) this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "("
                + "failFast=" + failFast
                + ", optional=" + optional
                + ", scope=" + scope
                + ", content=" + content
                + ", dependencyFilter=" + dependencyFilter
                + ", inlineDependencies=" + inlineDependencies
                + ", dependencies=" + dependencies
                + ", effective=" + effective
                + ", location=" + location
//                + ", repos=" + repos
                + ", displayOptions=" + displayOptions
                + ", session=" + getSession()
                + ')';
    }

    //    @Override
    public T setDependencyFilter(NutsDependencyFilter filter) {
        this.dependencyFilter = filter;
        return (T) this;
    }

    //    @Override
    public NutsDependencyFilter getDependencyFilter() {
        return dependencyFilter;
    }

    //    @Override
    public T setDependencyFilter(String filter) {
        checkSession();
        this.dependencyFilter = NutsDependencyFilters.of(getSession()).parse(filter);
        return (T) this;
    }
}
