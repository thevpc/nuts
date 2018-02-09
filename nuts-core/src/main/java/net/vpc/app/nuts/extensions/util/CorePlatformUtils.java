/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.NutsIllegalArgumentsException;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsIOException;

/**
 * Created by vpc on 5/16/17.
 */
public class CorePlatformUtils {

    public static final Map<String, String> SUPPORTED_ARCH_ALIASES = new HashMap<>();
    private static final Set<String> SUPPORTED_ARCH = new HashSet<>(Arrays.asList("x86", "ia64", "amd64", "ppc", "sparc"));
    private static final Set<String> SUPPORTED_OS = new HashSet<>(Arrays.asList("linux", "windows", "mac", "sunos", "freebsd"));
    private static Map<String, String> LOADED_OS_DIST_MAP = null;

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

    /**
     * this is inspired from
     * http://stackoverflow.com/questions/15018474/getting-linux-distro-from-java
     * so thanks //PbxMan//
     *
     * @return
     */
    public static Map<String, String> getOsDistMapLinux() {
        File dir = CoreIOUtils.createFileByCwd("/etc/", null);
        List<File> fileList = new ArrayList<>();
        if (dir.exists()) {
            File[] a = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.endsWith("-release");
                }
            });
            if (a != null) {
                fileList.addAll(Arrays.asList(a));
            }
        }
        File fileVersion = CoreIOUtils.createFileByCwd("/proc/version", null);
        if (fileVersion.exists()) {
            fileList.add(fileVersion);
        }
        String disId = null;
        String disName = null;
        String disVersion = null;
        File linuxOsrelease = CoreIOUtils.createFileByCwd("/proc/sys/kernel/osrelease", null);
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
                CoreIOUtils.execAndEcho(new String[]{"uname", "-r"}, null, null, osVersion, null, 50);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//prints all the version-related files
        for (File f : fileList) {
            try {
                BufferedReader myReader = new BufferedReader(new FileReader(f));
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
                        if (!CoreStringUtils.isEmpty(disVersion) && !CoreStringUtils.isEmpty(disName) && !CoreStringUtils.isEmpty(disId)) {
                            break;
                        }
//                        System.out.println(f.getName() + " : " + strLine);
                    }
                }
                myReader.close();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        Map<String, String> m = new HashMap<>();
        m.put("distId", disId);
        m.put("distName", disName);
        m.put("distVersion", disVersion);
        m.put("osVersion", osVersion.toString().trim());
        return m;
    }

    public static String getOsdist() {
        String osInfo = getOs();
        if (osInfo.startsWith("linux")) {
            Map<String, String> m = getOsDistMap();
            String distId = m.get("distId");
            String distVersion = m.get("distVersion");
            if (!CoreStringUtils.isEmpty(distId)) {
                if (!CoreStringUtils.isEmpty(distId)) {
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
    public static String getOs() {
        String property = System.getProperty("os.name").toLowerCase();
        if (property.startsWith("linux")) {
            Map<String, String> m = getOsDistMap();

            String v = m.get("osVersion");
            if (CoreStringUtils.isEmpty(v)) {
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
            if (CoreStringUtils.isEmpty(v)) {
                return "sunos";
            }
            return "sunos#" + v;
        }
        if (property.startsWith("freebsd")) {
            Map<String, String> m = getOsDistMap();

            String v = m.get("osVersion");
            if (CoreStringUtils.isEmpty(v)) {
                return "freebsd";
            }
            return "freebsd#" + v;
        }
        return property;
    }

    public static boolean checkSupportedArch(String arch) {
        if (CoreStringUtils.isEmpty(arch)) {
            return true;
        }
        if (SUPPORTED_ARCH.contains(arch)) {
            return true;
        }
        throw new NutsIllegalArgumentsException("Unsupported Architecture " + arch + " please do use one of " + SUPPORTED_ARCH);
    }

    public static boolean checkSupportedOs(String os) {
        if (CoreStringUtils.isEmpty(os)) {
            return true;
        }
        if (SUPPORTED_OS.contains(os)) {
            return true;
        }
        throw new NutsIllegalArgumentsException("Unsupported Operating System " + os + " please do use one of " + SUPPORTED_OS);
    }

    public static String getArch() {
        String property = System.getProperty("os.arch");
        String aliased = SUPPORTED_ARCH_ALIASES.get(property);
        return (aliased == null) ? property : aliased;
    }

    public static String[] subArray(String[] source, int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            beginIndex = 0;
        }
        if (endIndex > source.length) {
            beginIndex = 0;
        }
        if (beginIndex >= endIndex) {
            return new String[0];
        }
        String[] arr = new String[endIndex - beginIndex];
        System.arraycopy(source, beginIndex, arr, 0, endIndex - beginIndex);
        return arr;
    }

    public static File resolveLocalFileFromResource(Class cls, String url) throws MalformedURLException {
        return resolveLocalFileFromURL(resolveURLFromResource(cls, url));
    }

    public static File resolveLocalFileFromURL(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            return new File(url.getPath());
        }
    }

    public static URL resolveURLFromResource(Class cls, String urlPath) throws MalformedURLException {
        if (!urlPath.startsWith("/")) {
            throw new NutsIllegalArgumentsException("Unable to resolve url from " + urlPath);
        }
        URL url = cls.getResource(urlPath);
        String urlFile = url.getFile();
        int separatorIndex = urlFile.indexOf("!/");
        if (separatorIndex != -1) {
            String jarFile = urlFile.substring(0, separatorIndex);
            try {
                return new URL(jarFile);
            } catch (MalformedURLException ex) {
                // Probably no protocol in original jar URL, like "jar:C:/mypath/myjar.jar".
                // This usually indicates that the jar file resides in the file system.
                if (!jarFile.startsWith("/")) {
                    jarFile = "/" + jarFile;
                }
                return new URL("file:" + jarFile);
            }
        } else {
            String encoded = encodePath(urlPath);
            String url_tostring = url.toString();
            if (url_tostring.endsWith(encoded)) {
                return new URL(url_tostring.substring(0, url_tostring.length() - encoded.length()));
            }
            throw new NutsIllegalArgumentsException("Unable to resolve url from " + urlPath);
        }
    }

    private static String encodePath(String path) {
        StringTokenizer st = new StringTokenizer(path, "/", true);
        StringBuilder encoded = new StringBuilder();
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (t.equals("/")) {
                encoded.append(t);
            } else {
                try {
                    encoded.append(URLEncoder.encode(t, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    throw new NutsIllegalArgumentsException("Unable to encode " + t, ex);
                }
            }
        }
        return encoded.toString();
    }

    public static <K, V> Map<K, V> mergeMaps(Map<K, V> source, Map<K, V> dest) {
        if (dest == null) {
            dest = new HashMap<>();
        }
        if (source != null) {
            for (Map.Entry<K, V> e : source.entrySet()) {
                if (e.getValue() != null) {
                    dest.put(e.getKey(), e.getValue());
                } else {
                    dest.remove(e.getKey());
                }
            }
        }
        return dest;
    }

    public static String getterName(String name, Class type) {
        if (Boolean.TYPE.equals(type)) {
            return "is" + suffix(name);
        }
        return "get" + suffix(name);
    }

    public static String setterName(String name) {
        //Class<?> type = field.getDataType();
        return "set" + suffix(name);
    }

    private static String suffix(String s) {
        char[] chars = s.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
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
        return visited.values().toArray(new PlatformBeanProperty[visited.size()]);
    }

    public static PlatformBeanProperty findPlatformBeanProperty(String field, Class platformType) {
        Field jfield = null;
        try {
            jfield = platformType.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            //ignore
        }
        String g1 = getterName(field, Object.class);
        String g2 = getterName(field, Boolean.TYPE);
        String s = setterName(field);
        Class<?> x = platformType;
        Method getter = null;
        Method setter = null;
        Class propertyType = null;
        LinkedHashMap<Class, Method> setters = new LinkedHashMap<Class, Method>();
        while (x != null) {
            for (Method m : x.getDeclaredMethods()) {
                if (!Modifier.isStatic(m.getModifiers())) {
                    String mn = m.getName();
                    if (getter == null) {
                        if (g1.equals(mn) || g2.equals(mn)) {
                            if (m.getParameterTypes().length == 0 && !Void.TYPE.equals(m.getReturnType())) {
                                getter = m;
                                Class<?> ftype = getter.getReturnType();
                                for (Class key : new HashSet<Class>(setters.keySet())) {
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
            Method[] settersArray = setters.values().toArray(new Method[setters.size()]);
            setter = settersArray[0];
            if (settersArray.length > 1) {
                //TODO log?
            }
        }
        if (getter == null && setter != null && propertyType == null) {
            propertyType = setter.getParameterTypes()[0];
        }
        if (getter != null || setter != null) {
            return new DefaultPlatformBeanProperty(field, propertyType, jfield, getter, setter);
        }
        return null;
    }

    public static Boolean getExecutableJar(File file) {
        if (file == null || !file.isFile()) {
            return null;
        }
        return getMainClass(file) != null;
    }

    public static boolean isExecutableJar(File file) {
        return file.getName().toLowerCase().endsWith(".jar") && getMainClass(file) != null;
    }

    public static String getMainClass(File file) {
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
//            if(!CoreStringUtils.isEmpty(mainClass)) {
//                System.out.println(">> " + mainClass + " : " + file);
//            }
                return !CoreStringUtils.isEmpty(mainClass) ? mainClass : null;
            }
        } catch (Exception ex) {
            //invalid file
            return null;
        }
    }

    public static List<Class> loadServiceClasses(Class service, ClassLoader classLoader) {
        String fullName = "META-INF/services/" + service.getName();
        Enumeration<URL> configs;
        LinkedHashSet<String> names = new LinkedHashSet<>();
        try {
            if (classLoader == null) {
                configs = ClassLoader.getSystemResources(fullName);
            } else {
                configs = classLoader.getResources(fullName);
            }
        } catch (IOException ex) {
            throw new NutsIOException(ex);
        }
        while (configs.hasMoreElements()) {
            names.addAll(loadServiceClasses(service, configs.nextElement()));
        }
        List<Class> classes = new ArrayList<>();
        for (String n : names) {
            Class<?> c = null;
            try {
                c = Class.forName(n, false, classLoader);
            } catch (ClassNotFoundException x) {
                throw new NutsException(x);
            }
            if (!service.isAssignableFrom(c)) {
                throw new NutsException("Not a valid type " + c + " <> " + service);
            }
            classes.add(c);
        }
        return classes;
    }

    public static List<String> loadServiceClasses(Class<?> service, URL u)
            throws ServiceConfigurationError {
        InputStream in = null;
        BufferedReader r = null;
        List<String> names = new ArrayList<>();
        try {
            in = u.openStream();
            r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            int lc = 1;
            String line = null;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && line.charAt(0) != '#') {
                    names.add(line);
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(ex);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex2) {
                throw new NutsIOException(ex2);
            }
        }
        return names;
    }

    public static boolean isLoadedClassPath(File file, ClassLoader classLoader) {
//    private boolean isLoadedClassPath(NutsFile nutsFile) {
//        if (file.getId().isSameFullName(NutsId.parseOrErrorNutsId(NutsConstants.NUTS_COMPONENT_ID))) {
//            return true;
//        }
        try {
//            File file = nutsFile.getFile();
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
                                System.out.println("Loaded " + aClass + " from " + file);
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
}
