package net.thevpc.nuts.core.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * ARCH-13: licensing is governed by the root LICENSE file. The old per-module
 * licenseheader.txt templates were stale, unenforced, and must not come back.
 */
public class LicenseTemplateTest {

    @Test
    public void staleLicenseHeaderTemplatesAreAbsent() throws Exception {
        Path root = Paths.get("../..").toAbsolutePath().normalize();
        List<String> found = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(p -> p.getFileName().toString().equals("licenseheader.txt"))
                    .filter(p -> !p.toString().contains("/target/"))
                    .forEach(p -> found.add(root.relativize(p).toString()));
        }
        Assertions.assertTrue(found.isEmpty(), "stale license header templates: " + found);
        Assertions.assertTrue(Files.exists(root.resolve("LICENSE")));
    }
}
