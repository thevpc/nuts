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
 * <p>
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
package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.optional.jansi.OptionalJansi;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootModel;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by vpc on 5/16/17.
 */
public class CorePlatformUtils {

    public static final boolean SUPPORTS_UTF_ENCODING;
//    private static final Set<String> SUPPORTED_ARCH = new HashSet<>(Arrays.asList("x86_32", "x86_64", "itanium_32", "itanium_64"
//            , "sparc_32", "sparc_64", "arm_32", "aarch_64", "mips_32", "mipsel_32", "mips_64", "mipsel_64"
//            , "ppc_32", "ppcle_32", "ppc_64", "ppcle_64", "s390_32", "s390_64"
//    ));
//    private static final Set<String> SUPPORTED_OS = new HashSet<>(Arrays.asList("linux", "windows", "macos", "sunos"
//            , "freebsd", "openbsd", "netbsd", "aix", "hpux", "as400", "zos", "unknown"
//    ));
    public static boolean IS_CYGWIN =
            System.getenv("PWD") != null
                    && System.getenv("PWD").startsWith("/")
                    && !"cygwin".equals(System.getenv("TERM"));
    public static boolean IS_MINGW_XTERM =
            System.getenv("MSYSTEM") != null
                    && System.getenv("MSYSTEM").startsWith("MINGW")
                    && "xterm".equals(System.getenv("TERM"));
    private static Map<String, String> LOADED_OS_DIST_MAP = null;

    static {
//        SUPPORTED_ARCH_ALIASES.put("i386", "x86");
        boolean _e = new String("ø".getBytes()).equals("ø");
        if (_e) {
            switch (NutsOsFamily.getCurrent()) {
                case LINUX:
                case MACOS: {
                    //okkay
                    break;
                }
                case WINDOWS: {
                    if (CorePlatformUtils.IS_CYGWIN || CorePlatformUtils.IS_MINGW_XTERM) {
                        //okkay
                    } else {
                        _e = false;
                    }
                }
                case UNIX:
                case UNKNOWN:
                default: {
                    _e = false;
                }
            }
        }
        SUPPORTS_UTF_ENCODING = _e;
    }

    private static String buildUnixOsNameAndVersion(String name, NutsSession session) {
        Map<String, String> m = getOsDistMap(session);
        String v = m.get("osVersion");
        if (NutsBlankable.isBlank(v)) {
            return name;
        }
        return name + "#" + v;
    }

    public static Map<String, String> getOsDistMap(NutsSession session) {
        String property = System.getProperty("os.name").toLowerCase();
        if (property.startsWith("linux")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux(session);
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        if (property.startsWith("mac")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux(session);
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        if (property.startsWith("sunos")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux(session);
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        if (property.startsWith("freebsd")) {
            if (LOADED_OS_DIST_MAP == null) {
                LOADED_OS_DIST_MAP = getOsDistMapLinux(session);
            }
            return Collections.unmodifiableMap(LOADED_OS_DIST_MAP);
        }
        return new HashMap<>();
    }


    /**
     * this is inspired from
     * http://stackoverflow.com/questions/15018474/getting-linux-distro-from-java
     * thanks to //PbxMan//
     *
     * @param session session
     * @return os distribution map including keys distId, distName, distVersion,osVersion
     */
    public static Map<String, String> getOsDistMapLinux(NutsSession session) {
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
                        session.exec().setExecutionType(NutsExecutionType.SYSTEM)
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
                                case "DISTRIB_ID":
                                    if (v.startsWith("\"")) {
                                        v = v.substring(1, v.length() - 1);
                                    }
                                    disId = v;
                                    break;
                                case "VERSION_ID":
                                case "DISTRIB_RELEASE":
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
                            }
                            if (!NutsBlankable.isBlank(disVersion) && !NutsBlankable.isBlank(disName) && !NutsBlankable.isBlank(disId)) {
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.printf("error: %s%n", CoreStringUtils.exceptionToMessage(e));
            }
        }
        Map<String, String> m = new HashMap<>();
        m.put("distId", disId);
        m.put("distName", disName);
        m.put("distVersion", disVersion);
        m.put("osVersion", osVersion.toString().trim());
        return m;
    }

    public static String getPlatformOsDist(NutsSession session) {
        String osInfo = getPlatformOs(session);
        if (osInfo.startsWith("linux")) {
            Map<String, String> m = getOsDistMap(session);
            String distId = m.get("distId");
            String distVersion = m.get("distVersion");
            if (!NutsBlankable.isBlank(distId)) {
                if (!NutsBlankable.isBlank(distId)) {
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
     * @param ws workspace
     * @return platform os name
     */
    public static String getPlatformOs(NutsSession ws) {
        String property = System.getProperty("os.name").toLowerCase();
        if (property.startsWith("linux")) {
            return buildUnixOsNameAndVersion("linux", ws);
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
            return buildUnixOsNameAndVersion("macos", ws);
        }
        if (property.startsWith("sunos") || property.startsWith("solaris")) {
            return buildUnixOsNameAndVersion("sunos", ws);
        }
        if (property.startsWith("zos")) {
            return buildUnixOsNameAndVersion("zos", ws);
        }
        if (property.startsWith("freebsd")) {
            return buildUnixOsNameAndVersion("freebsd", ws);
        }
        if (property.startsWith("openbsd")) {
            return buildUnixOsNameAndVersion("openbsd", ws);
        }
        if (property.startsWith("netbsd")) {
            return buildUnixOsNameAndVersion("netbsd", ws);
        }
        if (property.startsWith("aix")) {
            return buildUnixOsNameAndVersion("aix", ws);
        }
        if (property.startsWith("hpux")) {
            return buildUnixOsNameAndVersion("hpux", ws);
        }
        if (property.startsWith("os400") && property.length() <= 5 || !Character.isDigit(property.charAt(5))) {
            return buildUnixOsNameAndVersion("os400", ws);
        }
        return "unknown";
//        return property;
    }


    public static boolean checkAcceptCondition(NutsEnvCondition condition, boolean currentVM, NutsSession session) {
        if(!CoreFilterUtils.acceptCondition(condition,currentVM,session)){
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported environment"));
        }
        return true;
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
                        if (!zname.endsWith("/") && zname.endsWith(".class") && !zname.contains("$")) {
                            String clz = zname.substring(0, zname.length() - 6).replace('/', '.');
                            try {
                                Class<?> aClass = (classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader).loadClass(clz);
                                if (terminal != null) {
                                    terminal.out().printf("loaded %s from %s%n", aClass, file);
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
                            //ignore;
                        }
                    }
                }

            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public static <T> T runWithinLoader(Callable<T> callable, ClassLoader loader, NutsSession session) {
        NutsRef<T> ref = new NutsRef<>();
        Thread thread = new Thread(() -> {
            try {
                ref.set(callable.call());
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new NutsException(session, NutsMessage.plain("run with loader failed"), ex);
            }
        }, "RunWithinLoader");
        thread.setContextClassLoader(loader);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ex) {
            throw new NutsException(session, NutsMessage.plain("run with loader failed"), ex);
        }
        return ref.get();
    }

    public static String getPackageName(String cn){
        int i = cn.lastIndexOf('.');
        if (i >= 0) {
            return cn.substring(0, i);
        }
        return "";
    }
    public static String getSimpleClassName(String cn){
        int i = cn.lastIndexOf('.');
        if (i>=0) {
            return cn.substring(i + 1);
        }
        return cn;
    }
}
