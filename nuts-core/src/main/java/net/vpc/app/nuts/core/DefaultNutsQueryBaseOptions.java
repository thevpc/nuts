/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import net.vpc.app.nuts.NutsDependencyScope;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsFetchStrategy;
import net.vpc.app.nuts.NutsQueryBaseOptions;

/**
 *
 * @author vpc
 * @param <T>
 */
public class DefaultNutsQueryBaseOptions<T extends NutsQueryBaseOptions> implements NutsQueryBaseOptions<T> {


    private boolean ignoreCache = false;
    private boolean transitive = true;
    private boolean cached = true;
    private Boolean indexEnabled = null;
    private NutsFetchStrategy mode = null;
    private NutsSession session;
    private Boolean acceptOptional = null;
    private Set<NutsDependencyScope> scope = EnumSet.noneOf(NutsDependencyScope.class);
    private boolean includeContent = true;
    private boolean includeDependencies = false;
    private boolean includeEffectiveDesc = false;
    private boolean includeInstallInfo = true;
    private Path location = null;

    


    @Override
    public T copyFrom(NutsQueryBaseOptions other) {
        if (other != null) {
            this.acceptOptional = other.getAcceptOptional();
            this.session = other.getSession();
            this.ignoreCache = other.isIgnoreCache();
            this.mode = other.getFetchStrategy();
            this.indexEnabled = other.getIndexEnabled();
            this.includeContent = other.isIncludeFile();
            this.includeDependencies = other.isIncludeDependencies();
            this.includeEffectiveDesc = other.isIncludeEffective();
            this.includeInstallInfo = other.isIncludeInstallInformation();
            this.scope = EnumSet.copyOf(other.getScope());
            this.includeEffectiveDesc = other.isIncludeEffective();
            this.includeInstallInfo = other.isIncludeInstallInformation();
            this.includeContent = other.isIncludeFile();
            this.includeDependencies = other.isIncludeDependencies();
            this.transitive = other.isTransitive();
            this.cached = other.isCached();
            this.location = other.getLocation();
        }
        return (T) this;
    }

    @Override
    public boolean isCached() {
        return cached;
    }

    @Override
    public T setCached(boolean cached) {
        this.cached = cached;
        return (T) this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public T setTransitive(boolean transitive) {
        this.transitive = transitive;
        return (T) this;
    }

    @Override
    public T transitive(boolean transitive) {
        return setTransitive(transitive);
    }

    @Override
    public T transitive() {
        return setTransitive(true);
    }

    @Override
    public T setFetchStratery(NutsFetchStrategy mode) {
        this.mode = mode;
        return (T) this;
    }

    @Override
    public T remote() {
        return setFetchStratery(NutsFetchStrategy.REMOTE);
    }

    @Override
    public T local() {
        return setFetchStratery(NutsFetchStrategy.LOCAL);
    }

    @Override
    public T offline() {
        return setFetchStratery(NutsFetchStrategy.OFFLINE);
    }

    @Override
    public T online() {
        return setFetchStratery(NutsFetchStrategy.ONLINE);
    }

    @Override
    public T wired() {
        return setFetchStratery(NutsFetchStrategy.WIRED);
    }

    @Override
    public T installed() {
        return setFetchStratery(NutsFetchStrategy.INSTALLED);
    }

    @Override
    public T anyWhere() {
        return setFetchStratery(NutsFetchStrategy.ANYWHERE);
    }

    @Override
    public NutsFetchStrategy getFetchStrategy() {
        return mode;
    }

    @Override
    public Boolean getIndexEnabled() {
        return indexEnabled;
    }

    @Override
    public boolean isIndexed() {
        return indexEnabled == null || indexEnabled;
    }

    @Override
    public T setIndexed(Boolean indexEnabled) {
        this.indexEnabled = indexEnabled;
        return (T) this;
    }

    @Override
    public T indexed() {
        return setIndexed(true);
    }

    @Override
    public T indexDisabled() {
        return setIndexed(false);
    }

    @Override
    public boolean isIgnoreCache() {
        return ignoreCache;
    }

    @Override
    public T setIgnoreCache(boolean ignoreCache) {
        this.ignoreCache = ignoreCache;
        return (T) this;
    }

    @Override
    public T ignoreCache() {
        return setIgnoreCache(true);
    }

    @Override
    public Boolean getAcceptOptional() {
        return acceptOptional;
    }

    @Override
    public T setAcceptOptional(Boolean acceptOptional) {
        this.acceptOptional = acceptOptional;
        return (T) this;
    }

    @Override
    public T setIncludeOptional(boolean includeOptional) {
        return setAcceptOptional(includeOptional ? null : false);
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public T session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public T setSession(NutsSession session) {
        this.session = session;
        return (T) this;
    }

    @Override
    public T addScope(Collection<NutsDependencyScope> scope) {
        this.scope = NutsDependencyScope.add(this.scope, scope);
        return (T) this;
    }

    @Override
    public T addScope(NutsDependencyScope scope) {
        this.scope = NutsDependencyScope.add(this.scope, scope);
        return (T) this;
    }

    @Override
    public T addScope(NutsDependencyScope... scope) {
        this.scope = NutsDependencyScope.add(this.scope, scope);
        return (T) this;
    }

    @Override
    public T removeScope(Collection<NutsDependencyScope> scope) {
        this.scope = NutsDependencyScope.remove(this.scope, scope);
        return (T) this;
    }

    @Override
    public T removeScope(NutsDependencyScope scope) {
        this.scope = NutsDependencyScope.remove(this.scope, scope);
        return (T) this;
    }

    @Override
    public Set<NutsDependencyScope> getScope() {
        return scope;
    }

    @Override
    public T setScope(NutsDependencyScope scope) {
        return setScope(scope == null ? null : EnumSet.of(scope));
    }

    @Override
    public T setScope(NutsDependencyScope... scope) {
        return setScope(scope == null ? null : EnumSet.<NutsDependencyScope>copyOf(Arrays.asList(scope)));
    }

    @Override
    public T setScope(Collection<NutsDependencyScope> scope) {
        this.scope = scope == null ? EnumSet.noneOf(NutsDependencyScope.class) : EnumSet.<NutsDependencyScope>copyOf(scope);
        return (T) this;
    }

    @Override
    public boolean isIncludeFile() {
        return includeContent;
    }

    @Override
    public T setIncludeFile(boolean includeContent) {
        this.includeContent = includeContent;
        return (T) this;
    }

    @Override
    public boolean isIncludeInstallInformation() {
        return includeInstallInfo;
    }

    @Override
    public T setIncludeInstallInformation(boolean includeInstallInfo) {
        this.includeInstallInfo = includeInstallInfo;
        return (T) this;
    }

    @Override
    public boolean isIncludeEffective() {
        return includeEffectiveDesc;
    }

    @Override
    public T setIncludeEffective(boolean includeEffectiveDesc) {
        this.includeEffectiveDesc = includeEffectiveDesc;
        return (T) this;
    }

    @Override
    public boolean isIncludeDependencies() {
        return includeDependencies;
    }

    @Override
    public T includeDependencies() {
        return setIncludeDependencies(true);
    }

    @Override
    public T setIncludeDependencies(boolean include) {
        includeDependencies = include;
        return (T) this;
    }

    @Override
    public T includeDependencies(boolean include) {
        return setIncludeDependencies(include);
    }

    @Override
    public Path getLocation() {
        return location;
    }
    @Override
    public T setLocation(Path location) {
        this.location = location;
        return (T)this;
    }

    @Override
    public T setDefaultLocation() {
        this.location = null;
        return (T)this;
    }
}
