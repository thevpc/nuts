/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.util.common;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.util.io.ProcessBuilder2;
import net.thevpc.nuts.runtime.util.io.SimpleClassStream;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by vpc on 5/16/17.
 */
public class CorePlatformUtils {

    //    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(CorePlatformUtils.class.getName());
    //    public static final Map<String, String> SUPPORTED_ARCH_ALIASES = new HashMap<>();
    private static final Set<String> SUPPORTED_ARCH = new HashSet<>(Arrays.asList("x86_32", "x86_64", "itanium_32", "itanium_64"
            , "sparc_32", "sparc_64", "arm_32", "aarch_64", "mips_32", "mipsel_32", "mips_64", "mipsel_64"
            , "ppc_32", "ppcle_32", "ppc_64", "ppcle_64", "s390_32", "s390_64"
    ));
    private static final Set<String> SUPPORTED_OS = new HashSet<>(Arrays.asList("linux", "windows", "macos", "sunos"
            , "freebsd", "openbsd", "netbsd", "aix", "hpux", "as400", "zos", "unknown"
    ));
    private static Map<String, String> LOADED_OS_DIST_MAP = null;
    private static final WeakHashMap<String, PlatformBeanProperty> cachedPlatformBeanProperties = new WeakHashMap<>();

    static {
//        SUPPORTED_ARCH_ALIASES.put("i386", "x86");
    }

    private static String buildUnixOsNameAndVersion(String name,NutsWorkspace ws) {
        Map<String, String> m = getOsDistMap(ws);
        String v = m.get("osVersion");
        if (CoreStringUtils.isBlank(v)) {
            return name;
        }
        return name + "#" + v;
    }

