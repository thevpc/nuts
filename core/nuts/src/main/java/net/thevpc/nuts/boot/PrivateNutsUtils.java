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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

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
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/15/17.
 *
 * @app.category Internal
 * @since 0.5.4
 */
final class PrivateNutsUtils {

    private static final Pattern DOLLAR_PLACE_HOLDER_PATTERN = Pattern.compile("[$][{](?<name>([a-zA-Z]+))[}]");
    private static Scanner inScanner;

    public static boolean isValidWorkspaceName(String workspace) {
        if (NutsUtilStrings.isBlank(workspace)) {
            return true;
        }
        String workspaceName = workspace.trim();
        return workspaceName.matches("[^/\\\\]+")
                && !workspaceName.equals(".")
                && !workspaceName.equals("..");
    }

    public static String resolveValidWorkspaceName(String workspace) {
        if (NutsUtilStrings.isBlank(workspace)) {
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

    public static String idToPath(NutsBootId id) {
        return id.getGroupId().replace('.', '/') + "/"
                + id.getArtifactId() + "/" + id.getVersion();
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
        return out.toString();
    }

    public static String readStringFromFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static boolean isAbsolutePath(String location) {
        return new File(location).isAbsolute();
    }

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
                        LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "loaded cached file from  {0}" + ((time > 0) ? " (time {1})" : ""), new Object[]{cacheFile.getPath(), formatPeriodMilli(time)});
                        return props;
                    } catch (IOException ex) {
                        LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "invalid cache. Ignored {0} : {1}", new Object[]{cacheFile.getPath(), ex.toString()});
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
                                    LOG.log(Level.CONFIG, NutsLogVerb.CACHE, "recover cached prp file {0} (from {1})" + ((time > 0) ? " (time {2})" : ""), new Object[]{cacheFile.getPath(), urlString, formatPeriodMilli(time)});
                                } else {
                                    LOG.log(Level.CONFIG, NutsLogVerb.CACHE, "cached prp file {0} (from {1})" + ((time > 0) ? " (time {2})" : ""), new Object[]{cacheFile.getPath(), urlString, formatPeriodMilli(time)});
                                }
                                return props;
                            }
                        }
                        long time = System.currentTimeMillis() - startTime;
                        LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "loading props file from  {0}" + ((time > 0) ? " (time {1})" : ""), new Object[]{urlString, formatPeriodMilli(time)});
                    } else {
                        long time = System.currentTimeMillis() - startTime;
                        LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "loading props file from  {0}" + ((time > 0) ? " (time {1})" : ""), new Object[]{urlString, formatPeriodMilli(time)});
                    }
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "loading props file from  {0}" + ((time > 0) ? " (time {1})" : ""), new Object[]{
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
        String exe = NutsUtilPlatforms.getPlatformOsFamily().equals(NutsOsFamily.WINDOWS) ? "java.exe" : "java";
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
            if (NutsUtilStrings.isBlank(javaHome) || "null".equals(javaHome)) {
                //this may happen is using a precompiled image (such as with graalvm)
                return exe;
            }
        }
        return javaHome + File.separator + "bin" + File.separator + exe;
    }

    public static long deleteAndConfirmAll(File[] folders, boolean force, String header, NutsSession session, PrivateNutsLog LOG, NutsWorkspaceOptions woptions) {
        return deleteAndConfirmAll(folders, force, new SimpleConfirmDelete(), header, session, LOG, woptions);
    }

    private static long deleteAndConfirmAll(File[] folders, boolean force, ConfirmDelete refForceAll, String header, NutsSession session, PrivateNutsLog LOG, NutsWorkspaceOptions woptions) {
        long count = 0;
        boolean headerWritten = false;
        if (folders != null) {
            for (File child : folders) {
                if (child.exists()) {
                    if (!headerWritten) {
                        headerWritten = true;
                        if (!force && !refForceAll.isForce()) {
                            if (header != null) {
                                if (!woptions.isBot()) {
                                    if (session != null) {
                                        session.err().println(header);
                                    } else {
                                        err_printf("%s%n", header);
                                    }
                                }
                            }
                        }
                    }
                    count += PrivateNutsUtils.deleteAndConfirm(child, force, refForceAll, session, LOG, woptions);
                }
            }
        }
        return count;
    }

    private static long deleteAndConfirm(File directory, boolean force, ConfirmDelete refForceAll, NutsSession session, PrivateNutsLog LOG, NutsWorkspaceOptions woptions) {
        if (directory.exists()) {
            if (!force && !refForceAll.isForce() && refForceAll.accept(directory)) {
                String line = null;
                if (session != null) {
                    line = session.getTerminal().ask()
                            .resetLine()
                            .forString("do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory).setSession(session).getValue();
                } else {
                    if (woptions.isBot()) {
                        if (woptions.getConfirm() == NutsConfirmationMode.YES) {
                            line = "y";
                        } else {
                            throw new NutsBootException(NutsMessage.plain("failed to delete files in --bot mode without auto confirmation"));
                        }
                    } else {
                        if (woptions.isGui()) {
                            line = PrivateNutsGui.inputString(
                                    NutsMessage.cstyle("do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory).toString(),
                                    null
                            );
                        } else {
                            NutsConfirmationMode cc = woptions.getConfirm();
                            if (cc == null) {
                                cc = NutsConfirmationMode.ASK;
                            }
                            switch (cc) {
                                case YES: {
                                    line = "y";
                                    break;
                                }
                                case NO: {
                                    line = "n";
                                    break;
                                }
                                case ERROR: {
                                    throw new NutsBootException(NutsMessage.plain("error response"));
                                }
                                case ASK: {
                                    err_printf("do you confirm deleting %s [y/n/c/a] (default 'n') ?", directory);
                                    err_printf(" : ");
                                    line = in_readLine();
                                }
                            }
                        }
                    }
                }
                if ("a".equalsIgnoreCase(line) || "all".equalsIgnoreCase(line)) {
                    refForceAll.setForce(true);
                } else if ("c".equalsIgnoreCase(line)) {
                    throw new NutsUserCancelException(session);
                } else if (!NutsUtilStrings.parseBoolean(line, false, false)) {
                    refForceAll.ignore(directory);
                    return 0;
                }
            }
            Path directoryPath = Paths.get(directory.getPath());
            long[] count = new long[1];
            try {
                Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        count[0]++;
//                        LOG.log(Level.FINEST, NutsLogVerb.WARNING, "delete file   : {0}", file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        count[0]++;
                        boolean deleted = false;
                        for (int i = 0; i < 2; i++) {
                            try {
                                Files.delete(dir);
                                deleted = true;
                                break;
                            } catch (DirectoryNotEmptyException e) {
                                // sometimes, on Windows OS, the Filesystem hasn't yet finished deleting
                                // the children (asynchronous)
                                //try three times and then exit!
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        if (!deleted) {
                            //do not catch, last time the exception is thrown
                            Files.delete(dir);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                count[0]++;
                LOG.log(Level.FINEST, NutsLogVerb.WARNING, "delete folder : {0} ({1} files/folders deleted)", new Object[]{directory, count[0]});
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return count[0];
        }
        return 0;
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
        if (NutsUtilStrings.isBlank(javaHome) || "null".equals(javaHome)) {
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
        return cmd.equals(jh + "/jre/bin/java.exe");
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
        if (NutsUtilStrings.isBlank(url)) {
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
        if (ff == null || !ff.exists()) {
            LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "not found {0}", new Object[]{ff});
            throw new FileNotFoundException(ff == null ? "" : ff.getPath());
        }
        try {
            Files.copy(ff.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "error copying {0} to {1} : {2}", new Object[]{ff, to, ex.toString()});
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
                        LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "error creating folder {0}", new Object[]{url});
                    }
                }
            }
            ReadableByteChannel rbc = Channels.newChannel(in);
            FileOutputStream fos = new FileOutputStream(to);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (FileNotFoundException ex) {
            LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "not found {0}", new Object[]{url});
            throw ex;
        } catch (IOException ex) {
            LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "error copying {0} to {1} : {2}", new Object[]{url, to, ex.toString()});
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
        return
                NutsUtilStrings.parseBoolean(System.getProperty("nuts." + property), defaultValue, false)
                        || NutsUtilStrings.parseBoolean(System.getProperty("nuts.export." + property), defaultValue, false)
                ;
    }


    public static Set<NutsBootId> parseDependencies(String s) {
        return PrivateNutsUtils.split(s, ";", false)
                .stream().map(x -> NutsBootId.parse(x))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static boolean isFileAccessible(Path path, Instant expireTime, PrivateNutsLog LOG) {
        boolean proceed = Files.isRegularFile(path);
        if (proceed) {
            try {
                if (expireTime != null) {
                    FileTime lastModifiedTime = Files.getLastModifiedTime(path);
                    if (lastModifiedTime.toInstant().compareTo(expireTime) < 0) {
                        return false;
                    }
                }
            } catch (Exception ex0) {
                LOG.log(Level.FINEST, NutsLogVerb.FAIL, "unable to get LastModifiedTime for file : {0}", new String[]{path.toString(), ex0.toString()});
            }
        }
        return proceed;
    }

    public static int firstIndexOf(String string, char[] chars) {
        char[] value = string.toCharArray();
        for (int i = 0; i < value.length; i++) {
            for (int j = 0; j < chars.length; j++) {
                if (value[i] == chars[j]) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static String resolveNutsVersionFromClassPath() {
        return resolvePomPattern("version");
    }

    public static String resolveNutsBuildNumber() {
        return resolvePomPattern("nuts.buildNumber");
    }

    public static String resolvePomPattern(String propName) {
//        boolean devMode = false;
        String propValue = null;
        try {
            propValue = PrivateNutsUtils.loadURLProperties(
                    Nuts.class.getResource("/META-INF/maven/net.thevpc.nuts/nuts/pom.properties"),
                    null, false, new PrivateNutsLog()).getProperty(propName);
        } catch (Exception ex) {
            //
        }
        if (propValue != null && !propValue.trim().isEmpty() && !propValue.equals("0.0.0")) {
            return propValue;
        }

        Pattern pattern = Pattern.compile("<" + propName.replace(".", "[.]") + ">(?<value>([0-9. ]+))</" + propName.replace(".", "[.]") + ">");

        URL pomXml = Nuts.class.getResource("/META-INF/maven/net.thevpc.nuts/nuts/pom.xml");
        if (pomXml != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (InputStream is = pomXml.openStream()) {
                copy(is, bos, false, false);
            } catch (Exception ex) {
                //
            }
            Matcher m = pattern.matcher(bos.toString());
            if (m.find()) {
                propValue = m.group("value").trim();
            }
        }
        if (propValue != null && !propValue.trim().isEmpty() && !propValue.equals("0.0.0")) {
            return propValue;
        }

        //check if we are in dev mode
        String cp = System.getProperty("java.class.path");
        for (String p : cp.split(File.pathSeparator)) {
            File f = new File(p);
            if (f.isDirectory()) {
                Matcher m = Pattern.compile("(?<src>.*)[/\\\\]+target[/\\\\]+classes[/\\\\]*")
                        .matcher(f.getPath().replace('/', File.separatorChar));
                if (m.find()) {
                    String src = m.group("src");
                    if (new File(src, "pom.xml").exists() && new File(src,
                            "src/main/java/net/thevpc/nuts/Nuts.java".replace('/', File.separatorChar)
                    ).exists()) {
//                            devMode = true;
                        String xml = null;
                        try {
                            byte[] bytes = Files.readAllBytes(new File(src, "pom.xml").toPath());
                            xml = new String(bytes);
                        } catch (IOException ex) {
                            //
                        }
                        if (xml != null) {
                            m = pattern.matcher(xml);
                            if (m.find()) {
                                propValue = m.group("value").trim();
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (propValue != null && !propValue.trim().isEmpty() && !propValue.equals("0.0.0")) {
            return propValue;
        }
        return null;
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

    public static String[] stacktraceToArray(Throwable th) {
        try {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
            List<String> s = new ArrayList<>();
            String line = null;
            while ((line = br.readLine()) != null) {
                s.add(line);
            }
            return s.toArray(new String[0]);
        } catch (Exception ex) {
            // ignore
        }
        return new String[0];
    }

    public static String stacktrace(Throwable th) {
        try {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            return sw.toString();
        } catch (Exception ex) {
            // ignore
        }
        return "";
    }

    public static boolean isInfiniteLoopThread(String className, String methodName) {
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

    static void err_printf(String msg, Object... p) {
        System.err.printf(msg, p);
        System.err.flush();
    }

    static void out_printf(String msg, Object... p) {
        System.out.printf(msg, p);
        System.out.flush();
    }

    static void err_printStack(Throwable exception) {
        exception.printStackTrace(System.err);
    }

    public static String in_readLine() {
        if (inScanner == null) {
            inScanner = new Scanner(System.in);
        }
        return inScanner.nextLine();
    }

    /**
     * @app.category Internal
     */
    private interface ConfirmDelete {

        boolean isForce();

        void setForce(boolean value);

        boolean accept(File directory);

        void ignore(File directory);
    }

    /**
     * @app.category Internal
     */
    private static class SimpleConfirmDelete implements ConfirmDelete {

        private final List<File> ignoreDeletion = new ArrayList<>();
        private boolean force;

        @Override
        public boolean isForce() {
            return force;
        }

        @Override
        public void setForce(boolean value) {
            this.force = value;
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

        public void ignore(File directory) {
            ignoreDeletion.add(directory);
        }
    }

    /**
     * @app.category Internal
     */
    public static class Deps {

        LinkedHashSet<NutsBootId> deps = new LinkedHashSet<>();
        LinkedHashSet<String> repos = new LinkedHashSet<>();
    }

    public static class ErrorInfoList {
        private final List<ErrorInfo> all = new ArrayList<>();

        public void removeErrorsFor(String nutsId) {
            all.removeIf(x -> x.getNutsId().equals(nutsId));
        }

        public void add(ErrorInfo e) {
            all.add(e);
        }

        public List<ErrorInfo> list() {
            return all;
        }
    }

    public static class ErrorInfo {
        private final String nutsId;
        private final String repository;
        private final String url;
        private final String message;
        private final Throwable throwable;

        public ErrorInfo(String nutsId, String repository, String url, String message, Throwable throwable) {
            this.nutsId = nutsId;
            this.repository = repository;
            this.url = url;
            this.message = message;
            this.throwable = throwable;
        }

        public String getNutsId() {
            return nutsId;
        }

        public String getRepository() {
            return repository;
        }

        public String getUrl() {
            return url;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public String toString() {
            return getMessage() + " " + getNutsId() + " from " + getUrl() + " (repository " + getRepository() + ") : "
                    + (getThrowable()==null?"":getThrowable().toString());
        }
    }
}
