package net.thevpc.nuts.runtime.standalone.dependency.solver.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.tson.impl.parser.javacc.TsonStreamParserImplTokenManager;

import java.util.*;

public class MavenNDependencySolver implements NDependencySolver {

    List<NDependencyTreeNodeBuild> defs = new ArrayList<>();
    private List<RootInfo> pending = new ArrayList<>();
    private NDependencyFilter dependencyFilter;
    private NRepositoryFilter repositoryFilter;
    NDependencyFilter effDependencyFilter;
    private boolean shouldIncludeContent = false;//shouldIncludeContent(this);
    private boolean failFast;
    boolean ignoreCurrentEnvironment;

    public MavenNDependencySolver() {
    }


    public boolean isIgnoreCurrentEnvironment() {
        return ignoreCurrentEnvironment;
    }

    public NDependencySolver setIgnoreCurrentEnvironment(boolean ignoreCurrentEnvironment) {
        this.ignoreCurrentEnvironment = ignoreCurrentEnvironment;
        return this;
    }

    public NDependencySolver addRootId(NId id) {
        pending.add(new RootInfo(id.toDependency(), null));
        return this;
    }

    @Override
    public NDependencySolver add(NDefinition def) {
        pending.add(new RootInfo(null, def));
        return this;
    }

    @Override
    public NDependencySolver add(NDependency dependency) {
        pending.add(new RootInfo(dependency, null));
        return this;
    }

    @Override
    public NDependencySolver setDependencyFilter(NDependencyFilter dependencyFilter) {
        this.dependencyFilter = dependencyFilter;
        this.effDependencyFilter = null;
        return this;
    }

    @Override
    public NDependencySolver setRepositoryFilter(NRepositoryFilter repositoryFilter) {
        this.repositoryFilter = repositoryFilter;
        return this;
    }

    @Override
    public NDependencies solve() {
        doLog("---- START SOLVE");
        if (getDependencyFilter() == null) {
            NDependencyFilters filter = NDependencyFilters.of();
            effDependencyFilter = filter.always();
        } else {
            effDependencyFilter = getDependencyFilter();
        }

        //session = NutsWorkspaceUtils.bindSession(ws, session);
        for (RootInfo rootInfo : pending) {
            addRootDefinition0(rootInfo.dependency, rootInfo.def);
        }
        pending.clear();
        PassProcessor pp = new PassProcessor(this);
        NDependencies run = pp.run();
        doLog("---- END SOLVE");
        return run;
    }

    void doLog(String message) {
//        System.out.println(message);
    }

    void logRejectedDependency(NDependency effDependency) {
        //
    }

    @Override
    public NDependencySolver add(NDependency dependency, NDefinition def) {
        pending.add(new RootInfo(dependency, def));
        return this;
    }

    public NDependencySolver addRootDefinition0(NDependency dependency, NDefinition def) {
        if (dependency == null) {
            if (def != null) {
                dependency = def.getId().toDependency();
            } else {
                NAssert.requireNonNull(dependency, "dependency");
            }
        }
        NDependencyTreeNodeBuild info = new NDependencyTreeNodeBuild(this, null, dependency, def, 0);
        defs.add(info);
        return this;
    }

    NDefinition searchOne(NDependency dep) {
        try {
            return search(dep)
                    .getResultDefinitions().findFirst().orNull();
        } catch (NNotFoundException | NoSuchElementException | NNoSuchElementException ex) {
            //
        }
        return null;
    }

    public boolean isShouldIncludeContent() {
        return shouldIncludeContent;
    }

    private NSearchCmd search(NDependency dep) {
        return NSearchCmd.of()
                .addIds(dep.toId())
                .setRepositoryFilter(getRepositoryFilter())
                .setContent(isShouldIncludeContent())
                .setIgnoreCurrentEnvironment(isIgnoreCurrentEnvironment())
                .setEffective(true)
                .setLatest(true)
                ;
    }

    public NRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    public NDependencyFilter getDependencyFilter() {
        return dependencyFilter;
    }

    NDependencyScope combineScopes(String parentScope0, String childScope0) {
        NDependencyScope parentScope = NDependencyScope.parse(parentScope0).orElse(NDependencyScope.API);
        NDependencyScope childScope = NDependencyScope.parse(childScope0).orElse(NDependencyScope.API);
        return combineScopes(parentScope, childScope);
    }

    private NDependencyScope combineScopes(NDependencyScope parentScope, NDependencyScope childScope) {
        boolean other = parentScope.isOther() || childScope.isOther();
        boolean test = parentScope.isTest() || childScope.isTest();
        boolean system = !other && (parentScope.isSystem() || childScope.isSystem());
        boolean provided = !other && (parentScope.isProvided() || childScope.isProvided());
        boolean runtime = !other && (parentScope.isRuntime() || childScope.isRuntime());
        boolean impl = (!other && !provided && !runtime && !system) && (parentScope.isImplementation() || childScope.isImplementation());
        boolean api = (!other && !provided && !runtime && !system && !impl);
        if (test) {
            if (other) {
                return NDependencyScope.TEST_OTHER;
            }
            if (system) {
                return NDependencyScope.TEST_SYSTEM;
            }
            if (provided) {
                return NDependencyScope.TEST_PROVIDED;
            }
            if (runtime) {
                return NDependencyScope.TEST_RUNTIME;
            }
            if (impl) {
                return NDependencyScope.TEST_IMPLEMENTATION;
            }
            return NDependencyScope.TEST_API;
        } else {
            if (other) {
                return NDependencyScope.OTHER;
            }
            if (system) {
                return NDependencyScope.SYSTEM;
            }
            if (provided) {
                return NDependencyScope.PROVIDED;
            }
            if (runtime) {
                return NDependencyScope.RUNTIME;
            }
            if (impl) {
                return NDependencyScope.IMPLEMENTATION;
            }
            return NDependencyScope.API;
        }
    }

    public boolean isFailFast() {
        return failFast;
    }

    public MavenNDependencySolver setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public String getName() {
        return "maven";
    }

}
