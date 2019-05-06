/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.util.common;

import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.DefaultPlatformBeanProperty;
import net.vpc.app.nuts.core.util.io.SimpleClassStream;
import net.vpc.app.nuts.core.util.common.PlatformBeanProperty;
import net.vpc.app.nuts.core.util.common.Ref;
import net.vpc.app.nuts.*;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.vpc.app.nuts.core.util.io.InputStreamVisitor;
import net.vpc.app.nuts.core.util.io.ProcessBuilder2;
import net.vpc.app.nuts.core.util.io.ZipUtils;

/**
 * Created by vpc on 5/16/17.
 */
public class CorePlatformUtils {

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(CorePlatformUtils.class.getName());
    public static final Map<String, String> SUPPORTED_ARCH_ALIASES = new HashMap<>();
    private static final Set<String> SUPPORTED_ARCH = new HashSet<>(Arrays.asList("x86", "ia64", "amd64", "ppc", "sparc"));
    private static final Set<String> SUPPORTED_OS = new HashSet<>(Arrays.asList("linux", "windows", "mac", "sunos", "freebsd"));
    private static Map<String, String> LOADED_OS_DIST_MAP = null;
    private static final WeakHashMap<String, PlatformBeanProperty> cachedPlatformBeanProperties = new WeakHashMap<>();

    static {
        SUPPORTED_ARCH_ALIASES.put("i386", "x86");
    }

