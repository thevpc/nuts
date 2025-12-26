package net.thevpc.nuts.runtime.standalone.dependency.solver.gradle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import net.thevpc.nuts.artifact.NDependencies;
import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyTreeNode;
import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.runtime.standalone.dependency.DefaultNDependencies;
import net.thevpc.nuts.runtime.standalone.dependency.solver.maven.NDependencyInfo;
import net.thevpc.nuts.runtime.standalone.dependency.solver.maven.NDependencyInfoSet;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

/**
 * Gradle-style pass processor implementing "newest wins" conflict resolution.
 * 
 * This is the main difference from Maven's ParallelPassProcessor:
 * - Maven: "nearest to root wins" - dependencies are accepted based on depth
 * - Gradle: "newest version wins" - when conflicts occur, the newest version is chosen
 */
class GradlePassProcessor {
    private final GradleNDependencySolver gradleSolver;
    private final GradleDependencyTreeNodeBuildComparator comparator = new GradleDependencyTreeNodeBuildComparator();
    
    List<GradleDependencyTreeNodeBuild> mergedRootNodeBuilders = new ArrayList<>();
    List<GradleDependencyTreeNodeBuild> nonMergedRootNodeBuilders = new ArrayList<>();
    // Queue sorted by depth to enable level-by-level parallel fetching
    PriorityQueue<GradleDependencyTreeNodeBuild> queue = new PriorityQueue<>(comparator);
    PriorityQueue<GradleDependencyTreeNodeBuild> providedQueue = new PriorityQueue<>(comparator);

    Set<NId> sourceIds = new LinkedHashSet<>();
    LinkedHashSet<NDependency> immediates = new LinkedHashSet<>();
    NDependencyInfoSet mergedVisitedSet = new NDependencyInfoSet();
    NDependencyInfoSet nonMergedVisitedSet = new NDependencyInfoSet();
    
    // Track version conflicts for "newest wins" resolution
    // Map: groupId:artifactId -> chosen version info
    private Map<String, VersionConflict> versionConflicts = new HashMap<>();
    
    // Executor for parallel fetching
    private final ExecutorService executor;

