package net.thevpc.nuts.core.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * ARCH-11: fully commented-out Java sources are dead weight and must not be
 * reintroduced into the runtime module.
 */
public class NoDeadSourceFilesTest {

    @Test
    public void runtimeHasNoFullyCommentedOutSources() throws Exception {
        Path root = Paths.get("../../core/nuts-runtime/src/main/java").toAbsolutePath().normalize();
        List<String> dead = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path file : (Iterable<Path>) paths.filter(p -> p.toString().endsWith(".java")
                    && !p.getFileName().toString().equals("package-info.java"))::iterator) {
                String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                boolean hasCode = false;
                for (String raw : text.split("\\r?\\n")) {
                    String line = raw.trim();
                    if (line.isEmpty() || line.startsWith("//") || line.startsWith("/*")
                            || line.startsWith("*") || line.startsWith("*/")
                            || line.startsWith("package ") || line.startsWith("import ")) {
                        continue;
                    }
                    hasCode = true;
                    break;
                }
                if (!hasCode) {
                    dead.add(root.relativize(file).toString());
                }
            }
        }
        Assertions.assertTrue(dead.isEmpty(), "fully commented-out sources found: " + dead);
    }
}
