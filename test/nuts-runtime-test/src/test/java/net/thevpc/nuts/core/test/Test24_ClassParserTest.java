/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import net.thevpc.nuts.NExecutionEntry;
import net.thevpc.nuts.NLibPaths;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.format.NVisitResult;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassByteCode;
import org.junit.jupiter.api.*;

/**
 * @author thevpc
 */
public class Test24_ClassParserTest {

    static NSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Test24_ClassParserTest.class.getName());
    private static long max = 0;

    @Test
    public void test1() throws Exception {
        Path path = Paths.get(System.getProperty("user.home")).resolve(".m2/repository/org/ow2/asm/asm-commons/7.0/asm-commons-7.0.jar");
        if (Files.exists(path)) {
            parseAnyFile(path, session);
        }
    }

    private static void parseAnyFile(Path file, NSession session) throws IOException {
        if (Files.isDirectory(file)) {
            parseFolder(file, session);
        } else {
            parseRegularFile(file, session);
        }
    }

    private static void parseRegularFile(Path file, NSession session) throws IOException {
        NChronometer from = NChronometer.startNow();
        if (file.getFileName().toString().endsWith(".class")) {
            parseClassFile(file.toAbsolutePath().normalize(), session);
        }
        if (file.getFileName().toString().endsWith(".jar")) {
            parseJarFile(file.toAbsolutePath().normalize(), session);
        }
        NDuration to = from.getDuration();
        if (max < to.getTimeAsNanos()) {
            max = to.getTimeAsNanos();
        }
        TestUtils.println("### TIME [" + file + "] " + to + " -- " + max);
    }

    private static void parseFolder(Path file, NSession session) throws IOException {
        Files.walkFileTree(file, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        parseRegularFile(file, session);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                }
        );
    }

    private static void parseJarFile(Path file, NSession session) throws IOException {
        TestUtils.println("parse jar " + file + " ... ");
        try (InputStream in = Files.newInputStream(file)) {
            TestUtils.println("parse jar " + file + " :: " + Arrays.asList(
                    NExecutionEntry.parse(in, "jar", file.toString(),session)
            ));
        }
    }

    private static void parseClassFile(Path file, NSession session) throws IOException {
        TestUtils.println(file);

        JavaClassByteCode scs = new JavaClassByteCode(Files.newInputStream(file), new JavaClassByteCode.Visitor() {
            @Override
            public NVisitResult visitVersion(int major, int minor) {
                TestUtils.println("\t" + major + "." + minor);
                return NVisitResult.CONTINUE;
            }

            public NVisitResult visitClassDeclaration(int accessFlags, String thisClass, String superClass, String[] interfaces) {
                TestUtils.println("\tclass " + accessFlags + " " + thisClass + " extends " + superClass + " implements " + Arrays.asList(interfaces));
                return NVisitResult.CONTINUE;
            }

            @Override
            public NVisitResult visitMethod(int accessFlags, String name, String descriptor) {
                TestUtils.println("\t\tmethod " + accessFlags + " " + name + " " + descriptor);
                return NVisitResult.CONTINUE;
            }

            @Override
            public NVisitResult visitField(int accessFlags, String name, String descriptor) {
                TestUtils.println("\t\tfield " + accessFlags + " " + name + " " + descriptor);
                return NVisitResult.CONTINUE;
            }

        }, session
        );
    }
}
