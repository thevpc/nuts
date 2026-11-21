package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.NWorkspaceProfilerImpl;
import net.thevpc.nuts.runtime.standalone.extension.NIdClassLoaderRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/**
 * ARCH-10: static caches must be resettable when several workspaces are created
 * in one JVM, otherwise classloaders/profiler data from a discarded workspace leak.
 */
public class StaticCacheLifecycleTest {

    private static final String[] REGISTRY_CACHES = {
            "BY_ID", "BY_PATH", "CLASS_TO_LEAF", "PACKAGE_TO_LEAVES", "RESOURCE_TO_LEAVES",
            "UNINDEXED_LEAVES", "LEAF_INDEXES", "RESOLVED_CLASS_CACHE", "NEGATIVE_CLASS_CACHE"
    };

    @Test
    public void profilerResetClearsAccumulatedData() {
        NWorkspaceProfilerImpl.of("arch10-test").incrementAndGet();
        Assertions.assertFalse(NWorkspaceProfilerImpl.map.isEmpty());
        NWorkspaceProfilerImpl.resetForTests();
        Assertions.assertTrue(NWorkspaceProfilerImpl.map.isEmpty());
    }

    @Test
    public void classLoaderRegistryResetAllowsWorkspaceRecreation() throws Exception {
        NWorkspace first = TestUtils.openNewMinTestWorkspace();
        Assertions.assertNotNull(first);
        NWorkspaceProfilerImpl.of("arch10-workspace").incrementAndGet();

        NIdClassLoaderRegistry.resetForTests();
        NWorkspaceProfilerImpl.resetForTests();
        assertRegistryCachesEmpty();
        Assertions.assertTrue(NWorkspaceProfilerImpl.map.isEmpty()
                || !NWorkspaceProfilerImpl.map.containsKey("arch10-workspace"));

        NWorkspace second = TestUtils.openNewMinTestWorkspace();
        Assertions.assertNotNull(second);
        Assertions.assertNotSame(first, second);
    }

    private static void assertRegistryCachesEmpty() throws Exception {
        for (String name : REGISTRY_CACHES) {
            Field field = NIdClassLoaderRegistry.class.getDeclaredField(name);
            field.setAccessible(true);
            Object value = field.get(null);
            if (value instanceof Map) {
                Assertions.assertTrue(((Map<?, ?>) value).isEmpty(), name + " was not cleared");
            } else if (value instanceof Collection) {
                Assertions.assertTrue(((Collection<?>) value).isEmpty(), name + " was not cleared");
            }
        }
    }
}
