package net.thevpc.nuts.core.test;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyBuilder;
import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NDescriptorBuilder;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for the effective-descriptor cache hot path (PERF-01/02).
 */
public class DescriptorCachePerformanceTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void repeatedEffectiveResolutionIsStable() {
        NDescriptor descriptor = NDescriptorBuilder.of()
                .id("test:perf-desc#1.0")
                .name("perf-desc")
                .build();

        NDescriptor first = NWorkspace.of().resolveEffectiveDescriptor(descriptor);
        NDescriptor second = NWorkspace.of().resolveEffectiveDescriptor(descriptor);
        Assertions.assertNotNull(first);
        Assertions.assertNotNull(second);
        Assertions.assertEquals(first.id().longName(), second.id().longName());
        Assertions.assertEquals(first.dependencies(), second.dependencies());
    }

    @Test
    public void standardDependencyMetadataIsIndexedByShortName() {
        NDependency standard = NDependencyBuilder.of("test", "perf-lib")
                .version("2.0")
                .scope("runtime")
                .optional("true")
                .build();
        NDependency dependency = NDependencyBuilder.of("test", "perf-lib")
                .version((String) null)
                .scope((String) null)
                .optional(null)
                .build();
        NDescriptor descriptor = NDescriptorBuilder.of()
                .id("test:perf-index#1.0")
                .name("perf-index")
                .addStandardDependency(standard)
                .addDependency(dependency)
                .build();

        NDescriptor effective = NWorkspace.of().resolveEffectiveDescriptor(descriptor);
        NDependency resolved = effective.dependencies().stream()
                .filter(d -> d.toId().shortName().contains("perf-lib"))
                .findFirst().orElse(null);
        Assertions.assertNotNull(resolved, "dependency must be preserved");
        Assertions.assertEquals("2.0", resolved.version().toString(),
                "version must be filled from the indexed standard dependency");
        Assertions.assertNotNull(resolved.scope());
        Assertions.assertFalse(resolved.scope().isEmpty());
    }
}