    public GradlePassProcessor(GradleNDependencySolver gradleSolver) {
        this.gradleSolver = gradleSolver;
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setName("nuts-gradle-dependency-fetcher-" + t.getId());
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Tracks version conflicts for "newest wins" resolution
     */
    private static class VersionConflict {
        String groupArtifact; // groupId:artifactId
        String chosenVersion;
        GradleDependencyTreeNodeBuild chosenNode;
        int depth; // Track depth for tie-breaking
        
        VersionConflict(String groupArtifact, String version, GradleDependencyTreeNodeBuild node, int depth) {
            this.groupArtifact = groupArtifact;
            this.chosenVersion = version;
            this.chosenNode = node;
            this.depth = depth;
        }
    }

    /**
     * Determine if this version should replace the currently chosen version.
     * Implements Gradle's "newest wins" strategy.
     */
    private boolean shouldReplaceVersion(VersionConflict current, String newVersion, int newDepth) {
        int comparison = gradleSolver.compareVersions(newVersion, current.chosenVersion);
        if (comparison > 0) {
            // New version is newer - always win
            return true;
        } else if (comparison < 0) {
            // New version is older - never win
            return false;
        } else {
            // Same version - prefer shallower depth (nearer to root)
            return newDepth < current.depth;
        }
    }

    private void processNode(GradleDependencyTreeNodeBuild currentNode, boolean pushProvidedElsewhere, NDescriptor preloadedDescriptor) {
        gradleSolver.doLog("processNode (Gradle) " + currentNode);
        
        // Check if this dependency conflicts with a previously seen version
        String groupArtifact = getGroupArtifact(currentNode.dependency);
        VersionConflict existingConflict = versionConflicts.get(groupArtifact);
        
        if (existingConflict != null) {
            String currentVersion = currentNode.dependency.toId().getVersion().getValue();
            
            if (!shouldReplaceVersion(existingConflict, currentVersion, currentNode.depth)) {
                gradleSolver.doLog("Gradle: skipping older/equal version " + currentVersion + 
                    " (chosen: " + existingConflict.chosenVersion + ") for " + groupArtifact);
                return; // Skip this version - newer one already chosen
            } else {
                gradleSolver.doLog("Gradle: replacing version " + existingConflict.chosenVersion + 
                    " with newer " + currentVersion + " for " + groupArtifact);
                // Update the conflict tracker
                existingConflict.chosenVersion = currentVersion;
                existingConflict.chosenNode = currentNode;
                existingConflict.depth = currentNode.depth;
                // Remove the old version from visited sets to allow re-processing
                mergedVisitedSet.remove(existingConflict.chosenNode.key);
                nonMergedVisitedSet.remove(existingConflict.chosenNode.key);
            }
        }
        
        NDependencyInfo oldDepInfo = mergedVisitedSet.find(currentNode.dependency);
        if (oldDepInfo != null) {
            if (oldDepInfo.dependency.getVersion().equals(currentNode.dependency.getVersion())) {
                gradleSolver.doLog("Gradle: already visited " + currentNode.dependency);
                return;
            } else {
                // Version conflict - should have been handled above, but double-check
                gradleSolver.doLog("Gradle: version conflict detected " + currentNode.dependency + " >>> " + oldDepInfo);
                return;
            }
        }
        
        if (currentNode.provided && pushProvidedElsewhere) {
            providedQueue.add(currentNode);
            return;
        }
        
        // Use the preloaded descriptor if available
        if (preloadedDescriptor != null) {
            currentNode.setPreloadedDescriptor(preloadedDescriptor);
        }
        currentNode.build0();
        
        if (currentNode.def == null) {
            // Optional and not found
            return;
        }
        
        NDescriptor effectiveDescriptor = currentNode.getEffectiveDescriptor();
        if (effectiveDescriptor != null) {
            if (!currentNode.isAcceptableDependency(currentNode.dependency)) {
                return;
            }
            
            NId id = currentNode.getEffectiveId();
            String version = id.getVersion().getValue();
            
            // Register this version as chosen for future conflict resolution
            if (existingConflict == null) {
                versionConflicts.put(groupArtifact, new VersionConflict(groupArtifact, version, currentNode, currentNode.depth));
            }
            
            if (currentNode.depth == 0) {
                if (sourceIds.add(id) && mergedVisitedSet.add(currentNode.key)) {
                    mergedRootNodeBuilders.add(currentNode);
                    currentNode.includedInClassPath = true;
                    List<NDependency> immediate = CoreFilterUtils.filterDependencies(id,
                            currentNode.getEffectiveDescriptor().getDependencies(),
                            gradleSolver.effDependencyFilter);
                    immediates.addAll(immediate);
                } else {
                    return;
                }
            } else {
                NDependencyInfo nextId = currentNode.key;
                if (!mergedVisitedSet.contains(nextId) && nonMergedVisitedSet.add(nextId)) {
                    mergedVisitedSet.add(nextId); // Ensure added to merged!
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
                    GradleDependencyTreeNodeBuild childNode = new GradleDependencyTreeNodeBuild(gradleSolver, currentNode, dependency, null, currentNode.depth + 1);
                    currentNode.children.add(childNode);
                    if (currentNode.provided && pushProvidedElsewhere) {
                        providedQueue.add(childNode);
                    } else {
                        queue.add(childNode);
                    }
                } else {
                    gradleSolver.logRejectedDependency(dependency);
                }
            }
        }
    }

    /**
     * Extract groupId:artifactId key for version conflict tracking
     */
    private String getGroupArtifact(NDependency dependency) {
        NId id = dependency.toId();
        return id.getGroupId() + ":" + id.getArtifactId();
    }

    public NDependencies run() {
        try {
            for (GradleDependencyTreeNodeBuild def : gradleSolver.defs) {
                queue.add(def);
            }

            while (!queue.isEmpty()) {
                // Snapshot current level
                List<GradleDependencyTreeNodeBuild> currentLevel = new ArrayList<>();
                int levelDepth = queue.peek().depth;
                while (!queue.isEmpty() && queue.peek().depth == levelDepth) {
                    currentLevel.add(queue.poll());
                }

                // Parallel Fetch
                Map<GradleDependencyTreeNodeBuild, Future<NDescriptor>> futures = new HashMap<>();
                for (GradleDependencyTreeNodeBuild build : currentLevel) {
                    boolean willProcessNow = !(build.provided && true);
                    
                    if (willProcessNow) {
                        futures.put(build, executor.submit(() -> {
                            try {
                                build.build0();
                                return build.getEffectiveDescriptor();
                            } catch (Exception e) {
                                return null;
                            }
                        }));
                    }
                }

                // Sequential Commit with Gradle "newest wins" logic
                for (GradleDependencyTreeNodeBuild build : currentLevel) {
                    NDescriptor descriptor = null;
                    Future<NDescriptor> future = futures.get(build);
                    if (future != null) {
                        try {
                            descriptor = future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            // Ignore
                        }
                    }
                    processNode(build, true, descriptor);
                }
            }

            if (gradleSolver.includedProvided) {
                queue.addAll(providedQueue);
                if (!queue.isEmpty()) {
                    while (!queue.isEmpty()) {
                        List<GradleDependencyTreeNodeBuild> currentLevel = new ArrayList<>();
                        int levelDepth = queue.peek().depth;
                        while (!queue.isEmpty() && queue.peek().depth == levelDepth) {
                            currentLevel.add(queue.poll());
                        }

                        Map<GradleDependencyTreeNodeBuild, Future<NDescriptor>> futures = new HashMap<>();
                        for (GradleDependencyTreeNodeBuild build : currentLevel) {
                            futures.put(build, executor.submit(() -> {
                                try {
                                    build.build0();
                                    return build.getEffectiveDescriptor();
                                } catch (Exception e) {
                                    return null;
                                }
                            }));
                        }

                        for (GradleDependencyTreeNodeBuild build : currentLevel) {
                            NDescriptor descriptor = null;
                            Future<NDescriptor> future = futures.get(build);
                            if (future != null) {
                                try {
                                    descriptor = future.get();
                                } catch (Exception e) {
                                    // Ignore
                                }
                            }
                            processNode(build, false, descriptor);
                        }
                    }
                }
            }
        } finally {
            executor.shutdownNow();
        }

        List<NDependencyTreeNode> mergedRootNodes = mergedRootNodeBuilders.stream().map(GradleDependencyTreeNodeBuild::build).collect(Collectors.toList());
        List<NDependencyTreeNode> nonMergedRootNodes = nonMergedRootNodeBuilders.stream().map(GradleDependencyTreeNodeBuild::build).collect(Collectors.toList());
        final NDependency[] mergedDepsList = mergedVisitedSet.visitedSet.values().stream().map(NDependencyInfo::getDependency)
                .toArray(NDependency[]::new);
        final NDependency[] nonMergedDepsList = nonMergedVisitedSet.visitedSet.values().stream().map(NDependencyInfo::getDependency)
                .toArray(NDependency[]::new);
        return new DefaultNDependencies(
                gradleSolver.getName(),
                sourceIds.toArray(new NId[0]), gradleSolver.getDependencyFilter(),
                immediates.toArray(new NDependency[0]),
                nonMergedDepsList,
                nonMergedRootNodes.toArray(new NDependencyTreeNode[0]),
                mergedDepsList,
                mergedRootNodes.toArray(new NDependencyTreeNode[0]),
                () -> NElement.ofString("gradle-solver")
        );
    }
}
