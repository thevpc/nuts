package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NUncompress;
import net.thevpc.nuts.runtime.standalone.io.util.NArchiveSecurityUtils;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Regression tests for safe archive extraction (Zip Slip and zip bomb protection).
 */
public class ArchiveSecurityTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    private static Path createZip(Path folder, String entryName, byte[] content) throws IOException {
        Path archive = folder.resolve("test.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(archive))) {
            out.putNextEntry(new ZipEntry(entryName));
            out.write(content);
            out.closeEntry();
        }
        return archive;
    }

    @Test
    public void zipSlipIsRejectedByUncompress() throws IOException {
        Path work = Files.createTempDirectory("nuts-archive-slip");
        Path archive = createZip(work, "../escaped.txt", "pwned".getBytes(StandardCharsets.UTF_8));
        Path parent = Files.createDirectories(work.resolve("parent"));
        Path target = Files.createDirectories(parent.resolve("target"));

        Assertions.assertThrows(NIOException.class, () ->
                NUncompress.of().packaging("zip").source(archive).target(target).run());

        Assertions.assertFalse(Files.exists(parent.resolve("escaped.txt")),
                "archive entry must not escape the extraction root");
        Assertions.assertEquals(0, Files.list(target).count(),
                "no file should be extracted from a rejected archive");
    }

    @Test
    public void zipSlipIsRejectedByZipUtils() throws IOException {
        Path work = Files.createTempDirectory("nuts-ziputils-slip");
        Path archive = createZip(work, "../escaped.txt", "pwned".getBytes(StandardCharsets.UTF_8));
        Path parent = Files.createDirectories(work.resolve("parent"));
        Path target = Files.createDirectories(parent.resolve("target"));

        Assertions.assertThrows(IOException.class, () ->
                ZipUtils.unzip(archive.toString(), target.toString(), null));

        Assertions.assertFalse(Files.exists(parent.resolve("escaped.txt")));
    }

    @Test
    public void extractionBudgetIsEnforced() throws IOException {
        Path work = Files.createTempDirectory("nuts-archive-bomb");
        Path archive = createZip(work, "data.txt", new byte[1024]);
        Path target = Files.createDirectories(work.resolve("target"));
        String old = System.getProperty(NArchiveSecurityUtils.PROP_MAX_TOTAL_BYTES);
        System.setProperty(NArchiveSecurityUtils.PROP_MAX_TOTAL_BYTES, "16");
        try {
            Assertions.assertThrows(NIOException.class, () ->
                    NUncompress.of().packaging("zip").source(archive).target(target).run());
            Assertions.assertFalse(Files.exists(target.resolve("data.txt")),
                    "partial files must be removed when a budget is exceeded");
        } finally {
            if (old == null) {
                System.clearProperty(NArchiveSecurityUtils.PROP_MAX_TOTAL_BYTES);
            } else {
                System.setProperty(NArchiveSecurityUtils.PROP_MAX_TOTAL_BYTES, old);
            }
        }
    }

    @Test
    public void entryNamesAreValidated() throws IOException {
        String[] invalid = {"../x", "a/../../x", "/etc/passwd", "C:/windows/x", "a\\..\\x", "a\\..\\b"};
        for (String entry : invalid) {
            Assertions.assertThrows(IOException.class,
                    () -> NArchiveSecurityUtils.validateEntryName(entry), entry);
        }
        Assertions.assertEquals("a/b.txt", NArchiveSecurityUtils.validateEntryName("a/b.txt"));
    }

    @Test
    public void resolvedEntriesCannotCrossSymlinks() throws IOException {
        Path root = Files.createTempDirectory("nuts-archive-symlink");
        Path outside = Files.createTempDirectory("nuts-archive-outside");
        Files.createSymbolicLink(root.resolve("link"), outside);
        Assertions.assertThrows(IOException.class, () ->
                NArchiveSecurityUtils.resolveSafeEntry(root, "link/evil.txt"));
        Assertions.assertEquals(root.resolve("ok.txt").toAbsolutePath().normalize(),
                NArchiveSecurityUtils.resolveSafeEntry(root, "ok.txt"));
    }
}
