/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.vpc.app.nuts.NutsDependencyScope;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsFetchStrategy;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsTraceFormat;

/**
 *
 * @author vpc
 * @param <T>
 */
public class DefaultNutsQueryBaseOptions<T> {

    private boolean transitive = true;
    private boolean cached = true;
    private Boolean indexed = null;
    private NutsFetchStrategy mode = null;
    protected NutsSession session;
    private Boolean acceptOptional = null;
    private Set<NutsDependencyScope> scope = EnumSet.noneOf(NutsDependencyScope.class);
    private boolean includeContent = true;
    private boolean includeDependencies = false;
    private boolean effective = false;
    private boolean includeInstallInfo = true;
    private Path location = null;
    private boolean trace = false;
    private NutsOutputFormat outputFormat = NutsOutputFormat.PLAIN;
    private boolean lenient = false;
    private final List<String> repos = new ArrayList<>();
    protected NutsTraceFormat[] traceFormats = new NutsTraceFormat[NutsOutputFormat.values().length];

    //@Override
    protected T copyFrom0(DefaultNutsQueryBaseOptions other) {
        if (other != null) {
            this.acceptOptional = other.getAcceptOptional();
            this.lenient = other.isLenient();
            this.session = other.getSession();
            this.mode = other.getFetchStrategy();
            this.indexed = other.getIndexed();
            this.includeContent = other.isIncludeContent();
            this.includeDependencies = other.isIncludeDependencies();
            this.effective = other.isEffective();
            this.includeInstallInfo = other.isIncludeInstallInformation();
            this.scope = EnumSet.copyOf(other.getScope());
            this.includeInstallInfo = other.isIncludeInstallInformation();
            this.includeContent = other.isIncludeContent();
            this.includeDependencies = other.isIncludeDependencies();
            this.transitive = other.isTransitive();
            this.cached = other.isCached();
            this.location = other.getLocation();
            System.arraycopy(other.traceFormats, 0, this.traceFormats, 0, NutsOutputFormat.values().length);
            this.repos.clear();
            this.repos.addAll(Arrays.asList(other.getRepositories()));
        }
        return (T) this;
    }

    public NutsTraceFormat getTraceFormat() {
        return traceFormats[getOutputFormat().ordinal()];
    }

    public T unsetTraceFormat(NutsOutputFormat f) {
        traceFormats[f.ordinal()] = null;
        return (T) this;
    }

    public T traceFormat(NutsTraceFormat traceFormat) {
        return setTraceFormat(traceFormat);
    }

    public T setTraceFormat(NutsTraceFormat f) {
        if (f == null) {
            throw new NullPointerException();
        }
        traceFormats[f.getSupportedFormat().ordinal()] = f;
        return (T) this;
    }

    public NutsTraceFormat[] getTraceFormats() {
        return Arrays.copyOf(traceFormats, traceFormats.length);
    }

    //@Override
    public boolean isCached() {
        return cached;
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
        this.cached = cached;
        return (T) this;
    }

    //@Override
    public boolean isTransitive() {
        return transitive;
    }

