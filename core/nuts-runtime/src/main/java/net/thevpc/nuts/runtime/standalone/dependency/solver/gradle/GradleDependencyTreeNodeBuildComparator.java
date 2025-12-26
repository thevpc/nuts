package net.thevpc.nuts.runtime.standalone.dependency.solver.gradle;

import java.util.Comparator;

/**
 * Comparator for Gradle dependency tree node ordering.
 * Orders by depth (breadth-first traversal).
 * <p>
 * Note: While Gradle uses "newest wins" (version-based) for conflict resolution,
 * we sort by depth here to:
 * 1. Enable level-by-level parallel processing in GradlePassProcessor
 * 2. Provide a stable tie-breaker (shallower wins) for identical versions
 * </p>
 */
class GradleDependencyTreeNodeBuildComparator implements Comparator<GradleDependencyTreeNodeBuild> {
    @Override
    public int compare(GradleDependencyTreeNodeBuild o1, GradleDependencyTreeNodeBuild o2) {
        // Order by depth (breadth-first)
        int c = Integer.compare(o1.depth, o2.depth);
        if (c != 0) {
            return c;
        }
        // Secondary ordering by dependency string for stability
        return o1.dependency.toString().compareTo(o2.dependency.toString());
    }
}
