package net.thevpc.nuts.core.test;

import net.thevpc.nuts.artifact.NClasspathEntry;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.runtime.standalone.extension.NClassLoaderDiagnostics;
import net.thevpc.nuts.runtime.standalone.extension.NIdClassLoaderRegistry;
import net.thevpc.nuts.runtime.standalone.extension.ServiceTypeIterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class NClassLoaderResolutionTest {

    public interface TestService {
        String greet();
    }

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test1_ClassLookupRegression() throws Exception {
        NIdClassLoaderRegistry.invalidateAll();
        Path tempDir = Files.createTempDirectory("nuts-test-reg");

        File jarA = compileAndJar(tempDir, "ImplA", "test.a",
                "public class ImplA {}", null);
        File jarB = compileAndJar(tempDir, "ImplB", "test.b",
                "public class ImplB {}", null);

        NPath pathA = NPath.of(jarA);
        NPath pathB = NPath.of(jarB);
        NId idA = NId.of("test:artifact-a#1.0.0");
        NId idB = NId.of("test:artifact-b#1.0.0");

        ClassLoader leafA = NIdClassLoaderRegistry.getIfPresent(idA);
        if (leafA == null) {
            registerLeaf(idA, pathA);
            leafA = NIdClassLoaderRegistry.getIfPresent(idA);
        }
        ClassLoader leafB = NIdClassLoaderRegistry.getIfPresent(idB);
        if (leafB == null) {
            registerLeaf(idB, pathB);
            leafB = NIdClassLoaderRegistry.getIfPresent(idB);
        }

        NClassLoader composite = NClassLoader.of("testComp1", null,
                NClasspathEntry.of(pathA),
                NClasspathEntry.of(pathB));

        Assertions.assertNotNull(leafA);
        Assertions.assertNotNull(leafB);

        Class<?> loadedFromComposite = composite.loadClass("test.a.ImplA");
        Assertions.assertNotNull(loadedFromComposite);

        Class<?> loadedFromLeaf = leafA.loadClass("test.b.ImplB");
        Assertions.assertNotNull(loadedFromLeaf);
        Assertions.assertEquals("test.b.ImplB", loadedFromLeaf.getName());
    }

    @Test
    public void test2_CrossLeafResourceDiscovery() throws Exception {
        NIdClassLoaderRegistry.invalidateAll();
        Path tempDir = Files.createTempDirectory("nuts-test-res2");

        String serviceInterface = TestService.class.getName().replace('$', '.');
        String serviceResource = "META-INF/services/" + TestService.class.getName();

        Map<String, String> resA = Collections.singletonMap(serviceResource, "test.a.ImplA\n");
        Map<String, String> resB = Collections.singletonMap(serviceResource, "test.b.ImplB\n");

        File jarA = compileAndJar(tempDir, "ImplA", "test.a",
                "public class ImplA implements " + serviceInterface + " { public String greet() { return \"A\"; } }", resA);
        File jarB = compileAndJar(tempDir, "ImplB", "test.b",
                "public class ImplB implements " + serviceInterface + " { public String greet() { return \"B\"; } }", resB);

        NPath pathA = NPath.of(jarA);
        NPath pathB = NPath.of(jarB);
        NId idA = NId.of("test:artifact-a#1.0.0");
        NId idB = NId.of("test:artifact-b#1.0.0");

        ClassLoader leafA = registerLeaf(idA, pathA);
        ClassLoader leafB = registerLeaf(idB, pathB);

        NClassLoader composite = NClassLoader.of("testComp2", null,
                NClasspathEntry.of(pathA),
                NClasspathEntry.of(pathB));

        Assertions.assertNotNull(leafA);
        Assertions.assertNotNull(leafB);

        Enumeration<URL> urlsA = leafA.getResources(serviceResource);
        List<URL> listA = toList(urlsA);
        Assertions.assertEquals(2, listA.size(), "leafA must discover resources from both leafA and sibling leafB");

        Enumeration<URL> urlsB = leafB.getResources(serviceResource);
        List<URL> listB = toList(urlsB);
        Assertions.assertEquals(2, listB.size(), "leafB must discover resources from both leafB and sibling leafA");
    }

    @Test
    public void test3_ServiceLoaderEndToEnd() throws Exception {
        NIdClassLoaderRegistry.invalidateAll();
        Path tempDir = Files.createTempDirectory("nuts-test-sl3");

        String serviceInterface = TestService.class.getName().replace('$', '.');
        String serviceResource = "META-INF/services/" + TestService.class.getName();

        Map<String, String> resA = Collections.singletonMap(serviceResource, "test.a.ImplA\n");
        Map<String, String> resB = Collections.singletonMap(serviceResource, "test.b.ImplB\n");

        File jarA = compileAndJar(tempDir, "ImplA", "test.a",
                "public class ImplA implements " + serviceInterface + " { public String greet() { return \"A\"; } }", resA);
        File jarB = compileAndJar(tempDir, "ImplB", "test.b",
                "public class ImplB implements " + serviceInterface + " { public String greet() { return \"B\"; } }", resB);

        NPath pathA = NPath.of(jarA);
        NPath pathB = NPath.of(jarB);
        NId idA = NId.of("test:artifact-a#1.0.0");
        NId idB = NId.of("test:artifact-b#1.0.0");

        ClassLoader leafA = registerLeaf(idA, pathA);
        ClassLoader leafB = registerLeaf(idB, pathB);

        NClassLoader composite = NClassLoader.of("testComp3", null,
                NClasspathEntry.of(pathA),
                NClasspathEntry.of(pathB));

        Assertions.assertNotNull(leafA);

        ServiceLoader<TestService> sl = ServiceLoader.load(TestService.class, leafA);
        List<String> greetings = new ArrayList<>();
        for (TestService ts : sl) {
            greetings.add(ts.greet());
        }
        Collections.sort(greetings);
        Assertions.assertEquals(Arrays.asList("A", "B"), greetings,
                "ServiceLoader started from leafA must find implementations from both leaves");
    }

    @Test
    public void test4_ServiceTypeIteratorEndToEnd() throws Exception {
        NIdClassLoaderRegistry.invalidateAll();
        Path tempDir = Files.createTempDirectory("nuts-test-sti4");

        String serviceInterface = TestService.class.getName().replace('$', '.');
        String serviceResource = "META-INF/services/" + TestService.class.getName();

        Map<String, String> resA = Collections.singletonMap(serviceResource, "test.a.ImplA\n");
        Map<String, String> resB = Collections.singletonMap(serviceResource, "test.b.ImplB\n");

        File jarA = compileAndJar(tempDir, "ImplA", "test.a",
                "public class ImplA implements " + serviceInterface + " { public String greet() { return \"A\"; } }", resA);
        File jarB = compileAndJar(tempDir, "ImplB", "test.b",
                "public class ImplB implements " + serviceInterface + " { public String greet() { return \"B\"; } }", resB);

        NPath pathA = NPath.of(jarA);
        NPath pathB = NPath.of(jarB);
        NId idA = NId.of("test:artifact-a#1.0.0");
        NId idB = NId.of("test:artifact-b#1.0.0");

        ClassLoader leafA = registerLeaf(idA, pathA);
        ClassLoader leafB = registerLeaf(idB, pathB);

        NClassLoader composite = NClassLoader.of("testComp4", null,
                NClasspathEntry.of(pathA),
                NClasspathEntry.of(pathB));

        Assertions.assertNotNull(leafA);

        List<Class<? extends TestService>> list = ServiceTypeIterator.loadList(TestService.class, leafA);
        Assertions.assertEquals(2, list.size(), "ServiceTypeIterator must find both implementations");
    }

    @Test
    public void test5_NoCompositeCase() throws Exception {
        NIdClassLoaderRegistry.invalidateAll();
        Path tempDir = Files.createTempDirectory("nuts-test-nc5");

        String serviceInterface = TestService.class.getName().replace('$', '.');
        String serviceResource = "META-INF/services/" + TestService.class.getName();

        Map<String, String> resA = Collections.singletonMap(serviceResource, "test.a.ImplA\n");
        Map<String, String> resB = Collections.singletonMap(serviceResource, "test.b.ImplB\n");

        File jarA = compileAndJar(tempDir, "ImplA", "test.a",
                "public class ImplA implements " + serviceInterface + " { public String greet() { return \"A\"; } }", resA);
        File jarB = compileAndJar(tempDir, "ImplB", "test.b",
                "public class ImplB implements " + serviceInterface + " { public String greet() { return \"B\"; } }", resB);

        NPath pathA = NPath.of(jarA);
        NPath pathB = NPath.of(jarB);
        NId idA = NId.of("test:artifact-a#1.0.0");
        NId idB = NId.of("test:artifact-b#1.0.0");

        // Create leaves individually in registry without placing them in any shared composite
        ClassLoader leafA = registerLeaf(idA, pathA);
        ClassLoader leafB = registerLeaf(idB, pathB);

        Assertions.assertNotNull(leafA);
        Assertions.assertNotNull(leafB);

        // Directly query leafA with no active composite on this thread
        Enumeration<URL> urls = leafA.getResources(serviceResource);
        List<URL> list = toList(urls);
        Assertions.assertEquals(2, list.size(),
                "Registry tier alone must resolve sibling resources when no composite is active");
    }

    @Test
    public void test6_VersionIsolationUnderRegistryFallback() throws Exception {
        NIdClassLoaderRegistry.invalidateAll();
        Path tempDir = Files.createTempDirectory("nuts-test-ver6");

        // Create guava-old (v20.0) with VersionMarker and OldOnlyHelper
        File jarOld = compileAndJar(tempDir, "VersionMarker", "com.google.common",
                "public class VersionMarker { public static String version() { return \"20.0\"; } }", null);
        File jarOldHelper = compileAndJar(tempDir, "OldOnlyHelper", "com.google.common",
                "public class OldOnlyHelper { public static String id() { return \"old-only\"; } }", null);

        // Create combined jar-old containing both VersionMarker and OldOnlyHelper
        File combinedOld = mergeJars(tempDir, "guava-old.jar", Arrays.asList(jarOld, jarOldHelper));

        // Create guava-new (v31.0) with VersionMarker only
        File jarNew = compileAndJar(tempDir, "VersionMarker", "com.google.common",
                "public class VersionMarker { public static String version() { return \"31.0\"; } }", null);
        File combinedNew = mergeJars(tempDir, "guava-new.jar", Collections.singletonList(jarNew));

        NId idOld = NId.of("com.google.guava:guava#20.0");
        NId idNew = NId.of("com.google.guava:guava#31.0");
        NPath pathOld = NPath.of(combinedOld);
        NPath pathNew = NPath.of(combinedNew);

        // 1. Unrelated part of the JVM loads guava-old into VM-wide registry
        ClassLoader leafOld = registerLeaf(idOld, pathOld);
        Assertions.assertNotNull(leafOld);

        // 2. Build a composite that resolves guava-new (v31.0)
        ClassLoader leafNew = registerLeaf(idNew, pathNew);
        NClassLoader compositeNew = NClassLoader.of("compositeNew", null, NClasspathEntry.of(pathNew));
        Assertions.assertNotNull(leafNew);

        // 3. Composite resolves VersionMarker -> must be 31.0
        Class<?> vmClass = compositeNew.loadClass("com.google.common.VersionMarker");
        Method versionMethod = vmClass.getMethod("version");
        String ver = (String) versionMethod.invoke(null);
        Assertions.assertEquals("31.0", ver, "Composite must resolve its own guava 31.0 VersionMarker");

        // 4. Force query for OldOnlyHelper from composite / leafNew:
        //    OldOnlyHelper is NOT in guava:31.0, only in registry's guava:20.0.
        //    Registry fallback MUST reject it due to version mismatch on shortName 'com.google.guava:guava'.
        Assertions.assertThrows(ClassNotFoundException.class, () -> {
            compositeNew.loadClass("com.google.common.OldOnlyHelper");
        }, "Registry fallback must reject class from conflicting version in registry");
    }

    @Test
    public void test7_Deduplication() throws Exception {
        NIdClassLoaderRegistry.invalidateAll();
        Path tempDir = Files.createTempDirectory("nuts-test-dedup7");

        String resPath = "META-INF/services/test.Shared";
        File jarA = compileAndJar(tempDir, "SharedA", "test.s", "public class SharedA {}",
                Collections.singletonMap(resPath, "providerA"));

        NPath pathA = NPath.of(jarA);
        NId idA = NId.of("test:artifact-a#1.0.0");

        ClassLoader leafA = registerLeaf(idA, pathA);
        NClassLoader composite = NClassLoader.of("compDedup", null, NClasspathEntry.of(pathA));
        Assertions.assertNotNull(leafA);

        Enumeration<URL> urls = leafA.getResources(resPath);
        List<URL> list = toList(urls);
        Assertions.assertEquals(1, list.size(), "getResources must not return duplicate URLs for the same resource");
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    public void test8_CycleSafety() throws Exception {
        NIdClassLoaderRegistry.invalidateAll();
        Path tempDir = Files.createTempDirectory("nuts-test-cycle8");

        File jarA = compileAndJar(tempDir, "CycleA", "test.c", "public class CycleA {}", null);
        File jarB = compileAndJar(tempDir, "CycleB", "test.c", "public class CycleB {}", null);

        NPath pathA = NPath.of(jarA);
        NPath pathB = NPath.of(jarB);
        NId idA = NId.of("test:cycle-a#1.0.0");
        NId idB = NId.of("test:cycle-b#1.0.0");

        ClassLoader leafA = registerLeaf(idA, pathA);
        ClassLoader leafB = registerLeaf(idB, pathB);

        NClassLoader composite = NClassLoader.of("compCycle", null,
                NClasspathEntry.of(pathA),
                NClasspathEntry.of(pathB));

        Assertions.assertNotNull(leafA);
        Assertions.assertNotNull(leafB);

        // Missing class lookup must terminate with ClassNotFoundException, not StackOverflowError
        Assertions.assertThrows(ClassNotFoundException.class, () -> leafA.loadClass("test.c.NonExistent"));
        Assertions.assertThrows(ClassNotFoundException.class, () -> leafB.loadClass("test.c.NonExistent"));

        // Missing resource lookup must terminate with empty enumeration
        Enumeration<URL> resA = leafA.getResources("META-INF/services/non.existent");
        Assertions.assertFalse(resA.hasMoreElements());
    }

    @Test
    public void test9_CleanupOnInvalidate() throws Exception {
        NIdClassLoaderRegistry.invalidateAll();
        Path tempDir = Files.createTempDirectory("nuts-test-inv9");

        String serviceInterface = TestService.class.getName().replace('$', '.');
        String serviceResource = "META-INF/services/" + TestService.class.getName();

        Map<String, String> resA = Collections.singletonMap(serviceResource, "test.a.ImplA\n");
        Map<String, String> resB = Collections.singletonMap(serviceResource, "test.b.ImplB\n");

        File jarA = compileAndJar(tempDir, "ImplA", "test.a",
                "public class ImplA implements " + serviceInterface + " { public String greet() { return \"A\"; } }", resA);
        File jarB = compileAndJar(tempDir, "ImplB", "test.b",
                "public class ImplB implements " + serviceInterface + " { public String greet() { return \"B\"; } }", resB);

        NPath pathA = NPath.of(jarA);
        NPath pathB = NPath.of(jarB);
        NId idA = NId.of("test:artifact-a#1.0.0");
        NId idB = NId.of("test:artifact-b#1.0.0");

        ClassLoader leafA = registerLeaf(idA, pathA);
        ClassLoader leafB = registerLeaf(idB, pathB);

        NClassLoader composite = NClassLoader.of("compInv", null,
                NClasspathEntry.of(pathA),
                NClasspathEntry.of(pathB));

        Assertions.assertNotNull(leafA);

        List<URL> listBefore = toList(leafA.getResources(serviceResource));
        Assertions.assertEquals(2, listBefore.size());

        // Invalidate jarB by ID
        NIdClassLoaderRegistry.invalidate(idB);

        List<URL> listAfter = toList(leafA.getResources(serviceResource));
        Assertions.assertEquals(1, listAfter.size(),
                "After invalidating leafB, leafA must no longer discover leafB's resources");
    }

    @Test
    public void test10_ThreadHopBehaviorAndDiagnostics() throws Exception {
        NIdClassLoaderRegistry.invalidateAll();
        Path tempDir = Files.createTempDirectory("nuts-test-th10");

        File jarA = compileAndJar(tempDir, "ImplA", "test.a", "public class ImplA {}", null);
        File jarB = compileAndJar(tempDir, "ImplB", "test.b", "public class ImplB {}", null);

        NPath pathA = NPath.of(jarA);
        NPath pathB = NPath.of(jarB);
        NId idA = NId.of("test:artifact-a#1.0.0");
        NId idB = NId.of("test:artifact-b#1.0.0");

        ClassLoader leafA = registerLeaf(idA, pathA);
        ClassLoader leafB = registerLeaf(idB, pathB);

        NClassLoader composite = NClassLoader.of("compTH", null,
                NClasspathEntry.of(pathA),
                NClasspathEntry.of(pathB));

        Assertions.assertNotNull(leafA);

        // Test diagnostics
        String classExplanation = NClassLoaderDiagnostics.explainClass(leafA, "test.b.ImplB");
        Assertions.assertNotNull(classExplanation);
        Assertions.assertTrue(classExplanation.contains("ImplB"));

        String resExplanation = NClassLoaderDiagnostics.explainResource(leafA, "META-INF/services/test.Service");
        Assertions.assertNotNull(resExplanation);

        // Test thread-hop with snapshot/restore
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            // Take snapshot
            Object snapshot = invokeSnapshot();

            Future<Class<?>> future = executor.submit(new Callable<Class<?>>() {
                @Override
                public Class<?> call() throws Exception {
                    // Restore snapshot on worker thread
                    invokeRestore(snapshot);
                    return leafA.loadClass("test.b.ImplB");
                }
            });

            Class<?> resolvedOnThread = future.get(5, TimeUnit.SECONDS);
            Assertions.assertNotNull(resolvedOnThread);
            Assertions.assertEquals("test.b.ImplB", resolvedOnThread.getName());
        } finally {
            executor.shutdown();
        }
    }

    private static ClassLoader registerLeaf(NId id, NPath path) throws Exception {
        Method getOrCreate = NIdClassLoaderRegistry.class.getDeclaredMethod("getOrCreate", NId.class, NPath.class);
        getOrCreate.setAccessible(true);
        return (ClassLoader) getOrCreate.invoke(null, id, path);
    }

    private static Object invokeSnapshot() throws Exception {
        Class<?> ctxClass = Class.forName("net.thevpc.nuts.runtime.standalone.extension.NClassLoaderContext");
        Method snapshotMethod = ctxClass.getDeclaredMethod("snapshot");
        snapshotMethod.setAccessible(true);
        return snapshotMethod.invoke(null);
    }

    private static void invokeRestore(Object snapshot) throws Exception {
        Class<?> ctxClass = Class.forName("net.thevpc.nuts.runtime.standalone.extension.NClassLoaderContext");
        Class<?> snapClass = Class.forName("net.thevpc.nuts.runtime.standalone.extension.NClassLoaderContext$Snapshot");
        Method restoreMethod = ctxClass.getDeclaredMethod("restore", snapClass);
        restoreMethod.setAccessible(true);
        restoreMethod.invoke(null, snapshot);
    }

    private static List<URL> toList(Enumeration<URL> e) {
        List<URL> list = new ArrayList<>();
        if (e != null) {
            while (e.hasMoreElements()) {
                list.add(e.nextElement());
            }
        }
        return list;
    }

    private File compileAndJar(Path tempDir, String simpleName, String pkg, String src, Map<String, String> extraResources) throws IOException {
        Path srcDir = tempDir.resolve(pkg.replace('.', '/'));
        Files.createDirectories(srcDir);
        Path srcFile = srcDir.resolve(simpleName + ".java");
        Files.write(srcFile, ("package " + pkg + ";\n" + src).getBytes());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String cp = System.getProperty("java.class.path");
        int rc = compiler.run(null, null, null, "-cp", cp, "-d", tempDir.toString(), srcFile.toString());
        Assertions.assertEquals(0, rc, "Compilation must succeed for " + simpleName);

        Path classFile = srcDir.resolve(simpleName + ".class");
        Assertions.assertTrue(Files.exists(classFile), "Class file must exist for " + simpleName);

        File jarFile = tempDir.resolve(simpleName + "-" + System.nanoTime() + ".jar").toFile();
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile))) {
            String entryName = pkg.replace('.', '/') + "/" + simpleName + ".class";
            jos.putNextEntry(new JarEntry(entryName));
            jos.write(Files.readAllBytes(classFile));
            jos.closeEntry();
            if (extraResources != null) {
                for (Map.Entry<String, String> res : extraResources.entrySet()) {
                    jos.putNextEntry(new JarEntry(res.getKey()));
                    jos.write(res.getValue().getBytes());
                    jos.closeEntry();
                }
            }
        }
        return jarFile;
    }

    private File mergeJars(Path tempDir, String outputJarName, List<File> inputJars) throws IOException {
        File out = tempDir.resolve(outputJarName).toFile();
        Set<String> added = new HashSet<>();
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(out))) {
            for (File inJar : inputJars) {
                try (java.util.jar.JarFile jar = new java.util.jar.JarFile(inJar)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry e = entries.nextElement();
                        if (added.add(e.getName())) {
                            jos.putNextEntry(new JarEntry(e.getName()));
                            try (java.io.InputStream is = jar.getInputStream(e)) {
                                byte[] buf = new byte[4096];
                                int n;
                                while ((n = is.read(buf)) > 0) {
                                    jos.write(buf, 0, n);
                                }
                            }
                            jos.closeEntry();
                        }
                    }
                }
            }
        }
        return out;
    }
}
