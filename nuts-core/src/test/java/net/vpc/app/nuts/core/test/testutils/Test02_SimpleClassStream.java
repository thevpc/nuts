/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.testutils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import net.vpc.app.nuts.core.util.SimpleClassStream;
import org.junit.Test;

/**
 *
 * @author vpc
 */
public class Test02_SimpleClassStream {

    @Test
    public void test1() throws Exception {
        mmFolder(Paths.get("target/classes"));
//        mm(Paths.get("/data/vpc/Data/xprojects/net/vpc/apps/nuts/nuts-core/target/classes/net/vpc/app/nuts/core/util/CommonRootsHelper.class"));
    }

    private static void mmFolder(Path file) throws IOException {
        Files.walkFileTree(file, new FileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(".class")) {
                    mm(file.normalize().toAbsolutePath());
                }
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

    private static void mm(Path file) throws IOException {
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
            public void visitMethod(int accessFlags, String name, String descriptor, SimpleClassStream.MethodAttribute[] attributes) {
                System.out.println("\t\tmethod " + accessFlags + " " + name + " " + descriptor);
            }

            @Override
            public void visitField(int accessFlags, String name, String descriptor, SimpleClassStream.FieldAttribute[] attributes) {
                System.out.println("\t\tfield " + accessFlags + " " + name + " " + descriptor);
            }

        }
        );
    }

}
