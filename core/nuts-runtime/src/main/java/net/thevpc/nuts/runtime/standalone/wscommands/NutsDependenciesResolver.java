package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDependencies;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDependencyTreeNode;

import java.util.*;

public class NutsDependenciesResolver {

    private List<NutsDependencyTreeNodeBuild> defs = new ArrayList<>();
    private NutsSession session;
    private NutsDependencyFilter dependencyFilter;
    private NutsDependencyFilter effDependencyFilter;
    private boolean shouldIncludeContent = false;//shouldIncludeContent(this);
    private boolean failFast;

    public NutsDependenciesResolver(NutsSession session) {
        this.session = session;
    }

    public NutsDependenciesResolver addRootId(NutsId id, boolean include) {
        NutsDefinition idDef = session.getWorkspace().search()
                .addId(id).setSession(session//.copy().setTrace(false)
        //                        .setProperty("monitor-allowed", false)
        ).setEffective(true)
                .setContent(false)
                .setEffective(true)
                .setLatest(true).getResultDefinitions().first();
        if (idDef != null) {
            addRootDefinition(idDef, include);
        }
        return this;
    }

    public NutsDependenciesResolver addRootDefinition(NutsDefinition def, boolean include) {
        return addRootDefinition(def.getId().toDependency(), def, include);
    }

    public NutsDependenciesResolver addRootDefinition(NutsDependency dependency, boolean include) {
        return addRootDefinition(dependency, null, include);
    }

    public NutsDependenciesResolver addRootDefinition(NutsDependency dependency, NutsDefinition def, boolean include) {
        if (dependency == null) {
            throw new NutsIllegalArgumentException(session.getWorkspace(), "missing dependency");
        }
        if (def == null) {
            NutsWorkspace ws = session.getWorkspace();
            def = ws.search()
                    .addId(dependency.toId()).setSession(session//.copy().setTrace(false)
            //                                .setProperty("monitor-allowed", false)
            ).setEffective(true)
                    .setContent(shouldIncludeContent)
                    .setEffective(true)
                    .setLatest(true).getResultDefinitions().required();
        }
        if (!def.isSetEffectiveDescriptor()) {
            throw new NutsIllegalArgumentException(session.getWorkspace(), "expected an effective definition for " + def.getId());
        }
        defs.add(new NutsDependencyTreeNodeBuild(null, def, dependency, dependency, 0,
                include ? NutsDependencyTreeNodeBuildType.ROOT_INCLUDED : NutsDependencyTreeNodeBuildType.ROOT_DISCARDED
        ));
        return this;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsDependenciesResolver setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public NutsDependencyFilter getDependencyFilter() {
        return dependencyFilter;
    }

    public NutsDependenciesResolver setDependencyFilter(NutsDependencyFilter dependencyFilter) {
        this.dependencyFilter = dependencyFilter;
        this.effDependencyFilter = null;
        return this;
    }

    public NutsDependencies resolve() {
        NutsWorkspace ws = session.getWorkspace();
        List<NutsDependencyTreeNodeBuild> roots = new ArrayList<>();
        Queue<NutsDependencyTreeNodeBuild> queue = new ArrayDeque<>();
        List<NutsId> ids = new ArrayList<>();
        LinkedHashSet<NutsDependency> immediates = new LinkedHashSet<>();
        queue.addAll(defs);
        NutsDependencyInfoSet visitedSet = new NutsDependencyInfoSet();
        NutsDependencyTreeNodeBuild currentNode;
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            NutsDescriptor descriptor = currentNode.def.getEffectiveDescriptor();
            if (descriptor == null) {
                throw new NutsIllegalArgumentException(session.getWorkspace(), "expected an effective definition for " + currentNode.def.getId());
            }
            switch (currentNode.buildType) {
                case ROOT_INCLUDED: {
                    ids.add(descriptor.getId());
                    roots.add(currentNode);
                    NutsDependency[] immediate = CoreFilterUtils.filterDependencies(descriptor.getId(), descriptor.getDependencies(),
                            getEffDependencyFilter(), session);
                    immediates.addAll(Arrays.asList(immediate));
                    break;
                }
                case ROOT_DISCARDED: {
                    ids.add(descriptor.getId());
                    NutsDependency[] immediate = CoreFilterUtils.filterDependencies(descriptor.getId(), descriptor.getDependencies(),
                            getEffDependencyFilter(), session);
                    immediates.addAll(Arrays.asList(immediate));
                    break;
                }
                case ROOT_CHILD: {
                    ids.add(descriptor.getId());
                    roots.add(currentNode);
                    break;
                }
            }
            if (currentNode.buildType == NutsDependencyTreeNodeBuildType.ROOT_INCLUDED || currentNode.buildType == NutsDependencyTreeNodeBuildType.ROOT_DISCARDED) {
                ids.add(descriptor.getId());
            }
            if (currentNode.buildType == NutsDependencyTreeNodeBuildType.ROOT_INCLUDED || currentNode.buildType == NutsDependencyTreeNodeBuildType.ROOT_CHILD) {
            }
            NutsDependencyInfo nextId = NutsDependencyInfo.of(currentNode);
            if (visitedSet.add(nextId)) {
                for (NutsDependency dependency : currentNode.def.getEffectiveDescriptor().getDependencies()) {
                    NutsDependencyScope parentScope = ws.dependency().parser().parseScope(currentNode.effDependency.getScope());
                    NutsDependencyScope childScope = ws.dependency().parser().parseScope(dependency.getScope());
                    NutsDependencyScope newScope = combineScopes(parentScope, childScope);
                    NutsDependency effDependency = newScope == childScope ? dependency
                            : dependency.builder().setScope(newScope).build();
                    if (getEffDependencyFilter().acceptDependency(
                            currentNode.def.getId(), effDependency, session
                    )) {
                        NutsDefinition def2 = ws.search()
                                .addId(dependency.toId()).setSession(session//.copy().setTrace(false)
                        //.setProperty("monitor-allowed", false)
                        ).setEffective(true)
                                .setContent(shouldIncludeContent)
                                .setLatest(true).getResultDefinitions().required();
                        NutsDependencyTreeNodeBuildType nextBuildType = NutsDependencyTreeNodeBuildType.CHILD;
                        switch (currentNode.buildType) {
                            case ROOT_INCLUDED: {
                                nextBuildType = NutsDependencyTreeNodeBuildType.CHILD;
                                break;
                            }
                            case ROOT_DISCARDED: {
                                nextBuildType = NutsDependencyTreeNodeBuildType.ROOT_CHILD;
                                break;
                            }
                            case CHILD:
                            case ROOT_CHILD: {
                                nextBuildType = NutsDependencyTreeNodeBuildType.CHILD;
                                break;
                            }
                        }
                        NutsDependencyTreeNodeBuild info = new NutsDependencyTreeNodeBuild(currentNode, def2, dependency, effDependency, currentNode.depth + 1, nextBuildType);
                        currentNode.children.add(info);
                        queue.add(info);
                    }
                }
            } else {
                currentNode.alreadyVisited = true;
            }
        }
        List<NutsDependencyTreeNode> ret = new ArrayList<>();
        for (NutsDependencyTreeNodeBuild root : roots) {
            //if(!root.alreadyVisited){
            ret.add(root.build());
            //}
        }

        return new DefaultNutsDependencies(
                ids.toArray(new NutsId[0]), getEffDependencyFilter(), immediates.toArray(new NutsDependency[0]),
                visitedSet.visitedSet.values().stream().map(NutsDependencyInfo::getDependency)
                        .toArray(NutsDependency[]::new),
                ret.toArray(new NutsDependencyTreeNode[0])
        );
    }

