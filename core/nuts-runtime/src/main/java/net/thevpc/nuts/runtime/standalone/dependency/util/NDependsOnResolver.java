package net.thevpc.nuts.runtime.standalone.dependency.util;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the version of a dependency an artifact depends on, directly or
 * transitively, the way maven mediates it : breadth first, the nearest
 * occurrence wins (first declared on ties), the root
 * {@code dependencyManagement} overrides transitive versions and exclusions
 * apply to the whole sub-tree. Only descriptors are fetched (never jars) and
 * the walk stops as soon as the dependency is reached.
 * <br>
 * Cost is kept low by, in order :
 * <ul>
 *     <li>the {@code nuts.depends.<groupId>.<artifactId>} descriptor property,
 *     when the artifact declares the resolved version itself (no walk);</li>
 *     <li>an in-memory cache, per workspace;</li>
 *     <li>the descriptor caching of the workspace, which the walk relies on.</li>
 * </ul>
 * Results are deliberately not persisted : artifacts are often rebuilt with
 * an unchanged version, and validating a persisted result would mean reading
 * the very descriptors the walk reads. Staleness is therefore exactly that of
 * the workspace descriptors (refreshed by {@code -N}/{@code --expire}).
 */
public final class NDependsOnResolver {

    /**
     * descriptor property prefix declaring the resolved version of a
     * dependency, as in {@code nuts.depends.net.thevpc.ntexup.ntexup-api=1.1.0}
     */
    public static final String PROPERTY_PREFIX = "nuts.depends.";
    private static final String ABSENT = "-";
    private static final NLog LOG = NLog.of(NDependsOnResolver.class);

    private NDependsOnResolver() {
    }

    /**
     * true if {@code root} depends, directly or transitively, on
     * {@code dependency} with a resolved version accepted by the version
     * (filter) of {@code dependency}.
     *
     * @param root       artifact to check
     * @param dependency dependency id, its version (if any) being a version filter
     * @return true if accepted
     */
    public static boolean accept(NId root, NId dependency) {
        if (root == null || dependency == null) {
            return false;
        }
        NVersion resolved = resolve(root, dependency.shortName());
        if (resolved == null) {
            return false;
        }
        NVersion expected = dependency.version();
        if (expected == null || expected.isBlank()) {
            return true;
        }
        return expected.toFilter().acceptVersion(resolved);
    }

    /**
     * the version of {@code dependencyShortName} resolved for {@code root}, or
     * null if {@code root} does not depend on it.
     *
     * @param root                artifact to check
     * @param dependencyShortName dependency {@code groupId:artifactId}
     * @return resolved version or null
     */
    public static NVersion resolve(NId root, String dependencyShortName) {
        String key = root.longName() + ">" + dependencyShortName;
        Map<String, String> memory = memoryCache();
        String v = memory.get(key);
        if (v == null) {
            v = compute(root, dependencyShortName);
            if (v == null) {
                // unresolvable for now (descriptor not available) : retry next time
                return null;
            }
            memory.put(key, v);
        }
        return ABSENT.equals(v) ? null : NVersion.get(v).orNull();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> memoryCache() {
        return NWorkspace.of().getOrComputeProperty(NDependsOnResolver.class.getName() + ".cache",
                ConcurrentHashMap::new);
    }

    /**
     * @return resolved version, {@link #ABSENT}, or null when the root
     * descriptor cannot be fetched
     */
    private static String compute(NId root, String dependencyShortName) {
        NDescriptor rootDescriptor = fetchDescriptor(root);
        if (rootDescriptor == null) {
            return null;
        }
        String hint = rootDescriptor.getPropertyValue(PROPERTY_PREFIX + dependencyShortName.replace(':', '.'))
                .flatMap(x -> x.asString()).orNull();
        if (!NBlankable.isBlank(hint)) {
            return NStringUtils.strip(hint);
        }
        Map<String, NVersion> managed = new HashMap<>();
        for (NDependency d : rootDescriptor.standardDependencies()) {
            if (d.version() != null && !d.version().isBlank()) {
                managed.put(d.shortName(), d.version());
            }
        }
        NDependencyFilter edgeFilter = NDependencyFilter.ofRunnable();
        Set<String> visited = new HashSet<>();
        visited.add(root.shortName());
        Deque<Node> queue = new ArrayDeque<>();
        queue.add(new Node(root, rootDescriptor, Collections.emptyList()));
        while (!queue.isEmpty()) {
            Node n = queue.poll();
            NDescriptor descriptor = n.descriptor != null ? n.descriptor : fetchDescriptor(n.id);
            if (descriptor == null) {
                continue;
            }
            for (NDependency d : descriptor.dependencies()) {
                if (!edgeFilter.acceptDependency(d, n.id) || isExcluded(d, n.exclusions)) {
                    continue;
                }
                String sn = d.shortName();
                if (!visited.add(sn)) {
                    // nearest (or first declared) occurrence already won
                    continue;
                }
                NVersion version = managed.getOrDefault(sn, d.version());
                NId depId = d.toId().builder().version(version).build();
                if (version == null || version.isBlank() || !version.isSingleValue()) {
                    NId r = fetchId(depId);
                    if (r != null) {
                        depId = r;
                        version = r.version();
                    }
                }
                if (sn.equals(dependencyShortName)) {
                    return (version == null || version.isBlank()) ? ABSENT : version.toString();
                }
                List<NId> exclusions = n.exclusions;
                if (!d.exclusions().isEmpty()) {
                    exclusions = new ArrayList<>(n.exclusions);
                    exclusions.addAll(d.exclusions());
                }
                queue.add(new Node(depId, null, exclusions));
            }
        }
        return ABSENT;
    }

    private static boolean isExcluded(NDependency d, List<NId> exclusions) {
        for (NId e : exclusions) {
            if (matches(e.groupId(), d.groupId()) && matches(e.artifactId(), d.artifactId())) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(String pattern, String value) {
        return NBlankable.isBlank(pattern) || "*".equals(pattern) || pattern.equals(value);
    }

    private static NDescriptor fetchDescriptor(NId id) {
        try {
            return NFetch.of(id).failFast(false).getResultEffectiveDescriptor();
        } catch (Exception ex) {
            LOG.log(NMsg.ofC("unable to fetch descriptor of %s : %s", id, ex).asFine());
            return null;
        }
    }

    private static NId fetchId(NId id) {
        try {
            return NFetch.of(id).failFast(false).getResultId();
        } catch (Exception ex) {
            return null;
        }
    }

    private static class Node {
        final NId id;
        final NDescriptor descriptor;
        final List<NId> exclusions;

        Node(NId id, NDescriptor descriptor, List<NId> exclusions) {
            this.id = id;
            this.descriptor = descriptor;
            this.exclusions = exclusions;
        }
    }
}
