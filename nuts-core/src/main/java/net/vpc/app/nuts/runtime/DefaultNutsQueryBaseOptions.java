/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.format.NutsFetchDisplayOptions;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.NutsDependencyScopes;
import net.vpc.app.nuts.runtime.wscommands.NutsWorkspaceCommandBase;

/**
 *
 * @author vpc
 * @param <T>
 */
public abstract class DefaultNutsQueryBaseOptions<T extends NutsWorkspaceCommand> extends NutsWorkspaceCommandBase<T> {

    private boolean failFast = false;
    private Boolean optional = null;
    private Set<NutsDependencyScope> scope = EnumSet.noneOf(NutsDependencyScope.class);
    private boolean content = false;
    private boolean inlineDependencies = false;
    private boolean dependencies = false;
    private boolean dependenciesTree = false;
    private boolean effective = false;
    private Path location = null;
    private final List<String> repos = new ArrayList<>();
    private NutsFetchDisplayOptions displayOptions;

//    private Boolean transitive = true;
//    private Boolean cached = true;
//    private Boolean indexed = null;
//    private NutsFetchStrategy fetchStrategy = null;

    public DefaultNutsQueryBaseOptions(NutsWorkspace ws, String name) {
        super(ws, name);
        this.session=ws.createSession();
        displayOptions = new NutsFetchDisplayOptions(ws);
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
            this.dependenciesTree = other.isDependenciesTree();
            this.effective = other.isEffective();
            this.scope = EnumSet.copyOf(other.getScope());
            this.location = other.getLocation();
            this.repos.clear();
            this.repos.addAll(Arrays.asList(other.getRepositories()));

        }
        return (T) this;
    }

    //@Override
    public boolean isCached() {
        return getSession().isCached();
    }

    //@Override
    public T cached() {
        return cached(true);
    }

    //@Override
    public T cached(boolean cached) {
        return setCached(cached);
    }

    //@Override
    public T setCached(boolean cached) {
        getSession().setCached(cached);
        return (T) this;
    }

    //@Override
    public boolean isTransitive() {
        return getSession().isTransitive();
    }

    //@Override
    public T setTransitive(boolean transitive) {
        getSession().setTransitive(transitive);
        return (T) this;
    }

    //@Override
    public T transitive(boolean transitive) {
        return setTransitive(transitive);
    }

    //@Override
    public T transitive() {
        return setTransitive(true);
    }

    //@Override
    public T fetchStrategy(NutsFetchStrategy mode) {
        return setFetchStrategy(mode);
    }

    //@Override
    public T setFetchStrategy(NutsFetchStrategy mode) {
        getSession().setFetchStrategy(mode);
        return (T) this;
    }

    //@Override
    public T remote() {
        return setFetchStrategy(NutsFetchStrategy.REMOTE);
    }

    //@Override
    public T offline() {
        return setFetchStrategy(NutsFetchStrategy.OFFLINE);
    }

    //@Override
    public T online() {
        return setFetchStrategy(NutsFetchStrategy.ONLINE);
    }