    private NutsDependencyScope combineScopes(NutsDependencyScope parentScope, NutsDependencyScope childScope) {
        boolean other = parentScope.isOther() || childScope.isOther();
        boolean test = parentScope.isTest() || childScope.isTest();
        boolean system = !other && (parentScope.isSystem() || childScope.isSystem());
        boolean provided = !other && (parentScope.isProvided() || childScope.isProvided());
        boolean runtime = !other && (parentScope.isRuntime() || childScope.isRuntime());
        boolean impl = (!other && !provided && !runtime && !system) && (parentScope.isImplementation() || childScope.isImplementation());
        boolean api = (!other && !provided && !runtime && !system && !impl);
        if (test) {
            if (other) {
                return NutsDependencyScope.TEST_OTHER;
            }
            if (system) {
                return NutsDependencyScope.TEST_SYSTEM;
            }
            if (provided) {
                return NutsDependencyScope.TEST_PROVIDED;
            }
            if (runtime) {
                return NutsDependencyScope.TEST_RUNTIME;
            }
            if (impl) {
                return NutsDependencyScope.TEST_IMPLEMENTATION;
            }
            return NutsDependencyScope.TEST_API;
        } else {
            if (other) {
                return NutsDependencyScope.OTHER;
            }
            if (system) {
                return NutsDependencyScope.SYSTEM;
            }
            if (provided) {
                return NutsDependencyScope.PROVIDED;
            }
            if (runtime) {
                return NutsDependencyScope.RUNTIME;
            }
            if (impl) {
                return NutsDependencyScope.IMPLEMENTATION;
            }
            return NutsDependencyScope.API;
        }
    }

