package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.io.NReservedMonitoredURLInputStream;
import net.thevpc.nuts.runtime.standalone.security.util.CoreDigestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Regression tests for file-descriptor and connection leak fixes (REL-02/03).
 */
public class ReliabilityLeakTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void closingPartialDownloadNeverThrows() throws Exception {
        Path file = Files.createTempFile("nuts-partial-download", ".txt");
        Files.write(file, "0123456789".getBytes(StandardCharsets.UTF_8));
        NReservedMonitoredURLInputStream in = NReservedMonitoredURLInputStream.of(
                file.toUri().toURL(), null);
        Assertions.assertEquals('0', in.read());
        Assertions.assertDoesNotThrow(in::close);
    }

    @Test
    public void directoryDigestDoesNotLeakFileDescriptors() throws Exception {
        Path dir = Files.createTempDirectory("nuts-digest-fds");
        for (int i = 0; i < 40; i++) {
            Files.write(dir.resolve("file-" + i + ".txt"),
                    ("content-" + i).getBytes(StandardCharsets.UTF_8));
        }
        Path fdDir = Paths.get("/proc/self/fd");
        Assumptions.assumeTrue(Files.isDirectory(fdDir), "requires Linux /proc");

        // warm up class loading and caches
        new CoreDigestHelper().append(dir).getDigest();
        int before = countOpenFds(fdDir);
        for (int i = 0; i < 5; i++) {
            new CoreDigestHelper().append(dir).getDigest();
        }
        int after = countOpenFds(fdDir);
        Assertions.assertTrue(after <= before + 2,
                "directory digest leaked file descriptors: before=" + before + ", after=" + after);
    }

    private static int countOpenFds(Path fdDir) {
        try (java.util.stream.Stream<Path> s = Files.list(fdDir)) {
            return (int) s.count();
        } catch (Exception ex) {
            return -1;
        }
    }
}
