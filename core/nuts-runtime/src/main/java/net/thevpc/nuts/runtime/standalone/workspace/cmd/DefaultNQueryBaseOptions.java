/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.dependency.NDependencyScopes;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * @param <T> Type
 * @author thevpc
 */
public abstract class DefaultNQueryBaseOptions<T extends NWorkspaceCommand> extends NWorkspaceCommandBase<T> {

    //    private final List<String> repos = new ArrayList<>();
    protected NDependencyFilter dependencyFilter;
    private boolean failFast = false;
    private Boolean optional = null;
    private Set<NDependencyScope> scope = EnumSet.noneOf(NDependencyScope.class);
    private boolean content = false;
    private boolean inlineDependencies = false;
    private boolean dependencies = false;
    private boolean effective = false;
    private NFetchDisplayOptions displayOptions;
    private NRepositoryFilter repositoryFilter;

    //    private Boolean transitive = true;
//    private Boolean cached = true;
//    private Boolean indexed = null;
//    private NutsFetchStrategy fetchStrategy = null;
    public DefaultNQueryBaseOptions(NSession session, String name) {
        super(session, name);
//        this.session=ws.createSession();
        displayOptions = new NFetchDisplayOptions(session);
    }

    //@Override
    public T copyFromDefaultNQueryBaseOptions(DefaultNQueryBaseOptions other) {
        if (other != null) {
            super.copyFromWorkspaceCommandBase(other);
            this.optional = other.getOptional();
            this.failFast = other.isFailFast();
            this.content = other.isContent();
            this.inlineDependencies = other.isInlineDependencies();
            this.dependencies = other.isDependencies();
            this.effective = other.isEffective();
            this.scope = EnumSet.copyOf(other.getScope());
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

    public T clearScopes() {
        this.scope = EnumSet.noneOf(NDependencyScope.class);
        return (T) this;
    }

    //@Override
    public T addScope(NDependencyScope scope) {
        this.scope = NDependencyScopes.add(this.scope, scope);
        return (T) this;
    }

    public T addScope(NDependencyScopePattern scope) {
        this.scope = NDependencyScopes.add(this.scope, scope);
        return (T) this;
    }

    //@Override
    public T addScopes(NDependencyScope... scope) {
        this.scope = NDependencyScopes.add(this.scope, scope);
        return (T) this;
    }

    public T addScopes(NDependencyScopePattern... scope) {
        this.scope = NDependencyScopes.add(this.scope, scope);
        return (T) this;
    }

    //@Override
    public T removeScopes(NDependencyScope... scope) {
        this.scope = NDependencyScopes.remove(this.scope, scope);
        return (T) this;
    }

    public T removeScopes(NDependencyScopePattern... scope) {
        this.scope = NDependencyScopes.remove(this.scope, scope);
        return (T) this;
    }

    //@Override
    public T removeScope(NDependencyScope scope) {
        this.scope = NDependencyScopes.remove(this.scope, scope);
        return (T) this;
    }

    public T removeScope(NDependencyScopePattern scope) {
        this.scope = NDependencyScopes.remove(this.scope, scope);
        return (T) this;
    }

    //@Override
    public Set<NDependencyScope> getScope() {
        return scope;
    }

    //@Override
    public T setScope(NDependencyScope scope) {
        return setScope(scope == null ? null : EnumSet.of(scope));
    }

    //@Override
    public T setScope(NDependencyScope... scope) {
        return setScope(scope == null ? null : EnumSet.<NDependencyScope>copyOf(Arrays.asList(scope)));
    }

    //@Override
    public T setScope(Collection<NDependencyScope> scope) {
        this.scope = scope == null ? EnumSet.noneOf(NDependencyScope.class) : EnumSet.<NDependencyScope>copyOf(scope);
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

    public T content() {
        return setContent(true);
    }

    public T effective() {
        return setEffective(true);
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

    public T failFast() {
        setFailFast(true);
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

    public T dependencies() {
        return setDependencies(true);
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

    public NFetchDisplayOptions getDisplayOptions() {
        return displayOptions;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        if (super.configureFirst(cmdLine)) {
            return true;
        }
        if (getDisplayOptions().configureFirst(cmdLine)) {
            return true;
        }
        NArg a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        switch (a.key()) {
            case "--failfast": {
                cmdLine.withNextFlag((v, r, s) -> this.setFailFast(v));
                return true;
            }
            case "-r":
            case "--repository": {
                cmdLine.withNextEntry((v, r, s) -> addRepositoryFilter(NRepositoryFilters.of(getSession()).byName(v)));
                return true;
            }
            case "--dependencies": {
                cmdLine.withNextFlag((v, r, s) -> this.setDependencies(v));
                return true;
            }
            case "--scope": {
                cmdLine.withNextEntry((v, r, s) -> this.addScope(NDependencyScopePattern.parse(v).orElse(NDependencyScopePattern.API)));
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
                cmdLine.withNextEntryValue((v, r, s) -> this.setOptional(
                        NLiteral.of(v.asString().get(session)).asBoolean()
                                .orNull()));
                return true;
            }
            case "--effective": {
                cmdLine.withNextFlag((v, r, s) -> this.setEffective(v));
                return true;
            }
            case "--content": {
                cmdLine.withNextFlag((v, r, s) -> this.setContent(v));
                return true;
            }
        }
        return false;
    }

    //    @Override
    public NRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    //    @Override
    public T setRepositoryFilter(String filter) {
        checkSession();
        if (NBlankable.isBlank(filter)) {
            this.repositoryFilter = null;
        } else {
            this.repositoryFilter = NRepositories.of(getSession()).filter().byName(filter);
        }
        return (T) this;
    }

    //    @Override
    public T setRepositoryFilter(NRepositoryFilter filter) {
        this.repositoryFilter = filter;
        return (T) this;
    }

    //    @Override
    public T addRepositoryFilter(NRepositoryFilter filter) {
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
//                + ", repos=" + repos
                + ", displayOptions=" + displayOptions
                + ", session=" + getSession()
                + ')';
    }

    //    @Override
    public T setDependencyFilter(NDependencyFilter filter) {
        this.dependencyFilter = filter;
        return (T) this;
    }

    //    @Override
    public NDependencyFilter getDependencyFilter() {
        return dependencyFilter;
    }

    //    @Override
    public T setDependencyFilter(String filter) {
        checkSession();
        this.dependencyFilter = NDependencyFilters.of(getSession()).parse(filter);
        return (T) this;
    }


}