    public boolean isFailFast() {
        return failFast;
    }

    public NutsDependenciesResolver setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    private static class NutsDependencyInfoSet {

        Map<NutsId, NutsDependencyInfo> visitedSet = new LinkedHashMap<>();

        public boolean add(NutsDependencyInfo other) {
            NutsDependencyInfo old = visitedSet.get(other.normalized);
            if (old == null) {
                visitedSet.put(other.normalized, other);
                return true;
            }
            if (old.depth == other.depth) {
                if (other.real.getVersion().compareTo(old.real.getVersion()) > 0) {
                    visitedSet.put(other.normalized, other);
                    return true;
                }
            }
            return false;
        }

    }

    private static class NutsDependencyInfo {

        NutsId normalized;
        NutsId real;
        int depth;
        NutsDependency dependency;

        public NutsDependencyInfo(NutsId normalized, NutsId real, NutsDependency dependency, int depth) {
            this.normalized = normalized;
            this.real = real;
            this.depth = depth;
            this.dependency = dependency;
        }

        public static NutsDependencyInfo of(NutsDependencyTreeNodeBuild currentNode) {
            return new NutsDependencyInfo(currentNode.def.getId().getShortNameId(), currentNode.def.getId(), currentNode.dependency, currentNode.depth);
        }

        public NutsDependency getDependency() {
            return dependency;
        }
    }

    private enum NutsDependencyTreeNodeBuildType {
        ROOT_DISCARDED,
        ROOT_INCLUDED,
        ROOT_CHILD,
        CHILD,
    }

    private static class NutsDependencyTreeNodeBuild {

        NutsDependencyTreeNodeBuild parent;
        NutsDefinition def;
        NutsDependency dependency;
        NutsDependency effDependency;
        List<NutsDependencyTreeNodeBuild> children = new ArrayList<>();
        boolean alreadyVisited;
        int depth;
        NutsDependencyTreeNodeBuildType buildType;

        public NutsDependencyTreeNodeBuild(NutsDependencyTreeNodeBuild parent, NutsDefinition def, NutsDependency dependency, NutsDependency effDependency, int depth, NutsDependencyTreeNodeBuildType buildType) {
            this.parent = parent;
            this.def = def;
            this.dependency = dependency;
            this.effDependency = effDependency;
            this.depth = depth;
            this.buildType = buildType;
        }

        private NutsDependencyTreeNode build() {
            NutsDependencyTreeNode[] nchildren = new NutsDependencyTreeNode[children.size()];
            for (int i = 0; i < nchildren.length; i++) {
                nchildren[i] = children.get(i).build();
            }
            return new DefaultNutsDependencyTreeNode(
                    dependency, nchildren, alreadyVisited
            );
        }
    }

    private boolean isAcceptDependency(NutsDependency s) {
        //by default ignore optionals
        String os = s.getOs();
        String arch = s.getArch();
        if (os.isEmpty() && arch.isEmpty()) {
            return false;
        }
        if (!os.isEmpty()) {
            NutsOsFamily eos = session.getWorkspace().env().getOsFamily();
            boolean osOk = false;
            for (String e : os.split("[,; ]")) {
                if (!e.isEmpty()) {
                    if (e.equalsIgnoreCase(eos.id())) {
                        osOk = true;
                        break;
                    }
                }
            }
            if (!osOk) {
                return false;
            }
        }
        if (!arch.isEmpty()) {
            NutsArchFamily earch = session.getWorkspace().env().getArchFamily();
            if (earch != null) {
                boolean archOk = false;
                for (String e : arch.split("[,; ]")) {
                    if (!e.isEmpty()) {
                        NutsArchFamily eo = NutsArchFamily.parseLenient(e);
                        if (eo != NutsArchFamily.UNKNOWN && eo == earch) {
                            archOk = true;
                            break;
                        }
                    }
                }
                if (!archOk) {
                    return false;
                }
            }
        }
        return true;
    }

    public NutsDependencyFilter getEffDependencyFilter() {
        if (effDependencyFilter == null) {
            NutsWorkspace ws = session.getWorkspace();
            if (dependencyFilter == null) {
                effDependencyFilter = ws.dependency().filter().byOs(ws.env().getOsFamily())
                        .and(ws.dependency().filter().byArch(ws.env().getArchFamily()));
            } else {
                effDependencyFilter
                        = dependencyFilter
                                .and(ws.dependency().filter().byOs(ws.env().getOsFamily()))
                                .and(ws.dependency().filter().byArch(ws.env().getArchFamily()));
            }

        }
        return effDependencyFilter;
    }

}
