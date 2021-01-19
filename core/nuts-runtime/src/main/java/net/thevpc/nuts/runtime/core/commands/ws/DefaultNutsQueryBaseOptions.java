/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.commands.ws;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.util.NutsDependencyScopes;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.NutsWorkspaceCommandBase;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @param <T> Type
 * @author thevpc
 */
public abstract class DefaultNutsQueryBaseOptions<T extends NutsWorkspaceCommand> extends NutsWorkspaceCommandBase<T> {

    private boolean failFast = false;
    private Boolean optional = null;
    private Set<NutsDependencyScope> scope = EnumSet.noneOf(NutsDependencyScope.class);
    private boolean content = false;
    private boolean inlineDependencies = false;
    private boolean dependencies = false;
    private boolean effective = false;
    private Path location = null;
    private final List<String> repos = new ArrayList<>();
    private NutsFetchDisplayOptions displayOptions;
    protected NutsDependencyFilter dependencyFilter;

//    private Boolean transitive = true;
//    private Boolean cached = true;
//    private Boolean indexed = null;
//    private NutsFetchStrategy fetchStrategy = null;

    public DefaultNutsQueryBaseOptions(NutsWorkspace ws, String name) {
        super(ws, name);
//        this.session=ws.createSession();
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
            this.effective = other.isEffective();
            this.scope = EnumSet.copyOf(other.getScope());
            this.location = other.getLocation();
            this.repos.clear();
            this.repos.addAll(Arrays.asList(other.getRepositories()));
            this.dependencyFilter = other.getDependencyFilter();

        }
        return (T) this;
    }

    //@Override
    public boolean isCached() {
        return getValidWorkspaceSession().isCached();
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
        getValidWorkspaceSession().setCached(cached);
        return (T) this;
    }

    //@Override
    public boolean isTransitive() {
        return getValidWorkspaceSession().isTransitive();
    }

    //@Override
    public T setTransitive(boolean transitive) {
        getValidWorkspaceSession().setTransitive(transitive);
        return (T) this;
    }

    //@Override
    public T fetchStrategy(NutsFetchStrategy mode) {
        return setFetchStrategy(mode);
    }

    //@Override
    public T setFetchStrategy(NutsFetchStrategy mode) {
        getValidWorkspaceSession().setFetchStrategy(mode);
        return (T) this;
    }

    //@Override
    public T setRemote() {
        return setFetchStrategy(NutsFetchStrategy.REMOTE);
    }

    //@Override
    public T setOffline() {
        return setFetchStrategy(NutsFetchStrategy.OFFLINE);
    }

    //@Override
    public T setOnline() {
        return setFetchStrategy(NutsFetchStrategy.ONLINE);
    }

//    //@Override
//    public T installed() {
//        return setFetchStrategy(NutsFetchStrategy.INSTALLED);
//    }

    //@Override
    public T setAnyWhere() {
        return setFetchStrategy(NutsFetchStrategy.ANYWHERE);
    }

    //@Override
    public NutsFetchStrategy getFetchStrategy() {
        return getValidWorkspaceSession().getFetchStrategy();
    }

    //@Override
    public boolean isIndexed() {
        return getValidWorkspaceSession().isIndexed();
    }

    public T indexed(Boolean indexEnabled) {
        return setIndexed(indexEnabled);
    }

    //@Override
    public T setIndexed(Boolean indexEnabled) {
        getValidWorkspaceSession().setIndexed(indexEnabled);
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

    public T addRepositories(Collection<String> values) {
        if (values != null) {
            addRepositories(values.toArray(new String[0]));
        }
        return (T) this;
    }

    public T removeRepository(String value) {
        repos.remove(value);
        return (T) this;
    }

    public T addRepositories(String... values) {
        if (values != null) {
            repos.addAll(Arrays.asList(values));
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
        boolean enabled = a.isEnabled();
        switch (a.getStringKey()) {
            case "--failfast": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    this.setFailFast(val);
                }
                return true;
            }
            case "-r":
            case "--repository": {
                String val = cmdLine.nextString().getStringValue();
                if (enabled) {
                    this.addRepository(val);
                }
                return true;
            }
            case "-f":
            case "--fetch": {
                String val = cmdLine.nextString().getStringValue();
                if (enabled) {
                    this.setFetchStrategy(NutsFetchStrategy.valueOf(val.toUpperCase().replace("-", "_")));
                }
                return true;
            }
            case "--dependencies": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    this.setDependencies(val);
                }
                return true;
            }
            case "--scope": {
                NutsDependencyScope val = CoreCommonUtils.parseEnumString(cmdLine.nextString().getStringValue(), NutsDependencyScope.class, false);
                if (enabled) {
                    this.addScope(val);
                }
                return true;
            }
            case "-a":
            case "--anywhere": {
                cmdLine.skip();
                if (enabled) {
                    this.setFetchStrategy(NutsFetchStrategy.ANYWHERE);
                }
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
            case "--offline": {
                cmdLine.skip();
                if (enabled) {
                    this.setFetchStrategy(NutsFetchStrategy.OFFLINE);
                }
                return true;
            }
            case "-O":
            case "--online": {
                cmdLine.skip();
                if (enabled) {
                    this.setFetchStrategy(NutsFetchStrategy.ONLINE);
                }
                return true;
            }
            case "-R":
            case "--remote": {
                cmdLine.skip();
                if (enabled) {
                    this.setFetchStrategy(NutsFetchStrategy.REMOTE);
                }
                return true;
            }
            case "--optional": {
                NutsArgument v = cmdLine.nextString();
                if (enabled) {
                    this.setOptional(CoreCommonUtils.parseBoolean(v.getString(), null));
                }
                return true;
            }
            case "--cached": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    this.setCached(val);
                }
                return true;
            }
            case "--effective": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    this.setEffective(val);
                }
                return true;
            }
            case "--indexed": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    this.setIndexed(val);
                }
                return true;
            }
            case "--content": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    this.setContent(val);
                }
                return true;
            }
            case "--location": {
                String location = cmdLine.nextString().getStringValue();
                if(enabled) {
                    this.setLocation(CoreStringUtils.isBlank(location) ? null : Paths.get(location));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "failFast=" + failFast +
                ", optional=" + optional +
                ", scope=" + scope +
                ", content=" + content +
                ", dependencyFilter=" + dependencyFilter +
                ", inlineDependencies=" + inlineDependencies +
                ", dependencies=" + dependencies +
                ", effective=" + effective +
                ", location=" + location +
                ", repos=" + repos +
                ", displayOptions=" + displayOptions +
                ", session=" + getSession() +
                ')';
    }

//    @Override
    public T setDependencyFilter(NutsDependencyFilter filter) {
        this.dependencyFilter = filter;
        return (T)this;
    }

//    @Override
    public NutsDependencyFilter getDependencyFilter() {
        return dependencyFilter;
    }

//    @Override
    public T setDependencyFilter(String filter) {
        this.dependencyFilter = ws.dependency().filter().byExpression(filter);
        return (T)this;
    }
}
