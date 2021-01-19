package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDependencies;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDependencyTreeNode;

import java.util.*;

public class NutsDependenciesResolver {
    List<NutsDefinition> defs = new ArrayList<>();
    NutsSession session;
    NutsDependencyFilter dependencyFilter;
    boolean failFast;

    public NutsDependenciesResolver(NutsSession session) {
        this.session = session;
    }

    public List<NutsDefinition> getRootDefinitions() {
        return defs;
    }

    public NutsDependenciesResolver setRootDefinitions(List<NutsDefinition> defs) {
        this.defs = defs;
        return this;
    }

    public NutsDependenciesResolver addRootId(NutsId id) {
        NutsDefinition idDef = session.getWorkspace().search()
                .addId(id).setSession(session//.copy().setTrace(false)
//                        .setProperty("monitor-allowed", false)
                ).setEffective(true)
                .setContent(false)
                .setEffective(true)
                .setLatest(true).getResultDefinitions().first();
        if (idDef != null) {
            addRootDefinition(idDef);
        }
        return this;
    }

    public NutsDependenciesResolver addRootDefinition(NutsDefinition def) {
        if (!def.isSetEffectiveDescriptor()) {
            throw new NutsIllegalArgumentException(session.getWorkspace(), "expected an effective definition for " + def.getId());
        }
        defs.add(def);
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
        return this;
    }

    public NutsDependencies resolve() {
        NutsWorkspace ws = session.getWorkspace();
        List<NutsDependencyTreeNodeBuild> roots = new ArrayList<>();
        Queue<NutsDependencyTreeNodeBuild> queue = new ArrayDeque<>();
        List<NutsId> ids = new ArrayList<>();
        List<NutsDependency> immediates = new ArrayList<>();
        boolean shouldIncludeContent = false;//shouldIncludeContent(this);
        for (NutsDefinition def : defs) {
            NutsDescriptor descriptor = def.getEffectiveDescriptor();
            if (descriptor == null) {
                throw new NutsIllegalArgumentException(session.getWorkspace(), "expected an effective definition for " + def.getId());
            }
            NutsId id = descriptor.getId();
            ids.add(id);
            NutsDependency[] immediate = CoreFilterUtils.filterDependencies(id, descriptor.getDependencies(),
                    dependencyFilter, session);
            immediates.addAll(Arrays.asList(immediate));
            for (NutsDependency dependency :
                    immediate) {
                NutsDefinition def2 = ws.search()
                        .addId(dependency.toId()).setSession(session//.copy().setTrace(false)
//                                .setProperty("monitor-allowed", false)
                        ).setEffective(true)
                        .setContent(shouldIncludeContent)
                        .setEffective(true)
                        .setLatest(true).getResultDefinitions().required();
                NutsDependencyTreeNodeBuild info = new NutsDependencyTreeNodeBuild(null, def2, dependency, dependency, 0);
                queue.add(info);
                roots.add(info);
            }
        }

        NutsDependencyInfoSet visitedSet = new NutsDependencyInfoSet();
        NutsDependencyTreeNodeBuild currentNode;
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            NutsDependencyInfo nextId = NutsDependencyInfo.of(currentNode);
            if (visitedSet.add(nextId)) {
                for (NutsDependency dependency : currentNode.def.getEffectiveDescriptor().getDependencies()) {
                    NutsDependencyScope parentScope = ws.dependency().parser().parseScope(currentNode.effDependency.getScope());
                    NutsDependencyScope childScope = ws.dependency().parser().parseScope(dependency.getScope());
                    NutsDependencyScope newScope = combineScopes(parentScope, childScope);
                    NutsDependency effDependency = newScope == childScope ? dependency :
                            dependency.builder().setScope(newScope).build();
                    if (dependencyFilter == null || dependencyFilter.acceptDependency(
                            currentNode.def.getId(), effDependency, session
                    )) {
                        NutsDefinition def2 = ws.search()
                                .addId(dependency.toId()).setSession(session//.copy().setTrace(false)
                                        //.setProperty("monitor-allowed", false)
                                ).setEffective(true)
                                .setContent(shouldIncludeContent)
                                .setLatest(true).getResultDefinitions().required();
                        NutsDependencyTreeNodeBuild info = new NutsDependencyTreeNodeBuild(currentNode, def2, dependency, effDependency, currentNode.depth + 1);
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
                ids.toArray(new NutsId[0]), dependencyFilter, immediates.toArray(new NutsDependency[0]),
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
                visitedSet.put(other.normalized,other);
                return true;
            }
            if (old.depth == other.depth) {
                if(other.real.getVersion().compareTo(old.real.getVersion())>0){
                    visitedSet.put(other.normalized,other);
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
            return new NutsDependencyInfo(currentNode.def.getId().getShortNameId(),currentNode.def.getId(), currentNode.dependency, currentNode.depth);
        }

        public NutsDependency getDependency() {
            return dependency;
        }
    }

    private static class NutsDependencyTreeNodeBuild {
        NutsDependencyTreeNodeBuild parent;
        NutsDefinition def;
        NutsDependency dependency;
        NutsDependency effDependency;
        List<NutsDependencyTreeNodeBuild> children = new ArrayList<>();
        boolean alreadyVisited;
        int depth;

        public NutsDependencyTreeNodeBuild(NutsDependencyTreeNodeBuild parent, NutsDefinition def, NutsDependency dependency, NutsDependency effDependency, int depth) {
            this.parent = parent;
            this.def = def;
            this.dependency = dependency;
            this.effDependency = effDependency;
            this.depth = depth;
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
}