    public static Map<String, String> getOsDistMap(NutsWorkspace ws) {
        String property = System.getProperty("os.name").toLowerCase();
        if (property.startsWith("linux")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux(ws);
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        if (property.startsWith("mac")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux(ws);
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        if (property.startsWith("sunos")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux(ws);
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        if (property.startsWith("freebsd")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux(ws);
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        return new HashMap<>();
    }

    /**
     * this is inspired from
     * http://stackoverflow.com/questions/15018474/getting-linux-distro-from-java
     * so thanks //PbxMan//
     *
     * @return
     */
    public static Map<String, String> getOsDistMapLinux(NutsWorkspace ws) {
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
                        ws.exec().setExecutionType(NutsExecutionType.USER_CMD)
                        .setCommand("uname", "-r")
                                .setRedirectErrorStream(true)
                                .grabOutputString()
                                .setSleepMillis(50)
                                .getOutputString()
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
                System.err.printf("Error: %s%n", CoreStringUtils.exceptionToString(e));
            }
        }
        Map<String, String> m = new HashMap<>();
        m.put("distId", disId);
        m.put("distName", disName);
        m.put("distVersion", disVersion);
        m.put("osVersion", osVersion.toString().trim());
        return m;
    }

    public static String getPlatformOsDist(NutsWorkspace ws) {
        String osInfo = getPlatformOs(ws);
        if (osInfo.startsWith("linux")) {
            Map<String, String> m = getOsDistMap(ws);
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
    public static String getPlatformOs(NutsWorkspace ws) {
        String property = System.getProperty("os.name").toLowerCase();
        if (property.startsWith("linux")) {
            return buildUnixOsNameAndVersion("linux",ws);
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
            if (property.startsWith("mac os x") || property.startsWith("macosx")) {
                return "macos#10";
            }
            return buildUnixOsNameAndVersion("macos",ws);
        }
        if (property.startsWith("sunos") || property.startsWith("solaris")) {
            return buildUnixOsNameAndVersion("sunos",ws);
        }
        if (property.startsWith("zos")) {
            return buildUnixOsNameAndVersion("zos",ws);
        }
        if (property.startsWith("freebsd")) {
            return buildUnixOsNameAndVersion("freebsd",ws);
        }
        if (property.startsWith("openbsd")) {
            return buildUnixOsNameAndVersion("openbsd",ws);
        }
        if (property.startsWith("netbsd")) {
            return buildUnixOsNameAndVersion("netbsd",ws);
        }
        if (property.startsWith("aix")) {
            return buildUnixOsNameAndVersion("aix",ws);
        }
        if (property.startsWith("hpux")) {
            return buildUnixOsNameAndVersion("hpux",ws);
        }
        if (property.startsWith("os400") && property.length() <= 5 || !Character.isDigit(property.charAt(5))) {
            return buildUnixOsNameAndVersion("os400",ws);
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
        throw new NutsIllegalArgumentException(null, "Unsupported Architecture " + arch + " please do use one of " + SUPPORTED_ARCH);
    }

    public static boolean checkSupportedOs(String os) {
        if (CoreStringUtils.isBlank(os)) {
            return true;
        }
        if (SUPPORTED_OS.contains(os)) {
            return true;
        }
        throw new NutsIllegalArgumentException(null, "Unsupported Operating System " + os + " please do use one of " + SUPPORTED_OS);
    }

    /**
     * impl-note: list updated from https://github.com/trustin/os-maven-plugin
     *
     * @return uniform platform architecture
     */
    public static String getPlatformArch() {
        String property = System.getProperty("os.arch").toLowerCase();
        switch (property) {
            case "x8632":
            case "x86":
            case "i386":
            case "i486":
            case "i586":
            case "i686":
            case "ia32":
            case "x32": {
                return "x86_32";
            }
            case "x8664":
            case "amd64":
            case "ia32e":
            case "em64t":
            case "x64": {
                return "x86_64";
            }
            case "ia64n": {
                return "itanium_32";
            }
            case "sparc":
            case "sparc32": {
                return "sparc_32";
            }
            case "sparcv9":
            case "sparc64": {
                return "sparc_64";
            }
            case "arm":
            case "arm32": {
                return "arm_32";
            }
            case "arm64": //merged with aarch64
            case "aarch64": {
                return "aarch_64";
            }
            case "mips":
            case "mips32": {
                return "mips_32";
            }
            case "mipsel":
            case "mips32el": {
                return "mipsel_32";
            }
            case "mips64": {
                return "mips_64";
            }
            case "mips64el": {
                return "mipsel_64";
            }
            case "ppc":
            case "ppc32": {
                return "ppc_32";
            }
            case "ppcle":
            case "ppc32le": {
                return "ppcle_32";
            }
            case "ppc64": {
                return "ppc_64";
            }
            case "ppc64le": {
                return "ppcle_64";
            }
            case "s390": {
                return "s390_32";
            }
            case "s390x": {
                return "s390_64";
            }
            case "ia64w":
            case "itanium64": {
                return "itanium_64";
            }
            default: {
                if (property.startsWith("ia64w") && property.length() == 6) {
                    return "itanium_64";
                }
                //on MacOsX arch=x86_64
                if (SUPPORTED_OS.contains(property)) {
                    return property;
                }
                return "unknown";
            }
        }
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
        return new NutsException(null, ex);
    }

    public static NutsException toNutsException(Throwable ex) {
        if (ex instanceof NutsException) {
            return (NutsException) ex;
        }
        return new NutsException(null, ex);
    }

    public static <T> T runWithinLoader(Callable<T> callable, ClassLoader loader) {
        Ref<T> ref = new Ref<>();
        Thread thread = new Thread(() -> {
            try {
                ref.set(callable.call());
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new NutsException(null, ex);
            }
        }, "RunWithinLoader");
        thread.setContextClassLoader(loader);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ex) {
            throw new NutsException(null, ex);
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
                if (superName != null && superName.equals("net/thevpc/nuts/NutsApplication")) {
                    nutsApp.set(true);

                    //this is nut version < 008.0
                } else if (superName != null && superName.equals("net/vpc/app/nuts/NutsApplication")) {
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

//
//    public static String getPlatformGlobalHomeFolder(NutsStoreLocation location, String workspaceName) {
//        String s = null;
//        String locationName = location.id();
//        NutsOsFamily platformOsFamily = getPlatformOsFamily();
//        s = PrivateNutsUtils.trim(System.getProperty("nuts.home.global." + locationName + "." + platformOsFamily.id()));
//        if (!s.isEmpty()) {
//            return s + "/" + workspaceName;
//        }
//        s = PrivateNutsUtils.trim(System.getProperty("nuts.export.home.global." + locationName + "." + platformOsFamily.id()));
//        if (!s.isEmpty()) {
//            return s.trim() + "/" + workspaceName;
//        }
//        switch (location) {
//            case APPS: {
//                switch (platformOsFamily) {
//                    case LINUX:
//                    case MACOS:
//                    case UNIX:
//                    case UNKNOWN: {
//                        return "/opt/nuts/apps/" + workspaceName;
//                    }
//                    case WINDOWS: {
//                        String pf = System.getenv("ProgramFiles");
//                        if (PrivateNutsUtils.isBlank(pf)) {
//                            pf = "C:\\Program Files";
//                        }
//                        return pf + "\\nuts\\" + PrivateNutsUtils.syspath(workspaceName);
//                    }
//                }
//                break;
//            }
//            case LIB: {
//                switch (platformOsFamily) {
//                    case LINUX:
//                    case MACOS:
//                    case UNIX:
//                    case UNKNOWN: {
//                        return "/opt/nuts/lib/" + workspaceName;
//                    }
//                    case WINDOWS: {
//                        String pf = System.getenv("ProgramFiles");
//                        if (PrivateNutsUtils.isBlank(pf)) {
//                            pf = "C:\\Program Files";
//                        }
//                        return pf + "\\nuts\\" + PrivateNutsUtils.syspath(workspaceName);
//                    }
//                }
//                break;
//            }
//            case CONFIG: {
//                switch (platformOsFamily) {
//                    case LINUX:
//                    case MACOS:
//                    case UNIX:
//                    case UNKNOWN: {
//                        return "/etc/opt/nuts/" + workspaceName;
//                    }
//                    case WINDOWS: {
//                        String pf = System.getenv("ProgramFiles");
//                        if (PrivateNutsUtils.isBlank(pf)) {
//                            pf = "C:\\Program Files";
//                        }
//                        return pf + "\\nuts" + PrivateNutsUtils.syspath(workspaceName);
//                    }
//                }
//                break;
//            }
//            case LOG: {
//                switch (platformOsFamily) {
//                    case LINUX:
//                    case MACOS:
//                    case UNIX:
//                    case UNKNOWN: {
//                        return "/var/log/nuts/" + workspaceName;
//                    }
//                    case WINDOWS: {
//                        String pf = System.getenv("ProgramFiles");
//                        if (PrivateNutsUtils.isBlank(pf)) {
//                            pf = "C:\\Program Files";
//                        }
//                        return pf + "\\nuts" + PrivateNutsUtils.syspath(workspaceName);
//                    }
//                }
//                break;
//            }
//            case CACHE: {
//                switch (platformOsFamily) {
//                    case LINUX:
//                    case MACOS:
//                    case UNIX:
//                    case UNKNOWN: {
//                        return "/var/cache/nuts/" + workspaceName;
//                    }
//                    case WINDOWS: {
//                        String pf = System.getenv("ProgramFiles");
//                        if (PrivateNutsUtils.isBlank(pf)) {
//                            pf = "C:\\Program Files";
//                        }
//                        return pf + "\\nuts" + PrivateNutsUtils.syspath(workspaceName);
//                    }
//                }
//                break;
//            }
//            case VAR: {
//                switch (platformOsFamily) {
//                    case LINUX:
//                    case MACOS:
//                    case UNIX:
//                    case UNKNOWN: {
//                        return "/var/opt/nuts/" + workspaceName;
//                    }
//                    case WINDOWS: {
//                        String pf = System.getenv("ProgramFiles");
//                        if (PrivateNutsUtils.isBlank(pf)) {
//                            pf = "C:\\Program Files";
//                        }
//                        return pf + "\\nuts" + PrivateNutsUtils.syspath(workspaceName);
//                    }
//                }
//                break;
//            }
//            case TEMP: {
//                switch (platformOsFamily) {
//                    case LINUX:
//                    case MACOS:
//                    case UNIX:
//                    case UNKNOWN: {
//                        return "/tmp/nuts/global/" + workspaceName;
//                    }
//                    case WINDOWS: {
//                        String pf = System.getenv("TMP");
//                        if (PrivateNutsUtils.isBlank(pf)) {
//                            pf = "C:\\windows\\TEMP";
//                        }
//                        return pf + "\\nuts" + PrivateNutsUtils.syspath(workspaceName);
//                    }
//                }
//                break;
//            }
//            case RUN: {
//                switch (platformOsFamily) {
//                    case LINUX:
//                    case MACOS:
//                    case UNIX:
//                    case UNKNOWN: {
//                        return "/tmp/run/nuts/global/" + workspaceName;
//                    }
//                    case WINDOWS: {
//                        String pf = System.getenv("TMP");
//                        if (PrivateNutsUtils.isBlank(pf)) {
//                            pf = "C:\\windows\\TEMP";
//                        }
//                        return pf + "\\nuts\\run" + PrivateNutsUtils.syspath(workspaceName);
//                    }
//                }
//                break;
//            }
//        }
//        throw new UnsupportedOperationException();
//    }
//
//    /**
//     * resolves nuts home folder.Home folder is the root for nuts folders.It
//     * depends on folder type and store layout. For instance log folder depends
//     * on on the underlying operating system (linux,windows,...).
//     * Specifications: XDG Base Directory Specification
//     * (https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html)
//     *
//     * @param location folder type to resolve home for
//     * @param platformOsFamily location layout to resolve home for
//     * @param homeLocations workspace home locations
//     * @param global global workspace
//     * @param workspaceName workspace name or id (discriminator)
//     * @return home folder path
//     */
//    public static String getPlatformHomeFolder(
//            NutsOsFamily platformOsFamily,
//            NutsStoreLocation location,
//            Map<String, String> homeLocations,
//            boolean global,
//            String workspaceName) {
//        NutsStoreLocation folderType0 = location;
//        if (location == null) {
//            location = NutsStoreLocation.CONFIG;
//        }
//        if (workspaceName == null || workspaceName.isEmpty()) {
//            workspaceName = NutsConstants.Names.DEFAULT_WORKSPACE_NAME;
//        }
//        boolean wasSystem = false;
//        if (platformOsFamily == null) {
//            platformOsFamily = PrivateNutsPlatformUtils.getPlatformOsFamily();
//        }
//        String s = null;
//        String locationName = location.id();
//        s = PrivateNutsUtils.trim(System.getProperty("nuts.home." + locationName + "." + platformOsFamily.id()));
//        if (!s.isEmpty()) {
//            return s + "/" + workspaceName;
//        }
//        s = PrivateNutsUtils.trim(System.getProperty("nuts.export.home." + locationName + "." + platformOsFamily.id()));
//        if (!s.isEmpty()) {
//            return s.trim() + "/" + workspaceName;
//        }
//        String key = PrivateBootWorkspaceOptions.createHomeLocationKey(platformOsFamily, location);
//        s = homeLocations == null ? "" : PrivateNutsUtils.trim(homeLocations.get(key));
//        if (!s.isEmpty()) {
//            return s.trim() + "/" + workspaceName;
//        }
//        key = PrivateBootWorkspaceOptions.createHomeLocationKey(null, location);
//        s = homeLocations == null ? "" : PrivateNutsUtils.trim(homeLocations.get(key));
//        if (!s.isEmpty()) {
//            return s.trim() + "/" + workspaceName;
//        } else if (global) {
//            return getPlatformGlobalHomeFolder(location, workspaceName);
//        } else {
//            switch (location) {
//                case VAR:
//                case APPS:
//                case LIB: {
//                    switch (platformOsFamily) {
//                        case WINDOWS: {
//                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/Roaming/nuts/" + locationName + "/" + workspaceName);
//                        }
//                        case MACOS:
//                        case LINUX: {
//                            String val = PrivateNutsUtils.trim(System.getenv("XDG_DATA_HOME"));
//                            if (!val.isEmpty()) {
//                                return val + "/nuts/" + locationName + "/" + workspaceName;
//                            }
//                            return System.getProperty("user.home") + "/.local/share/nuts/" + locationName + "/" + workspaceName;
//                        }
//                    }
//                    break;
//                }
//                case LOG: {
//                    switch (platformOsFamily) {
//                        case WINDOWS: {
//                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/LocalLow/nuts/" + locationName + "/" + workspaceName);
//                        }
//                        case MACOS:
//                        case LINUX: {
//                            String val = PrivateNutsUtils.trim(System.getenv("XDG_LOG_HOME"));
//                            if (!val.isEmpty()) {
//                                return val + "/nuts/" + workspaceName;
//                            }
//                            return System.getProperty("user.home") + "/.local/log/nuts" + "/" + workspaceName;
//                        }
//                    }
//                    break;
//                }
//                case RUN: {
//                    switch (platformOsFamily) {
//                        case WINDOWS: {
//                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/Local/nuts/" + locationName + "/" + workspaceName);
//                        }
//                        case MACOS:
//                        case LINUX: {
//                            String val = PrivateNutsUtils.trim(System.getenv("XDG_RUNTIME_DIR"));
//                            if (!val.isEmpty()) {
//                                return val + "/nuts" + "/" + workspaceName;
//                            }
//                            return System.getProperty("user.home") + "/.local/run/nuts" + "/" + workspaceName;
//                        }
//                    }
//                    break;
//                }
//                case CONFIG: {
//                    switch (platformOsFamily) {
//                        case WINDOWS: {
//                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/Roaming/nuts/" + locationName + "/" + workspaceName
//                                    + (folderType0 == null ? "" : "/config")
//                            );
//                        }
//                        case MACOS:
//                        case LINUX: {
//                            String val = PrivateNutsUtils.trim(System.getenv("XDG_CONFIG_HOME"));
//                            if (!val.isEmpty()) {
//                                return val + "/nuts" + "/" + workspaceName + (folderType0 == null ? "" : "/config");
//                            }
//                            return System.getProperty("user.home") + "/.config/nuts" + "/" + workspaceName + (folderType0 == null ? "" : "/config");
//                        }
//                    }
//                    break;
//                }
//                case CACHE: {
//                    switch (platformOsFamily) {
//                        case WINDOWS: {
//                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/Local/nuts/cache/" + workspaceName);
//                        }
//                        case MACOS:
//                        case LINUX: {
//                            String val = PrivateNutsUtils.trim(System.getenv("XDG_CACHE_HOME"));
//                            if (!val.isEmpty()) {
//                                return val + "/nuts" + "/" + workspaceName;
//                            }
//                            return System.getProperty("user.home") + "/.cache/nuts" + "/" + workspaceName;
//                        }
//                    }
//                    break;
//                }
//                case TEMP: {
//                    switch (platformOsFamily) {
//                        case WINDOWS:
//                            return System.getProperty("user.home") + PrivateNutsUtils.syspath("/AppData/Local/nuts/log/" + workspaceName);
//                        case MACOS:
//                        case LINUX:
//                            //on macos/unix/linux temp folder is shared. will add user folder as discriminator
//                            return System.getProperty("java.io.tmpdir") + PrivateNutsUtils.syspath("/" + System.getProperty("user.name") + "/nuts" + "/" + workspaceName);
//                    }
//                }
//            }
//        }
//        throw new NutsIllegalArgumentException(null, "Unsupported " + platformOsFamily + "/" + location + " for " + workspaceName);
//    }

}
