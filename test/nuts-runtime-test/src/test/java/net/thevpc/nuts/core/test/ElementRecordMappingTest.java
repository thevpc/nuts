package net.thevpc.nuts.core.test;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NProperty;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ElementRecordMappingTest {
    @TempDir
    Path tempDir;

    @BeforeAll
    static void setUpWorkspace() {
        Nuts.require();
    }

    @Test
    void defaultMapperSupportsRecordsWithAndWithoutNProperty() throws Exception {
        Assumptions.assumeTrue(hasRecordSupport(),
                "Record mapping requires a JDK with record support");
        compileFixtures();
        String fixtures = "net.thevpc.nuts.core.test.RecordFixtures";
        try (URLClassLoader loader = new URLClassLoader(
                new URL[]{tempDir.resolve("classes").toUri().toURL()},
                Thread.currentThread().getContextClassLoader())) {
            Class<?> plainType = Class.forName(fixtures + "$PlainRecord", true, loader);
            Object plain = plainType.getConstructor(long.class, String.class)
                    .newInstance(42L, "Which one?");
            NElement plainElement = NElement.defaultToElement(plain);

            assertEquals(42L, plainElement.asObject().get().getLongValue("taskId").get());
            assertEquals("Which one?", plainElement.asObject().get().getStringValue("prompt").get());
            Object plainCopy = NElement.defaultFromElement(plainElement, (Class) plainType).get();
            assertEquals(42L, plainType.getMethod("taskId").invoke(plainCopy));
            assertEquals("Which one?", plainType.getMethod("prompt").invoke(plainCopy));

            Class<?> namedType = Class.forName(fixtures + "$NamedRecord", true, loader);
            Object named = namedType.getConstructor(long.class, String.class)
                    .newInstance(7L, "Display name");
            NElement namedElement = NElement.of(named);

            assertEquals(7L, namedElement.asObject().get().getLongValue("user_id").get());
            assertEquals("Display name", namedElement.asObject().get().getStringValue("display_name").get());
            Object namedCopy = NElement.defaultFromElement(namedElement, (Class) namedType).get();
            assertEquals(7L, namedType.getMethod("taskId").invoke(namedCopy));
            assertEquals("Display name", namedType.getMethod("name").invoke(namedCopy));

            Class<?> nestedType = Class.forName(fixtures + "$NestedRecord", true, loader);
            Constructor<?> nestedConstructor = nestedType.getConstructor(List.class, plainType);
            Object nested = nestedConstructor.newInstance(java.util.Arrays.asList("a", "b"), plain);
            NElement nestedElement = NElement.of(nested);
            Object nestedCopy = NElement.convertAny(nestedElement, (Class) nestedType);

            assertEquals(java.util.Arrays.asList("a", "b"), nestedType.getMethod("labels").invoke(nestedCopy));
            Object copiedPlain = nestedType.getMethod("item").invoke(nestedCopy);
            assertEquals(42L, plainType.getMethod("taskId").invoke(copiedPlain));
            assertEquals("Which one?", plainType.getMethod("prompt").invoke(copiedPlain));
        }
    }

    @Test
    void propertyAnnotationRenamesRecordComponent() throws Exception {
        Assumptions.assumeTrue(hasRecordSupport(),
                "Record mapping requires a JDK with record support");
        compileFixtures();
        String fixtures = "net.thevpc.nuts.core.test.RecordFixtures";
        try (URLClassLoader loader = new URLClassLoader(
                new URL[]{tempDir.resolve("classes").toUri().toURL()},
                Thread.currentThread().getContextClassLoader())) {
            Class<?> type = Class.forName(fixtures + "$PropRecord", true, loader);
            Object value = type.getConstructor(long.class, String.class).newInstance(9L, "n");
            NElement element = NElement.defaultToElement(value);
            assertEquals(9L, element.asObject().get().getLongValue("user_id").get());
            assertEquals("n", element.asObject().get().getStringValue("name").get());
            Object copy = NElement.defaultFromElement(element, (Class) type).get();
            assertEquals(9L, type.getMethod("taskId").invoke(copy));
            assertEquals("n", type.getMethod("name").invoke(copy));
        }
    }

    @Test
    void componentNamesSurviveAnExplicitCanonicalConstructor() throws Exception {
        Assumptions.assumeTrue(hasRecordSupport(),
                "Record mapping requires a JDK with record support");
        compileFixtures();
        String fixtures = "net.thevpc.nuts.core.test.RecordFixtures";
        try (URLClassLoader loader = new URLClassLoader(
                new URL[]{tempDir.resolve("classes").toUri().toURL()},
                Thread.currentThread().getContextClassLoader())) {
            assertRenamingRoundTrip(Class.forName(fixtures + "$ExplicitPlainCtorRecord", true, loader),
                    "user_id", "name");
            assertRenamingRoundTrip(Class.forName(fixtures + "$ExplicitCreatorRecord", true, loader),
                    "user_id", "display_name");
        }
    }

    private void assertRenamingRoundTrip(Class<?> type, String idName, String secondName) throws Exception {
        Object value = type.getConstructor(long.class, String.class).newInstance(11L, "n");
        NElement element = NElement.defaultToElement(value);
        assertEquals(11L, element.asObject().get().getLongValue(idName).get());
        assertEquals("n", element.asObject().get().getStringValue(secondName).get());
        Object copy = NElement.defaultFromElement(element, (Class) type).get();
        assertEquals(11L, type.getMethod("taskId").invoke(copy));
        assertEquals("n", type.getMethod("name").invoke(copy));
    }

    private void compileFixtures() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "record mapping test requires a JDK compiler");
        Path sourceRoot = tempDir.resolve("src/net/thevpc/nuts/core/test");
        Path outputRoot = tempDir.resolve("classes");
        Files.createDirectories(sourceRoot);
        Files.createDirectories(outputRoot);
        Path source = sourceRoot.resolve("RecordFixtures.java");
        Files.write(source, (
                "package net.thevpc.nuts.core.test;\n" +
                "import net.thevpc.nuts.elem.NCreator;\n" +
                "import net.thevpc.nuts.elem.NProperty;\n" +
                "import net.thevpc.nuts.elem.NElement;\n" +
                "import net.thevpc.nuts.elem.NToElement;\n" +
                "public class RecordFixtures {\n" +
                "  public record PlainRecord(long taskId, String prompt) {}\n" +
                "  public record NamedRecord(@NProperty( \"user_id\") long taskId, " +
                "@NProperty( \"display_name\") String name) implements NToElement {\n" +
                "    public NElement toElement() { return NElement.defaultToElement(this).asObject().get()" +
                ".builder().name(\"NamedRecord\").build(); }\n" +
                "  }\n" +
                "  public record PropRecord(@NProperty(\"user_id\") long taskId, String name) {}\n" +
                "  public record ExplicitPlainCtorRecord(@NProperty(\"user_id\") long taskId, String name) {\n" +
                "    public ExplicitPlainCtorRecord(long taskId, String name) {\n" +
                "      this.taskId = taskId; this.name = name;\n" +
                "    }\n" +
                "  }\n" +
                "  public record ExplicitCreatorRecord(@NProperty(\"user_id\") long taskId, " +
                "@NProperty(\"display_name\") String name) {\n" +
                "    @NCreator public ExplicitCreatorRecord(long taskId, String name) {\n" +
                "      this.taskId = taskId; this.name = name;\n" +
                "    }\n" +
                "  }\n" +
                "  public record NestedRecord(java.util.List<String> labels, PlainRecord item) {}\n" +
                "}\n"
        ).getBytes(StandardCharsets.UTF_8));
        URL apiLocation = NProperty.class.getProtectionDomain().getCodeSource().getLocation();
        int exitCode = compiler.run(null, null, null,
                "-classpath", new File(apiLocation.toURI()).getAbsolutePath(),
                "-d", outputRoot.toString(),
                source.toString());
        assertEquals(0, exitCode, "record test fixtures should compile");
    }

    private static boolean hasRecordSupport() {
        try {
            Class.class.getMethod("isRecord");
            return true;
        } catch (NoSuchMethodException ex) {
            return false;
        }
    }
}
