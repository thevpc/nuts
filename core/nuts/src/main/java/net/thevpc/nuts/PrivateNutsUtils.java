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
package net.thevpc.nuts;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/15/17.
 *
 * @since 0.5.4
 * %category Internal
 */
final class PrivateNutsUtils {

    private static final Pattern DOLLAR_PLACE_HOLDER_PATTERN = Pattern.compile("[$][{](?<name>([a-zA-Z]+))[}]");
    public static final boolean NO_M2 = PrivateNutsUtils.getSysBoolNutsProperty("no-m2", false);

    public static boolean isPreferConsole(String[] args){
        try {
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                // non gui mode
                return true;
            }
        }catch (Exception ex){
            return true;
        }
        if(System.console()!=null) {
            return true;
        }
        Map<String, String> getenv = System.getenv();
        if(getenv.get("XDG_SESSION_DESKTOP")!=null){
            return false;
        }
        return false;
    }
    public static boolean isValidWorkspaceName(String workspace) {
        if (isBlank(workspace)) {
            return true;
        }
        String workspaceName = workspace.trim();
        return workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..");
    }

    public static String resolveValidWorkspaceName(String workspace) {
        if (isBlank(workspace)) {
            return NutsConstants.Names.DEFAULT_WORKSPACE_NAME;
        }
        String workspaceName = workspace.trim();
        if (workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..")) {
            return workspaceName;
        } else {
            String p = null;
            try {
                p = new File(workspaceName).getCanonicalFile().getName();
            } catch (IOException ex) {
                p = new File(workspaceName).getAbsoluteFile().getName();
            }
            if (p.isEmpty() || p.equals(".") || p.equals("..")) {
                return "unknown";
            }
            return p;
        }
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static String trim(String str) {
        if (str == null) {
            return "";
        }
        return str.trim();
    }

    public static String idToPath(PrivateNutsId id) {
        return id.getGroupId().replace('.', '/') + "/" +
                id.getArtifactId() + "/" + id.getVersion();
    }

//    public static String idToPath(String str) {
//        int status = 0;
//
//        char[] chars = str.toCharArray();
//        for (int i = 0; i < chars.length; i++) {
//            char c = chars[i];
//            switch (status) {
//                case 0: {
//                    switch (c) {
//                        case ':': {
//                            status = 1;
//                            chars[i] = '/';
//                            break;
//                        }
//                        case '.': {
//                            chars[i] = '/';
//                            break;
//                        }
//                        case '#': {
//                            status = 2;
//                            chars[i] = '/';
//                            break;
//                        }
//                    }
//                }
//                case 1: {
//                    switch (c) {
//                        case '#': {
//                            status = 2;
//                            chars[i] = '/';
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//        return new String(chars);
//    }

    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String s = str.trim();
        return s.length() == 0 ? null : s;
    }

    public static List<String> split(String str, String separators, boolean trim) {
        if (str == null) {
            return Collections.EMPTY_LIST;
        }
        StringTokenizer st = new StringTokenizer(str, separators);
        List<String> result = new ArrayList<>();
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            if (trim) {
                s = s.trim();
            }
            result.add(s);
        }
        return result;
    }

    public static List<String> split(String str, String separators) {
        if (str == null) {
            return Collections.EMPTY_LIST;
        }
        StringTokenizer st = new StringTokenizer(str, separators);
        List<String> result = new ArrayList<>();
        while (st.hasMoreElements()) {
            result.add(st.nextToken());
        }
        return result;
    }

    public static String getAbsolutePath(String path) {
        return new File(path).toPath().toAbsolutePath().normalize().toString();
    }

    public static String readStringFromURL(URL requestURL) throws IOException {
        File f = toFile(requestURL);
        if (f != null) {
            return new String(Files.readAllBytes(f.toPath()));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(requestURL.openStream(), out, true, true);
        return new String(out.toByteArray());
    }

    public static String readStringFromFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static boolean isAbsolutePath(String location) {
        return new File(location).isAbsolute();
    }

//    public static boolean storeProperties(Properties p, File file) {
//        Writer writer = null;
//        try {
//            File parentFile = file.getParentFile();
//            if (parentFile != null) {
//                parentFile.mkdirs();
//            }
//            try {
//                p.store(writer = new FileWriter(file), null);
//            } finally {
//                if (writer != null) {
//                    writer.close();
//                }
//            }
//            return true;
//        } catch (IOException e) {
//            LOG.log(Level.SEVERE, "unable to store {0}", file);
//        }
//        return false;
//    }
//
//    public static Properties loadURLPropertiesFromLocalFile(File file) {
//
//        Properties p = new Properties();
//        if (file.isFile()) {
//            try (InputStream in = Files.newInputStream(file.toPath())) {
//                p.load(in);
//            } catch (IOException ex) {
//                //ignore...
//            }
//        }
//        return p;
//    }

    public static Properties loadURLProperties(URL url, File cacheFile, boolean useCache, PrivateNutsLog LOG) {
        long startTime = System.currentTimeMillis();
        Properties props = new Properties();
        InputStream inputStream = null;
        File urlFile = toFile(url);
        try {
            if (useCache) {
                if (cacheFile != null && cacheFile.isFile()) {
                    try {
                        inputStream = new FileInputStream(cacheFile);
                        props.load(inputStream);
                        long time = System.currentTimeMillis() - startTime;
                        LOG.log(Level.CONFIG, PrivateNutsLog.SUCCESS, "loaded cached file from  {0}" + ((time > 0) ? " (time {1})" : ""), new Object[]{cacheFile.getPath(), formatPeriodMilli(time)});
                        return props;
                    } catch (IOException ex) {
                        LOG.log(Level.CONFIG, PrivateNutsLog.FAIL, "invalid cache. Ignored {0} : {1}", new Object[]{cacheFile.getPath(), ex.toString()});
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Exception ex) {
                                if (LOG != null) {
                                    LOG.log(Level.FINE, "unable to close stream", ex);
                                }
                                //
                            }
                        }
                    }
                }
            }
            inputStream = null;
            try {
                if (url != null) {
                    String urlString = url.toString();
                    inputStream = url.openStream();
                    if (inputStream != null) {
                        props.load(inputStream);
                        if (cacheFile != null) {
                            boolean copy = true;
                            //dont override self!
                            if (urlFile != null) {
                                if (getAbsolutePath(urlFile.getPath()).equals(getAbsolutePath(cacheFile.getPath()))) {
                                    copy = false;
                                }
                            }
                            if (copy) {
                                File pp = cacheFile.getParentFile();
                                if (pp != null) {
                                    pp.mkdirs();
                                }
                                boolean cachedRecovered = cacheFile.isFile();
                                if (urlFile != null) {
                                    copy(urlFile, cacheFile, LOG);
                                } else {
                                    copy(url, cacheFile, LOG);
                                }
                                long time = System.currentTimeMillis() - startTime;
                                if (cachedRecovered) {
                                    LOG.log(Level.CONFIG, PrivateNutsLog.CACHE, "recover cached prp file {0} (from {1})" + ((time > 0) ? " (time {2})" : ""), new Object[]{cacheFile.getPath(), urlString, formatPeriodMilli(time)});
                                } else {
                                    LOG.log(Level.CONFIG, PrivateNutsLog.CACHE, "cached prp file {0} (from {1})" + ((time > 0) ? " (time {2})" : ""), new Object[]{cacheFile.getPath(), urlString, formatPeriodMilli(time)});
                                }
                                return props;
                            }
                        }
                        long time = System.currentTimeMillis() - startTime;
                        LOG.log(Level.CONFIG, PrivateNutsLog.SUCCESS, "loading props file from  {0}" + ((time > 0) ? " (time {1})" : ""), new Object[]{urlString, formatPeriodMilli(time)});
                    } else {
                        long time = System.currentTimeMillis() - startTime;
                        LOG.log(Level.CONFIG, PrivateNutsLog.FAIL, "loading props file from  {0}" + ((time > 0) ? " (time {1})" : ""), new Object[]{urlString, formatPeriodMilli(time)});
                    }
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            LOG.log(Level.CONFIG, PrivateNutsLog.FAIL, "loading props file from  {0}" + ((time > 0) ? " (time {1})" : ""), new Object[]{
                    String.valueOf(url),
                    formatPeriodMilli(time)});
        }
        return props;
    }

    public static boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            //
        }
        return false;
    }

    public static String toMavenFileName(String nutsId, String extension) {
        String[] arr = nutsId.split("[:#]");
        return arr[1]
                + "-"
                + arr[2]
                + "."
                + extension;
    }

    public static String toMavenPath(String nutsId) {
        String[] arr = nutsId.split("[:#]");
        StringBuilder sb = new StringBuilder();
        sb.append(arr[0].replace(".", "/"));
        sb.append("/");
        sb.append(arr[1]);
        if (arr.length > 2) {
            sb.append("/");
            sb.append(arr[2]);
        }
        return sb.toString();
    }


    public static String replaceDollarString(String path, Function<String, String> m) {
        Matcher matcher = DOLLAR_PLACE_HOLDER_PATTERN.matcher(path);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String x = m.apply(matcher.group("name"));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(x));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static List<String> splitUrlStrings(String repositories) {
        return split(repositories, "\n;", true);
    }

    public static int parseFileSize(String s) {
        s = s.toLowerCase();
        int multiplier = 1;
        int val;
        if (s.endsWith("g")) {
            val = Integer.parseInt(s.substring(0, s.length() - 1));
            multiplier = 1024 * 1024 * 1024;
        } else if (s.endsWith("gb")) {
            val = Integer.parseInt(s.substring(0, s.length() - 2));
            multiplier = 1024 * 1024 * 1024;
        } else if (s.endsWith("m")) {
            val = Integer.parseInt(s.substring(0, s.length() - 1));
            multiplier = 1024 * 1024;
        } else if (s.endsWith("mb")) {
            val = Integer.parseInt(s.substring(0, s.length() - 2));
            multiplier = 1024 * 1024;
        } else if (s.endsWith("k")) {
            val = Integer.parseInt(s.substring(0, s.length() - 1));
            multiplier = 1024 * 1024;
        } else if (s.endsWith("kb")) {
            val = Integer.parseInt(s.substring(0, s.length() - 2));
            multiplier = 1024 * 1024;
        } else {
            val = Integer.parseInt(s);
//            multiplier = 1;
        }
        return val * multiplier;
    }

    public static String formatPeriodMilli(long period) {
        StringBuilder sb = new StringBuilder();
        boolean started = false;
        int h = (int) (period / (1000L * 60L * 60L));
        int mn = (int) ((period % (1000L * 60L * 60L)) / 60000L);
        int s = (int) ((period % 60000L) / 1000L);
        int ms = (int) (period % 1000L);
        if (h > 0) {
            sb.append(formatRight(String.valueOf(h), 2)).append("h ");
            started = true;
        }
        if (mn > 0 || started) {
            sb.append(formatRight(String.valueOf(mn), 2)).append("mn ");
            started = true;
        }
        if (s > 0 || started) {
            sb.append(formatRight(String.valueOf(s), 2)).append("s ");
            //started=true;
        }
        sb.append(formatRight(String.valueOf(ms), 3)).append("ms");
        return sb.toString();
    }

    public static String formatRight(String str, int size) {
        StringBuilder sb = new StringBuilder(size);
        sb.append(str);
        while (sb.length() < size) {
            sb.insert(0, ' ');
        }
        return sb.toString();
    }

    public static String resolveJavaCommand(String javaHome) {
        String exe = PrivateNutsPlatformUtils.getPlatformOsFamily().equals(NutsOsFamily.WINDOWS) ? "java.exe" : "java";
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
            if (PrivateNutsUtils.isBlank(javaHome) || "null".equals(javaHome)) {
                //this may happen is using a precompiled image (such as with graalvm)
                return exe;
            }
        }
        return javaHome + File.separator + "bin" + File.separator + exe;
    }

    private static class SimpleConfirmDelete implements ConfirmDelete {
        private boolean force;
        private List<File> ignoreDeletion = new ArrayList<>();

        @Override
        public boolean isForce() {
            return force;
        }

        @Override
        public void setForce(boolean value) {
            this.force = value;
        }

        public void ignore(File directory) {
            ignoreDeletion.add(directory);
        }

        @Override
        public boolean accept(File directory) {
            for (File ignored : ignoreDeletion) {
                String s = ignored.getPath() + File.separatorChar;
                if (s.startsWith(directory.getPath() + "/")) {
                    return false;
                }
            }
            return true;
        }
    }

    private interface ConfirmDelete {
        boolean isForce();

        void setForce(boolean value);

        boolean accept(File directory);

        void ignore(File directory);
    }

    public static int deleteAndConfirmAll(File[] folders, boolean force, String header, NutsTerminal term, NutsSession session, PrivateNutsLog LOG) {
        return deleteAndConfirmAll(folders, force, new SimpleConfirmDelete(), header, term, session, LOG);
    }

    private static int deleteAndConfirmAll(File[] folders, boolean force, ConfirmDelete refForceAll, String header, NutsTerminal term, NutsSession session, PrivateNutsLog LOG) {
        int count = 0;
        boolean headerWritten = false;
        if (folders != null) {
            for (File child : folders) {
                if (child.exists()) {
                    if (!headerWritten) {
                        headerWritten = true;
                        if (!force && !refForceAll.isForce()) {
                            if (header != null) {
                                if (term != null) {
                                    term.out().println(header);
                                } else {
                                    System.out.println(header);
                                }
                            }
                        }
                    }
                    if (PrivateNutsUtils.deleteAndConfirm(child, force, refForceAll, term, session, LOG)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static boolean deleteAndConfirm(File directory, boolean force, ConfirmDelete refForceAll, NutsTerminal term, NutsSession session, PrivateNutsLog LOG) {
        if (directory.exists()) {
            if (!force && !refForceAll.isForce() && refForceAll.accept(directory)) {
                String line;
                if (term != null) {
                    line = term.ask().forString("Do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory).setSession(session).getValue();
                } else {
                    Scanner s = new Scanner(System.in);
                    System.out.printf("Do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory);
                    System.out.print(" : ");
                    System.out.flush();
                    line = s.nextLine();
                }
                if ("a".equalsIgnoreCase(line) || "all".equalsIgnoreCase(line)) {
                    refForceAll.setForce(true);
                } else if ("c".equalsIgnoreCase(line)) {
                    throw new NutsUserCancelException(null);
                } else if (!PrivateNutsUtils.parseBoolean(line, false)) {
                    refForceAll.ignore(directory);
                    return false;
                }
            }
            Path directoryPath = Paths.get(directory.getPath());
            try {
                Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        LOG.log(Level.FINEST, PrivateNutsLog.WARNING, "delete file   : {0}", file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
                LOG.log(Level.FINEST, PrivateNutsLog.WARNING, "delete folder : {0}", directory);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return true;
        }
        return false;
    }

    public static boolean isActualJavaOptions(String options) {
        //FIX ME
        return true;
    }

    public static boolean isActualJavaCommand(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) {
            return true;
        }
        String javaHome = System.getProperty("java.home");
        if (PrivateNutsUtils.isBlank(javaHome) || "null".equals(javaHome)) {
            return cmd.equals("java") || cmd.equals("java.exe") || cmd.equals("javaw.exe") || cmd.equals("javaw");
        }
        String jh = javaHome.replace("\\", "/");
        cmd = cmd.replace("\\", "/");
        if (cmd.equals(jh + "/bin/java")) {
            return true;
        }
        if (cmd.equals(jh + "/bin/java.exe")) {
            return true;
        }
        if (cmd.equals(jh + "/bin/javaw")) {
            return true;
        }
        if (cmd.equals(jh + "/bin/javaw.exe")) {
            return true;
        }
        if (cmd.equals(jh + "/jre/bin/java")) {
            return true;
        }
        if (cmd.equals(jh + "/jre/bin/java.exe")) {
            return true;
        }
        return false;
    }

    public static String desc(Object s) {
        if (s == null) {
            return "<EMPTY>";
        }
        String ss
                = (s instanceof Enum) ? ((Enum) s).name().toLowerCase().replace('_', '-')
                : s.toString().trim();
        return ss.isEmpty() ? "<EMPTY>" : ss;
    }

    public static String syspath(String s) {
        return s.replace('/', File.separatorChar);
    }

    public static String nvl(Object... all) {
        for (Object object : all) {
            if (object != null) {
                return desc(object);
            }
        }
        return desc(null);
    }

    public static String formatLogValue(Object unresolved, Object resolved) {
        String a = PrivateNutsUtils.desc(unresolved);
        String b = PrivateNutsUtils.desc(resolved);
        if (a.equals(b)) {
            return a;
        } else {
            return a + " => " + b;
        }
    }

    public static File toFile(String url) {
        if (isBlank(url)) {
            return null;
        }
        URL u = null;
        try {
            u = new URL(url);
            return toFile(u);
        } catch (MalformedURLException e) {
            //
            return new File(url);
        }
    }

    public static File toFile(URL url) {
        if (url == null) {
            return null;
        }
        if ("file".equals(url.getProtocol())) {
            try {
                return Paths.get(url.toURI()).toFile();
            } catch (URISyntaxException e) {
                //
            }
        }
        return null;
    }

    public static long copy(InputStream from, OutputStream to, boolean closeInput, boolean closeOutput) throws IOException {
        byte[] bytes = new byte[10240];
        int count;
        long all = 0;
        try {
            try {
                while ((count = from.read(bytes)) > 0) {
                    to.write(bytes, 0, count);
                    all += count;
                }
                return all;
            } finally {
                if (closeInput) {
                    from.close();
                }
            }
        } finally {
            if (closeOutput) {
                to.close();
            }
        }
    }

    public static void copy(File ff, File to, PrivateNutsLog LOG) throws IOException {
        if (to.getParentFile() != null) {
            to.getParentFile().mkdirs();
        }
        try {
            Files.copy(ff.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LOG.log(Level.CONFIG, PrivateNutsLog.FAIL, "error copying {0} to {1} : {2}", new Object[]{ff, to, ex.toString()});
            throw ex;
        }
    }

    public static void copy(URL url, File to, PrivateNutsLog LOG) throws IOException {
        try {
            InputStream in = url.openStream();
            if (in == null) {
                throw new IOException("Empty Stream " + url);
            }
            if (to.getParentFile() != null) {
                if (!to.getParentFile().isDirectory()) {
                    boolean mkdirs = to.getParentFile().mkdirs();
                    if (!mkdirs) {
                        LOG.log(Level.CONFIG, PrivateNutsLog.FAIL, "error creating folder {0}", new Object[]{url});
                    }
                }
            }
            ReadableByteChannel rbc = Channels.newChannel(in);
            FileOutputStream fos = new FileOutputStream(to);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException ex) {
            LOG.log(Level.CONFIG, PrivateNutsLog.FAIL, "error copying {0} to {1} : {2}", new Object[]{url, to, ex.toString()});
            throw ex;
        }
    }

    public static String formatURL(URL url) {
        if (url == null) {
            return "<EMPTY>";
        }
        File f = toFile(url);
        if (f != null) {
            return f.getPath();
        }
        return url.toString();
    }

    public static String getSystemString(String property, String defaultValue) {
        String v = System.getProperty(property);
        if (v == null || v.trim().isEmpty()) {
            return defaultValue;
        }
        return v;
    }

    public static boolean getSysBoolNutsProperty(String property, boolean defaultValue) {
        return (getSystemBoolean("nuts." + property, defaultValue)
                || getSystemBoolean("nuts.export." + property, defaultValue));
    }

    public static Boolean getSystemBoolean(String property, Boolean defaultValue) {
        String o = System.getProperty(property);
        if (o == null) {
            return defaultValue;
        }
        return parseBoolean(o, defaultValue);
    }

    public static Boolean parseBoolean(String value, Boolean defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        value = value.trim().toLowerCase();
        if (value.matches("true|enable|enabled|yes|always|y|on|ok|t|o")) {
            return true;
        }
        if (value.matches("false|disable|disabled|no|none|never|n|off|ko|f")) {
            return false;
        }
        return defaultValue;
    }

    /**
     * v1 and v2 are supposed in the following form nbr1.nbr2, ... where all
     * items (nbr) between dots are positive numbers
     *
     * @param v1 version 1
     * @param v2 version 2
     * @return 1, 0 or -1
     */
    public static int compareRuntimeVersion(String v1, String v2) {
        String[] a1 = v1.split("\\.");
        String[] a2 = v2.split("\\.");
        int max = Math.max(a1.length, a2.length);
        for (int i = 0; i < max; i++) {
            if (i >= a1.length) {
                return -1;
            }
            if (i >= a2.length) {
                return 1;
            }
            int i1 = Integer.parseInt(a1[i]);
            int i2 = Integer.parseInt(a2[i]);
            if (i1 != i2) {
                return Integer.compare(i1, i2);
            }
        }
        return 0;
    }

    public static Set<String> parseDependencies(String s) {
        return new LinkedHashSet<>(PrivateNutsUtils.split(s, ";", false));
    }

    public static class Mvn {

//        public static String resolveMavenReleaseVersion(String mavenURLBase, String nutsId) {
//            String mvnUrl = (mavenURLBase + toMavenPath(nutsId) + "/maven-metadata.xml");
//            String str = null;
//            try {
//                str = PrivateNutsUtils.readStringFromURL(new URL(mvnUrl));
//            } catch (IOException e) {
//                throw new UncheckedIOException(e);
//            }
//            if (str != null) {
//                for (String line : str.split("\n")) {
//                    line = line.trim();
//                    if (line.startsWith("<release>")) {
//                        return line.substring("<release>".length(), line.length() - "</release>".length()).trim();
//                    }
//                }
//            }
//            throw new NutsNotFoundException(null, nutsId);
//        }

        public static String resolveMavenFullPath(String repo, String nutsId, String ext) {
            String jarPath = toMavenPath(nutsId) + "/" + toMavenFileName(nutsId, ext);
            String mvnUrl = repo;
            String sep = "/";
            if (!isURL(repo)) {
                sep = File.separator;
            }
            if (!mvnUrl.endsWith("/") && !mvnUrl.endsWith(sep)) {
                mvnUrl += sep;
            }
            return mvnUrl + jarPath;
        }

        public static File resolveOrDownloadJar(String nutsId, String[] repositories, String cacheFolder, PrivateNutsLog LOG, boolean includeDesc, Instant expire) {
            includeDesc = false;
            String descPath = toMavenPath(nutsId) + "/" + toMavenFileName(nutsId, "pom");
            String jarPath = toMavenPath(nutsId) + "/" + toMavenFileName(nutsId, "jar");
            File cachedJarFile = new File(resolveMavenFullPath(cacheFolder, nutsId, "jar"));
            File cachedPomFile = new File(resolveMavenFullPath(cacheFolder, nutsId, "pom"));
            if (cachedJarFile.isFile()) {
                try {
                    if(expire!=null && Files.getLastModifiedTime(cachedJarFile.toPath()).toInstant().compareTo(expire)<0){
                        //ignore
                    }else {
                        return cachedJarFile;
                    }
                } catch (IOException e) {
                    //suppose update, if not it will be re-downloaded every time
                    return cachedJarFile;
                }
            }
            for (String r : repositories) {
                LOG.log(Level.FINE, PrivateNutsLog.CACHE, "checking {0} from {1}", new Object[]{nutsId, r});
//                File file = toFile(r);
                if (includeDesc) {
                    String path = resolveMavenFullPath(r, nutsId, "pom");
//                    if (file == null) {
                    try {
                        copy(new URL(path), cachedPomFile, LOG);
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, PrivateNutsLog.FAIL, "unable to load {0} from {1}.\n", new Object[]{nutsId, r});
                        continue;
                        //ex.printStackTrace();
                        //throw new NutsIllegalArgumentException("Unable to load nuts from " + mvnUrl);
                    }
//                    } else {
//                        //file
//                        File f = new File(r, descPath);
//                        if (f.isFile()) {
//                            return f;
//                        } else {
//                            LOG.log(Level.SEVERE, PrivateNutsLog.FAIL, "unable to load {0} from {1}.\n", new Object[]{nutsId, r});
//                        }
//                    }
                }
                String path = resolveMavenFullPath(r, nutsId, "jar");
//                if (file == null) {
                try {
                    copy(new URL(path), cachedJarFile, LOG);
                    LOG.log(Level.CONFIG, PrivateNutsLog.CACHE, "cache jar file {0}", new Object[]{cachedJarFile.getPath()});
                    return cachedJarFile;
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, PrivateNutsLog.FAIL, "unable to load {0} from {1}.\n", new Object[]{nutsId, r});
                    //ex.printStackTrace();
                    //throw new NutsIllegalArgumentException("Unable to load nuts from " + mvnUrl);
                }
//                } else {
//                    //file
//                    File f = new File(r, jarPath);
//                    if (f.isFile()) {
//                        return f;
//                    } else {
//                        LOG.log(Level.SEVERE, PrivateNutsLog.FAIL, "unable to load {0} from {1}.\n", new Object[]{nutsId, r});
//                    }
//                }
            }
            return null;
        }

        static Deps loadDependencies(PrivateNutsId rid, PrivateNutsLog LOG, Collection<String> repos) {
            String urlPath = idToPath(rid) + "/" + rid.getArtifactId() + "-" + rid.getVersion() + ".pom";
            return loadDependencies(urlPath, LOG, repos);
        }

        static Deps loadDependencies(String urlPath, PrivateNutsLog LOG, Collection<String> repos) {
            Deps depsAndRepos = null;
            for (String baseUrl : repos) {
                depsAndRepos = loadDependenciesAndRepositoriesFromPomUrl(baseUrl + "/" + urlPath, LOG);
                if (!depsAndRepos.deps.isEmpty()) {
                    break;
                }
            }
            return depsAndRepos;
        }

        static Deps loadDependenciesAndRepositoriesFromPomUrl(String url, PrivateNutsLog LOG) {
            Deps depsAndRepos = new Deps();
            InputStream xml = null;
            try {
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    URL url1 = new URL(url);
                    try {
                        xml = url1.openStream();
                    } catch (IOException ex) {
                        //do not need to log error
                        return depsAndRepos;
                    }
                } else if (url.startsWith("file://")) {
                    URL url1 = new URL(url);
                    File file = toFile(url1);
                    if (file == null) {
                        // was not able to resolve to File
                        try {
                            xml = url1.openStream();
                        } catch (IOException ex) {
                            //do not need to log error
                            return depsAndRepos;
                        }
                    } else if (file.isFile()) {
                        xml = Files.newInputStream(file.toPath());
                    } else {
                        return depsAndRepos;
                    }
                } else {
                    File file = new File(url);
                    if (file.isFile()) {
                        xml = Files.newInputStream(file.toPath());
                    } else {
                        return depsAndRepos;
                    }
                }
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(xml);
                Element c = doc.getDocumentElement();
                for (int i = 0; i < c.getChildNodes().getLength(); i++) {
                    if (c.getChildNodes().item(i) instanceof Element && c.getChildNodes().item(i).getNodeName().equals("dependencies")) {
                        Element c2 = (Element) c.getChildNodes().item(i);
                        for (int j = 0; j < c2.getChildNodes().getLength(); j++) {
                            if (c2.getChildNodes().item(j) instanceof Element && c2.getChildNodes().item(j).getNodeName().equals("dependency")) {
                                Element c3 = (Element) c2.getChildNodes().item(j);
                                String groupId = null;
                                String artifactId = null;
                                String version = null;
                                String scope = null;
                                for (int k = 0; k < c3.getChildNodes().getLength(); k++) {
                                    if (c3.getChildNodes().item(k) instanceof Element) {
                                        Element c4 = (Element) c3.getChildNodes().item(k);
                                        switch (c4.getNodeName()) {
                                            case "groupId": {
                                                groupId = c4.getTextContent().trim();
                                                break;
                                            }
                                            case "artifactId": {
                                                artifactId = c4.getTextContent().trim();
                                                break;
                                            }
                                            case "version": {
                                                version = c4.getTextContent().trim();
                                                break;
                                            }
                                            case "scope": {
                                                scope = c4.getTextContent().trim();
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (isBlank(groupId)) {
                                    throw new NutsIllegalArgumentException(null, "unexpected empty groupId");
                                } else if (groupId.contains("$")) {
                                    throw new NutsIllegalArgumentException(null, "unexpected maven variable in groupId=" + groupId);
                                }
                                if (isBlank(artifactId)) {
                                    throw new NutsIllegalArgumentException(null, "unexpected empty artifactId");
                                } else if (artifactId.contains("$")) {
                                    throw new NutsIllegalArgumentException(null, "unexpected maven variable in artifactId=" + artifactId);
                                }
                                if (isBlank(version)) {
                                    throw new NutsIllegalArgumentException(null, "unexpected empty artifactId");
                                } else if (version.contains("$")) {
                                    throw new NutsIllegalArgumentException(null, "unexpected maven variable in artifactId=" + version);
                                }
                                //this is maven dependency, using "compile"
                                if (isBlank(scope) || scope.equals("compile")) {
                                    depsAndRepos.deps.add(groupId + ":" + artifactId + "#" + version);
                                } else if (version.contains("$")) {
                                    throw new NutsIllegalArgumentException(null, "unexpected maven variable in artifactId=" + version);
                                }
                            }
                        }
                    } else if (c.getChildNodes().item(i) instanceof Element && c.getChildNodes().item(i).getNodeName().equals("properties")) {
                        Element c2 = (Element) c.getChildNodes().item(i);
                        for (int j = 0; j < c2.getChildNodes().getLength(); j++) {
                            if (c2.getChildNodes().item(j) instanceof Element) {
                                Element c3 = (Element) c2.getChildNodes().item(j);
                                switch (c3.getNodeName()) {
                                    case "nuts-runtime-repositories": {
                                        String t = c3.getTextContent().trim();
                                        if (t.length() > 0) {
                                            depsAndRepos.deps.addAll(split(t, ";", true));
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (Exception ex) {
                LOG.log(Level.FINE, "unable to loadDependenciesAndRepositoriesFromPomUrl " + url, ex);
            } finally {
                if (xml != null) {
                    try {
                        xml.close();
                    } catch (IOException ex) {
                        //ignore
                    }
                }
            }

            return depsAndRepos;
        }

        /**
         * find latest maven artifact
         *
         * @param filter filter
         * @return latest runtime version
         */
        static String resolveLatestMavenId(PrivateNutsId zId, Predicate<String> filter, PrivateNutsLog LOG, Collection<String> bootRepositories) {
            LOG.log(Level.FINEST, PrivateNutsLog.START, "looking for " + zId);
            String path = zId.getGroupId().replace('.', '/') + '/' + zId.getArtifactId();
            String bestVersion = null;
            String bestPath = null;
            for (String repoUrl : bootRepositories) {
                boolean found = false;
                if (!repoUrl.contains("://")) {
                    File mavenNutsCoreFolder = new File(repoUrl, path + "/".replace("/", File.separator));
                    if (mavenNutsCoreFolder.isDirectory()) {
                        File[] children = mavenNutsCoreFolder.listFiles();
                        if (children != null) {
                            for (File file : children) {
                                if (file.isDirectory()) {
                                    String[] goodChildren = file.list(new FilenameFilter() {
                                        @Override
                                        public boolean accept(File dir, String name) {
                                            return name.endsWith(".pom");
                                        }
                                    });
                                    if (goodChildren != null && goodChildren.length > 0) {
                                        String p = file.getName();
                                        if (filter == null || filter.test(p)) {
                                            if (bestVersion == null || compareRuntimeVersion(bestVersion, p) < 0) {
                                                bestVersion = p;
                                                bestPath = "Local location : " + file.getPath();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (!repoUrl.endsWith("/")) {
                        repoUrl = repoUrl + "/";
                    }
                    String mavenMetadata = repoUrl + path + "/maven-metadata.xml";
                    try {
                        URL runtimeMetadata = new URL(mavenMetadata);
                        found = true;
                        DocumentBuilderFactory factory
                                = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        InputStream is = null;
                        try {
                            is = runtimeMetadata.openStream();
                        } catch (IOException ex) {
                            //do not need to log error
                            //ignore
                        }
                        if (is != null) {
                            LOG.log(Level.FINEST, PrivateNutsLog.SUCCESS, "parsing " + mavenMetadata);
                            Document doc = builder.parse(is);
                            Element c = doc.getDocumentElement();
                            for (int i = 0; i < c.getChildNodes().getLength(); i++) {
                                if (c.getChildNodes().item(i) instanceof Element && c.getChildNodes().item(i).getNodeName().equals("versioning")) {
                                    Element c2 = (Element) c.getChildNodes().item(i);
                                    for (int j = 0; j < c2.getChildNodes().getLength(); j++) {
                                        if (c2.getChildNodes().item(j) instanceof Element && c2.getChildNodes().item(j).getNodeName().equals("versions")) {
                                            Element c3 = (Element) c2.getChildNodes().item(j);
                                            for (int k = 0; k < c3.getChildNodes().getLength(); k++) {
                                                if (c3.getChildNodes().item(k) instanceof Element && c3.getChildNodes().item(k).getNodeName().equals("version")) {
                                                    Element c4 = (Element) c3.getChildNodes().item(k);
                                                    String p = c4.getTextContent();
                                                    if (filter == null || filter.test(p)) {
                                                        if (bestVersion == null || compareRuntimeVersion(bestVersion, p) < 0) {
                                                            bestVersion = p;
                                                            bestPath = "remote file " + mavenMetadata;
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                                //NutsConstants.Ids.NUTS_RUNTIME.replaceAll("[.:]", "/")
                            }
                        }
                    } catch (Exception ex) {
                        LOG.log(Level.FINE, "unable to parse " + mavenMetadata, ex);
                        // ignore any error
                    }
                }
                if (found) {
                    break;
                }
            }
            if (bestVersion == null) {
                return null;
            }
            String s = zId.getGroupId() + ":" + zId.getArtifactId() + "#" + bestVersion;
            LOG.log(Level.FINEST, PrivateNutsLog.SUCCESS, "resolved " + s + " from " + bestPath);
            return s;
        }

        static File createFile(String parent, String child) {
            String userHome = System.getProperty("user.home");
            if (child.startsWith("~/")) {
                child = new File(userHome, child.substring(2)).getPath();
            }
            if ((child.startsWith("/") || child.startsWith("\\") || new File(child).isAbsolute())) {
                return new File(child);
            }
            if (parent != null) {
                if (parent.startsWith("~/")) {
                    parent = new File(userHome, parent.substring(2)).getPath();
                }
            } else {
                parent = ".";
            }
            return new File(parent, child);
        }

        static boolean isInfiniteLoopThread(String className, String methodName) {
            Thread thread = Thread.currentThread();
            StackTraceElement[] elements = thread.getStackTrace();

            if (elements == null || elements.length == 0) {
                return false;
            }

            for (int i = 0; i < elements.length; i++) {
                StackTraceElement element = elements[elements.length - (i + 1)];
                if (className.equals(element.getClassName())) {
                    if (methodName.equals(element.getMethodName())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static boolean isFileAccessible(Path path, Instant expireTime,PrivateNutsLog LOG){
        boolean proceed=Files.isRegularFile(path);
        if(proceed) {
            try {
                if (expireTime != null) {
                    FileTime lastModifiedTime = Files.getLastModifiedTime(path);
                    if (lastModifiedTime.toInstant().compareTo(expireTime) < 0) {
                        return false;
                    }
                }
            } catch (Exception ex0) {
                LOG.log(Level.FINEST, PrivateNutsLog.FAIL, "unable to get LastModifiedTime for file : {0}", new String[]{path.toString(), ex0.toString()});
            }
        }
        return proceed;
    }

    public static class Deps {

        LinkedHashSet<String> deps = new LinkedHashSet<>();
        LinkedHashSet<String> repos = new LinkedHashSet<>();
    }
}
