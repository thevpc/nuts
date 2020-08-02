/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.whitebox.utilities;

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

import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.test.utils.TestUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.util.io.SimpleClassStream;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class Test02_SimpleClassStream {
    private static String baseFolder;

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Test02_SimpleClassStream.class.getName());
    private static long max = 0;

    @Test
    public void test1() throws Exception {
        NutsWorkspace ws = Nuts.openWorkspace("-y","--workspace", baseFolder + "/" + TestUtils.getCallerMethodName());
//        parseAnyFile(Paths.get(System.getProperty("user.home")).resolve(".m2/repository"));
        parseAnyFile(Paths.get("/home/vpc/.m2/repository/org/ow2/asm/asm-commons/7.0/asm-commons-7.0.jar"),ws);
//        parseAnyFile(Paths.get("/home/vpc/.m2/repository/com/ibm/icu/icu4j/2.6.1/icu4j-2.6.1.jar"));

        ///home/vpc/.m2/repository/org/ow2/asm/asm-commons/7.0/asm-commons-7.0.jar
        //Paths.get("/data/vpc/Data/xprojects/net/vpc/apps/nuts/toolbox/nsh/target/nsh-0.5.4.0.jar")
//        mmFolder(Paths.get("target/classes"));
//        mm(Paths.get("/data/vpc/Data/xprojects/net/vpc/apps/nuts/nuts-core/target/classes/net/vpc/app/nuts/core/util/CommonRootsHelper.class"));
    }

    private static void parseAnyFile(Path file,NutsWorkspace ws) throws IOException {
        if (Files.isDirectory(file)) {
            parseFolder(file,ws);
        } else {
            parseRegularFile(file,ws);
        }
    }

    private static void parseRegularFile(Path file,NutsWorkspace ws) throws IOException {
        long from = System.currentTimeMillis();
        if (file.getFileName().toString().endsWith(".class")) {
            parseClassFile(file.normalize().toAbsolutePath(),ws);
        }
        if (file.getFileName().toString().endsWith(".jar")) {
            parseJarFile(file.normalize().toAbsolutePath(),ws);
        }
        long to = System.currentTimeMillis();
        if (max < to - from) {
            max = to - from;
        }
        TestUtils.println("### TIME [" + file + "] " + CoreCommonUtils.formatPeriodMilli(to - from) + " -- " + max);
    }

    private static void parseFolder(Path file,NutsWorkspace ws) throws IOException {
        Files.walkFileTree(file, new FileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                parseRegularFile(file,ws);
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

    private static void parseJarFile(Path file,NutsWorkspace ws) throws IOException {
        TestUtils.println("parse jar " + file + " ... ");
        try (InputStream in = Files.newInputStream(file)) {
            TestUtils.println("parse jar " + file + " :: " + Arrays.asList(NutsWorkspaceUtils.of(ws).parseJarExecutionEntries(in, file.toString())));
        }
    }

    private static void parseClassFile(Path file, NutsWorkspace ws) throws IOException {
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
    @BeforeClass
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null,new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }
}
