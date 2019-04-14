///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.vpc.app.nuts.core.test.testutils;
//
//import java.io.IOException;
//import java.nio.file.FileVisitResult;
//import java.nio.file.FileVisitor;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.attribute.BasicFileAttributes;
//import java.util.Arrays;
//import net.vpc.app.nuts.core.util.SimpleClassStream;
//import org.junit.Test;
//import org.objectweb.asm.ClassReader;
//import org.objectweb.asm.ClassVisitor;
//import org.objectweb.asm.FieldVisitor;
//import org.objectweb.asm.MethodVisitor;
//import org.objectweb.asm.Opcodes;
//
///**
// *
// * @author vpc
// */
//public class Test02_Asm {
//
//    
//    
//    @Test
//    public void test2() throws Exception {
//        String file = "/data/vpc/Data/xprojects/net/vpc/apps/nuts/nuts-core/target/classes/net/vpc/app/nuts/core/util/CommonRootsHelper.class";
//        System.out.println(file);
//                ClassReader classReader = new ClassReader(Files.newInputStream(Paths.get(file)));
//        classReader.accept(new ClassVisitor(Opcodes.ASM4) {
//            
//            @Override
//            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
//                System.out.println("\t\tmethod " + access + " " + name + " " + desc);
//                return null;
//            }
//
//            @Override
//            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
//                System.out.println("\t\tfield " + access + " " + name + " " + desc);
//                return null;
//            }
//
//            @Override
//            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
//                System.out.println("\tclass " + access + " " + name + " " +signature+" extends " + superName + " implements " + Arrays.asList(interfaces));
//            }
//            
//        }, 0);
//
//    }
//
//}
