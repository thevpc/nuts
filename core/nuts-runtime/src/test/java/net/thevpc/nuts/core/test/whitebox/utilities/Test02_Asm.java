package net.thevpc.nuts.core.test.whitebox.utilities;

///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nuts.core.test.testutils;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.UncheckedIOException;
//import java.lang.reflect.Modifier;
//import java.nio.file.FileVisitResult;
//import java.nio.file.FileVisitor;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.attribute.BasicFileAttributes;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.function.Predicate;
//import NutsExecutionEntry;
//import net.thevpc.nuts.runtime.util.CoreCommonUtils;
//import net.thevpc.nuts.runtime.util.CoreNutsUtils;
//import net.thevpc.nuts.runtime.util.bundledlibs.io.InputStreamVisitor;
//import net.thevpc.nuts.runtime.util.bundledlibs.io.ZipUtils;
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
//    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Test02_Asm.class.getName());
//    private static long max=0;
//    @Test
//    public void test1() throws Exception {
//        parseAnyFile(Paths.get(System.getProperty("user.home")).resolve(".m2/repository"));
////        parseAnyFile(Paths.get("/home/vpc/.m2/repository/org/ow2/asm/asm-commons/7.0/asm-commons-7.0.jar"));
////        parseAnyFile(Paths.get(System.getProperty("user.home")).resolve(".m2/repository/com/ibm/icu/icu4j/2.6.1/icu4j-2.6.1.jar"));
//
//        ///home/vpc/.m2/repository/org/ow2/asm/asm-commons/7.0/asm-commons-7.0.jar
//        //Paths.get("/data/vpc/Data/xprojects/net/thevpc/nuts/toolbox/nsh/target/nsh-0.5.4.0.jar")
////        mmFolder(Paths.get("target/classes"));
////        mm(Paths.get("/data/vpc/Data/xprojects/net/thevpc/nuts/nuts-runtime/target/classes/net/thevpc/nuts/core/util/CommonRootsHelper.class"));
//    }
//
//    private static void parseAnyFile(Path file) throws IOException {
//        if (Files.isDirectory(file)) {
//            parseFolder(file);
//        } else if (Files.isRegularFile(file)) {
//            parseRegularFile(file);
//        }
//    }
//
//    private static void parseRegularFile(Path file) throws IOException {
//        long from=System.currentTimeMillis();
//        if (file.getFileName().toString().endsWith(".class")) {
//            paseClassFile(file.normalize().toAbsolutePath());
//        }
//        if (file.getFileName().toString().endsWith(".jar")) {
//            parseJarFile(file.normalize().toAbsolutePath());
//        }
//        long to=System.currentTimeMillis();
//        if(max<to-from){
//            max=to-from;
//        }
//        TestUtils.println("### TIME ["+file+"] "+CoreCommonUtils.formatPeriodMilli(to-from)+" -- "+max);
//    }
//
//    private static void parseFolder(Path file) throws IOException {
//        Files.walkFileTree(file, new FileVisitor<Path>() {
//            @Override
//            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                parseRegularFile(file);
//                return FileVisitResult.CONTINUE;
//            }
//
//            @Override
//            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                return FileVisitResult.CONTINUE;
//            }
//
//            @Override
//            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
//                return FileVisitResult.CONTINUE;
//            }
//
//            @Override
//            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//                return FileVisitResult.CONTINUE;
//            }
//        }
//        );
//    }
//
//    private static void parseJarFile(Path file) throws IOException {
//        TestUtils.println("parse jar " + file + " ... ");
//        try (InputStream in = Files.newInputStream(file)) {
//            TestUtils.println("parse jar " + file + " :: " + Arrays.asList(parseMainClasses(in, file.toString())));
//        }
//    }
//
//    private static void paseClassFile(Path file) throws IOException {
//        TestUtils.println(file);
//
//        ClassReader scs = new ClassReader(Files.newInputStream(file));
//        scs.accept(new ClassVisitor(Opcodes.ASM4) {
//            @Override
//            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
//                TestUtils.println("\t\tmethod " + access + " " + name + " " + desc);
//                return null;
//            }
//
//            @Override
//            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
//                TestUtils.println("\t\tfield " + access + " " + name + " " + desc);
//                return null;
//            }
//
//            @Override
//            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
//                TestUtils.println("\tclass " + access + " " + name + " " + signature + " extends " + superName + " implements " + Arrays.asList(interfaces));
//            }
//
//        }, 0
//        );
//    }
////    @Test
//
//    public void test2() throws Exception {
//        String file = "/data/vpc/Data/xprojects/net/thevpc/nuts/nuts-runtime/target/classes/net/thevpc/nuts/core/util/CommonRootsHelper.class";
//        TestUtils.println(file);
//        ClassReader classReader = new ClassReader(Files.newInputStream(Paths.get(file)));
//        classReader.accept(new ClassVisitor(Opcodes.ASM4) {
//
//            @Override
//            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
//                TestUtils.println("\t\tmethod " + access + " " + name + " " + desc);
//                return null;
//            }
//
//            @Override
//            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
//                TestUtils.println("\t\tfield " + access + " " + name + " " + desc);
//                return null;
//            }
//
//            @Override
//            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
//                TestUtils.println("\tclass " + access + " " + name + " " + signature + " extends " + superName + " implements " + Arrays.asList(interfaces));
//            }
//
//        }, 0);
//
//    }
//
//    public static NutsExecutionEntry[] parseMainClasses(InputStream jarStream, String sourceName) {
//
//        final List<NutsExecutionEntry> classes = new ArrayList<>();
//        final List<String> manifiestClass = new ArrayList<>();
//        try {
//            ZipUtils.visitZipStream(jarStream, new Predicate<String>() {
//                @Override
//                public boolean test(String path) {
//                    return path.endsWith(".class")
//                            || path.equals("META-INF/MANIFEST.MF");
//                }
//            }, new InputStreamVisitor() {
//                @Override
//                public boolean visit(String path, InputStream inputStream) throws IOException {
//                    if (path.endsWith(".class")) {
//                        int mainClass = -1;
//                        try {
//                            getMainClassType(inputStream);
//                        } catch (Exception ex) {
//                            log.log(java.util.logging.Level.SEVERE, "Invalid file format " + sourceName);
//                            log.log(java.util.logging.Level.FINER, "Invalid file format " + sourceName, ex);
//                        }
//                        if (mainClass == 1 || mainClass == 3) {
//                            classes.add(new NutsExecutionEntry(
//                                    path.replace('/', '.').substring(0, path.length() - ".class".length()),
//                                    false,
//                                    mainClass == 3
//                            ));
//                        }
//                    } else {
//                        try (BufferedReader b = new BufferedReader(new InputStreamReader(inputStream))) {
//                            String line = null;
//                            while ((line = b.readLine()) != null) {
//                                if (line.startsWith("Main-Class:")) {
//                                    String c = line.substring("Main-Class:".length()).trim();
//                                    if (c.length() > 0) {
//                                        manifiestClass.add(c);
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    return true;
//                }
//            });
//        } catch (IOException ex) {
//            throw new UncheckedIOException(ex);
//        }
//        List<NutsExecutionEntry> entries = new ArrayList<>();
//        String defaultEntry = null;
//        if (manifiestClass.size() > 0) {
//            defaultEntry = manifiestClass.get(0);
//        }
//        for (NutsExecutionEntry entry : classes) {
//            if (defaultEntry != null && defaultEntry.equals(entry.getName())) {
//                entries.add(new NutsExecutionEntry(entry.getName(), true, entry.isApp()));
//            } else {
//                entries.add(entry);
//            }
//        }
//        return entries.toArray(new NutsExecutionEntry[0]);
//    }
//
//    /**
//     * @param stream
//     * @return
//     * @throws IOException
//     */
//    public static int getMainClassType(InputStream stream) throws IOException {
//        final List<Boolean> mainClass = new ArrayList<>(1);
//        final List<Boolean> nutsApp = new ArrayList<>(1);
//        ClassVisitor cl = new ClassVisitor(Opcodes.ASM4) {
//            String lastClass = null;
//
//            /**
//             * When a method is encountered
//             */
//            @Override
//            public MethodVisitor visitMethod(int access, String name,
//                    String desc, String signature, String[] exceptions) {
//                if (name.equals("main") && desc.equals("([Ljava/lang/String;)V")
//                        && Modifier.isPublic(access)
//                        && Modifier.isStatic(access)) {
//                    mainClass.add(true);
//                }
//                return super.visitMethod(access, name, desc, signature, exceptions);
//            }
//
//            @Override
//            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
//                if (superName != null && superName.equals("net/vpc/app/nuts/app/NutsApplication")) {
//                    nutsApp.add(true);
//                }
//                super.visit(version, access, name, signature, superName, interfaces);
//            }
//        };
//        ClassReader classReader = new ClassReader(stream);
//        classReader.accept(cl, 0);
//        return ((mainClass.isEmpty()) ? 0 : 1) + (nutsApp.isEmpty() ? 0 : 2);
//    }
//
//}
