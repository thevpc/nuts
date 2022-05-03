package net.thevpc.nuts.runtime.standalone.dependency.solver;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.dependency.DefaultNutsDependencies;
import net.thevpc.nuts.runtime.standalone.dependency.DefaultNutsDependencyTreeNode;
import net.thevpc.nuts.spi.NutsDependencySolver;
import net.thevpc.nuts.util.NutsUtils;

import java.util.*;
import java.util.stream.Collectors;

public class MavenNutsDependencySolver implements NutsDependencySolver {

    private List<NutsDependencyTreeNodeBuild> defs = new ArrayList<>();
    private List<RootInfo> pending = new ArrayList<>();
    private NutsWorkspace ws;
    private NutsSession session;
    private NutsDependencyFilter dependencyFilter;
    private NutsDependencyFilter effDependencyFilter;
    private boolean shouldIncludeContent = false;//shouldIncludeContent(this);
    private boolean failFast;

    public MavenNutsDependencySolver(NutsSession session) {
        this.session=session;
        this.ws=session.getWorkspace();
    }

    ;

    public NutsDependencySolver addRootId(NutsId id) {
        pending.add(new RootInfo(id.toDependency(), null));
        return this;
    }

    @Override
    public NutsDependencySolver add(NutsDefinition def) {
        pending.add(new RootInfo(null, def));
        return this;
    }

    @Override
    public NutsDependencySolver add(NutsDependency dependency) {
        pending.add(new RootInfo(dependency, null));
        return this;
    }

    @Override
    public NutsDependencySolver setFilter(NutsDependencyFilter dependencyFilter) {
        this.dependencyFilter = dependencyFilter;
        this.effDependencyFilter = null;
        return this;
    }

