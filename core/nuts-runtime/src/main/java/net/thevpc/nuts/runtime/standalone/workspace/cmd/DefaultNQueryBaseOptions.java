/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.dependency.NDependencyFilterUtils;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.time.Instant;

/**
 * @param <T> Type
 * @author thevpc
 */
public abstract class DefaultNQueryBaseOptions<T extends NWorkspaceCmd> extends NWorkspaceCmdBase<T> {

    //    private final List<String> repos = new ArrayList<>();
    protected NDependencyFilter dependencyFilter;
    private boolean failFast = false;
//    private boolean content = false;
    private boolean inlineDependencies = false;
//    private boolean dependencies = false;
//    private boolean effective = false;
    private NFetchDisplayOptions displayOptions;
    private NRepositoryFilter repositoryFilter;
    private NFetchStrategy fetchStrategy;
    private Boolean transitive;
    private Instant expireTime;

    //    private Boolean transitive = true;
//    private Boolean cached = true;
//    private Boolean indexed = null;
//    private NutsFetchStrategy fetchStrategy = null;
    public DefaultNQueryBaseOptions(String name) {
        super(name);
//        this.session=ws.createSession();
        displayOptions = new NFetchDisplayOptions();
        NSession s = NSession.of();
        this.fetchStrategy=s.getFetchStrategy().orNull();
        this.transitive=s.getTransitive().orNull();
        this.expireTime=s.getExpireTime().orNull();
    }

    //@Override
    public T copyFromDefaultNQueryBaseOptions(DefaultNQueryBaseOptions other) {
        if (other != null) {
            super.copyFromWorkspaceCommandBase(other);
            this.failFast = other.isFailFast();
//            this.content = other.isContent();
            this.inlineDependencies = other.isInlineDependencies();
//            this.dependencies = other.isDependencies();
//            this.effective = other.isEffective();
            this.dependencyFilter = other.getDependencyFilter();
            this.repositoryFilter = other.getRepositoryFilter();
            this.fetchStrategy=((DefaultNQueryBaseOptions<T>)other).getFetchStrategy().orNull();
            this.transitive=((DefaultNQueryBaseOptions<T>)other).getTransitive().orNull();
            this.expireTime=((DefaultNQueryBaseOptions<T>)other).getExpireTime().orNull();

        }
        return (T) this;
    }

    public NOptional<Instant> getExpireTime() {
        return NOptional.ofNamed(expireTime,"expireTime").orElseUse(()-> NSession.of().getExpireTime());
    }

    public NOptional<NFetchStrategy> getFetchStrategy() {
        return NOptional.ofNamed(fetchStrategy,"fetchStrategy").orElseUse(()-> NSession.of().getFetchStrategy());
    }

    public NOptional<Boolean> getTransitive() {
        return NOptional.ofNamed(transitive,"transitive").orElseUse(()-> NSession.of().getTransitive());
    }

    public T setFetchStrategy(NFetchStrategy fetchStrategy) {
        this.fetchStrategy = fetchStrategy;
        return (T)this;
    }

    public T setTransitive(Boolean transitive) {
        this.transitive = transitive;
        return (T)this;
    }
    public T setExpireTime(Instant transitive) {
        this.expireTime = expireTime;
        return (T)this;
    }


//    //@Override
//    public boolean isContent() {
//        return content;
//    }
//
//    //@Override
//    public T setContent(boolean includeContent) {
//        this.content = includeContent;
//        return (T) this;
//    }


//    //@Override
//    public boolean isEffective() {
//        return effective;
//    }
//
//    //@Override
//    public T setEffective(boolean includeEffectiveDesc) {
//        this.effective = includeEffectiveDesc;
//        return (T) this;
//    }
//
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

//    public boolean isDependencies() {
//        return dependencies;
//    }
//
//    //@Override
//    public T setDependencies(boolean include) {
//        dependencies = include;
//        return (T) this;
//    }


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
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        switch (a.key()) {
            case "--failfast": {
                return cmdLine.matcher().matchFlag((v) -> this.setFailFast(v.booleanValue())).anyMatch();
            }
            case "-r":
            case "--repository": {
                return cmdLine.matcher().matchEntry((v) -> addRepositoryFilter(NRepositoryFilters.of().bySelector(v.stringValue()))).anyMatch();
            }

            case "--scope": {
                return cmdLine.matcher().matchEntry((v) -> NDependencyFilterUtils.addScope(getDependencyFilter(),NDependencyScopePattern.parse(v.stringValue()).orElse(NDependencyScopePattern.API))).anyMatch();
            }

//            case "-i":
//            case "--installed":
//            {
//                cmdLine.skip();
//                this.setFetchStrategy(NutsFetchStrategy.INSTALLED);
//                return true;
//            }
            case "--optional": {
                return cmdLine.matcher().matchEntry((v) ->
                        this.setDependencyFilter(NDependencyFilters.of().nonnull(this.getDependencyFilter()).and(NDependencyFilters.of().byOptional(NLiteral.of(v.asString().get()).asBoolean()
                                .orNull())))).anyMatch();
            }

        }
        return false;
    }

    //    @Override
    public NRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
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
                + ", dependencyFilter=" + dependencyFilter
                + ", inlineDependencies=" + inlineDependencies
//                + ", effective=" + effective
//                + ", repos=" + repos
                + ", displayOptions=" + displayOptions
                + ')';
    }

    //    @Override
    public T setDependencyFilter(NDependencyFilter filter) {
        this.dependencyFilter = filter;
        return (T) this;
    }

    public T addDependencyFilter(NDependencyFilter filter) {
        if(filter!=null){
            if(this.dependencyFilter==null){
                this.dependencyFilter=filter;
            }else{
                this.dependencyFilter.and(filter);
            }
        }
        return (T) this;
    }

    //    @Override
    public NDependencyFilter getDependencyFilter() {
        return dependencyFilter;
    }

    //    @Override
    public T setDependencyFilter(String filter) {
        this.dependencyFilter = NDependencyFilters.of().parse(filter);
        return (T) this;
    }


}
