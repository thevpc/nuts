package net.thevpc.nuts.core.test;

import net.thevpc.nuts.artifact.NArtifactCall;
import net.thevpc.nuts.artifact.NArtifactCallBuilder;
import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NDescriptorBuilder;
import net.thevpc.nuts.command.NExecutionEntry;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.text.NDescriptorWriter;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreSecurityUtilsV1;
import net.thevpc.nuts.runtime.standalone.platform.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaJarUtils;
import net.thevpc.nuts.util.NIllegalArgumentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Regression tests for P1 correctness fixes (BUG-05/06/07/12).
 */
public class P1CorrectnessTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void classpathScanContinuesAfterUnloadableClass() throws Exception {
        java.nio.file.Path jar = java.nio.file.Files.createTempFile("nuts-cp-scan", ".jar");
        try (ZipOutputStream zip = new ZipOutputStream(java.nio.file.Files.newOutputStream(jar))) {
            zip.putNextEntry(new ZipEntry("does/not/exist/Missing.class"));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("java/lang/String.class"));
            zip.closeEntry();
        }
        Assertions.assertTrue(CorePlatformUtils.isLoadedClassPath(
                jar.toFile(), getClass().getClassLoader(), null),
                "a single unloadable class must not hide a loadable one");
    }

    @Test
    public void mainClassSeparatedArgumentIsParsed() throws Exception {
        NArtifactCall executor = NArtifactCallBuilder.of()
                .id(NId.of("test:runner#1.0"))
                .arguments("--main-class", "com.example.Main")
                .build();
        NDescriptor descriptor = NDescriptorBuilder.of()
                .id("test:main-class#1.0")
                .executor(executor)
                .build();
        String descriptorJson;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8)) {
            NDescriptorWriter.of().print(descriptor, writer);
            writer.flush();
            descriptorJson = bos.toString(StandardCharsets.UTF_8.name());
        }

        byte[] jarBytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(bos)) {
            zip.putNextEntry(new ZipEntry("META-INF/nuts/test/nuts.json"));
            zip.write(descriptorJson.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            jarBytes = bos.toByteArray();
        }

        List<NExecutionEntry> entries = JavaJarUtils.parseJarExecutionEntries(
                new ByteArrayInputStream(jarBytes));
        Assertions.assertTrue(entries.stream().anyMatch(
                e -> "com.example.Main".equals(e.name())),
                "--main-class Foo must register Foo as the entry, not '--main-class'");
    }

    @Test
    public void legacyEncryptionRoundTripsNonAscii() {
        char[] encrypted = CoreSecurityUtilsV1.INSTANCE.defaultEncryptChars(
                "héllo-ünïcode".toCharArray(), "passphrase");
        char[] decrypted = CoreSecurityUtilsV1.INSTANCE.defaultDecryptChars(encrypted, "passphrase");
        Assertions.assertEquals("héllo-ünïcode", new String(decrypted));
    }

    @Test
    public void truncatedCiphertextIsRejected() {
        Assertions.assertThrows(NIllegalArgumentException.class, () ->
                CoreSecurityUtilsV1.INSTANCE.defaultDecryptChars("AAAA".toCharArray(), "passphrase"));
    }
}
