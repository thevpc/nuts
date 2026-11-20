package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.io.cache.DefaultCachedSupplier;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.clinfo.NCliInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Regression tests for P0 correctness fixes.
 */
public class P0CorrectnessTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void saveVarPersistsChangedValues() throws Exception {
        Path config = Files.createTempDirectory("nuts-clinfo").resolve("config.json");
        Assertions.assertNull(NCliInfo.loadVar(config, "test-key", null));
        Assertions.assertNull(NCliInfo.saveVar(config, "test-key", "v1"));
        Assertions.assertEquals("v1", NCliInfo.loadVar(config, "test-key", null));
        Assertions.assertEquals("v1", NCliInfo.saveVar(config, "test-key", "v2"));
        Assertions.assertEquals("v2", NCliInfo.loadVar(config, "test-key", null));
        Assertions.assertEquals("v2", NCliInfo.saveVar(config, "test-key", null));
        Assertions.assertNull(NCliInfo.loadVar(config, "test-key", null));
    }

    @Test
    public void memCacheHitsUntilCacheIdChanges() {
        final MutableValidator validator = new MutableValidator();
        final AtomicInteger calls = new AtomicInteger();
        DefaultCachedSupplier<String> supplier = DefaultCachedSupplier.ofMem(
                () -> "value-" + calls.incrementAndGet(), validator);

        Assertions.assertEquals("value-1", supplier.getValue());
        Assertions.assertEquals("value-1", supplier.getValue(), "unchanged cache id must reuse the cached value");
        Assertions.assertEquals(1, calls.get());

        validator.cacheId = "v2";
        Assertions.assertEquals("value-2", supplier.getValue());
        Assertions.assertEquals(2, calls.get());
    }

    @Test
    public void progressOptionUsesTheParsedArgument() {
        NSession session = NSession.of();
        String old = session.progressOptions();
        try {
            session.configure(false, "--progress=false");
            Assertions.assertEquals("false", session.progressOptions());

            session.configure(false, "--progress=none");
            Assertions.assertEquals("none", session.progressOptions());
        } finally {
            session.progressOptions(old);
        }
    }

    private static final class MutableValidator implements DefaultCachedSupplier.CacheValidator<String> {
        private String cacheId = "v1";

        @Override
        public String getCacheId() {
            return cacheId;
        }

        @Override
        public boolean isValidValue(String value) {
            return value != null && !value.isEmpty();
        }
    }
}
