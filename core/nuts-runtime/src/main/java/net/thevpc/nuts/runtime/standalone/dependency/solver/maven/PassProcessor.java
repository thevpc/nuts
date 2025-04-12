package net.thevpc.nuts.runtime.standalone.dependency.solver.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.runtime.standalone.dependency.DefaultNDependencies;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

import java.util.*;
import java.util.stream.Collectors;

class PassProcessor {
    private final MavenNDependencySolver mavenNDependencySolver;
    NDependencyTreeNodeBuildComparator nDependencyTreeNodeBuildComparator = new NDependencyTreeNodeBuildComparator();
    List<NDependencyTreeNodeBuild> mergedRootNodeBuilders = new ArrayList<>();
    List<NDependencyTreeNodeBuild> nonMergedRootNodeBuilders = new ArrayList<>();
    PriorityQueue<NDependencyTreeNodeBuild> queue = new PriorityQueue<>(nDependencyTreeNodeBuildComparator);
    PriorityQueue<NDependencyTreeNodeBuild> providedQueue = new PriorityQueue<>(nDependencyTreeNodeBuildComparator);

    Set<NId> sourceIds = new LinkedHashSet<>();
    LinkedHashSet<NDependency> immediates = new LinkedHashSet<>();
    NDependencyInfoSet mergedVisitedSet = new NDependencyInfoSet();
    NDependencyInfoSet nonMergedVisitedSet = new NDependencyInfoSet();
    NDependencyFilter effDependencyFilter = null;
    NDependencyTreeNodeBuild currentNode;
    int currentDepth;

    public PassProcessor(MavenNDependencySolver mavenNDependencySolver) {
        this.mavenNDependencySolver = mavenNDependencySolver;
    }

    private void processNode(NDependencyTreeNodeBuild currentNode, boolean pushProvidedElsewhere) {
        mavenNDependencySolver.doLog("processCurrentNonProvided " + currentNode);
        if (currentNode.depth > currentDepth) {
            currentDepth = currentNode.depth;
        } else if (currentNode.depth < currentDepth) {
            throw new IllegalArgumentException("invalid depth " + currentNode.depth + "<" + currentDepth);
        }
        NDependencyInfo oldDepInfo = mergedVisitedSet.find(currentNode.dependency);
        if (oldDepInfo != null) {
            if (oldDepInfo.dependency.getVersion().equals(currentNode.dependency.getVersion())) {
                mavenNDependencySolver.doLog("processCurrentNonProvided already visited " + currentNode.dependency);
                return;
            } else {
                mavenNDependencySolver.doLog("processCurrentNonProvided already visited with other version " + currentNode.dependency + " >>> " + oldDepInfo);
                return;
            }
        }
        if (currentNode.provided && pushProvidedElsewhere) {
            providedQueue.add(currentNode);
            return;
        }
        currentNode.build0();
        if (currentNode.def == null) {
            //optional and not found
            return;
        }
        NDescriptor effectiveDescriptor = currentNode.getEffectiveDescriptor();
        if (effectiveDescriptor != null) {
            if (!currentNode.isAcceptableDependency(currentNode.dependency)) {
                return;
            }
            NId id = currentNode.getEffectiveId();
            if (currentNode.depth == 0) {
                if (sourceIds.add(id) && mergedVisitedSet.add(currentNode.key)) {
                    mergedRootNodeBuilders.add(currentNode);
                    currentNode.includedInClassPath = true;
                    List<NDependency> immediate = CoreFilterUtils.filterDependencies(id,
                            currentNode.getEffectiveDescriptor().getDependencies(),
                            effDependencyFilter);
                    immediates.addAll(immediate);
                } else {
                    return;
                }
            } else {
                NDependencyInfo nextId = currentNode.key;
                if (!mergedVisitedSet.contains(nextId) && nonMergedVisitedSet.add(nextId)) {
                    mergedVisitedSet.add(nextId);//ensure added to merged!
                    currentNode.includedInClassPath = true;
                } else {
                    return;
                }
            }
            if (currentNode.depth == 0) {
                nonMergedRootNodeBuilders.add(currentNode);
                currentNode.includedInClassPath = true;
            } else {
                mergedRootNodeBuilders.add(currentNode);
                currentNode.includedInClassPath = true;
            }
            for (NDependency dependency : effectiveDescriptor.getDependencies()) {
                if (currentNode.isAcceptableDependency(dependency)) {
                    NDependencyTreeNodeBuild childNode = new NDependencyTreeNodeBuild(mavenNDependencySolver, currentNode, dependency, null, currentNode.depth + 1);
                    currentNode.children.add(childNode);
                    if (currentNode.provided && pushProvidedElsewhere) {
                        providedQueue.add(currentNode);
                    }else {
                        queue.add(childNode);
                    }
                } else {
                    mavenNDependencySolver.logRejectedDependency(dependency);
                }

            }
        }
    }

    public NDependencies run() {
//        for (NDependencyTreeNodeBuild currentNode : mavenNDependencySolver.defs) {
//            NId id = currentNode.getEffectiveId();
//            if (sourceIds.add(id)) {
//                if (mergedVisitedSet.add(currentNode.key)) {
//                    mergedRootNodeBuilders.add(currentNode);
//                    List<NDependency> immediate = CoreFilterUtils.filterDependencies(id,
//                            currentNode.getEffectiveDescriptor().getDependencies(),
//                            effDependencyFilter);
//                    immediates.addAll(immediate);
//                    processCurrentNonProvided(currentNode, true);
//                }
//            }
//        }
        for (NDependencyTreeNodeBuild def : mavenNDependencySolver.defs) {
            queue.add(def);
        }
        while (!queue.isEmpty()) {
            currentNode = queue.remove();
            processNode(currentNode, true);
        }
        if(mavenNDependencySolver.includedProvided) {
            queue.addAll(providedQueue);
            if (!queue.isEmpty()) {
                currentDepth = 0;
                while (!queue.isEmpty()) {
                    currentNode = queue.remove();
                    processNode(currentNode, false);
                }
            }
        }
        List<NDependencyTreeNode> mergedRootNodes = mergedRootNodeBuilders.stream().map(NDependencyTreeNodeBuild::build).collect(Collectors.toList());
        List<NDependencyTreeNode> nonMergedRootNodes = nonMergedRootNodeBuilders.stream().map(NDependencyTreeNodeBuild::build).collect(Collectors.toList());
        final NDependency[] mergedDepsList = mergedVisitedSet.visitedSet.values().stream().map(NDependencyInfo::getDependency)
                .toArray(NDependency[]::new);
        final NDependency[] nonMergedDepsList = nonMergedVisitedSet.visitedSet.values().stream().map(NDependencyInfo::getDependency)
                .toArray(NDependency[]::new);
        return new DefaultNDependencies(
                mavenNDependencySolver.getName(),
                sourceIds.toArray(new NId[0]), effDependencyFilter,
                immediates.toArray(new NDependency[0]),
                nonMergedDepsList,
                nonMergedRootNodes.toArray(new NDependencyTreeNode[0]),
                mergedDepsList,
                mergedRootNodes.toArray(new NDependencyTreeNode[0]),
                () -> NElements.of().ofString("solver")
        );
    }


}