    @Override
    public NutsDependencies solve() {
        //session = NutsWorkspaceUtils.bindSession(ws, session);
        for (RootInfo rootInfo : pending) {
            addRootDefinition0(rootInfo.dependency, rootInfo.def, session);
        }
        pending.clear();
        List<NutsDependencyTreeNodeBuild> mergedRootNodeBuilders = new ArrayList<>();
        List<NutsDependencyTreeNodeBuild> nonMergedRootNodeBuilders = new ArrayList<>();
        Queue<NutsDependencyTreeNodeBuild> queue = new ArrayDeque<>();
        Set<NutsId> sourceIds = new LinkedHashSet<>();
        LinkedHashSet<NutsDependency> immediates = new LinkedHashSet<>();
        NutsDependencyInfoSet mergedVisitedSet = new NutsDependencyInfoSet();
        NutsDependencyInfoSet nonMergedVisitedSet = new NutsDependencyInfoSet();
        NutsDependencyFilter effDependencyFilter = null;
        NutsDependencyFilters filter = NutsDependencyFilters.of(session);
        if (dependencyFilter == null) {
            effDependencyFilter = filter.always();
        } else {
            effDependencyFilter = dependencyFilter;
        }
        for (NutsDependencyTreeNodeBuild currentNode : defs) {
            NutsId id = currentNode.getEffectiveId();
            if (sourceIds.add(id)) {
//                sourceIds.add(id);
                if (mergedVisitedSet.add(currentNode.key)) {
                    mergedRootNodeBuilders.add(currentNode);
                    List<NutsDependency> immediate = CoreFilterUtils.filterDependencies(id,
                            currentNode.getEffectiveDescriptor().getDependencies(),
                            effDependencyFilter, session);
                    immediates.addAll(immediate);
                    for (NutsDependency dependency : currentNode.def.getEffectiveDescriptor().get(session).getDependencies()) {
                        dependency = dependency.builder().setProperty("provided-by", currentNode.id.toString()).build();
//                        if(dependency.toId().contains("jai_imageio")){
//                            System.out.print("");
//                        }
                        NutsDependency effDependency = dependency.builder()
                                .setScope(combineScopes(currentNode.effDependency.getScope(), dependency.getScope()))
                                .build();
                        if (effDependencyFilter.acceptDependency(currentNode.def.getId(), effDependency, session)
                                && !currentNode.exclusions.contains(dependency.toId().getShortId())
                        ) {
                            NutsDefinition def2 = null;
                            try {
                                def2 = session.search()
                                        .addId(dependency.toId())
                                        .setSession(session).setEffective(true)
                                        .setContent(shouldIncludeContent)
                                        .setLatest(true).getResultDefinitions().required();
                            } catch (NutsNotFoundException ex) {
                                //
                            }
                            NutsDependencyTreeNodeBuild info = new NutsDependencyTreeNodeBuild(currentNode, def2, dependency, effDependency, currentNode.depth + 1, session);
                            info.exclusions.addAll(currentNode.exclusions);
                            for (NutsId exclusion : dependency.getExclusions()) {
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
        NutsDependencyTreeNodeBuild currentNode;
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            NutsDependencyInfo nextId = currentNode.key;
            if (!mergedVisitedSet.contains(nextId) && nonMergedVisitedSet.add(nextId)) {
                mergedVisitedSet.add(nextId);//ensure added to merged!
                NutsDescriptor effectiveDescriptor = currentNode.getEffectiveDescriptor();
                if (effectiveDescriptor != null) {
                    for (NutsDependency dependency : effectiveDescriptor.getDependencies()) {
                        dependency = dependency.builder().setProperty("provided-by", currentNode.id.toString()).build();
//                        if(dependency.toId().contains("jai_imageio")){
//                            System.out.print("");
//                        }
                        NutsDependency effDependency = dependency.builder()
                                .setScope(combineScopes(currentNode.effDependency.getScope(), dependency.getScope()))
                                .build();
                        if (effDependencyFilter.acceptDependency(
                                currentNode.getEffectiveId(), effDependency, session
                        ) && !currentNode.exclusions.contains(dependency.toId().getShortId())) {
                            NutsDefinition def2 = null;
                            try {
                                def2 = session.search()
                                        .addId(dependency.toId())
                                        .setSession(session).setEffective(true)
                                        .setContent(shouldIncludeContent)
                                        .setLatest(true).getResultDefinitions().required();
                            } catch (NutsNotFoundException ex) {
                                //
                            }
                            NutsDependencyTreeNodeBuild info = new NutsDependencyTreeNodeBuild(currentNode, def2, dependency, effDependency, currentNode.depth + 1, session);
                            info.exclusions.addAll(currentNode.exclusions);
                            for (NutsId exclusion : dependency.getExclusions()) {
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
        List<NutsDependencyTreeNode> mergedRootNodes = mergedRootNodeBuilders.stream().map(NutsDependencyTreeNodeBuild::build).collect(Collectors.toList());
        List<NutsDependencyTreeNode> nonMergedRootNodes = nonMergedRootNodeBuilders.stream().map(NutsDependencyTreeNodeBuild::build).collect(Collectors.toList());
        final NutsDependency[] mergedDepsList = mergedVisitedSet.visitedSet.values().stream().map(NutsDependencyInfo::getDependency)
                .toArray(NutsDependency[]::new);
        final NutsDependency[] nonMergedDepsList = nonMergedVisitedSet.visitedSet.values().stream().map(NutsDependencyInfo::getDependency)
                .toArray(NutsDependency[]::new);
        return new DefaultNutsDependencies(
                getName(),
                sourceIds.toArray(new NutsId[0]), effDependencyFilter,
                immediates.toArray(new NutsDependency[0]),
                nonMergedDepsList,
                nonMergedRootNodes.toArray(new NutsDependencyTreeNode[0]),
                mergedDepsList,
                mergedRootNodes.toArray(new NutsDependencyTreeNode[0]),
                e-> NutsElements.of(e).ofString("solver"),session
        );
    }

    @Override
    public NutsDependencySolver add(NutsDependency dependency, NutsDefinition def) {
        pending.add(new RootInfo(dependency,def));
        return this;
    }

    public NutsDependencySolver addRootDefinition0(NutsDependency dependency, NutsDefinition def, NutsSession session) {

        if (dependency == null) {
            if (def != null) {
                dependency = def.getId().toDependency();
            } else {
                NutsUtils.requireNonNull(dependency,session,"dependency");
            }
        }
        if (def == null) {
            def = session.search()
                    .addId(dependency.toId()).setSession(session
                    ).setEffective(true)
                    .setContent(shouldIncludeContent)
                    .setEffective(true)
                    .setLatest(true).getResultDefinitions().required();
        }
        if (def.getEffectiveDescriptor().isNotPresent()) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("expected an effective definition for %s", def.getId()));
        }
        NutsDependencyTreeNodeBuild info = new NutsDependencyTreeNodeBuild(null, def, dependency, dependency, 0, session);
        for (NutsId exclusion : dependency.getExclusions()) {
            info.exclusions.add(exclusion.getShortId());
        }
        defs.add(info);
        return this;
    }

    public NutsDependencyFilter getDependencyFilter() {
        return dependencyFilter;
    }

    private NutsDependencyScope combineScopes(String parentScope0, String childScope0) {
        NutsDependencyScope parentScope = NutsDependencyScope.parse(parentScope0).orElse( NutsDependencyScope.API);
        NutsDependencyScope childScope = NutsDependencyScope.parse(childScope0).orElse( NutsDependencyScope.API);
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

    public MavenNutsDependencySolver setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    private static class RootInfo {
        NutsDependency dependency;
        NutsDefinition def;

        public RootInfo(NutsDependency dependency, NutsDefinition def) {
            this.dependency = dependency;
            this.def = def;
        }
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
            return old.depth<other.depth;
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
            }else if(old.depth>other.depth){
                visitedSet.put(other.normalized, other);
                return true;
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
            return new NutsDependencyInfo(id.getShortId(), id, currentNode.dependency, currentNode.depth);
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
        NutsSession session;

        public NutsDependencyTreeNodeBuild(NutsDependencyTreeNodeBuild parent, NutsDefinition def, NutsDependency dependency, NutsDependency effDependency, int depth, NutsSession session) {
            this.parent = parent;
            this.def = def;
            this.dependency = dependency;
            this.effDependency = effDependency;
            this.depth = depth;
            this.id = def != null ? def.getId() : dependency != null ? dependency.toId() : null;
            this.key = NutsDependencyInfo.of(this);
            this.session = session;
        }

        private NutsId getEffectiveId() {
            return getEffectiveDescriptor().getId();
        }

        private NutsDescriptor getEffectiveDescriptor() {
            if (effDescriptor == null && def != null) {
                effDescriptor = def.getEffectiveDescriptor().orNull();
                if (effDescriptor == null) {
                    throw new NutsIllegalArgumentException(session,
                            NutsMessage.ofCstyle("expected an effective definition for %s", def.getId()));
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
//            NutsOsFamily eos = session.env().getOsFamily();
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
//            NutsArchFamily earch = session.env().getArchFamily();
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
//                effDependencyFilter = session.dependency().filter().byOs(session.env().getOsFamily())
//                        .and(session.dependency().filter().byArch(session.env().getArchFamily()));
//            } else {
//                effDependencyFilter
//                        = dependencyFilter
//                                .and(session.dependency().filter().byOs(session.env().getOsFamily()))
//                                .and(session.dependency().filter().byArch(session.env().getArchFamily()));
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
