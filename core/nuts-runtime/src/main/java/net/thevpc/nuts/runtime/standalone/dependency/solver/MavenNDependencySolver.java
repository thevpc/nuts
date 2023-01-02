package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.dependency.DefaultNDependencies;
import net.thevpc.nuts.runtime.standalone.dependency.DefaultNDependencyTreeNode;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.util.NAssert;

import java.util.*;
import java.util.stream.Collectors;

public class MavenNDependencySolver implements NDependencySolver {

    private List<NDependencyTreeNodeBuild> defs = new ArrayList<>();
    private List<RootInfo> pending = new ArrayList<>();
    private NWorkspace ws;
    private NSession session;
    private NDependencyFilter dependencyFilter;
    private NDependencyFilter effDependencyFilter;
    private boolean shouldIncludeContent = false;//shouldIncludeContent(this);
    private boolean failFast;

    public MavenNDependencySolver(NSession session) {
        this.session=session;
        this.ws=session.getWorkspace();
    }

    ;

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
    public NDependencySolver setFilter(NDependencyFilter dependencyFilter) {
        this.dependencyFilter = dependencyFilter;
        this.effDependencyFilter = null;
        return this;
    }

    @Override
    public NDependencies solve() {
        //session = NutsWorkspaceUtils.bindSession(ws, session);
        for (RootInfo rootInfo : pending) {
            addRootDefinition0(rootInfo.dependency, rootInfo.def, session);
        }
        pending.clear();
        List<NDependencyTreeNodeBuild> mergedRootNodeBuilders = new ArrayList<>();
        List<NDependencyTreeNodeBuild> nonMergedRootNodeBuilders = new ArrayList<>();
        Queue<NDependencyTreeNodeBuild> queue = new ArrayDeque<>();
        Set<NId> sourceIds = new LinkedHashSet<>();
        LinkedHashSet<NDependency> immediates = new LinkedHashSet<>();
        NDependencyInfoSet mergedVisitedSet = new NDependencyInfoSet();
        NDependencyInfoSet nonMergedVisitedSet = new NDependencyInfoSet();
        NDependencyFilter effDependencyFilter = null;
        NDependencyFilters filter = NDependencyFilters.of(session);
        if (dependencyFilter == null) {
            effDependencyFilter = filter.always();
        } else {
            effDependencyFilter = dependencyFilter;
        }
        for (NDependencyTreeNodeBuild currentNode : defs) {
            NId id = currentNode.getEffectiveId();
            if (sourceIds.add(id)) {
//                sourceIds.add(id);
                if (mergedVisitedSet.add(currentNode.key)) {
                    mergedRootNodeBuilders.add(currentNode);
                    List<NDependency> immediate = CoreFilterUtils.filterDependencies(id,
                            currentNode.getEffectiveDescriptor().getDependencies(),
                            effDependencyFilter, session);
                    immediates.addAll(immediate);
                    for (NDependency dependency : currentNode.def.getEffectiveDescriptor().get(session).getDependencies()) {
                        dependency = dependency.builder().setProperty("provided-by", currentNode.id.toString()).build();
//                        if(dependency.toId().contains("jai_imageio")){
//                            System.out.print("");
//                        }
                        NDependency effDependency = dependency.builder()
                                .setScope(combineScopes(currentNode.effDependency.getScope(), dependency.getScope()))
                                .build();
                        if (effDependencyFilter.acceptDependency(currentNode.def.getId(), effDependency, session)
                                && !currentNode.exclusions.contains(dependency.toId().getShortId())
                        ) {
                            NDefinition def2 = null;
                            try {
                                def2 = NSearchCommand.of(session)
                                        .addId(dependency.toId())
                                        .setSession(session).setEffective(true)
                                        .setContent(shouldIncludeContent)
                                        .setLatest(true).getResultDefinitions().required();
                            } catch (NNotFoundException ex) {
                                //
                            }
                            NDependencyTreeNodeBuild info = new NDependencyTreeNodeBuild(currentNode, def2, dependency, effDependency, currentNode.depth + 1, session);
                            info.exclusions.addAll(currentNode.exclusions);
                            for (NId exclusion : dependency.getExclusions()) {
                                info.exclusions.add(exclusion.getShortId());
                            }
                            currentNode.children.add(info);
                            nonMergedRootNodeBuilders.add(info);
                            queue.add(info);
                        }
                    }
                }
            }
        }
        NDependencyTreeNodeBuild currentNode;
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            NDependencyInfo nextId = currentNode.key;
            if (!mergedVisitedSet.contains(nextId) && nonMergedVisitedSet.add(nextId)) {
                mergedVisitedSet.add(nextId);//ensure added to merged!
                NDescriptor effectiveDescriptor = currentNode.getEffectiveDescriptor();
                if (effectiveDescriptor != null) {
                    for (NDependency dependency : effectiveDescriptor.getDependencies()) {
                        dependency = dependency.builder().setProperty("provided-by", currentNode.id.toString()).build();
//                        if(dependency.toId().contains("jai_imageio")){
//                            System.out.print("");
//                        }
                        NDependency effDependency = dependency.builder()
                                .setScope(combineScopes(currentNode.effDependency.getScope(), dependency.getScope()))
                                .build();
                        if (effDependencyFilter.acceptDependency(
                                currentNode.getEffectiveId(), effDependency, session
                        ) && !currentNode.exclusions.contains(dependency.toId().getShortId())) {
                            NDefinition def2 = null;
                            try {
                                def2 = NSearchCommand.of(session)
                                        .addId(dependency.toId())
                                        .setSession(session).setEffective(true)
                                        .setContent(shouldIncludeContent)
                                        .setLatest(true).getResultDefinitions().required();
                            } catch (NNotFoundException ex) {
                                //
                            }
                            NDependencyTreeNodeBuild info = new NDependencyTreeNodeBuild(currentNode, def2, dependency, effDependency, currentNode.depth + 1, session);
                            info.exclusions.addAll(currentNode.exclusions);
                            for (NId exclusion : dependency.getExclusions()) {
                                info.exclusions.add(exclusion.getShortId());
                            }
                            currentNode.children.add(info);
                            if(!mergedVisitedSet.contains(info.key)) {
                                queue.add(info);
                            }
                        }
                    }
                }
            } else {
                currentNode.alreadyVisited = true;
            }
        }
        List<NDependencyTreeNode> mergedRootNodes = mergedRootNodeBuilders.stream().map(NDependencyTreeNodeBuild::build).collect(Collectors.toList());
        List<NDependencyTreeNode> nonMergedRootNodes = nonMergedRootNodeBuilders.stream().map(NDependencyTreeNodeBuild::build).collect(Collectors.toList());
        final NDependency[] mergedDepsList = mergedVisitedSet.visitedSet.values().stream().map(NDependencyInfo::getDependency)
                .toArray(NDependency[]::new);
        final NDependency[] nonMergedDepsList = nonMergedVisitedSet.visitedSet.values().stream().map(NDependencyInfo::getDependency)
                .toArray(NDependency[]::new);
        return new DefaultNDependencies(
                getName(),
                sourceIds.toArray(new NId[0]), effDependencyFilter,
                immediates.toArray(new NDependency[0]),
                nonMergedDepsList,
                nonMergedRootNodes.toArray(new NDependencyTreeNode[0]),
                mergedDepsList,
                mergedRootNodes.toArray(new NDependencyTreeNode[0]),
                e-> NElements.of(e).ofString("solver"),session
        );
    }

    @Override
    public NDependencySolver add(NDependency dependency, NDefinition def) {
        pending.add(new RootInfo(dependency,def));
        return this;
    }

    public NDependencySolver addRootDefinition0(NDependency dependency, NDefinition def, NSession session) {

        if (dependency == null) {
            if (def != null) {
                dependency = def.getId().toDependency();
            } else {
                NAssert.requireNonNull(dependency, "dependency", session);
            }
        }
        if (def == null) {
            def = NSearchCommand.of(session)
                    .addId(dependency.toId()).setSession(session
                    ).setEffective(true)
                    .setContent(shouldIncludeContent)
                    .setEffective(true)
                    .setLatest(true).getResultDefinitions().required();
        }
        if (def.getEffectiveDescriptor().isNotPresent()) {
            throw new NIllegalArgumentException(session, NMsg.ofCstyle("expected an effective definition for %s", def.getId()));
        }
        NDependencyTreeNodeBuild info = new NDependencyTreeNodeBuild(null, def, dependency, dependency, 0, session);
        for (NId exclusion : dependency.getExclusions()) {
            info.exclusions.add(exclusion.getShortId());
        }
        defs.add(info);
        return this;
    }

    public NDependencyFilter getDependencyFilter() {
        return dependencyFilter;
    }

    private NDependencyScope combineScopes(String parentScope0, String childScope0) {
        NDependencyScope parentScope = NDependencyScope.parse(parentScope0).orElse( NDependencyScope.API);
        NDependencyScope childScope = NDependencyScope.parse(childScope0).orElse( NDependencyScope.API);
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

    private static class RootInfo {
        NDependency dependency;
        NDefinition def;

        public RootInfo(NDependency dependency, NDefinition def) {
            this.dependency = dependency;
            this.def = def;
        }
    }

    private static class NDependencyInfoSet {

        Map<NId, NDependencyInfo> visitedSet = new LinkedHashMap<>();

        public boolean contains(NDependencyInfo other) {
            NDependencyInfo old = visitedSet.get(other.normalized);
            if (old == null) {
                return false;
            }
            if (old.depth == other.depth) {
                if (other.real.getVersion().compareTo(old.real.getVersion()) > 0) {
                    return false;
                }
            }
            return old.depth<other.depth;
        }

        public boolean add(NDependencyInfo other) {
            NDependencyInfo old = visitedSet.get(other.normalized);
            if (old == null) {
                visitedSet.put(other.normalized, other);
                return true;
            }
            if (old.depth == other.depth) {
                if (other.real.getVersion().compareTo(old.real.getVersion()) > 0) {
                    visitedSet.put(other.normalized, other);
                    return true;
                }
            }else if(old.depth>other.depth){
                visitedSet.put(other.normalized, other);
                return true;
            }
            return false;
        }

    }

    private static class NDependencyInfo {

        NId normalized;
        NId real;
        int depth;
        NDependency dependency;

        public NDependencyInfo(NId normalized, NId real, NDependency dependency, int depth) {
            this.normalized = normalized;
            this.real = real;
            this.depth = depth;
            this.dependency = dependency;
        }

        public static NDependencyInfo of(NDependencyTreeNodeBuild currentNode) {
            NId id = currentNode.def == null ? null : currentNode.def.getId();
            if (id == null) {
                id = currentNode.dependency.toId();
            }
            return new NDependencyInfo(id.getShortId(), id, currentNode.dependency, currentNode.depth);
        }

        public NDependency getDependency() {
            return dependency;
        }
    }

    private class NDependencyTreeNodeBuild {

        NDependencyTreeNodeBuild parent;
        NId id;
        NDefinition def;
        NDependency dependency;
        NDependency effDependency;
        List<NDependencyTreeNodeBuild> children = new ArrayList<>();
        List<NId> exclusions = new ArrayList<>();
        boolean alreadyVisited;
        int depth;
        NDescriptor effDescriptor;
        NDependencyInfo key;
        NSession session;

        public NDependencyTreeNodeBuild(NDependencyTreeNodeBuild parent, NDefinition def, NDependency dependency, NDependency effDependency, int depth, NSession session) {
            this.parent = parent;
            this.def = def;
            this.dependency = dependency;
            this.effDependency = effDependency;
            this.depth = depth;
            this.id = def != null ? def.getId() : dependency != null ? dependency.toId() : null;
            this.key = NDependencyInfo.of(this);
            this.session = session;
        }

        private NId getEffectiveId() {
            return getEffectiveDescriptor().getId();
        }

        private NDescriptor getEffectiveDescriptor() {
            if (effDescriptor == null && def != null) {
                effDescriptor = def.getEffectiveDescriptor().orNull();
                if (effDescriptor == null) {
                    throw new NIllegalArgumentException(session,
                            NMsg.ofCstyle("expected an effective definition for %s", def.getId()));
                }
            }
            return effDescriptor;
        }

        private NDependencyTreeNode build() {
            NDependencyTreeNode[] nchildren = new NDependencyTreeNode[children.size()];
            for (int i = 0; i < nchildren.length; i++) {
                nchildren[i] = children.get(i).build();
            }
            return new DefaultNDependencyTreeNode(
                    dependency, Arrays.asList(nchildren), alreadyVisited
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
//            NutsOsFamily eos = NEnvs.of(session).getOsFamily();
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
//            NutsArchFamily earch = NEnvs.of(session).getArchFamily();
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
//    public NutsDependencyFilter getEffDependencyFilter(NutsSession session) {
//        if (effDependencyFilter == null) {
//            if (dependencyFilter == null) {
//                effDependencyFilter = session.dependency().filter().byOs(NEnvs.of(session).getOsFamily())
//                        .and(session.dependency().filter().byArch(NEnvs.of(session).getArchFamily()));
//            } else {
//                effDependencyFilter
//                        = dependencyFilter
//                                .and(session.dependency().filter().byOs(NEnvs.of(session).getOsFamily()))
//                                .and(session.dependency().filter().byArch(NEnvs.of(session).getArchFamily()));
//            }
//
//        }
//        return effDependencyFilter;
//    }


    @Override
    public String getName() {
        return "maven";
    }

}
