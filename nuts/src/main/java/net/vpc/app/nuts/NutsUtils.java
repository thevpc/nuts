/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/15/17.
 */
final class NutsUtils {
    private static final Logger log = Logger.getLogger(NutsUtils.class.getName());
    private static Pattern JSON_BOOT_KEY_VAL = Pattern.compile("\"(?<key>(boot.+))\"\\s*:\\s*\"(?<val>[^\"]*)\"");

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static String trim(String str) {
        if (str == null) {
            return "";
        }
        return str.trim();
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

    public static String mergeLists(String sep, String... lists) {
        LinkedHashSet<String> all = new LinkedHashSet<>(Arrays.asList(splitAndRemoveDuplicates(Arrays.asList(lists))));
        return join(sep, all);
    }

    public static String join(String sep, String[] items) {
        return join(sep, Arrays.asList(items));
    }

    public static String join(String sep, Collection<String> items) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> i = items.iterator();
        if (i.hasNext()) {
            sb.append(i.next());
        }
        while (i.hasNext()) {
            sb.append(sep);
            sb.append(i.next());
        }
        return sb.toString();
    }

    public static String[] splitAndRemoveDuplicates(List<String>... possibilities) {
        LinkedHashSet<String> allValid = new LinkedHashSet<>();
        for (List<String> initial : possibilities) {
            for (String v : initial) {
                if (!isEmpty(v)) {
                    v = v.trim();
                    for (String v0 : v.split(";")) {
                        v0 = v0.trim();
                        if (!allValid.contains(v0)) {
                            allValid.add(v0);
                        }
                    }
                }
            }
        }
        return allValid.toArray(new String[0]);
    }

    public static String[] splitAndRemoveDuplicates(String... possibilities) {
        LinkedHashSet<String> allValid = new LinkedHashSet<>();
        for (String v : possibilities) {
            if (!isEmpty(v)) {
                v = v.trim();
                for (String v0 : v.split(";")) {
                    v0 = v0.trim();
                    if (!allValid.contains(v0)) {
                        allValid.add(v0);
                    }
                }
            }
        }
        return allValid.toArray(new String[0]);
    }

    public static Map<String, Object> parseJson(String json) {
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine engine = sem.getEngineByName("javascript");
        String script = "Java.asJSONCompatible(" + json + ")";
        Map result = null;
        try {
            result = (Map) engine.eval(script);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("Invalid json " + json);
        }
        return result;
    }

    public static File createFile(String path) {
        return new File(getAbsolutePath(path));
    }

    public static File createFile(File parent, String path) {
        return new File(parent, path);
    }

    public static File createFile(String parent, String path) {
        return new File(getAbsolutePath(parent), path);
    }

    public static String getAbsolutePath(String path) {
        try {
            return getAbsoluteFile(new File(path)).getCanonicalPath();
        } catch (IOException e) {
            return getAbsoluteFile(new File(path)).getAbsolutePath();
        }
    }

    public static File getAbsoluteFile(File path) {
        if (path.isAbsolute()) {
            return path;
        }
        try {
            return path.getCanonicalFile();
        } catch (IOException e) {
            return path.getAbsoluteFile();
        }
    }

