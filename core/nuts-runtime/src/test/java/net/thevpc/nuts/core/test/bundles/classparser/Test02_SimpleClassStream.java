/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.bundles.classparser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.bundles.io.SimpleClassStream;
import net.thevpc.nuts.runtime.core.util.CoreTimeUtils;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test02_SimpleClassStream {

    private static String baseFolder;

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Test02_SimpleClassStream.class.getName());
    private static long max = 0;

    @Test
    public void test1() throws Exception {
        NutsWorkspace ws = Nuts.openWorkspace("-y", "--workspace", baseFolder + "/" + TestUtils.getCallerMethodName());
        Path path = Paths.get("/home/vpc/.m2/repository/org/ow2/asm/asm-commons/7.0/asm-commons-7.0.jar");
        NutsSession session=ws.createSession();
        if (Files.exists(path)) {
//        parseAnyFile(Paths.get(System.getProperty("user.home")).resolve(".m2/repository"));
            parseAnyFile(path, session);
        }
//        parseAnyFile(Paths.get("/home/vpc/.m2/repository/com/ibm/icu/icu4j/2.6.1/icu4j-2.6.1.jar"));

        ///home/vpc/.m2/repository/org/ow2/asm/asm-commons/7.0/asm-commons-7.0.jar
        //Paths.get("/data/vpc/Data/xprojects/net/thevpc/nuts/toolbox/nsh/target/nsh-0.5.4.0.jar")
//        mmFolder(Paths.get("target/classes"));
//        mm(Paths.get("/data/vpc/Data/xprojects/net/thevpc/nuts/nuts-runtime/target/classes/net/thevpc/nuts/core/util/CommonRootsHelper.class"));
    }

    private static void parseAnyFile(Path file, NutsSession ws) throws IOException {
        if (Files.isDirectory(file)) {
            parseFolder(file, ws);
        } else {
            parseRegularFile(file, ws);
        }
    }

    private static void parseRegularFile(Path file, NutsSession ws) throws IOException {
        long from = System.currentTimeMillis();
        if (file.getFileName().toString().endsWith(".class")) {
            parseClassFile(file.toAbsolutePath().normalize(), ws);
        }
        if (file.getFileName().toString().endsWith(".jar")) {
            parseJarFile(file.toAbsolutePath().normalize(), ws);
        }
        long to = System.currentTimeMillis();
        if (max < to - from) {
            max = to - from;
        }
        TestUtils.println("### TIME [" + file + "] " + CoreTimeUtils.formatPeriodMilli(to - from) + " -- " + max);
    }

    private static void parseFolder(Path file, NutsSession ws) throws IOException {
        Files.walkFileTree(file, new FileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                parseRegularFile(file, ws);
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

    private static void parseJarFile(Path file, NutsSession session) throws IOException {
        TestUtils.println("parse jar " + file + " ... ");
        try (InputStream in = Files.newInputStream(file)) {
            TestUtils.println("parse jar " + file + " :: " + Arrays.asList(NutsWorkspaceUtils.of(session.getWorkspace()).parseJarExecutionEntries(in, file.toString(), session)));
        }
    }

    private static void parseClassFile(Path file, NutsSession ws) throws IOException {
        TestUtils.println(file);

        SimpleClassStream scs = new SimpleClassStream(Files.newInputStream(file), new SimpleClassStream.Visitor() {
            @Override
            public void visitVersion(int major, int minor) {
                TestUtils.println("\t" + major + "." + minor);
            }

            public void visitClassDeclaration(int accessFlags, String thisClass, String superClass, String[] interfaces) {
                TestUtils.println("\tclass " + accessFlags + " " + thisClass + " extends " + superClass + " implements " + Arrays.asList(interfaces));
            }

            @Override
            public void visitMethod(int accessFlags, String name, String descriptor) {
                TestUtils.println("\t\tmethod " + accessFlags + " " + name + " " + descriptor);
            }

            @Override
            public void visitField(int accessFlags, String name, String descriptor) {
                TestUtils.println("\t\tfield " + accessFlags + " " + name + " " + descriptor);
            }

        }
        );
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null, new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ " + TestUtils.getCallerClassSimpleName());
    }
}
