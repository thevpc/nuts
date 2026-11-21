package net.thevpc.nuts.core.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * ARCH-09: package cycles in the existing runtime are grandfathered, but no new
 * bidirectional dependency may be introduced. The baseline lives in
 * {@code arch09-package-cycles.txt}.
 */
public class PackageCycleGuardTest {

    private static final Pattern IMPORT = Pattern.compile(
            "^\\s*import\\s+(?:static\\s+)?([\\w.]+(?:\\.\\*)?)\\s*;");

    @Test
    public void noNewBidirectionalPackageDependencies() throws Exception {
        Path root = Paths.get("../../core/nuts-runtime/src/main/java").toAbsolutePath().normalize();
        Map<String, Set<String>> packages = new HashMap<>();
        Set<String> known = new HashSet<>();
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path file : (Iterable<Path>) paths.filter(p -> p.toString().endsWith(".java"))::iterator) {
                String pkg = packageOf(root.relativize(file));
                if (pkg != null) {
                    known.add(pkg);
                    packages.put(pkg, new HashSet<String>());
                }
            }
        }
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path file : (Iterable<Path>) paths.filter(p -> p.toString().endsWith(".java"))::iterator) {
                String pkg = packageOf(root.relativize(file));
                if (pkg == null) {
                    continue;
                }
                for (String raw : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                    Matcher m = IMPORT.matcher(raw);
                    if (!m.matches()) {
                        continue;
                    }
                    String imported = m.group(1);
                    if (imported.endsWith(".*")) {
                        imported = imported.substring(0, imported.length() - 2);
                    }
                    String candidate = longestKnownPackage(imported, known);
                    if (candidate != null && !candidate.equals(pkg)) {
                        packages.get(pkg).add(candidate);
                    }
                }
            }
        }

        Set<String> current = new TreeSet<>();
        for (Map.Entry<String, Set<String>> entry : packages.entrySet()) {
            String from = entry.getKey();
            for (String to : entry.getValue()) {
                if (to.compareTo(from) > 0 && packages.getOrDefault(to, new HashSet<String>()).contains(from)) {
                    current.add(from + " <-> " + to);
                }
            }
        }
        Assertions.assertFalse(current.isEmpty(), "the cycle detector must find the grandfathered cycles");

        Set<String> baseline = new HashSet<>();
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("arch09-package-cycles.txt")) {
            Assertions.assertNotNull(in, "missing ARCH-09 cycle baseline resource");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        baseline.add(line);
                    }
                }
            }
        }

        Set<String> added = new TreeSet<>(current);
        added.removeAll(baseline);
        Assertions.assertTrue(added.isEmpty(), "new bidirectional package dependencies: " + added);
    }

    private static String packageOf(Path relative) {
        Path parent = relative.getParent();
        if (parent == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Path part : parent) {
            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(part.toString());
        }
        return sb.toString();
    }

    private static String longestKnownPackage(String imported, Set<String> known) {
        String candidate = imported;
        while (!candidate.isEmpty()) {
            if (known.contains(candidate)) {
                return candidate;
            }
            int dot = candidate.lastIndexOf('.');
            if (dot < 0) {
                return null;
            }
            candidate = candidate.substring(0, dot);
        }
        return null;
    }
}
