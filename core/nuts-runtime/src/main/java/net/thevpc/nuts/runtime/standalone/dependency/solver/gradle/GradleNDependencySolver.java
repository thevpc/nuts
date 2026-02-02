package net.thevpc.nuts.runtime.standalone.dependency.solver.gradle;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import net.thevpc.nuts.artifact.NArtifactNotFoundException;
import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDependencies;
import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.artifact.NDependencyFilters;
import net.thevpc.nuts.artifact.NDependencyScope;
import net.thevpc.nuts.artifact.NDependencyTreeNode;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.command.NSearch;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.runtime.standalone.dependency.solver.maven.RootInfo;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgFormattable;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTreeNode;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NNoSuchElementException;

/**
 * Gradle-style dependency solver implementing "newest wins" conflict resolution.
 * 
 * Key differences from Maven:
 * - Gradle uses "newest version wins" for conflict resolution
 * - Maven uses "nearest to root wins" for conflict resolution
 * 
 * @author Taha Ben Salah (taha.bensalah@gmail.com)
 */
public class GradleNDependencySolver implements NDependencySolver {

    public boolean includedProvided = false;
    List<GradleDependencyTreeNodeBuild> defs = new ArrayList<>();
    private final List<RootInfo> pending = new ArrayList<>();
    private NDependencyFilter dependencyFilter;
    private NRepositoryFilter repositoryFilter;
    NDependencyFilter effDependencyFilter;
    private boolean failFast;
    boolean ignoreCurrentEnvironment;

    public GradleNDependencySolver() {
    }

    public boolean isIgnoreCurrentEnvironment() {
        return ignoreCurrentEnvironment;
    }

    @Override
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
        doLog("---- START GRADLE SOLVE (NEWEST WINS)");
        if (getDependencyFilter() == null) {
            NDependencyFilters filter = NDependencyFilters.of();
            effDependencyFilter = filter.always();
        } else {
            effDependencyFilter = getDependencyFilter();
        }

        for (RootInfo rootInfo : pending) {
            addRootDefinition0(rootInfo.dependency, rootInfo.def);
        }
        pending.clear();
        
        // Use Gradle-specific processor with "newest wins" logic
        GradlePassProcessor pp = new GradlePassProcessor(this);
        NDependencies run = pp.run();
        doLogDependencyTree(run);
        doLog("---- END GRADLE SOLVE");
        return run;
    }

    private void doLogDependencyTree(NDependencies run) {
        if (true) {
            return;
        }
        class NDependencyTreeNodeAndFormat implements NTreeNode, NMsgFormattable {
            NDependencyTreeNode node;

            public NDependencyTreeNodeAndFormat(NDependencyTreeNode node) {
                this.node = node;
            }

            @Override
            public NText value() {
                return NText.of(toMsg());
            }

            @Override
            public String toString() {
                return node == null ? "" : node.toString();
            }

            @Override
            public NMsg toMsg() {
                return NMsg.ofC("%s", node.getDependency().builder().removeCondition().setExclusions(null).build());
            }

            @Override
            public List<NTreeNode> children() {
                if (node == null) {
                    return run.transitiveNodes().toList().stream().map(x -> new NDependencyTreeNodeAndFormat(x)).collect(Collectors.toList());
                }
                return node.getChildren().stream().map(x -> new NDependencyTreeNodeAndFormat(x)).collect(Collectors.toList());
            }
        }

        NSession.of().out().println(new NDependencyTreeNodeAndFormat(null));
    }

    void doLog(String message) {
//        NOut.println(message);
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
                NAssert.requireNamedNonNull(dependency, "dependency");
            }
        }
        GradleDependencyTreeNodeBuild info = new GradleDependencyTreeNodeBuild(this, null, dependency, def, 0);
        defs.add(info);
        return this;
    }

    NDefinition searchOne(NDependency dep) {
        NDefinition def = null;
        try {
            def = search(dep)
                    .getResultDefinitions().findFirst().orNull();
        } catch (NArtifactNotFoundException | NoSuchElementException | NNoSuchElementException ex) {
            doLog("Unable to load dependency: " + dep);
        }
        return def;
    }

    private NSearch search(NDependency dep) {
        return NSearch.of()
                .addIds(dep.toId())
                .setDependencyFilter(getDependencyFilter())
                .setRepositoryFilter(getRepositoryFilter())
                .setIgnoreCurrentEnvironment(isIgnoreCurrentEnvironment())
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

    public GradleNDependencySolver setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public String getName() {
        return "gradle";
    }

    /**
     * Compares two version strings to determine which is newer.
     * Used for Gradle's "newest wins" conflict resolution.
     * 
     * @param version1 first version
     * @param version2 second version
     * @return positive if version1 is newer, negative if version2 is newer, 0 if equal
     */
    public int compareVersions(String version1, String version2) {
        if (version1 == null && version2 == null) {
            return 0;
        }
        if (version1 == null) {
            return -1;
        }
        if (version2 == null) {
            return 1;
        }
        
        // Directly compare version strings using NVersion
        return NVersion.of(version1).compareTo(NVersion.of(version2));
    }
}
