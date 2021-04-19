package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDependencies;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDependencyTreeNode;

import java.util.*;
import java.util.stream.Collectors;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

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
        return addRootDefinition(def.getId().toDependency(), def);
    }

    public NutsDependenciesResolver addRootDefinition(NutsDependency dependency) {
        return addRootDefinition(dependency, null);
    }

    public NutsDependenciesResolver addRootDefinition(NutsDependency dependency, NutsDefinition def) {

        if (dependency == null) {
            throw new NutsIllegalArgumentException(session, "missing dependency");
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
            throw new NutsIllegalArgumentException(session, "expected an effective definition for " + def.getId());
        }
        defs.add(new NutsDependencyTreeNodeBuild(null, def, dependency, dependency, 0));
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
        List<NutsDependencyTreeNodeBuild> mergedRootNodeBuilders = new ArrayList<>();
        List<NutsDependencyTreeNodeBuild> nonMergedRootNodeBuilders = new ArrayList<>();
        Queue<NutsDependencyTreeNodeBuild> queue = new ArrayDeque<>();
        Set<NutsId> sourceIds = new LinkedHashSet<>();
        LinkedHashSet<NutsDependency> immediates = new LinkedHashSet<>();
        NutsDependencyInfoSet mergedVisitedSet = new NutsDependencyInfoSet();
        NutsDependencyInfoSet nonMergedVisitedSet = new NutsDependencyInfoSet();
        for (NutsDependencyTreeNodeBuild currentNode : defs) {
            NutsId id = currentNode.getEffectiveId();
            if (sourceIds.add(id)) {
//                sourceIds.add(id);
                if (mergedVisitedSet.add(currentNode.key)) {
                    mergedRootNodeBuilders.add(currentNode);
                    NutsDependency[] immediate = CoreFilterUtils.filterDependencies(id, currentNode.getEffectiveDescriptor().getDependencies(),
                            getEffDependencyFilter(), session);
                    immediates.addAll(Arrays.asList(immediate));
                    for (NutsDependency dependency : currentNode.def.getEffectiveDescriptor().getDependencies()) {
                        NutsDependency effDependency = dependency.builder()
                                .setScope(combineScopes(currentNode.effDependency.getScope(), dependency.getScope()))
                                .build();
                        if (getEffDependencyFilter().acceptDependency(currentNode.def.getId(), effDependency, session)
                                && !currentNode.exclusions.contains(dependency.toId().getShortNameId())
                                ) {
                            NutsDefinition def2 = null;
                            try {
                                def2 = ws.search()
                                        .addId(dependency.toId())
                                        .setSession(session).setEffective(true)
                                        .setContent(shouldIncludeContent)
                                        .setLatest(true).getResultDefinitions().required();
                            } catch (NutsNotFoundException ex) {
                                //
                            }
                            NutsDependencyTreeNodeBuild info = new NutsDependencyTreeNodeBuild(currentNode, def2, dependency, effDependency, currentNode.depth + 1);
                            info.exclusions.addAll(currentNode.exclusions);
                            for (NutsId exclusion : dependency.getExclusions()) {
                                info.exclusions.add(exclusion.getShortNameId());
                            }
                            currentNode.children.add(info);
                            nonMergedRootNodeBuilders.add(info);
                            queue.add(info);
                        }
                    }
                }
            }
        }
        NutsDependencyTreeNodeBuild currentNode;
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            NutsDependencyInfo nextId = currentNode.key;
            if (!mergedVisitedSet.contains(nextId) && nonMergedVisitedSet.add(nextId)) {
                mergedVisitedSet.add(nextId);//ensure added to merged!
                NutsDescriptor effectiveDescriptor = currentNode.getEffectiveDescriptor();
                if (effectiveDescriptor != null) {
                    for (NutsDependency dependency : effectiveDescriptor.getDependencies()) {
                        NutsDependency effDependency = dependency.builder()
                                .setScope(combineScopes(currentNode.effDependency.getScope(), dependency.getScope()))
                                .build();
                        if (getEffDependencyFilter().acceptDependency(
                                currentNode.getEffectiveId(), effDependency, session
                        )  && !currentNode.exclusions.contains(dependency.toId().getShortNameId())) {
                            NutsDefinition def2 = null;
                            try {
                                def2 = ws.search()
                                        .addId(dependency.toId()).setSession(session//.copy().setTrace(false)
                                //.setProperty("monitor-allowed", false)
                                ).setEffective(true)
                                        .setContent(shouldIncludeContent)
                                        .setLatest(true).getResultDefinitions().required();
                            } catch (NutsNotFoundException ex) {
                                //
                            }
                            NutsDependencyTreeNodeBuild info = new NutsDependencyTreeNodeBuild(currentNode, def2, dependency, effDependency, currentNode.depth + 1);
                            info.exclusions.addAll(currentNode.exclusions);
                            for (NutsId exclusion : dependency.getExclusions()) {
                                info.exclusions.add(exclusion.getShortNameId());
                            }
                            currentNode.children.add(info);
                            queue.add(info);
                        }
                    }
                }
            } else {
                currentNode.alreadyVisited = true;
            }
        }
        List<NutsDependencyTreeNode> mergedRootNodes = mergedRootNodeBuilders.stream().map(x -> x.build()).collect(Collectors.toList());
        List<NutsDependencyTreeNode> nonMergedRootNodes = nonMergedRootNodeBuilders.stream().map(x -> x.build()).collect(Collectors.toList());
        final NutsDependency[] mergedDepsList = mergedVisitedSet.visitedSet.values().stream().map(NutsDependencyInfo::getDependency)
                .toArray(NutsDependency[]::new);
        final NutsDependency[] nonMergedDepsList = nonMergedVisitedSet.visitedSet.values().stream().map(NutsDependencyInfo::getDependency)
                .toArray(NutsDependency[]::new);
        return new DefaultNutsDependencies(
                sourceIds.toArray(new NutsId[0]), getEffDependencyFilter(),
                immediates.toArray(new NutsDependency[0]),
                nonMergedDepsList,
                nonMergedRootNodes.toArray(new NutsDependencyTreeNode[0]),
                mergedDepsList,
                mergedRootNodes.toArray(new NutsDependencyTreeNode[0])
        );
    }

    private NutsDependencyScope combineScopes(String parentScope0, String childScope0) {
        NutsWorkspace ws = session.getWorkspace();
        NutsDependencyScope parentScope = ws.dependency().parser().parseScope(parentScope0);
        NutsDependencyScope childScope = ws.dependency().parser().parseScope(childScope0);
        return combineScopes(parentScope, childScope);
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

        public boolean contains(NutsDependencyInfo other) {
            NutsDependencyInfo old = visitedSet.get(other.normalized);
            if (old == null) {
                return false;
            }
            if (old.depth == other.depth) {
                if (other.real.getVersion().compareTo(old.real.getVersion()) > 0) {
                    return false;
                }
            }
            return true;
        }

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
            NutsId id = currentNode.def == null ? null : currentNode.def.getId();
            if (id == null) {
                id = currentNode.dependency.toId();
            }
            return new NutsDependencyInfo(id.getShortNameId(), id, currentNode.dependency, currentNode.depth);
        }

        public NutsDependency getDependency() {
            return dependency;
        }
    }

    private class NutsDependencyTreeNodeBuild {

        NutsDependencyTreeNodeBuild parent;
        NutsId id;
        NutsDefinition def;
        NutsDependency dependency;
        NutsDependency effDependency;
        List<NutsDependencyTreeNodeBuild> children = new ArrayList<>();
        List<NutsId> exclusions = new ArrayList<>();
        boolean alreadyVisited;
        int depth;
        NutsDescriptor effDescriptor;
        NutsDependencyInfo key;

        public NutsDependencyTreeNodeBuild(NutsDependencyTreeNodeBuild parent, NutsDefinition def, NutsDependency dependency, NutsDependency effDependency, int depth) {
            this.parent = parent;
            this.def = def;
            this.dependency = dependency;
            this.effDependency = effDependency;
            this.depth = depth;
            this.id = def != null ? def.getId() : dependency != null ? dependency.toId() : null;
            this.key = NutsDependencyInfo.of(this);
        }

        private NutsId getEffectiveId() {
            return getEffectiveDescriptor().getId();
        }

        private NutsDescriptor getEffectiveDescriptor() {
            if (effDescriptor == null && def != null) {
                effDescriptor = def.getEffectiveDescriptor();
                if (effDescriptor == null) {
                    throw new NutsIllegalArgumentException(session, "expected an effective definition for " + def.getId());
                }
            }
            return effDescriptor;
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

//    private boolean isAcceptDependency(NutsDependency s) {
//        //by default ignore optionals
//        String os = s.getOs();
//        String arch = s.getArch();
//        if (os.isEmpty() && arch.isEmpty()) {
//            return false;
//        }
//        if (!os.isEmpty()) {
//            NutsOsFamily eos = session.getWorkspace().env().getOsFamily();
//            boolean osOk = false;
//            for (String e : os.split("[,; ]")) {
//                if (!e.isEmpty()) {
//                    if (e.equalsIgnoreCase(eos.id())) {
//                        osOk = true;
//                        break;
//                    }
//                }
//            }
//            if (!osOk) {
//                return false;
//            }
//        }
//        if (!arch.isEmpty()) {
//            NutsArchFamily earch = session.getWorkspace().env().getArchFamily();
//            if (earch != null) {
//                boolean archOk = false;
//                for (String e : arch.split("[,; ]")) {
//                    if (!e.isEmpty()) {
//                        NutsArchFamily eo = NutsArchFamily.parseLenient(e);
//                        if (eo != NutsArchFamily.UNKNOWN && eo == earch) {
//                            archOk = true;
//                            break;
//                        }
//                    }
//                }
//                if (!archOk) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
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
