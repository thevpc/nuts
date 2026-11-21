package net.thevpc.nuts.core.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ARCH-17: the generated root CONTRIBUTING.md and its source template must
 * describe the same compatibility matrix and test lanes.
 */
public class DocumentationConsistencyTest {

    @Test
    public void contributingSourceAndGeneratedFileAgreeOnBuildRules() throws Exception {
        Path root = Paths.get("../..").toAbsolutePath().normalize();
        String source = read(root.resolve("documentation/repo/src/main/CONTRIBUTING.md"));
        String generated = read(root.resolve("CONTRIBUTING.md"));
        String[] expected = {
                "Java 25",
                "8, 17, 21 and 25",
                "mvn -B -ntp install -Dexcluded.groups=local-only",
                "local-only",
                "test/nuts-runtime-test",
                "LICENSE"
        };
        for (String token : expected) {
            Assertions.assertTrue(source.contains(token), "source CONTRIBUTING.md missing: " + token);
            Assertions.assertTrue(generated.contains(token), "root CONTRIBUTING.md missing: " + token);
        }
        String workflow = read(root.resolve(".github/workflows/maven.yml"));
        Assertions.assertTrue(workflow.contains("java: [ '8', '17', '21', '25' ]"),
                "CI matrix must match the documented compatibility range");
        Assertions.assertFalse(source.contains("Java 24+"));
        Assertions.assertFalse(generated.contains("Java 24+"));
    }

    private static String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
