package net.thevpc.nuts.core.test;

import net.thevpc.nuts.boot.NBootWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ARCH-04: the API/implementation versions in the POMs and in the Java constants
 * must not drift apart.
 */
public class BuildVersionConsistencyTest {

    private static final Pattern PROPERTY = Pattern.compile(
            "<nuts\\.(api|impl)\\.version>([^<]+)</nuts\\.(api|impl)\\.version>");
    private static final Pattern NUTS_DEPENDENCY = Pattern.compile(
            "<dependency>[^<]*(?:(?!</dependency>).)*?<groupId>net\\.thevpc\\.nuts</groupId>.*?</dependency>",
            Pattern.DOTALL);
    private static final Pattern LITERAL_VERSION = Pattern.compile(
            "<version>(1\\.2\\.0(?:\\.0)?)</version>");

    @Test
    public void javaConstantsMatchPomProperties() throws Exception {
        Path parentPom = Paths.get("../../parent/pom.xml").toAbsolutePath().normalize();
        String pom = new String(Files.readAllBytes(parentPom), StandardCharsets.UTF_8);
        String api = property(pom, "api");
        String impl = property(pom, "impl");
        Assertions.assertNotNull(api);
        Assertions.assertNotNull(impl);

        Assertions.assertEquals(api, NBootWorkspace.NUTS_BOOT_VERSION,
                "NBootWorkspace.NUTS_BOOT_VERSION must match nuts.api.version");
        Assertions.assertEquals(impl, DefaultNWorkspace.RUNTIME_VERSION,
                "DefaultNWorkspace.RUNTIME_VERSION must match nuts.impl.version");
    }

    @Test
    public void pomsUseVersionPropertiesForNutsDependencies() throws Exception {
        Path root = Paths.get("../..").toAbsolutePath().normalize();
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path pom : paths.filter(p -> p.getFileName().toString().equals("pom.xml"))
                    .filter(p -> !p.toString().contains("/target/"))
                    .collect(Collectors.toList())) {
                String text = new String(Files.readAllBytes(pom), StandardCharsets.UTF_8);
                Matcher dependencies = NUTS_DEPENDENCY.matcher(text);
                while (dependencies.find()) {
                    Matcher version = LITERAL_VERSION.matcher(dependencies.group());
                    if (version.find()) {
                        offenders.add(root.relativize(pom) + " -> " + version.group(1));
                    }
                }
            }
        }
        Assertions.assertTrue(offenders.isEmpty(),
                "hardcoded nuts dependency versions (use ${nuts.api.version}/${nuts.impl.version}): " + offenders);
    }

    private static String property(String pom, String kind) {
        Matcher m = PROPERTY.matcher(pom);
        while (m.find()) {
            if (kind.equals(m.group(1)) && kind.equals(m.group(3))) {
                return m.group(2);
            }
        }
        return null;
    }
}
