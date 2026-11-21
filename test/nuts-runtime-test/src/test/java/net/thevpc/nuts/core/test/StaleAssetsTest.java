package net.thevpc.nuts.core.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * ARCH-05: the hand-written TSON lexer is the only parser; stale JavaCC assets
 * and machine-specific comparison scripts must not be reintroduced.
 */
public class StaleAssetsTest {

    @Test
    public void staleJavaccAssetsAreAbsent() throws Exception {
        Path runtime = Paths.get("../../core/nuts-runtime").toAbsolutePath().normalize();
        Assertions.assertFalse(Files.exists(runtime.resolve("run-javacc")));
        Assertions.assertFalse(Files.exists(runtime.resolve("compare-with-tson.txt")));

        List<Path> grammars = new ArrayList<>();
        try (java.util.stream.Stream<Path> paths = Files.walk(runtime)) {
            paths.filter(p -> p.toString().endsWith(".jj")).forEach(grammars::add);
        }
        Assertions.assertTrue(grammars.isEmpty(), "stale JavaCC grammars: " + grammars);
    }
}
