package net.thevpc.nuts.runtime.standalone.dependency.solver.maven;

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
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

class ParallelPassProcessor {
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
    
    // Executor for parallel fetching
    private final ExecutorService executor;

    public ParallelPassProcessor(MavenNDependencySolver mavenNDependencySolver) {
        this.mavenNDependencySolver = mavenNDependencySolver;
        // Use a cached thread pool to adapt to the number of dependencies
        // We could also use a fixed pool based on CPU cores or configuration
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setName("nuts-dependency-fetcher-" + t.getId());
            t.setDaemon(true);
            return t;
        });
    }

    private void processNode(NDependencyTreeNodeBuild currentNode, boolean pushProvidedElsewhere, NDescriptor preloadedDescriptor) {
        mavenNDependencySolver.doLog("processCurrentNonProvided " + currentNode);
        
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
        
        // Use the preloaded descriptor if available, otherwise fallback (shouldn't happen in parallel mode)
        if (preloadedDescriptor != null) {
            currentNode.setPreloadedDescriptor(preloadedDescriptor);
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
                            mavenNDependencySolver.effDependencyFilter);
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
        try {
            for (NDependencyTreeNodeBuild def : mavenNDependencySolver.defs) {
                queue.add(def);
            }

            while (!queue.isEmpty()) {
                // 1. Snapshot current level
                List<NDependencyTreeNodeBuild> currentLevel = new ArrayList<>();
                NDependencyTreeNodeBuild node;
                // Since queue is a PriorityQueue ordered by depth, we can drain all nodes of the same (lowest) depth
                int levelDepth = queue.peek().depth;
                while (!queue.isEmpty() && queue.peek().depth == levelDepth) {
                    currentLevel.add(queue.poll());
                }

                // 2. Parallel Fetch
                Map<NDependencyTreeNodeBuild, Future<NDescriptor>> futures = new HashMap<>();
                for (NDependencyTreeNodeBuild build : currentLevel) {
                    // Only fetch if not provided (or if we are processing provided queue later)
                    // Logic from processNode: if provided && pushProvidedElsewhere, we just add to providedQueue
                    // So we only need to fetch if we are actually going to process it now.
                    // However, to be safe and simple, we can fetch all. 
                    // But wait, processNode adds to providedQueue BEFORE build0() if provided is true.
                    // So we should check that condition here to avoid unnecessary fetches.
                    boolean willProcessNow = !(build.provided && true); // pushProvidedElsewhere is true here
                    
                    if (willProcessNow) {
                         futures.put(build, executor.submit(() -> {
                             try {
                                 // This triggers the fetch
                                 build.build0(); 
                                 return build.getEffectiveDescriptor();
                             } catch (Exception e) {
                                 // Log or handle? build0() usually swallows exceptions or sets state
                                 return null;
                             }
                         }));
                    }
                }

                // 3. Sequential Commit
                for (NDependencyTreeNodeBuild build : currentLevel) {
                    NDescriptor descriptor = null;
                    Future<NDescriptor> future = futures.get(build);
                    if (future != null) {
                        try {
                            descriptor = future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            // Ignore, build0() likely handled it or it failed
                        }
                    }
                    processNode(build, true, descriptor);
                }
            }

            if(mavenNDependencySolver.includedProvided) {
                queue.addAll(providedQueue);
                if (!queue.isEmpty()) {
                    // For provided queue, we can also parallelize if needed, but let's stick to the pattern
                    // Provided queue might have mixed depths? The comparator sorts by depth.
                    
                    while (!queue.isEmpty()) {
                         // Snapshot level
                        List<NDependencyTreeNodeBuild> currentLevel = new ArrayList<>();
                        int levelDepth = queue.peek().depth;
                        while (!queue.isEmpty() && queue.peek().depth == levelDepth) {
                            currentLevel.add(queue.poll());
                        }

                        // Parallel Fetch
                        Map<NDependencyTreeNodeBuild, Future<NDescriptor>> futures = new HashMap<>();
                        for (NDependencyTreeNodeBuild build : currentLevel) {
                             futures.put(build, executor.submit(() -> {
                                 try {
                                     build.build0(); 
                                     return build.getEffectiveDescriptor();
                                 } catch (Exception e) {
                                     return null;
                                 }
                             }));
                        }

                        // Sequential Commit
                        for (NDependencyTreeNodeBuild build : currentLevel) {
                            NDescriptor descriptor = null;
                            Future<NDescriptor> future = futures.get(build);
                            if (future != null) {
                                try {
                                    descriptor = future.get();
                                } catch (Exception e) {
                                    // ignore
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

        List<NDependencyTreeNode> mergedRootNodes = mergedRootNodeBuilders.stream().map(NDependencyTreeNodeBuild::build).collect(Collectors.toList());
        List<NDependencyTreeNode> nonMergedRootNodes = nonMergedRootNodeBuilders.stream().map(NDependencyTreeNodeBuild::build).collect(Collectors.toList());
        final NDependency[] mergedDepsList = mergedVisitedSet.visitedSet.values().stream().map(NDependencyInfo::getDependency)
                .toArray(NDependency[]::new);
        final NDependency[] nonMergedDepsList = nonMergedVisitedSet.visitedSet.values().stream().map(NDependencyInfo::getDependency)
                .toArray(NDependency[]::new);
        return new DefaultNDependencies(
                mavenNDependencySolver.getName(),
                sourceIds.toArray(new NId[0]), mavenNDependencySolver.getDependencyFilter(),
                immediates.toArray(new NDependency[0]),
                nonMergedDepsList,
                nonMergedRootNodes.toArray(new NDependencyTreeNode[0]),
                mergedDepsList,
                mergedRootNodes.toArray(new NDependencyTreeNode[0]),
                () -> NElement.ofString("solver")
        );
    }
}
