package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.io.util.NAtomicFileWriter;
import net.thevpc.nuts.io.NIOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Regression tests for atomic configuration writes (REL-05).
 */
public class AtomicConfigWriteTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void writeReplacesTargetAndLeavesNoTemporaryFiles() throws Exception {
        Path dir = Files.createTempDirectory("nuts-atomic-write");
        Path target = dir.resolve("nested").resolve("config.json");
        NAtomicFileWriter.writeString(target, "{\"a\":1}", StandardCharsets.UTF_8);
        NAtomicFileWriter.writeString(target, "{\"a\":2}", StandardCharsets.UTF_8);

        Assertions.assertEquals("{\"a\":2}", new String(Files.readAllBytes(target), StandardCharsets.UTF_8));
        try (java.util.stream.Stream<Path> s = Files.list(target.getParent())) {
            Assertions.assertEquals(1, s.count(), "no .part files must remain");
        }
    }

    @Test
    public void failedWriteKeepsOriginalContent() throws Exception {
        Path dir = Files.createTempDirectory("nuts-atomic-failure");
        Path target = dir.resolve("config.json");
        NAtomicFileWriter.writeString(target, "original", StandardCharsets.UTF_8);

        Assertions.assertThrows(NIOException.class, () ->
                NAtomicFileWriter.write(target, StandardCharsets.UTF_8, out -> {
                    out.write("partial");
                    throw new IOException("simulated crash");
                }));

        Assertions.assertEquals("original", new String(Files.readAllBytes(target), StandardCharsets.UTF_8));
        try (java.util.stream.Stream<Path> s = Files.list(dir)) {
            Assertions.assertEquals(1, s.count(), "failed write must clean up its temp file");
        }
    }
}