    public static String readStringFromURL(URL requestURL) throws IOException {
        try {
            return new String(Files.readAllBytes(Paths.get(requestURL.toURI())));
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
//        try (Scanner scanner = new Scanner(requestURL.openStream(),
//                StandardCharsets.UTF_8.toString())) {
//            scanner.useDelimiter("\\A");
//            return scanner.hasNext() ? scanner.next() : "";
//        }
    }

    public static String readStringFromFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static void copy(InputStream from, File to, boolean mkdirs, boolean closeInput) throws IOException {
        try {
            File parentFile = to.getParentFile();
            if (mkdirs && parentFile != null) {
                parentFile.mkdirs();
            }
            File temp = new File(to.getPath() + "~");
            try {
                Files.copy(from, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Files.move(temp.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } finally {
                temp.delete();
            }
        } finally {
            if (closeInput) {
                from.close();
            }
        }
    }

    public static void copy(File from, File to, boolean mkdirs) throws IOException {
        File parentFile = to.getParentFile();
        if (mkdirs && parentFile != null) {
            parentFile.mkdirs();
        }
        File temp = new File(to.getPath() + "~");
        try {
            Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            temp.delete();
        }
    }

    public static boolean isAbsolutePath(String location) {
        return new File(location).isAbsolute();
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

    public static String expandPath(String path) {
        if (path.equals("~") || path.equals("~/") || path.equals("~\\") || path.equals("~\\")) {
            return System.getProperty("user.home");
        }
        if (path.startsWith("~/") || path.startsWith("~\\")) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    public static File resolvePath(String path, File baseFolder, String nutsHome) {
        if (isEmpty(nutsHome)) {
            nutsHome = NutsConstants.DEFAULT_NUTS_HOME;
        }
        if (path != null && path.length() > 0) {
            String firstItem = "";
            if ('\\' == File.separatorChar) {
                String[] split = path.split("([/\\\\])");
                if (split.length > 0) {
                    firstItem = split[0];
                }
            } else {
                String[] split = path.split("(/|" + File.separatorChar + ")");
                if (split.length > 0) {
                    firstItem = split[0];
                }
            }
            if (firstItem.equals("~~")) {
                return resolvePath(nutsHome + "/" + path.substring(2), null, null);
            } else if (firstItem.equals("~")) {
                return new File(System.getProperty("user.home"), path.substring(1));
            } else if (isAbsolutePath(path)) {
                return new File(path);
            } else if (baseFolder != null) {
                return createFile(baseFolder, path);
            } else {
                return createFile(path);
            }
        }
        return null;
    }

    public static boolean storeProperties(Properties p, File file) {
        Writer writer = null;
        try {
            File parentFile = file.getParentFile();
            if(parentFile!=null){
                parentFile.mkdirs();
            }
            try {
                p.store(writer = new FileWriter(file), null);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
            return true;
        } catch (IOException e) {
            log.log(Level.SEVERE, "[ERROR  ] Unable to store {0}", file);
        }
        return false;
    }

    public static Properties loadFileProperties(File file) {
        Properties props = new Properties();
        InputStream inputStream = null;
        try {
            try {
                if (file != null && file.isFile()) {
                    inputStream = new FileInputStream(file);
                    props.load(inputStream);
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return props;
    }

    public static Properties loadURLProperties(String url,File cacheFile) {
        try {
            if (url != null) {
                return loadURLProperties(new URL(url),cacheFile);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return new Properties();
    }

    public static Properties loadFileProperties(String file) {
        try {
            if (file != null) {
                return loadFileProperties(new File(file));
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return new Properties();
    }

    public static File urlToFile(String url) {
        if (url != null) {
            URL u = null;
            try {
                u = new URL(url);
            } catch (Exception ex) {
                //
            }
            if (u != null) {
                if ("file".equals(u.getProtocol())) {
                    try {
                        return new File(u.toURI());
                    } catch (Exception ex) {
                        return new File(u.getPath());
                    }
                }
            }
        }
        return null;
    }

    public static Properties loadURLProperties(URL url,File cacheFile) {
        long startTime = System.currentTimeMillis();
        Properties props = new Properties();
        InputStream inputStream = null;
        try {
            try {
                if (url != null) {
                    inputStream = url.openStream();
                    if(inputStream!=null) {
                        props.load(inputStream);
                        if(cacheFile!=null && !isFileURL(url.toString())){
                            copy(url.openStream(),cacheFile,true,true);
                            log.log(Level.CONFIG, "[CACHED ] Caching props file to    {0}", new Object[]{cacheFile.getPath()});
                        }
                        long time = System.currentTimeMillis() - startTime;
                        if (time > 0) {
                            log.log(Level.CONFIG, "[SUCCESS] Loading props file from  {0} (time {1})", new Object[]{url.toString(), formatPeriodMilli(time)});
                        } else {
                            log.log(Level.CONFIG, "[SUCCESS] Loading props file from  {0}", new Object[]{url.toString()});
                        }
                    }else{
                        long time = System.currentTimeMillis() - startTime;
                        if (time > 0) {
                            log.log(Level.CONFIG, "[ERROR  ] Loading props file from  {0} (time {1})", new Object[]{url.toString(), formatPeriodMilli(time)});
                        } else {
                            log.log(Level.CONFIG, "[ERROR  ] Loading props file from  {0}", new Object[]{url.toString()});
                        }
                    }
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            if (time > 0) {
                log.log(Level.CONFIG, "[ERROR  ] Loading props file from  {0} (time {1})", new Object[]{url.toString(), formatPeriodMilli(time)});
            } else {
                log.log(Level.CONFIG, "[ERROR  ] Loading props file from  {0}", new Object[]{url.toString()});
            }
            //e.printStackTrace();
        }
        return props;
    }

    public static boolean isRemoteURL(String url) {
        if (url == null) {
            return false;
        }
        url = url.toLowerCase();
        return (url.startsWith("http://") || url.startsWith("https://"));
    }

    public static String resolveWorkspaceLocation(String home, String workspace) {
        if (home == null) {
            home = NutsConstants.DEFAULT_NUTS_HOME;
        }
        if (workspace == null) {
            workspace = NutsConstants.DEFAULT_WORKSPACE_NAME;
        }
        String baseFolder = (new File(workspace).isAbsolute() ? workspace : (home + "/" + workspace));
        baseFolder = baseFolder.replace('\\', '/');
        if (baseFolder.startsWith("~/")) {
            baseFolder = System.getProperty("user.home") + baseFolder.substring(1);
        }
        return baseFolder.replace('/', File.separatorChar);
    }

    public static String toMavenFileName(String nutsId, String extension) {
        String[] arr = nutsId.split("[:#]");
        StringBuilder sb = new StringBuilder();
        sb.append(arr[1]);
        sb.append("-");
        sb.append(arr[2]);
        sb.append(".");
        sb.append(extension);
        return sb.toString();
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

    public static String resolveMavenReleaseVersion(String mavenURLBase, String nutsId) {
        String mvnUrl = (mavenURLBase + toMavenPath(nutsId) + "/maven-metadata.xml");
        String str = null;
        try {
            str = NutsUtils.readStringFromURL(new URL(mvnUrl));
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
        if (str != null) {
            for (String line : str.split("\n")) {
                line = line.trim();
                if (line.startsWith("<release>")) {
                    return line.substring("<release>".length(), line.length() - "</release>".length()).trim();
                }
            }
        }
        throw new NutsIOException("Nuts not found " + nutsId);
    }

    public static boolean isFileURL(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return false;
        }
        return true;
    }

    public static String resolveMavenFullPath(String repo, String nutsId, String ext) {
        String jarPath = toMavenPath(nutsId) + "/" + toMavenFileName(nutsId, ext);
        String mvnUrl = repo;
        String sep = "/";
        if (isFileURL(repo)) {
            sep = File.separator;
        }
        if (!mvnUrl.endsWith("/") && !mvnUrl.endsWith(sep)) {
            mvnUrl += sep;
        }
        return mvnUrl + jarPath;
    }

    public static File resolveOrDownloadJar(String nutsId, String[] repositories, String cacheFolder) {
        String jarPath = toMavenPath(nutsId) + "/" + toMavenFileName(nutsId, "jar");
        for (int i = 0; i < repositories.length; i++) {
            String r = repositories[i];
            log.fine("Checking " + nutsId + " jar from " + r);
            String path = resolveMavenFullPath(r, nutsId, "jar");
            if (!isFileURL(r)) {
                try {
                    File cachedFile = new File(resolveMavenFullPath(cacheFolder, nutsId, "jar"));
                    if (cachedFile.getParentFile() != null) {
                        cachedFile.getParentFile().mkdirs();
                    }
                    ReadableByteChannel rbc = Channels.newChannel(new URL(path).openStream());
                    FileOutputStream fos = new FileOutputStream(cachedFile);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    log.log(Level.CONFIG, "[CACHED ] Caching jar file {0}", new Object[]{cachedFile.getPath()});
                    return cachedFile;
                } catch (Exception ex) {
                    System.err.printf("Unable to load " + nutsId + " from " + r + ".\n");
                    //ex.printStackTrace();
                    //throw new NutsIllegalArgumentException("Unable to load nuts from " + mvnUrl);
                }
            } else {
                //file
                File f = new File(r, jarPath);
                if (f.isFile()) {
                    return f;
                } else {
                    System.err.printf("Unable to load " + nutsId + " from " + r + ".\n");
                }
            }
        }
        return null;
    }


    public static NutsBootConfig loadNutsBootConfig(String nutsHome, String workspace) {
        File versionFile = new File(resolveWorkspaceLocation(nutsHome, workspace), NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
        try {
            if (versionFile.isFile()) {
                String str = readStringFromFile(versionFile);
                if (str != null) {
                    str = str.trim();
                    if (str.length() > 0) {
                        Pattern bootRuntime = JSON_BOOT_KEY_VAL;
                        Matcher matcher = bootRuntime.matcher(str);
                        NutsBootConfig c = new NutsBootConfig();
                        while (matcher.find()) {
                            String k = matcher.group("key");
                            String val = matcher.group("val");
                            if (k != null) {
                                switch (k) {
                                    case "bootAPIVersion": {
                                        c.setBootAPIVersion(val);
                                        break;
                                    }
                                    case "bootRuntime": {
                                        c.setBootRuntime(val);
                                        break;
                                    }
                                    case "bootRepositories": {
                                        c.setBootRepositories(val);
                                        break;
                                    }
                                    case "bootRuntimeDependencies": {
                                        c.setBootRuntimeDependencies(val);
                                        break;
                                    }
                                    case "bootJavaCommand": {
                                        c.setBootJavaCommand(val);
                                        break;
                                    }
                                    case "bootJavaOptions": {
                                        c.setBootJavaOptions(val);
                                        break;
                                    }
                                }
                            }
                        }
                        return c;
                        //return parseJson(str);
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.CONFIG, "Unable to load nuts version file " + versionFile + ".\n", ex);
        }
        return new NutsBootConfig();
    }


    public static List<String> splitUrlStrings(String repositories) {
        return split(repositories, "\n;", true);
    }

    public static NutsBootConfig createNutsBootConfig(Properties properties) {
        String id = properties.getProperty("project.id");
        String version = properties.getProperty("project.version");
        String dependencies = properties.getProperty("project.dependencies.compile");
        if (NutsUtils.isEmpty(id)) {
            throw new IllegalArgumentException("Missing id");
        }
        if (NutsUtils.isEmpty(version)) {
            throw new IllegalArgumentException("Missing version");
        }
        if (NutsUtils.isEmpty(dependencies)) {
            throw new IllegalArgumentException("Missing dependencies");
        }
        String repositories = properties.getProperty("project.repositories");
        if (repositories == null) {
            repositories = "";
        }
        return new NutsBootConfig()
                .setBootRuntime(id + "#" + version)
                .setBootRuntimeDependencies(dependencies)
                .setBootRepositories(repositories)
                ;
    }

    public static int parseFileSize(String s) {
        s = s.toLowerCase();
        int multiplier = 1;
        int val = 1;
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

    public static void showError(NutsBootConfig actualBootConfig, NutsBootConfig bootConfig, String home, String workspace, String extraMessage) {
        System.err.printf("Unable to locate nuts-core component. It is essential for Nuts to work.\n");
        System.err.printf("This component needs Internet connexion to initialize Nuts configuration.\n");
        System.err.printf("Don't panic, once components are downloaded, you will be able to work offline...\n");
        System.err.printf("Here after current environment info :\n");
        System.err.printf("  nuts-boot-api-version            : %s\n", bootConfig.getBootAPIVersion() == null ? "<?> Not Found!" : bootConfig.getBootAPIVersion());
        System.err.printf("  nuts-boot-runtime                : %s\n", bootConfig.getBootRuntime() == null ? "<?> Not Found!" : bootConfig.getBootRuntime());
        System.err.printf("  nuts-workspace-boot-api-version  : %s\n", bootConfig.getBootAPIVersion() == null ? "<?> Not Found!" : bootConfig.getBootAPIVersion());
        System.err.printf("  nuts-workspace-boot-runtime      : %s\n", bootConfig.getBootRuntime() == null ? "<?> Not Found!" : bootConfig.getBootRuntime());
        System.err.printf("  nuts-home                        : %s\n", home);
        System.err.printf("  workspace-location               : %s\n", (workspace == null ? "<default-location>" : workspace));
        System.err.printf("  java-version                     : %s\n", System.getProperty("java.version"));
        System.err.printf("  java-executable                  : %s\n", System.getProperty("java.home") + "/bin/java");
        System.err.printf("  java-class-path                  : %s\n", System.getProperty("java.class.path"));
        System.err.printf("  java-library-path                : %s\n", System.getProperty("java.library.path"));
        System.err.printf("  os-name                          : %s\n", System.getProperty("os.name"));
        System.err.printf("  os-arch                          : %s\n", System.getProperty("os.arch"));
        System.err.printf("  os-version                       : %s\n", System.getProperty("os.version"));
        System.err.printf("  user-name                        : %s\n", System.getProperty("user.name"));
        System.err.printf("  user-home                        : %s\n", System.getProperty("user.home"));
        System.err.printf("  user-dir                         : %s\n", System.getProperty("user.dir"));
        System.err.printf("Reported Error is :\n");
        System.err.printf(extraMessage + "\n");
        System.err.printf("If the problem persists you may want to get more debug info by adding '--verbose' argument :\n");
        System.err.printf("  java -jar nuts.jar --verbose [...]\n");
        System.err.printf("Now exiting Nuts, Bye!\n");
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
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
        }
        String exe = isOSWindow() ? "java.exe" : "java";
        return javaHome + File.separator + "bin" + File.separator + exe;
    }

    public static boolean isOSWindow() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static String[] parseDependenciesFromMaven(URL url,File cacheFile) {

        long startTime = System.currentTimeMillis();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        List<String> deps=new ArrayList<>();
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            InputStream stream = url.openStream();
            if (stream == null) {
                return null;
            }
            Document doc = dBuilder.parse(stream);
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            NodeList properties = doc.getDocumentElement().getElementsByTagName("properties");
            NodeList rootChildList = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < rootChildList.getLength(); i++) {
                Element dependencies = toElement(rootChildList.item(i), "dependencies");
                if (dependencies != null) {
                    NodeList dependenciesChildList = dependencies.getChildNodes();
                    for (int j = 0; j < dependenciesChildList.getLength(); j++) {
                        Element dependency = toElement(dependenciesChildList.item(j), "dependency");
                        if (dependency != null) {
                            NodeList dependencyChildList = dependency.getChildNodes();
                            String groupId="";
                            String artifactId="";
                            String version="";
                            String scope="";
                            for (int k = 0; k < dependencyChildList.getLength(); k++) {
                                Element c = toElement(dependencyChildList.item(k));
                                if (c != null) {
                                    switch (c.getTagName()) {
                                        case "groupId": {
                                            groupId = c.getTextContent() == null ? "" : c.getTextContent().trim();
                                            break;
                                        }
                                        case "artifactId": {
                                            artifactId = c.getTextContent() == null ? "" : c.getTextContent().trim();
                                            break;
                                        }
                                        case "version": {
                                            version = c.getTextContent() == null ? "" : c.getTextContent().trim();
                                            break;
                                        }
                                        case "scope": {
                                            scope = c.getTextContent() == null ? "" : c.getTextContent().trim();
                                            break;
                                        }
                                    }
                                }
                            }
                            if (scope.isEmpty() || scope.equals("compile")) {
                                deps.add(new BootNutsId(
                                        groupId, artifactId, version
                                ).toString());
                            }
                        }
                    }
                }
            }
            if(cacheFile!=null && !isFileURL(url.toString())){
                copy(url.openStream(),cacheFile,true,true);
                log.log(Level.CONFIG, "[CACHED ] Caching pom.xml file {0}", new Object[]{cacheFile.getPath()});
            }
            long time = System.currentTimeMillis() - startTime;
            if (time > 0) {
                log.log(Level.CONFIG, "[SUCCESS] Loading pom.xml file from  {0} (time {1})", new Object[]{url.toString(), formatPeriodMilli(time)});
            } else {
                log.log(Level.CONFIG, "[SUCCESS] Loading pom.xml file from  {0}", new Object[]{url.toString()});
            }
            return deps.toArray(new String[0]);
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            if (time > 0) {
                log.log(Level.CONFIG, "[ERROR  ] Loading pom.xml file from  {0} (time {1})", new Object[]{url.toString(), formatPeriodMilli(time)});
            } else {
                log.log(Level.CONFIG, "[ERROR  ] Loading pom.xml file from  {0}", new Object[]{url.toString()});
            }
            return null;
        }
    }

    private static Element toElement(Node n) {
        if (n instanceof Element) {
            return (Element) n;
        }
        return null;
    }

    private static Element toElement(Node n, String name) {
        if (n instanceof Element) {
            if (((Element) n).getTagName().equals(name)) {
                return (Element) n;
            }
        }
        return null;
    }

    public static boolean deleteAndConfirm(File directoryName,boolean force) throws IOException {
        if(directoryName.exists()) {
            if (!force) {
                Scanner s = new Scanner(System.in);
                System.out.println("Deleting folder " + directoryName);
                System.out.print("\t Are you sure? : ");
                String line = s.nextLine();
                if (!"y".equals(line) && !"yes".equals(line)) {
                    throw new NutsUserCancelException();
                }
            }
            delete(directoryName.getPath());
            return true;
        }
        return false;
    }

    public static void delete(String directoryName) throws IOException {

        Path directory = Paths.get(directoryName);
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file,
                                             BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