//    //@Override
//    public T installed() {
//        return setFetchStrategy(NutsFetchStrategy.INSTALLED);
//    }

    //@Override
    public T anyWhere() {
        return setFetchStrategy(NutsFetchStrategy.ANYWHERE);
    }

    //@Override
    public NutsFetchStrategy getFetchStrategy() {
        return getSession().getFetchStrategy();
    }

    //@Override
    public boolean isIndexed() {
        return getSession().isIndexed();
    }

    public T indexed(Boolean indexEnabled) {
        return setIndexed(indexEnabled);
    }

    //@Override
    public T setIndexed(Boolean indexEnabled) {
        getSession().setIndexed(indexEnabled);
        return (T) this;
    }

    //@Override
    public T indexed() {
        return setIndexed(true);
    }

    //@Override
    public T indexed(boolean indexed) {
        return setIndexed(indexed);
    }

    //@Override
    public Boolean getOptional() {
        return optional;
    }

    public T optional() {
        return optional(true);
    }

    //@Override
    public T optional(Boolean acceptOptional) {
        return DefaultNutsQueryBaseOptions.this.setOptional(acceptOptional);
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
//    public T addScopes(Collection<NutsDependencyScope> scope) {
//        this.scope = NutsDependencyScopes.add(this.scope, scope);
//        return (T) this;
//    }
    public T scope(NutsDependencyScope scope) {
        return addScope(scope);
    }

    public T scope(NutsDependencyScopePattern scope) {
        return addScope(scope);
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

    public T scopes(NutsDependencyScope... scopes) {
        return addScopes(scopes);
    }

    public T scopes(NutsDependencyScopePattern... scopes) {
        return addScopes(scopes);
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

    public T content(boolean includeContent) {
        return (T) setContent(includeContent);
    }

    public T content() {
        return (T) setContent(true);
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
    public T effective(boolean effective) {
        return setEffective(effective);
    }

    //@Override
    public T effective() {
        return setEffective(true);
    }

    //@Override
    public boolean isInlineDependencies() {
        return inlineDependencies;
    }

    //@Override
    public T inlineDependencies() {
        return setInlineDependencies(true);
    }

    //@Override
    public T setInlineDependencies(boolean include) {
        inlineDependencies = include;
        return (T) this;
    }

    //@Override
    public T inlineDependencies(boolean include) {
        return setInlineDependencies(include);
    }

    public boolean isDependencies() {
        return dependencies;
    }

    //@Override
    public T dependencies() {
        return setDependencies(true);
    }

    //@Override
    public T setDependencies(boolean include) {
        dependencies = include;
        return (T) this;
    }

    //@Override
    public T dependencies(boolean include) {
        return setDependencies(include);
    }

    public boolean isDependenciesTree() {
        return dependenciesTree;
    }

    //@Override
    public T dependenciesTree() {
        return setDependenciesTree(true);
    }

    //@Override
    public T setDependenciesTree(boolean include) {
        dependenciesTree = include;
        return (T) this;
    }

    //@Override
    public T dependenciesTree(boolean include) {
        return setDependenciesTree(include);
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
    public T location(Path location) {
        return setLocation(location);
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

    public T failFast() {
        return failFast(true);
    }

    public T failFast(boolean enable) {
        return setFailFast(enable);
    }

    public T repositories(Collection<String> value) {
        return addRepositories(value);
    }

    public T repositories(String... values) {
        return addRepositories(values);
    }

    public T addRepositories(Collection<String> value) {
        if (value != null) {
            addRepositories(value.toArray(new String[0]));
        }
        return (T) this;
    }

    public T removeRepository(String value) {
        repos.remove(value);
        return (T) this;
    }

    public T addRepositories(String... value) {
        if (value != null) {
            repos.addAll(Arrays.asList(value));
        }
        return (T) this;
    }

    public T clearRepositories() {
        repos.clear();
        return (T) this;
    }

    public T addRepository(String value) {
        repos.add(value);
        return (T) this;
    }

    public T repository(String value) {
        return addRepository(value);
    }

    public String[] getRepositories() {
        return repos.toArray(new String[0]);
    }

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
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "--failfast": {
                this.setFailFast(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "-r":
            case "--repository": {
                this.addRepository(cmdLine.nextString().getStringValue());
                return true;
            }
            case "-f":
            case "--fetch": {
                this.setFetchStrategy(NutsFetchStrategy.valueOf(cmdLine.nextString().getStringValue().toUpperCase().replace("-", "_")));
                return true;
            }
            case "--dependencies": {
                this.dependencies(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--dependencies-tree": {
                this.dependenciesTree(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--scope": {
                this.addScope(CoreCommonUtils.parseEnumString(cmdLine.nextString().getStringValue(), NutsDependencyScope.class, false));
                return true;
            }
            case "-a": 
            case "--anywhere": 
            {
                cmdLine.skip();
                this.setFetchStrategy(NutsFetchStrategy.ANYWHERE);
                return true;
            }
//            case "-i":
//            case "--installed":
//            {
//                cmdLine.skip();
//                this.setFetchStrategy(NutsFetchStrategy.INSTALLED);
//                return true;
//            }
            case "-F":
            case "--offline":
            {
                cmdLine.skip();
                this.setFetchStrategy(NutsFetchStrategy.OFFLINE);
                return true;
            }
            case "-O": 
            case "--online": 
            {
                cmdLine.skip();
                this.setFetchStrategy(NutsFetchStrategy.ONLINE);
                return true;
            }
            case "-R": 
            case "--remote": 
            {
                cmdLine.skip();
                this.setFetchStrategy(NutsFetchStrategy.REMOTE);
                return true;
            }
            case "--optional": {
                NutsArgument v = cmdLine.nextString();
                this.setOptional(CoreCommonUtils.parseBoolean(v.getString(), null));
                return true;
            }
            case "--cached": {
                this.setCached(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--effective": {
                this.setEffective(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--indexed": {
                this.setIndexed(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--content": {
                this.setContent(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--location": {
                String location = cmdLine.nextString().getStringValue();
                this.setLocation(CoreStringUtils.isBlank(location) ? null : Paths.get(location));
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"(" +
                "failFast=" + failFast +
                ", optional=" + optional +
                ", scope=" + scope +
                ", content=" + content +
                ", inlineDependencies=" + inlineDependencies +
                ", dependencies=" + dependencies +
                ", dependenciesTree=" + dependenciesTree +
                ", effective=" + effective +
                ", location=" + location +
                ", repos=" + repos +
                ", displayOptions=" + displayOptions +
                ", session=" + getSession() +
                ')';
    }
}
