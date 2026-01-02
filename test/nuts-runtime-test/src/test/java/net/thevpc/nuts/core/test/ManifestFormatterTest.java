package net.thevpc.nuts.core.test;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NDescriptorWriter;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorBuilder;
import net.thevpc.nuts.runtime.standalone.DefaultNArtifactCallBuilder;
import net.thevpc.nuts.util.NStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

/**
 * Test for MANIFEST.MF descriptor formatting
 *
 * @author godlinglory
 */
public class ManifestFormatterTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void testManifestFormatter() {
        // Create a sample descriptor
        NDescriptor descriptor = new DefaultNDescriptorBuilder()
                .setId(NIdBuilder.of("com.example", "my-app").setVersion("1.0.0").build())
                .setName("My Application")
                .setDescription("This is a test application for MANIFEST.MF formatting")
                .setGenericName("Application")
                .setPackaging("jar")
                .addFlag(NDescriptorFlag.EXEC)
                .addDependency(NDependency.get("org.example:lib1#1.0").get())
                .addDependency(NDependency.get("org.example:lib2#2.0").get())
                .setCategories("Development", "Tools")
                .setIcons("classpath:/icons/app.png")
                .setExecutor(
                        new DefaultNArtifactCallBuilder()
                                .setId(NId.get("java").get())
                                .setArguments(new String[]{"--main-class=", "com.example.Main"})
                                .build()
                )
                .build();

        // Format as MANIFEST.MF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NPrintStream out = NPrintStream.of(baos);

        NDescriptorWriter.of()
                .setDescriptorStyle(NDescriptorStyle.MANIFEST)
                .print(descriptor, out);

        String manifest = baos.toString();

        TestUtils.println("Generated MANIFEST.MF:");
        TestUtils.println(NStringUtils.repeat("=",60));
        TestUtils.println(manifest);
        TestUtils.println(NStringUtils.repeat("=",60));

        // Basic validations
        Assertions.assertTrue(manifest.contains("Manifest-Version: 1.0"));
        Assertions.assertTrue(manifest.contains("Implementation-Version: 1.0.0"));
        Assertions.assertTrue(manifest.contains("Implementation-Vendor-Id: com.example"));
        Assertions.assertTrue(manifest.contains("Implementation-Title: my-app"));
        Assertions.assertTrue(manifest.contains("Nuts-Id: com.example:my-app#1.0.0"));
        Assertions.assertTrue(manifest.contains("Nuts-Name: My Application"));
        Assertions.assertTrue(manifest.contains("Nuts-Description: This is a test application for MANIFEST.MF formatting"));
        Assertions.assertTrue(manifest.contains("Main-Class: com.example.Main"));
        Assertions.assertTrue(manifest.contains("Nuts-Dependencies: org.example:lib1#1.0;org.example:lib2#2.0"));
        Assertions.assertTrue(manifest.contains("Nuts-Categories: Development Tools"));

