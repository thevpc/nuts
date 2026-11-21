package net.thevpc.nuts.core.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ARCH-12: remove dead duplicate stacks, and keep the two deliberately
 * parallel implementations (legacy/new secret codecs, two zip paths) from
 * drifting apart on security fixes.
 */
public class DuplicateImplementationTest {

    private static final Path SRC = Paths.get("../../core/nuts-runtime/src/main/java")
            .toAbsolutePath().normalize();

    @Test
    public void unusedDeprecatedPackageIsGone() {
        Assertions.assertFalse(Files.exists(SRC.resolve("net/thevpc/nuts/runtime/deprecated")),
                "the unused runtime/deprecated stack must stay removed");
    }

    @Test
    public void bothZipImplementationsUseTheSharedSafeEntryResolver() throws Exception {
        String zipUtils = source("net/thevpc/nuts/runtime/standalone/io/util/ZipUtils.java");
        String uncompress = source("net/thevpc/nuts/runtime/standalone/xtra/compress/NUncompressZip.java");
        Assertions.assertTrue(zipUtils.contains("NArchiveSecurityUtils.resolveSafeEntry"));
        Assertions.assertTrue(uncompress.contains("NArchiveSecurityUtils.resolveSafeEntry"),
                "the second zip implementation must use the same Zip Slip guard");
    }

    @Test
    public void legacyAndV2SecretCodecsAreBothRetained() {
        Assertions.assertTrue(Files.exists(SRC.resolve(
                "net/thevpc/nuts/runtime/standalone/io/util/CoreSecurityUtilsV1.java")),
                "V1 is required to read legacy stored secrets (SEC-10)");
        Assertions.assertTrue(Files.exists(SRC.resolve(
                "net/thevpc/nuts/runtime/standalone/io/util/CoreSecurityUtilsV2.java")),
                "V2 is the default for new secrets (SEC-10)");
    }

    private static String source(String relative) throws Exception {
        return new String(Files.readAllBytes(SRC.resolve(relative)), StandardCharsets.UTF_8);
    }
}
