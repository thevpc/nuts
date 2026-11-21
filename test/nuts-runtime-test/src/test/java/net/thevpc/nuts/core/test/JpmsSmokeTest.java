package net.thevpc.nuts.core.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

/**
 * ARCH-01/ARCH-02: the runtime must be a valid multi-release jar whose versioned
 * module descriptor can actually be resolved and used on the module path.
 */
public class JpmsSmokeTest {

    private static final String API_GROUP = "nuts";
    private static final String API_VERSION = "1.2.0";
    private static final String BOOT_GROUP = "nuts-boot";
    private static final String BOOT_VERSION = "1.2.0";
    private static final String RUNTIME_GROUP = "nuts-runtime";
    private static final String RUNTIME_VERSION = "1.2.0.0";

    @TempDir
    Path tempDir;

    @Test
    public void runtimeJarHasVersionedModuleDescriptor() throws Exception {
        Path runtimeJar = requireJar(RUNTIME_GROUP, RUNTIME_VERSION,
                "nuts-runtime-" + RUNTIME_VERSION + ".jar",
                "nuts-runtime/target/nuts-runtime-" + RUNTIME_VERSION + ".jar");

        try (JarFile jar = new JarFile(runtimeJar.toFile())) {
            Assertions.assertNull(jar.getJarEntry("module-info.class"),
                    "module-info.class must not be at the jar root (ARCH-02)");
            Assertions.assertNotNull(jar.getJarEntry("META-INF/versions/9/module-info.class"),
                    "module descriptor must live under META-INF/versions/9 (ARCH-02)");
        }

        String described = runProcess(Paths.get(System.getProperty("java.home"), "bin", "jar").toString(),
                "--describe-module", "--file", runtimeJar.toString(), "--release", "9");
        Assertions.assertTrue(described.contains("net.thevpc.nuts.runtime@"), described);
        Assertions.assertTrue(described.contains("uses net.thevpc.nuts.spi.NComponent"), described);
        Assertions.assertTrue(described.contains("uses net.thevpc.nuts.boot.NBootWorkspaceFactory"), described);
        Assertions.assertTrue(described.contains("provides net.thevpc.nuts.spi.NComponent with"), described);
        Assertions.assertTrue(described.contains("provides net.thevpc.nuts.boot.NBootWorkspaceFactory with"), described);
        Assertions.assertTrue(described.contains("exports net.thevpc.nuts.runtime.standalone.boot"), described);

        int componentProviders = countProviders(described, "net.thevpc.nuts.spi.NComponent");
        Assertions.assertTrue(componentProviders >= 100,
                "too few NComponent providers: " + componentProviders + "\n" + described);
    }

    @Test
    public void providersAreDiscoverableOnModulePath() throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Assumptions.assumeTrue(compiler != null, "a JDK (not a JRE) is required for the JPMS smoke test");

        Path apiJar = requireJar(API_GROUP, API_VERSION,
                "nuts-" + API_VERSION + ".jar", "nuts-api/target/nuts-" + API_VERSION + ".jar");
        Path bootJar = requireJar(BOOT_GROUP, BOOT_VERSION,
                "nuts-boot-" + BOOT_VERSION + ".jar", "nuts-boot/target/nuts-boot-" + BOOT_VERSION + ".jar");
        Path runtimeJar = requireJar(RUNTIME_GROUP, RUNTIME_VERSION,
                "nuts-runtime-" + RUNTIME_VERSION + ".jar",
                "nuts-runtime/target/nuts-runtime-" + RUNTIME_VERSION + ".jar");

        Path src = Files.createDirectories(tempDir.resolve("src"));
        Path smokeDir = Files.createDirectories(src.resolve("smoke"));
        Path out = Files.createDirectories(tempDir.resolve("out"));
        Files.write(src.resolve("module-info.java"),
                ("module smoke {\n" +
                        "    requires net.thevpc.nuts;\n" +
                        "    requires net.thevpc.nuts.runtime;\n" +
                        "    uses net.thevpc.nuts.spi.NComponent;\n" +
                        "}\n").getBytes(StandardCharsets.UTF_8));
        Files.write(smokeDir.resolve("Smoke.java"),
                ("package smoke;\n" +
                        "import net.thevpc.nuts.spi.NComponent;\n" +
                        "import java.util.ServiceLoader;\n" +
                        "public class Smoke {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        long n = ServiceLoader.load(NComponent.class).stream().count();\n" +
                        "        System.out.println(\"providers=\" + n);\n" +
                        "        if (n == 0) {\n" +
                        "            System.exit(2);\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n").getBytes(StandardCharsets.UTF_8));

        String modulePath = apiJar + File.pathSeparator + bootJar + File.pathSeparator + runtimeJar;
        int compiled = compiler.run(null, null, null,
                "--module-path", modulePath,
                "-d", out.toString(),
                src.resolve("module-info.java").toString(),
                smokeDir.resolve("Smoke.java").toString());
        Assertions.assertEquals(0, compiled, "smoke module must compile against the named modules");

        String output = runProcess(Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
                "--module-path", modulePath + File.pathSeparator + out,
                "-m", "smoke/smoke.Smoke");
        Assertions.assertTrue(output.contains("providers="), output);
        int providers = Integer.parseInt(output.trim().replace("providers=", ""));
        Assertions.assertTrue(providers >= 100, "expected JPMS providers, got: " + output);
    }

    /** Java 8-compatible process runner used for the JDK 9+ tooling invoked by this test. */
    private static String runProcess(String... command) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            Assertions.assertTrue(process.waitFor(120, TimeUnit.SECONDS), "process timed out: " + command[0]);
            String output = sb.toString();
            Assertions.assertEquals(0, process.exitValue(), output);
            return output;
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private static int countProviders(String described, String service) {
        String marker = "provides " + service + " with";
        for (String line : described.split("\\r?\\n")) {
            if (line.startsWith(marker)) {
                return line.split("net\\.thevpc\\.nuts\\.runtime\\.", -1).length - 1;
            }
        }
        return 0;
    }

    private static Path requireJar(String group, String version, String jarName, String targetRelative) {
        List<Path> candidates = new ArrayList<>();
        Path target = Paths.get("../../core/" + targetRelative).toAbsolutePath().normalize();
        if (Files.isRegularFile(target)) {
            candidates.add(target);
        }
        Path localRepository = Paths.get(System.getProperty("user.home"), ".m2", "repository",
                "net", "thevpc", "nuts", group, version, jarName);
        if (Files.isRegularFile(localRepository)) {
            candidates.add(localRepository);
        }
        Path best = candidates.stream()
                .max(Comparator.comparingLong(p -> p.toFile().lastModified()))
                .orElse(null);
        Assumptions.assumeTrue(best != null, "missing jar for " + group + ":" + version + "; run mvn install first");
        return best;
    }
}