    //@Override
    public T setTransitive(boolean transitive) {
        this.transitive = transitive;
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
    public T fetchStratery(NutsFetchStrategy mode) {
        return setFetchStratery(mode);
    }

    //@Override
    public T setFetchStratery(NutsFetchStrategy mode) {
        this.mode = mode;
        return (T) this;
    }

    //@Override
    public T remote() {
        return setFetchStratery(NutsFetchStrategy.REMOTE);
    }

    //@Override
    public T local() {
        return setFetchStratery(NutsFetchStrategy.LOCAL);
    }

    //@Override
    public T offline() {
        return setFetchStratery(NutsFetchStrategy.OFFLINE);
    }

    //@Override
    public T online() {
        return setFetchStratery(NutsFetchStrategy.ONLINE);
    }

    //@Override
    public T wired() {
        return setFetchStratery(NutsFetchStrategy.WIRED);
    }

    //@Override
    public T installed() {
        return setFetchStratery(NutsFetchStrategy.INSTALLED);
    }

    //@Override
    public T anyWhere() {
        return setFetchStratery(NutsFetchStrategy.ANYWHERE);
    }

    //@Override
    public NutsFetchStrategy getFetchStrategy() {
        return mode;
    }

    //@Override
    public Boolean getIndexed() {
        return indexed;
    }

    //@Override
    public boolean isIndexed() {
        return indexed == null || indexed;
    }

    public T indexed(Boolean indexEnabled) {
        return setIndexed(indexEnabled);
    }

    //@Override
    public T setIndexed(Boolean indexEnabled) {
        this.indexed = indexEnabled;
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
    public Boolean getAcceptOptional() {
        return acceptOptional;
    }

    public T acceptOptional() {
        return acceptOptional(true);
    }

    //@Override
    public T acceptOptional(Boolean acceptOptional) {
        return setAcceptOptional(acceptOptional);
    }

    //@Override
    public T setAcceptOptional(Boolean acceptOptional) {
        this.acceptOptional = acceptOptional;
        return (T) this;
    }

    //@Override
    public T includeOptional() {
        return includeOptional(true);
    }

    //@Override
    public T includeOptional(boolean includeOptional) {
        return setAcceptOptional(includeOptional ? null : false);
    }

    public T setIncludeOptional(boolean includeOptional) {
        return setAcceptOptional(includeOptional ? null : false);
    }

    //@Override
    public NutsSession getSession() {
        return session;
    }

    //@Override
    public T session(NutsSession session) {
        return setSession(session);
    }

    //@Override
    public T setSession(NutsSession session) {
        this.session = session;
        return (T) this;
    }

    //@Override
    public T scopes(Collection<NutsDependencyScope> scope) {
        return DefaultNutsQueryBaseOptions.this.addScopes(scope);
    }

    //@Override
    public T clearScopes() {
        this.scope = EnumSet.noneOf(NutsDependencyScope.class);
        return (T) this;
    }

    //@Override
    public T addScopes(Collection<NutsDependencyScope> scope) {
        this.scope = NutsDependencyScope.add(this.scope, scope);
        return (T) this;
    }

    public T scope(NutsDependencyScope scope) {
        return addScope(scope);
    }

    //@Override
    public T addScope(NutsDependencyScope scope) {
        this.scope = NutsDependencyScope.add(this.scope, scope);
        return (T) this;
    }

    public T scopes(NutsDependencyScope... scopes) {
        return addScopes(scopes);
    }

    //@Override
    public T addScopes(NutsDependencyScope... scope) {
        this.scope = NutsDependencyScope.add(this.scope, scope);
        return (T) this;
    }

    //@Override
    public T removeScopes(Collection<NutsDependencyScope> scope) {
        this.scope = NutsDependencyScope.remove(this.scope, scope);
        return (T) this;
    }

    //@Override
    public T removeScope(NutsDependencyScope scope) {
        this.scope = NutsDependencyScope.remove(this.scope, scope);
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
    public boolean isIncludeContent() {
        return includeContent;
    }

    //@Override
    public T setIncludeContent(boolean includeContent) {
        this.includeContent = includeContent;
        return (T) this;
    }

    public T includeContent(boolean includeContent) {
        return (T) setIncludeContent(includeContent);
    }

    public T includeContent() {
        return (T) setIncludeContent(true);
    }

    //@Override
    public boolean isIncludeInstallInformation() {
        return includeInstallInfo;
    }

    //@Override
    public T includeInstallInformation() {
        return includeInstallInformation(true);
    }

    //@Override
    public T includeInstallInformation(boolean includeInstallInfo) {
        return setIncludeInstallInformation(includeInstallInfo);
    }

    //@Override
    public T setIncludeInstallInformation(boolean includeInstallInfo) {
        this.includeInstallInfo = includeInstallInfo;
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
    public T effective(boolean effective) {
        return setEffective(effective);
    }

    //@Override
    public T effective() {
        return setEffective(true);
    }

    //@Override
    public boolean isIncludeDependencies() {
        return includeDependencies;
    }

    //@Override
    public T includeDependencies() {
        return setIncludeDependencies(true);
    }

    //@Override
    public T setIncludeDependencies(boolean include) {
        includeDependencies = include;
        return (T) this;
    }

    //@Override
    public T includeDependencies(boolean include) {
        return setIncludeDependencies(include);
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

    public T outputFormat(NutsOutputFormat outputFormat) {
        return setOutputFormat(outputFormat);
    }

    public T setOutputFormat(NutsOutputFormat outputFormat) {
        if (outputFormat == null) {
            outputFormat = NutsOutputFormat.PLAIN;
        }
        this.outputFormat = outputFormat;
        return (T) this;
    }

    public NutsOutputFormat getOutputFormat() {
        return this.outputFormat;
    }

    public boolean isTrace() {
        return trace;
    }

    public T setTrace(boolean trace) {
        this.trace = trace;
        return (T) this;
    }

    public T trace(boolean trace) {
        return setTrace(trace);
    }

    public T trace() {
        return trace(true);
    }

    public boolean isLenient() {
        return lenient;
    }

    public T setLenient(boolean ignoreNotFound) {
        this.lenient = ignoreNotFound;
        return (T) this;
    }

    public T lenient() {
        return setLenient(true);
    }

    public T lenient(boolean lenient) {
        return setLenient(lenient);
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

}
