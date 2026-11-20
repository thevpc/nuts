package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.NDownloadIntegrity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Regression tests for downloaded archive integrity verification.
 */
public class DownloadIntegrityTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void matchingChecksumIsAccepted() throws Exception {
        Path file = Files.createTempFile("nuts-integrity", ".bin");
        Files.write(file, "hello integrity".getBytes(StandardCharsets.UTF_8));
        String sha256 = NDigest.of().sha256().source(file).computeString();
        String sha1 = NDigest.of().sha1().source(file).computeString();

        Assertions.assertDoesNotThrow(() -> NDownloadIntegrity.verify(NPath.of(file), sha256));
        Assertions.assertDoesNotThrow(() -> NDownloadIntegrity.verify(NPath.of(file), "sha256:" + sha256));
        Assertions.assertDoesNotThrow(() -> NDownloadIntegrity.verify(NPath.of(file), sha1));
    }

    @Test
    public void missingOrMismatchingChecksumFailsClosed() throws Exception {
        Path file = Files.createTempFile("nuts-integrity", ".bin");
        Files.write(file, "hello integrity".getBytes(StandardCharsets.UTF_8));

        Assertions.assertThrows(NIOException.class, () -> NDownloadIntegrity.verify(NPath.of(file), null));
        Assertions.assertThrows(NIOException.class, () -> NDownloadIntegrity.verify(NPath.of(file), "bogus"));
        Assertions.assertThrows(NIOException.class, () -> NDownloadIntegrity.verify(NPath.of(file),
                "0000000000000000000000000000000000000000000000000000000000000000"));
    }
}