    public static Map<String, String> getOsDistMap() {
        String property = System.getProperty("os.name").toLowerCase();
        if (property.startsWith("linux")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux();
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        if (property.startsWith("mac")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux();
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        if (property.startsWith("sunos")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux();
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        if (property.startsWith("freebsd")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux();
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        return new HashMap<>();
    }

//    public static String getPlatformOsLib() {
//        switch (CoreNutsUtils.parseNutsId(getPlatformOs()).getSimpleName()) {
//            case "linux":
//            case "mac":
//            case "sunos":
//            case "freebsd": {
//                return "/usr/share";
//            }
//            case "windows": {
//                String pf = System.getenv("ProgramFiles");
//                if (CoreStringUtils.isEmpty(pf)) {
//                    pf = "C:\\Program Files";
//                }
//                return pf;
//            }
//        }
//        return "/usr/share";
//    }
    /**
     * this is inspired from
     * http://stackoverflow.com/questions/15018474/getting-linux-distro-from-java
     * so thanks //PbxMan//
     *
     * @return
     */
    public static Map<String, String> getOsDistMapLinux() {
        File dir = new File("/etc/");
        List<File> fileList = new ArrayList<>();
        if (dir.exists()) {
            File[] a = dir.listFiles((File dir1, String filename) -> filename.endsWith("-release"));
            if (a != null) {
                fileList.addAll(Arrays.asList(a));
            }
        }
        File fileVersion = new File("/proc/version");
        if (fileVersion.exists()) {
            fileList.add(fileVersion);
        }
        String disId = null;
        String disName = null;
        String disVersion = null;
        File linuxOsrelease = new File("/proc/sys/kernel/osrelease");
        StringBuilder osVersion = new StringBuilder();
        if (linuxOsrelease.isFile()) {
            BufferedReader myReader = null;
            String strLine = null;
            try {
                try {
                    myReader = new BufferedReader(new FileReader(linuxOsrelease));
                    while ((strLine = myReader.readLine()) != null) {
                        osVersion.append(strLine).append("\n");
                    }
                } finally {
                    if (myReader != null) {
                        myReader.close();
                    }
                }
            } catch (IOException e) {
                //ignore
            }
        }
        if (osVersion.toString().trim().isEmpty()) {
            CoreStringUtils.clear(osVersion);
            try {
                osVersion.append(
                        new ProcessBuilder2().setCommand("uname", "-r")
                                .setRedirectErrorStream(true)
                                .grabOutputString()
                                .setSleepMillis(50)
                                .waitFor().getOutputString()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//prints all the version-related files
        for (File f : fileList) {
            try {
                try (BufferedReader myReader = new BufferedReader(new FileReader(f))) {
                    String strLine = null;
                    while ((strLine = myReader.readLine()) != null) {
                        strLine = strLine.trim();
                        if (!strLine.startsWith("#") && strLine.contains("=")) {
                            int i = strLine.indexOf('=');
                            String n = strLine.substring(0, i);
                            String v = strLine.substring(i + 1);
                            switch (n) {
                                case "ID":
                                    if (v.startsWith("\"")) {
                                        v = v.substring(1, v.length() - 1);
                                    }
                                    disId = v;
                                    break;
                                case "VERSION_ID":
                                    if (v.startsWith("\"")) {
                                        v = v.substring(1, v.length() - 1);
                                    }
                                    disVersion = v;
                                    break;
                                case "PRETTY_NAME":
                                    if (v.startsWith("\"")) {
                                        v = v.substring(1, v.length() - 1);
                                    }
                                    disName = v;
                                    break;
                                case "DISTRIB_ID":
                                    if (v.startsWith("\"")) {
                                        v = v.substring(1, v.length() - 1);
                                    }
                                    disName = v;
                                    break;
                                case "DISTRIB_RELEASE":
                                    if (v.startsWith("\"")) {
                                        v = v.substring(1, v.length() - 1);
                                    }
                                    disVersion = v;
                                    break;
                            }
                            if (!CoreStringUtils.isBlank(disVersion) && !CoreStringUtils.isBlank(disName) && !CoreStringUtils.isBlank(disId)) {
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.printf("Error: %s%n", e.getMessage());
            }
        }
        Map<String, String> m = new HashMap<>();
        m.put("distId", disId);
        m.put("distName", disName);
        m.put("distVersion", disVersion);
        m.put("osVersion", osVersion.toString().trim());
        return m;
    }

    public static String getPlatformOsDist() {
        String osInfo = getPlatformOs();
        if (osInfo.startsWith("linux")) {
            Map<String, String> m = getOsDistMap();
            String distId = m.get("distId");
            String distVersion = m.get("distVersion");
            if (!CoreStringUtils.isBlank(distId)) {
                if (!CoreStringUtils.isBlank(distId)) {
                    return distId + "#" + distVersion;
                } else {
                    return distId;
                }
            }
        }
        return null;
    }

    /**
     * https://en.wikipedia.org/wiki/List_of_Microsoft_Windows_versions
     *
     * @return
     */
    public static String getPlatformOs() {
        String property = System.getProperty("os.name").toLowerCase();
        if (property.startsWith("linux")) {
            Map<String, String> m = getOsDistMap();

            String v = m.get("osVersion");
            if (CoreStringUtils.isBlank(v)) {
                return "linux";
            }
            return "linux#" + v;
        }
        if (property.startsWith("win")) {
            if (property.startsWith("windows 10")) {
                return "windows#10";
            }
            if (property.startsWith("windows 8.1")) {
                return "windows#6.3";
            }
            if (property.startsWith("windows 8")) {
                return "windows#6.2";
            }
            if (property.startsWith("windows 7")) {
                return "windows#6.1";
            }
            if (property.startsWith("windows vista")) {
                return "windows#6";
            }
            if (property.startsWith("windows xp pro")) {
                return "windows#5.2";
            }
            if (property.startsWith("windows xp")) {
                return "windows#5.1";
            }
            return "windows";
        }
        if (property.startsWith("mac")) {
            if (property.startsWith("mac os x")) {
                return "mac#10";
            }
            return "mac";
        }
        if (property.startsWith("sunos")) {
            Map<String, String> m = getOsDistMap();

            String v = m.get("osVersion");
            if (CoreStringUtils.isBlank(v)) {
                return "sunos";
            }
            return "sunos#" + v;
        }
        if (property.startsWith("freebsd")) {
            Map<String, String> m = getOsDistMap();

            String v = m.get("osVersion");
            if (CoreStringUtils.isBlank(v)) {
                return "freebsd";
            }
            return "freebsd#" + v;
        }
        return "unknown";
//        return property;
    }

    public static boolean checkSupportedArch(String arch) {
        if (CoreStringUtils.isBlank(arch)) {
            return true;
        }
        if (SUPPORTED_ARCH.contains(arch)) {
            return true;
        }
        throw new NutsIllegalArgumentException("Unsupported Architecture " + arch + " please do use one of " + SUPPORTED_ARCH);
    }

    public static boolean checkSupportedOs(String os) {
        if (CoreStringUtils.isBlank(os)) {
            return true;
        }
        if (SUPPORTED_OS.contains(os)) {
            return true;
        }
        throw new NutsIllegalArgumentException("Unsupported Operating System " + os + " please do use one of " + SUPPORTED_OS);
    }

    public static String getPlatformArch() {
        String property = System.getProperty("os.arch");
        String aliased = SUPPORTED_ARCH_ALIASES.get(property);
        return (aliased == null) ? property : aliased;
    }

    public static PlatformBeanProperty[] findPlatformBeanProperties(Class platformType) {
        LinkedHashMap<String, PlatformBeanProperty> visited = new LinkedHashMap<>();
        Class curr = platformType;
        while (curr != null && !curr.equals(Object.class)) {
            for (Method method : curr.getDeclaredMethods()) {
                String n = method.getName();
                String field = null;
                if (method.getParameterTypes().length == 0 && method.getReturnType().equals(Boolean.TYPE) && n.startsWith("is") && n.length() > 2 && Character.isUpperCase(n.charAt(2))) {
                    field = n.substring(2);
                } else if (method.getParameterTypes().length == 0 && !method.getReturnType().equals(Boolean.TYPE) && n.startsWith("get") && n.length() > 3 && Character.isUpperCase(n.charAt(3))) {
                    field = n.substring(3);
                } else if (method.getParameterTypes().length == 1 && method.getReturnType().equals(Void.TYPE) && n.startsWith("set") && n.length() > 3 && Character.isUpperCase(n.charAt(3))) {
                    field = n.substring(3);
                }
                if (field != null) {
                    char[] chars = field.toCharArray();
                    chars[0] = Character.toLowerCase(chars[0]);
                    field = new String(chars);
                    if (!visited.containsKey(field)) {
                        PlatformBeanProperty platformBeanProperty = findPlatformBeanProperty(field, platformType);
                        if (platformBeanProperty != null) {
                            visited.put(field, platformBeanProperty);
                        }
                    }
                }
            }
            curr = curr.getSuperclass();
        }
        return visited.values().toArray(new PlatformBeanProperty[0]);
    }

    public static PlatformBeanProperty findPlatformBeanProperty(String field, Class platformType) {
        String ckey = platformType.getName() + "." + field;
        PlatformBeanProperty old = cachedPlatformBeanProperties.get(ckey);
        if (old == null) {
            Field jfield = null;
            try {
                jfield = platformType.getDeclaredField(field);
            } catch (NoSuchFieldException e) {
                //ignore
            }
            String g1 = CoreCommonUtils.getterName(field, Object.class);
            String g2 = CoreCommonUtils.getterName(field, Boolean.TYPE);
            String s = CoreCommonUtils.setterName(field);
            Class<?> x = platformType;
            Method getter = null;
            Method setter = null;
            Class propertyType = null;
            LinkedHashMap<Class, Method> setters = new LinkedHashMap<>();
            while (x != null) {
                for (Method m : x.getDeclaredMethods()) {
                    if (!Modifier.isStatic(m.getModifiers())) {
                        String mn = m.getName();
                        if (getter == null) {
                            if (g1.equals(mn) || g2.equals(mn)) {
                                if (m.getParameterTypes().length == 0 && !Void.TYPE.equals(m.getReturnType())) {
                                    getter = m;
                                    Class<?> ftype = getter.getReturnType();
                                    for (Class key : new HashSet<>(setters.keySet())) {
                                        if (!key.equals(ftype)) {
                                            setters.remove(key);
                                        }
                                    }
                                    if (setter == null) {
                                        setter = setters.get(ftype);
                                    }
                                }
                            }
                        }
                        if (setter == null) {
                            if (s.equals(mn)) {
                                if (m.getParameterTypes().length == 1) {
                                    Class<?> stype = m.getParameterTypes()[0];
                                    if (getter != null) {
                                        Class<?> gtype = getter.getReturnType();
                                        if (gtype.equals(stype)) {
                                            if (!setters.containsKey(stype)) {
                                                setters.put(stype, m);
                                            }
                                            if (setter == null) {
                                                setter = m;
                                            }
                                        }
                                    } else {
                                        if (!setters.containsKey(stype)) {
                                            setters.put(stype, m);
                                        }
                                    }
                                }
                            }
                        }
                        if (getter != null && setter != null) {
                            break;
                        }
                    }
                }
                if (getter != null && setter != null) {
                    break;
                }
                x = x.getSuperclass();
            }
            if (getter != null) {
                propertyType = getter.getReturnType();
            }
            if (getter == null && setter == null && setters.size() > 0) {
                Method[] settersArray = setters.values().toArray(new Method[0]);
                setter = settersArray[0];
                if (settersArray.length > 1) {
                    //TODO log?
                }
            }
            if (getter == null && setter != null && propertyType == null) {
                propertyType = setter.getParameterTypes()[0];
            }
            if (getter != null || setter != null) {
                old = new DefaultPlatformBeanProperty(field, propertyType, jfield, getter, setter);
            } else {
                old = null;
            }
            cachedPlatformBeanProperties.put(ckey, old);
        }
        return old;
    }

    public static Boolean getExecutableJar(File file) {
        if (file == null || !file.isFile()) {
            return null;
        }
        return resolveMainClass(file) != null;
    }

    public static boolean isExecutableJar(File file) {
        return file.getName().toLowerCase().endsWith(".jar") && resolveMainClass(file) != null;
    }

    public static String[] getMainClassAndLibs(File jarFile, boolean foreComponentNames) throws IOException {
        String main = null;
        List<String> clsAndLibs = new ArrayList<>();
        JarFile jarfile = new JarFile(jarFile);
        Manifest manifest = jarfile.getManifest();
        Attributes attrs = manifest.getMainAttributes();

        for (Object o : attrs.keySet()) {
            Attributes.Name attrName = (Attributes.Name) o;
            if ("Main-Class".equals(attrName.toString())) {
                main = attrs.getValue(attrName);
            } else if ("Class-Path".equals(attrName.toString())) {
                for (String s : attrs.getValue(attrName).split(" ")) {
                    if (foreComponentNames) {
                        if (s.indexOf('/') >= 0) {
                            s = s.substring(s.lastIndexOf("/") + 1);
                        }
                        if (s.toLowerCase().endsWith(".jar")) {
                            s = s.substring(0, s.length() - 4);
                        }
                        clsAndLibs.add(s);
                    } else {
                        clsAndLibs.add(s);
                    }
                }
            }
        }
        clsAndLibs.add(main);
        return clsAndLibs.toArray(new String[0]);
    }

    public static boolean isLoadedClassPath(File file, ClassLoader classLoader, NutsSessionTerminal terminal) {
        try {
            if (file != null) {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(file);
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();

                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        String zname = zipEntry.getName();
                        if (!zname.endsWith("/") && zname.endsWith(".class")) {
                            String clz = zname.substring(0, zname.length() - 6).replace('/', '.');
                            try {
                                Class<?> aClass = (classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader).loadClass(clz);
                                if (terminal != null) {
                                    terminal.out().printf("Loaded %s from %s%n", aClass, file);
                                }
                                return true;
                            } catch (ClassNotFoundException e) {
                                return false;
                            }
                        }
                    }
                } finally {
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException e) {
                            //ignorereturn false;
                        }
                    }
                }

            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public static RuntimeException toRuntimeException(Throwable ex) {
        if (ex instanceof RuntimeException) {
            return (RuntimeException) ex;
        }
        return new NutsException(ex);
    }

    public static NutsException toNutsException(Throwable ex) {
        if (ex instanceof NutsException) {
            return (NutsException) ex;
        }
        return new NutsException(ex);
    }

    public static <T> T runWithinLoader(Callable<T> callable, ClassLoader loader) {
        Ref<T> ref = new Ref<>();
        Thread thread = new Thread(() -> {
            try {
                ref.set(callable.call());
            } catch (NutsException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new NutsException(ex);
            }
        }, "RunWithinLoader");
        thread.setContextClassLoader(loader);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ex) {
            throw new NutsException(ex);
        }
        return ref.get();
    }

    public static String resolveMainClass(File file) {
        if (file == null || !file.isFile()) {
            return null;
        }
        try {
            try (JarFile f = new JarFile(file)) {
                Manifest manifest = f.getManifest();
                if (manifest == null) {
                    return null;
                }
                String mainClass = manifest.getMainAttributes().getValue("Main-Class");
                return !CoreStringUtils.isBlank(mainClass) ? mainClass : null;
            }
        } catch (Exception ex) {
            //invalid file
            return null;
        }
    }

    public static NutsExecutionEntry parseClassExecutionEntry(InputStream classStream, String sourceName) {
        MainClassType mainClass = null;
        try {
            mainClass = getMainClassType(classStream);
        } catch (Exception ex) {
            log.log(java.util.logging.Level.SEVERE, "Invalid file format {0}", sourceName);
            log.log(java.util.logging.Level.FINER, "Invalid file format " + sourceName, ex);
        }
        if (mainClass != null) {
            return new NutsExecutionEntry(
                    mainClass.getName(),
                    false,
                    mainClass.isApp() && mainClass.isMain()
            );
        }
        return null;
    }

    public static NutsExecutionEntry[] parseJarExecutionEntries(InputStream jarStream, String sourceName) {
        if(!(jarStream instanceof BufferedInputStream)){
           jarStream=new BufferedInputStream(jarStream);
        }
        final List<NutsExecutionEntry> classes = new ArrayList<>();
        final List<String> manifiestClass = new ArrayList<>();
        try {
            ZipUtils.visitZipStream(jarStream, new Predicate<String>() {
                @Override
                public boolean test(String path) {
                    return path.endsWith(".class")
                            || path.equals("META-INF/MANIFEST.MF");
                }
            }, new InputStreamVisitor() {
                @Override
                public boolean visit(String path, InputStream inputStream) throws IOException {
                    if (path.endsWith(".class")) {
                        NutsExecutionEntry mainClass = parseClassExecutionEntry(inputStream, path);
                        if (mainClass != null) {
                            classes.add(mainClass);
                        }
                    } else {
                        try (BufferedReader b = new BufferedReader(new InputStreamReader(inputStream))) {
                            String line = null;
                            while ((line = b.readLine()) != null) {
                                if (line.startsWith("Main-Class:")) {
                                    String c = line.substring("Main-Class:".length()).trim();
                                    if (c.length() > 0) {
                                        manifiestClass.add(c);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    return true;
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        List<NutsExecutionEntry> entries = new ArrayList<>();
        String defaultEntry = null;
        if (manifiestClass.size() > 0) {
            defaultEntry = manifiestClass.get(0);
        }
        for (NutsExecutionEntry entry : classes) {
            if (defaultEntry != null && defaultEntry.equals(entry.getName())) {
                entries.add(new NutsExecutionEntry(entry.getName(), true, entry.isApp()));
            } else {
                entries.add(entry);
            }
        }
        return entries.toArray(new NutsExecutionEntry[0]);
    }

    public static class MainClassType {

        private String name;
        private boolean app;
        private boolean main;

        public MainClassType(String name, boolean main, boolean app) {
            this.name = name;
            this.app = app;
            this.main = main;
        }

        public String getName() {
            return name;
        }

        public boolean isApp() {
            return app;
        }

        public boolean isMain() {
            return main;
        }

    }

    /**
     * @param stream
     * @return
     * @throws IOException
     */
    public static MainClassType getMainClassType(InputStream stream) throws IOException {
        final Ref<Boolean> mainClass = new Ref<>();
        final Ref<Boolean> nutsApp = new Ref<>();
        final Ref<String> className = new Ref<>();
        SimpleClassStream.Visitor cl = new SimpleClassStream.Visitor() {
            String lastClass = null;

            @Override
            public void visitMethod(int access, String name, String desc) {
//                System.out.println("\t::: visit method "+name);
                if (name.equals("main") && desc.equals("([Ljava/lang/String;)V")
                        && Modifier.isPublic(access)
                        && Modifier.isStatic(access)) {
                    mainClass.set(true);
                }
            }

            @Override
            public void visitClassDeclaration(int access, String name, String superName, String[] interfaces) {
//                System.out.println("::: visit class "+name);
                if (superName != null && superName.equals("net/vpc/app/nuts/app/NutsApplication")) {
                    nutsApp.set(true);
                }
                className.set(name.replace('/', '.'));
            }
        };
        SimpleClassStream classReader = new SimpleClassStream(new BufferedInputStream(stream), cl);
        if (mainClass.isSet() || nutsApp.isSet()) {
            return new MainClassType(className.get(), mainClass.isSet(), nutsApp.isSet());
        }
        return null;
    }

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
    public static String getPlatformOsFamily() {
        String property = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (property.startsWith("linux")) {
            return "linux";
        }
        if (property.startsWith("win")) {
            return "windows";
        }
        if (property.startsWith("mac")) {
            return "mac";
        }
        if (property.startsWith("sunos")) {
            return "unix";
        }
        if (property.startsWith("freebsd")) {
            return "unix";
        }
        return "unknown";
    }

}
