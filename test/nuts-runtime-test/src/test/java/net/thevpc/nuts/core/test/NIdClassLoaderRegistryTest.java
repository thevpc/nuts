package net.thevpc.nuts.core.test;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.runtime.standalone.extension.NIdClassLoaderRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class NIdClassLoaderRegistryTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void testRegistryCachingAndInvalidation() throws Exception {
        NIdClassLoaderRegistry.invalidateAll();

        // 1. Create two test jars with real compiled classes
        Path tempDir = Files.createTempDirectory("nuts-test-cl");
        File jar1 = compileAndJar(tempDir, "Alpha", "net.thevpc.test", "public class Alpha {}");
        File jar2 = compileAndJar(tempDir, "Beta", "net.thevpc.test", "public class Beta {}");

        // 2. Register both jars via path
        NPath p1 = NPath.of(jar1);
        NPath p2 = NPath.of(jar2);

        NClassLoader cl1 = NClassLoader.of("cl1", null, net.thevpc.nuts.artifact.NClasspathEntry.of(p1));
        NClassLoader cl2 = NClassLoader.of("cl2", null, net.thevpc.nuts.artifact.NClasspathEntry.of(p2));

        ClassLoader leaf1 = NIdClassLoaderRegistry.getIfPresent(p1);
        ClassLoader leaf2 = NIdClassLoaderRegistry.getIfPresent(p2);
        Assertions.assertNotNull(leaf1, "leaf1 should be registered");
        Assertions.assertNotNull(leaf2, "leaf2 should be registered");

        // 3. Cross-leaf lookup: asking leaf1 for Beta (which is in leaf2)
        Class<?> betaClass1 = leaf1.loadClass("net.thevpc.test.Beta");
        Assertions.assertNotNull(betaClass1);
        Assertions.assertEquals("net.thevpc.test.Beta", betaClass1.getName());

        // 4. Verify caching: repeated lookup returns identical Class object
        Class<?> betaClass2 = leaf1.loadClass("net.thevpc.test.Beta");
        Assertions.assertSame(betaClass1, betaClass2, "Second lookup must return the cached class");

        // 5. Verify negative caching: non-existent class throws CNFE
        Assertions.assertThrows(ClassNotFoundException.class, () -> leaf1.loadClass("net.thevpc.test.NonExistent"));
        // Second lookup should hit negative cache and throw immediately
        Assertions.assertThrows(ClassNotFoundException.class, () -> leaf1.loadClass("net.thevpc.test.NonExistent"));

        // 6. Test invalidating specific class
        NIdClassLoaderRegistry.invalidateClass("net.thevpc.test.NonExistent");
        NIdClassLoaderRegistry.invalidateClass("net.thevpc.test.Beta");

        // Reload after cache invalidation should still succeed (re-resolves from leaf2)
        Class<?> betaClass3 = leaf1.loadClass("net.thevpc.test.Beta");
        Assertions.assertEquals("net.thevpc.test.Beta", betaClass3.getName());

        // 7. Test invalidateCache()
        NIdClassLoaderRegistry.invalidateCache();
        Class<?> betaClass4 = leaf1.loadClass("net.thevpc.test.Beta");
        Assertions.assertEquals("net.thevpc.test.Beta", betaClass4.getName());

        // 8. Test leaf invalidation by path
        NIdClassLoaderRegistry.invalidate(p2);
        Assertions.assertNull(NIdClassLoaderRegistry.getIfPresent(p2), "leaf2 should be removed");

        // After leaf2 is invalidated, leaf1 should no longer be able to load Beta
        Assertions.assertThrows(ClassNotFoundException.class, () -> leaf1.loadClass("net.thevpc.test.Beta"));

        // 9. Test invalidateAll()
        NIdClassLoaderRegistry.invalidateAll();
        Assertions.assertNull(NIdClassLoaderRegistry.getIfPresent(p1));

        // Clean up
        jar1.delete();
        jar2.delete();
    }

    private File compileAndJar(Path tempDir, String simpleName, String pkg, String src) throws IOException {
        Path srcDir = tempDir.resolve(pkg.replace('.', '/'));
        Files.createDirectories(srcDir);
        Path srcFile = srcDir.resolve(simpleName + ".java");
        Files.write(srcFile, ("package " + pkg + ";\n" + src).getBytes());

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int rc = compiler.run(null, null, null, "-d", tempDir.toString(), srcFile.toString());
        Assertions.assertEquals(0, rc, "Compilation must succeed");

        Path classFile = srcDir.resolve(simpleName + ".class");
        Assertions.assertTrue(Files.exists(classFile), "Class file must exist");

        File jarFile = tempDir.resolve(simpleName + ".jar").toFile();
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile))) {
            String entryName = pkg.replace('.', '/') + "/" + simpleName + ".class";
            jos.putNextEntry(new JarEntry(entryName));
            jos.write(Files.readAllBytes(classFile));
            jos.closeEntry();
        }
        return jarFile;
    }
}
