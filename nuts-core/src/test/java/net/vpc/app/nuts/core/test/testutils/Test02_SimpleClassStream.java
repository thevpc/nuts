/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.testutils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;
import net.vpc.app.nuts.core.util.io.SimpleClassStream;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class Test02_SimpleClassStream {

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Test02_SimpleClassStream.class.getName());
    private static long max = 0;

    @Test
    public void test1() throws Exception {
//        parseAnyFile(Paths.get(System.getProperty("user.home")).resolve(".m2/repository"));
        parseAnyFile(Paths.get("/home/vpc/.m2/repository/org/ow2/asm/asm-commons/7.0/asm-commons-7.0.jar"));
//        parseAnyFile(Paths.get("/home/vpc/.m2/repository/com/ibm/icu/icu4j/2.6.1/icu4j-2.6.1.jar"));

        ///home/vpc/.m2/repository/org/ow2/asm/asm-commons/7.0/asm-commons-7.0.jar
        //Paths.get("/data/vpc/Data/xprojects/net/vpc/apps/nuts/toolbox/nsh/target/nsh-0.5.4.0.jar")
//        mmFolder(Paths.get("target/classes"));
//        mm(Paths.get("/data/vpc/Data/xprojects/net/vpc/apps/nuts/nuts-core/target/classes/net/vpc/app/nuts/core/util/CommonRootsHelper.class"));
    }

    private static void parseAnyFile(Path file) throws IOException {
        if (Files.isDirectory(file)) {
            parseFolder(file);
        } else {
            parseRegularFile(file);
        }
    }

    private static void parseRegularFile(Path file) throws IOException {
        long from = System.currentTimeMillis();
        if (file.getFileName().toString().endsWith(".class")) {
            paseClassFile(file.normalize().toAbsolutePath());
        }
        if (file.getFileName().toString().endsWith(".jar")) {
            parseJarFile(file.normalize().toAbsolutePath());
        }
        long to = System.currentTimeMillis();
        if (max < to - from) {
            max = to - from;
        }
        System.out.println("### TIME [" + file + "] " + CoreCommonUtils.formatPeriodMilli(to - from) + " -- " + max);
    }

    private static void parseFolder(Path file) throws IOException {
        Files.walkFileTree(file, new FileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                parseRegularFile(file);
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

    private static void parseJarFile(Path file) throws IOException {
        System.out.println("parse jar " + file + " ... ");
        try (InputStream in = Files.newInputStream(file)) {
            System.out.println("parse jar " + file + " :: " + Arrays.asList(CorePlatformUtils.parseJarExecutionEntries(in, file.toString())));
        }
    }

    private static void paseClassFile(Path file) throws IOException {
        System.out.println(file);

        SimpleClassStream scs = new SimpleClassStream(Files.newInputStream(file), new SimpleClassStream.Visitor() {
            @Override
            public void visitVersion(int major, int minor) {
                System.out.println("\t" + major + "." + minor);
            }

            public void visitClassDeclaration(int accessFlags, String thisClass, String superClass, String[] interfaces) {
                System.out.println("\tclass " + accessFlags + " " + thisClass + " extends " + superClass + " implements " + Arrays.asList(interfaces));
            }

            @Override
            public void visitMethod(int accessFlags, String name, String descriptor) {
                System.out.println("\t\tmethod " + accessFlags + " " + name + " " + descriptor);
            }

            @Override
            public void visitField(int accessFlags, String name, String descriptor) {
                System.out.println("\t\tfield " + accessFlags + " " + name + " " + descriptor);
            }

        }
        );
    }

}