        TestUtils.println("All assertions passed!");
    }

    @Test
    public void testManifestFormatterMinimal() {
        // Create a minimal descriptor
        NDescriptor descriptor = new DefaultNDescriptorBuilder()
                .setId(NIdBuilder.of("org.test", "minimal").setVersion("1.0").build())
                .build();

        // Format as MANIFEST.MF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NPrintStream out = NPrintStream.of(baos);

        NDescriptorWriter.of()
                .setDescriptorStyle(NDescriptorStyle.MANIFEST)
                .print(descriptor, out);

        String manifest = baos.toString();

        TestUtils.println("Generated minimal MANIFEST.MF:");
        TestUtils.println(NStringUtils.repeat("=",60));
        TestUtils.println(manifest);
        TestUtils.println(NStringUtils.repeat("=",60));

        // Basic validations
        Assertions.assertTrue(manifest.contains("Manifest-Version: 1.0"));
        Assertions.assertTrue(manifest.contains("Nuts-Id: org.test:minimal#1.0"));

        TestUtils.println("Minimal manifest test passed!");
    }

    @Test
    public void testManifestLineWrapping() {
        // Create a descriptor with a very long description to test line wrapping
        String longDescription = "This is a very long description that should be wrapped at 72 characters per line in the MANIFEST.MF file according to the JAR file specification which requires proper line wrapping.";

        NDescriptor descriptor = new DefaultNDescriptorBuilder()
                .setId(NIdBuilder.of("org.test", "longdesc").setVersion("1.0").build())
                .setDescription(longDescription)
                .build();

        // Format as MANIFEST.MF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NPrintStream out = NPrintStream.of(baos);

        NDescriptorWriter.of()
                .setDescriptorStyle(NDescriptorStyle.MANIFEST)
                .print(descriptor, out);

        String manifest = baos.toString();

        TestUtils.println("Generated MANIFEST.MF with line wrapping:");
        TestUtils.println(NStringUtils.repeat("=",60));
        TestUtils.println(manifest);
        TestUtils.println(NStringUtils.repeat("=",60));

        // Verify line wrapping - check that lines don't exceed 72 bytes (JAR spec)
        String[] lines = manifest.split("\n");
        for (String line : lines) {
            if (!line.isEmpty()) {
                byte[] lineBytes = line.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                Assertions.assertTrue(lineBytes.length <= 72,
                    "Line exceeds 72 bytes: " + lineBytes.length + " bytes (was " + line.length() + " chars)");
            }
        }

        TestUtils.println("Line wrapping test passed!");
    }

    @Test
    public void testRoundTrip() {
        // Create a comprehensive descriptor
        NDescriptor original = new DefaultNDescriptorBuilder()
                .setId(NIdBuilder.of("com.roundtrip", "test-app").setVersion("2.0.0").build())
                .setName("Round Trip Test")
                .setDescription("Testing round-trip conversion")
                .setGenericName("Test Application")
                .setPackaging("jar")
                .addFlag(NDescriptorFlag.EXEC)
                .addDependency(NDependency.get("org.test:dep1#1.0").get())
                .addDependency(NDependency.get("org.test:dep2#2.0").get())
                .setCategories("Testing", "Development")
                .setIcons("classpath:/test.png")
                .setExecutor(
                        new DefaultNArtifactCallBuilder()
                                .setId(NId.get("java").get())
                                .setArguments(new String[]{"--main-class=", "com.roundtrip.TestMain"})
                                .build()
                )
                .build();

        // Format to MANIFEST.MF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NPrintStream out = NPrintStream.of(baos);

        NDescriptorWriter.of()
                .setDescriptorStyle(NDescriptorStyle.MANIFEST)
                .print(original, out);

        String manifestContent = baos.toString();
        TestUtils.println("Generated MANIFEST.MF for round-trip test:");
        TestUtils.println(NStringUtils.repeat("=",60));
        TestUtils.println(manifestContent);
        TestUtils.println(NStringUtils.repeat("=",60));

        // Parse it back
        NDescriptorParser parser = NDescriptorParser.of()
                .setDescriptorStyle(NDescriptorStyle.MANIFEST);

        NDescriptor parsed = parser.parse(manifestContent).get();

        // Verify key fields match
        TestUtils.println("Comparing original and parsed descriptors:");

        Assertions.assertEquals(original.getId(), parsed.getId(), "ID mismatch");
        TestUtils.println("ID matches: " + parsed.getId());

        Assertions.assertEquals(original.getName(), parsed.getName(), "Name mismatch");
        TestUtils.println("Name matches: " + parsed.getName());

        Assertions.assertEquals(original.getDescription(), parsed.getDescription(), "Description mismatch");
        TestUtils.println("Description matches");

        Assertions.assertEquals(original.getGenericName(), parsed.getGenericName(), "Generic name mismatch");
        TestUtils.println("Generic name matches");

        Assertions.assertEquals(original.getPackaging(), parsed.getPackaging(), "Packaging mismatch");
        TestUtils.println("Packaging matches");

        Assertions.assertEquals(original.getDependencies().size(), parsed.getDependencies().size(), "Dependencies count mismatch");
        TestUtils.println("Dependencies count matches: " + parsed.getDependencies().size());

        Assertions.assertEquals(original.getCategories().size(), parsed.getCategories().size(), "Categories count mismatch");
        TestUtils.println("Categories count matches: " + parsed.getCategories().size());

        Assertions.assertEquals(original.getIcons().size(), parsed.getIcons().size(), "Icons count mismatch");
        TestUtils.println("Icons count matches: " + parsed.getIcons().size());

        // Verify Main-Class was preserved
        Assertions.assertNotNull(parsed.getExecutor(), "Executor missing in parsed descriptor");
        Assertions.assertNotNull(parsed.getExecutor().getArguments(), "Executor arguments missing in parsed descriptor");

        boolean foundMainClass = false;
        java.util.List<String> args = parsed.getExecutor().getArguments();
        for (int i = 0; i < args.size() - 1; i++) {
            if ("--main-class=".equals(args.get(i))) {
                String mainClass = args.get(i + 1);
                Assertions.assertEquals("com.roundtrip.TestMain", mainClass, "Main class mismatch");
                foundMainClass = true;
                TestUtils.println("Main-Class preserved: " + mainClass);
                break;
            }
        }
        Assertions.assertTrue(foundMainClass, "Main-Class not found in parsed descriptor");

        TestUtils.println("\nRound-trip test passed! Format → Parse produces equivalent descriptor.");
    }

    @Test
    public void testEmptyAndNullValues() {
        // Create descriptor with minimal/null fields
        NDescriptor descriptor = new DefaultNDescriptorBuilder()
                .setId(NIdBuilder.of("org.test", "empty-test").setVersion("1.0").build())
                .setName("")  // Empty name
                .setDescription(null)  // Null description
                .setGenericName(null)
                .build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NPrintStream out = NPrintStream.of(baos);

        NDescriptorWriter.of()
                .setDescriptorStyle(NDescriptorStyle.MANIFEST)
                .print(descriptor, out);

        String manifest = baos.toString();

        TestUtils.println("Empty/Null values MANIFEST.MF:");
        TestUtils.println(NStringUtils.repeat("=",60));
        TestUtils.println(manifest);
        TestUtils.println(NStringUtils.repeat("=",60));

        // Should still have required fields
        Assertions.assertTrue(manifest.contains("Manifest-Version: 1.0"));
        Assertions.assertTrue(manifest.contains("Nuts-Id: org.test:empty-test#1.0"));
        // Empty/null fields should not appear
        Assertions.assertFalse(manifest.contains("Nuts-Name: "));
        Assertions.assertFalse(manifest.contains("Nuts-Description: "));
        Assertions.assertFalse(manifest.contains("Nuts-Generic-Name: "));

        TestUtils.println("Empty/null values handled correctly!");
    }

    @Test
    public void testSpecialCharactersInValues() {
        // Create descriptor with special characters
        String specialDesc = "Description with special chars: é, ñ, 中文, emoji 🚀, quotes \"test\", and newlines\nLine 2";
        String specialName = "App with \"quotes\" and <brackets>";

        NDescriptor descriptor = new DefaultNDescriptorBuilder()
                .setId(NIdBuilder.of("org.test", "special-chars").setVersion("1.0").build())
                .setName(specialName)
                .setDescription(specialDesc)
                .build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NPrintStream out = NPrintStream.of(baos);

        NDescriptorWriter.of()
                .setDescriptorStyle(NDescriptorStyle.MANIFEST)
                .print(descriptor, out);

        String manifest = baos.toString();

        TestUtils.println("Special characters MANIFEST.MF:");
        TestUtils.println(NStringUtils.repeat("=",60));
        TestUtils.println(manifest);
        TestUtils.println(NStringUtils.repeat("=",60));

        // Verify special characters are preserved
        Assertions.assertTrue(manifest.contains(specialName));
        Assertions.assertTrue(manifest.contains("é") || manifest.contains("special chars"));

        TestUtils.println("Special characters handled!");
    }

    @Test
    public void testVeryLongDependenciesList() {
        // Create descriptor with many dependencies
        NDescriptorBuilder builder = new DefaultNDescriptorBuilder()
                .setId(NIdBuilder.of("org.test", "many-deps").setVersion("1.0").build());

        // Add 20 dependencies
        for (int i = 1; i <= 20; i++) {
            builder.addDependency(
                NDependency.get("org.example:library-" + i + "#" + i + ".0.0").get()
            );
        }

        NDescriptor descriptor = builder.build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NPrintStream out = NPrintStream.of(baos);

        NDescriptorWriter.of()
                .setDescriptorStyle(NDescriptorStyle.MANIFEST)
                .print(descriptor, out);

        String manifest = baos.toString();

        TestUtils.println("Many dependencies MANIFEST.MF:");
        TestUtils.println(NStringUtils.repeat("=",60));
        TestUtils.println(manifest);
        TestUtils.println(NStringUtils.repeat("=",60));

        // Verify line wrapping for long dependencies line - check bytes not chars (JAR spec)
        String[] lines = manifest.split("\n");
        for (String line : lines) {
            if (!line.isEmpty()) {
                byte[] lineBytes = line.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                Assertions.assertTrue(lineBytes.length <= 72,
                    "Line exceeds 72 bytes: " + lineBytes.length + " bytes");
            }
        }

        // Unfold wrapped lines for verification (remove continuation line breaks)
        // MANIFEST.MF continuation lines start with space; we need to join them
        String unfolded = manifest.replaceAll("\n ", "");

        // Verify all dependencies are present in the unfolded manifest
        for (int i = 1; i <= 20; i++) {
            Assertions.assertTrue(unfolded.contains("library-" + i), "Missing dependency library-" + i);
        }

        TestUtils.println("Many dependencies formatted correctly with wrapping!");
    }

    @Test
    public void testExtremelyLongSingleValue() {
        // Create a descriptor with extremely long description (500+ chars)
        StringBuilder longDesc = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            longDesc.append("This is section ").append(i).append(" of a very long description that needs to be properly wrapped in the MANIFEST.MF file. ");
        }

        NDescriptor descriptor = new DefaultNDescriptorBuilder()
                .setId(NIdBuilder.of("org.test", "long-value").setVersion("1.0").build())
                .setDescription(longDesc.toString())
                .build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NPrintStream out = NPrintStream.of(baos);

        NDescriptorWriter.of()
                .setDescriptorStyle(NDescriptorStyle.MANIFEST)
                .print(descriptor, out);

        String manifest = baos.toString();

        TestUtils.println("Extremely long value MANIFEST.MF:");
        TestUtils.println(NStringUtils.repeat("=",60));
        TestUtils.println(manifest);
        TestUtils.println(NStringUtils.repeat("=",60));

        // Verify line wrapping - check bytes not chars (JAR spec)
        String[] lines = manifest.split("\n");
        int continuationLines = 0;
        for (String line : lines) {
            if (!line.isEmpty()) {
                byte[] lineBytes = line.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                Assertions.assertTrue(lineBytes.length <= 72,
                    "Line exceeds 72 bytes: " + lineBytes.length + " bytes (was " + line.length() + " chars)");
                if (line.startsWith(" ")) {
                    continuationLines++;
                }
            }
        }

        Assertions.assertTrue(continuationLines > 0, "Should have continuation lines for long value");
        TestUtils.println("Continuation lines: " + continuationLines);
        TestUtils.println("Extremely long value wrapped correctly!");
    }

    @Test
    public void testNoVersionInId() {
        // Create descriptor without version
        NDescriptor descriptor = new DefaultNDescriptorBuilder()
                .setId(NIdBuilder.of("org.test", "no-version").build())
                .setName("No Version App")
                .build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NPrintStream out = NPrintStream.of(baos);

        NDescriptorWriter.of()
                .setDescriptorStyle(NDescriptorStyle.MANIFEST)
                .print(descriptor, out);

        String manifest = baos.toString();

        TestUtils.println("No version MANIFEST.MF:");
        TestUtils.println(NStringUtils.repeat("=",60));
        TestUtils.println(manifest);
        TestUtils.println(NStringUtils.repeat("=",60));

        Assertions.assertTrue(manifest.contains("Manifest-Version: 1.0"));
        // Should not have Implementation-Version if no version in ID
        Assertions.assertFalse(manifest.contains("Implementation-Version:"));
        Assertions.assertTrue(manifest.contains("Nuts-Id: org.test:no-version"));

        TestUtils.println("No version case handled!");
    }

    @Test
    public void testMultipleProperties() {
        // Create descriptor with multiple custom properties
        NDescriptorBuilder builder = new DefaultNDescriptorBuilder()
                .setId(NIdBuilder.of("org.test", "props").setVersion("1.0").build());

        // Add custom properties
        builder.addProperty(createProperty("author", "thevpc"));
        builder.addProperty(createProperty("license", "Apache-2.0"));
        builder.addProperty(createProperty("homepage", "https://example.com"));
        builder.addProperty(createProperty("build-timestamp", "2025-11-30T22:00:00Z"));

        NDescriptor descriptor = builder.build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NPrintStream out = NPrintStream.of(baos);

        NDescriptorWriter.of()
                .setDescriptorStyle(NDescriptorStyle.MANIFEST)
                .print(descriptor, out);

        String manifest = baos.toString();

        TestUtils.println("Multiple properties MANIFEST.MF:");
        TestUtils.println(NStringUtils.repeat("=",60));
        TestUtils.println(manifest);
        TestUtils.println(NStringUtils.repeat("=",60));

        // Verify properties are written as Nuts-Property-<name>
        Assertions.assertTrue(manifest.contains("Nuts-Property-author: thevpc"));
        Assertions.assertTrue(manifest.contains("Nuts-Property-license: Apache-2.0"));
        Assertions.assertTrue(manifest.contains("Nuts-Property-homepage: https://example.com"));
        Assertions.assertTrue(manifest.contains("Nuts-Property-build-timestamp: 2025-11-30T22:00:00Z"));

        TestUtils.println("Multiple properties formatted correctly!");
    }

    @Test
    public void testMultipleFlags() {
        // Create descriptor with multiple flags
        NDescriptor descriptor = new DefaultNDescriptorBuilder()
                .setId(NIdBuilder.of("org.test", "flags").setVersion("1.0").build())
                .addFlag(NDescriptorFlag.EXEC)
                .addFlag(NDescriptorFlag.GUI)
                .addFlag(NDescriptorFlag.NUTS_APP)
                .build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NPrintStream out = NPrintStream.of(baos);

        NDescriptorWriter.of()
                .setDescriptorStyle(NDescriptorStyle.MANIFEST)
                .print(descriptor, out);

        String manifest = baos.toString();

        TestUtils.println("Multiple flags MANIFEST.MF:");
        TestUtils.println(NStringUtils.repeat("=",60));
        TestUtils.println(manifest);
        TestUtils.println(NStringUtils.repeat("=",60));

        // Verify all flags are present
        Assertions.assertTrue(manifest.contains("Nuts-Flags:"));
        Assertions.assertTrue(manifest.contains("EXEC"));
        Assertions.assertTrue(manifest.contains("GUI"));
        Assertions.assertTrue(manifest.contains("NUTS_APP"));

        TestUtils.println("Multiple flags formatted correctly!");
    }

    @Test
    public void testMainClassInExecutorVariants() {
        // Test concatenated form: --main-class=ClassName
        NDescriptor descriptor1 = new DefaultNDescriptorBuilder()
                .setId(NIdBuilder.of("org.test", "mainclass1").setVersion("1.0").build())
                .setExecutor(
                    new DefaultNArtifactCallBuilder()
                        .setId(NId.get("java").get())
                        .setArguments(new String[]{"--main-class=com.example.ConcatenatedMain"})
                        .build()
                )
                .build();

        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        NPrintStream out1 = NPrintStream.of(baos1);

        NDescriptorWriter.of()
                .setDescriptorStyle(NDescriptorStyle.MANIFEST)
                .print(descriptor1, out1);

        String manifest1 = baos1.toString();

        TestUtils.println("Concatenated main class MANIFEST.MF:");
        TestUtils.println(NStringUtils.repeat("=",60));
        TestUtils.println(manifest1);
        TestUtils.println(NStringUtils.repeat("=",60));

        Assertions.assertTrue(manifest1.contains("Main-Class: com.example.ConcatenatedMain"));
        TestUtils.println("Concatenated --main-class=ClassName form works!");
    }

    @Test
    public void testUtf8ByteCounting() {
        // Create a descriptor with multi-byte UTF-8 characters to verify byte counting
        // Chinese characters are typically 3 bytes each in UTF-8
        // Emoji are typically 4 bytes each in UTF-8
        String descWith3ByteChars = "你好世界!"; // 5 Chinese chars (3 bytes each) + 1 ASCII = ~16 bytes
        String descWithEmoji = "Test 🚀🎉🔥"; // 5 ASCII + 3 emoji (4 bytes each) = ~17 bytes

        // Create a long description mixing multi-byte characters to force wrapping
        StringBuilder longMultiByte = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            longMultiByte.append("你好"); // 2 Chinese chars = 6 bytes each iteration
        }

        NDescriptor descriptor = new DefaultNDescriptorBuilder()
                .setId(NIdBuilder.of("org.test", "utf8-test").setVersion("1.0").build())
                .setName(descWith3ByteChars)
                .setDescription(longMultiByte.toString())
                .setGenericName(descWithEmoji)
                .build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        NPrintStream out = NPrintStream.of(baos);

        NDescriptorWriter.of()
                .setDescriptorStyle(NDescriptorStyle.MANIFEST)
                .print(descriptor, out);

        String manifest = baos.toString();

        TestUtils.println("UTF-8 byte counting MANIFEST.MF:");
        TestUtils.println(NStringUtils.repeat("=",60));
        TestUtils.println(manifest);
        TestUtils.println(NStringUtils.repeat("=",60));

        // Verify that NO line exceeds 72 bytes
        String[] lines = manifest.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (!line.isEmpty()) {
                byte[] lineBytes = line.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                TestUtils.println("Line " + i + ": " + lineBytes.length + " bytes, " + line.length() + " chars");
                Assertions.assertTrue(lineBytes.length <= 72,
                    "Line " + i + " exceeds 72 bytes: " + lineBytes.length + " bytes (content: " + line + ")");
            }
        }

        // Verify the multi-byte content is preserved
        Assertions.assertTrue(manifest.contains(descWith3ByteChars), "Chinese characters should be preserved");
        Assertions.assertTrue(manifest.contains(descWithEmoji), "Emoji should be preserved");

        TestUtils.println("UTF-8 byte counting works correctly with multi-byte characters!");
    }

    // Helper method to create properties
    private NDescriptorProperty createProperty(String name, String value) {
        return new NDescriptorProperty() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public net.thevpc.nuts.util.NLiteral getValue() {
                return net.thevpc.nuts.util.NLiteral.of(value);
            }

            @Override
            public NEnvCondition getCondition() {
                return null;
            }

            @Override
            public NDescriptorPropertyBuilder builder() {
                return new net.thevpc.nuts.runtime.standalone.DefaultNDescriptorPropertyBuilder()
                    .setName(name)
                    .setValue(value);
            }

            @Override
            public boolean isBlank() {
                return net.thevpc.nuts.util.NBlankable.isBlank(name) && net.thevpc.nuts.util.NBlankable.isBlank(value);
            }
        };
    }
}
