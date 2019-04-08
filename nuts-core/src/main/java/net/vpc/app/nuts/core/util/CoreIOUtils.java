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
package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsVersion;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultHttpTransportComponent;
import net.vpc.app.nuts.core.DefaultNutsDescriptorContentParserContext;
import net.vpc.app.nuts.core.util.bundledlibs.io.InputStreamMonitor;
import net.vpc.app.nuts.core.util.bundledlibs.io.MonitoredInputStream;
import net.vpc.app.nuts.core.util.bundledlibs.io.ProcessBuilder2;
import net.vpc.app.nuts.core.util.bundledlibs.io.ZipOptions;
import net.vpc.app.nuts.core.util.bundledlibs.io.ZipUtils;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreIOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final Logger log = Logger.getLogger(CoreIOUtils.class.getName());
    public static final DirectoryStream.Filter<Path> DIR_FILTER = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path pathname) throws IOException {
            try {
                return Files.isDirectory(pathname);
            } catch (Exception e) {
                //ignore
                return false;
            }
        }
    };

    public static int execAndWait(NutsDefinition nutMainFile, NutsWorkspace workspace, NutsSession session, Properties execProperties, String[] args, Map<String, String> env, String directory, NutsSessionTerminal terminal, boolean showCommand, boolean failFast) throws NutsExecutionException {
        NutsId id = nutMainFile.getId();
        Path installerFile = nutMainFile.getContent().getPath();
        Path storeFolder = nutMainFile.getInstallation().getInstallFolder();
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> envmap = new HashMap<>();
//        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
//            map.put((String) entry.getKey(), (String) entry.getValue());
//        }
        for (Map.Entry<Object, Object> entry : execProperties.entrySet()) {
            map.put((String) entry.getKey(), (String) entry.getValue());
        }
        Path nutsJarFile = workspace.fetch().nutsApi().session(session).getResultPath();
        if (nutsJarFile != null) {
            map.put("nuts.jar", nutsJarFile.toAbsolutePath().normalize().toString());
        }
        map.put("nuts.id", id.toString());
        map.put("nuts.id.version", id.getVersion().getValue());
        map.put("nuts.id.name", id.getName());
        map.put("nuts.id.fullName", id.getSimpleName());
        map.put("nuts.id.group", id.getGroup());
        map.put("nuts.file", nutMainFile.getContent().getPath().toString());
        String defaultJavaCommand = resolveJavaCommand("", workspace);

        map.put("nuts.java", defaultJavaCommand);
        if (map.containsKey("nuts.jar")) {
            map.put("nuts.cmd", map.get("nuts.java") + " -jar " + map.get("nuts.jar"));
        }
        map.put("nuts.workspace", workspace.config().getWorkspaceLocation().toString());
        map.put("nuts.version", id.getVersion().getValue());
        map.put("nuts.name", id.getName());
        map.put("nuts.group", id.getGroup());
        map.put("nuts.face", id.getFace());
        map.put("nuts.namespace", id.getNamespace());
        map.put("nuts.id", id.toString());
        if (installerFile != null) {
            map.put("nuts.installer", installerFile.toString());
        }
        if (storeFolder == null && installerFile != null) {
            map.put("nuts.store", installerFile.getParent().toString());
        } else if (storeFolder != null) {
            map.put("nuts.store", storeFolder.toString());
        }
        if (env != null) {
            map.putAll(env);
        }
        Function<String, String> mapper = new Function<String, String>() {
            @Override
            public String apply(String skey) {
                if (skey.equals("java") || skey.startsWith("java#")) {
                    String javaVer = skey.substring(4);
                    if (CoreStringUtils.isBlank(javaVer)) {
                        return defaultJavaCommand;
                    }
                    return resolveJavaCommand(javaVer, workspace);
                } else if (skey.equals("nuts")) {
                    NutsDefinition nutsDefinition;
                    nutsDefinition = workspace.fetch().id(NutsConstants.Ids.NUTS_API).setSession(session).getResultDefinition();
                    if (nutsDefinition.getContent().getPath() != null) {
                        return ("<::expand::> " + apply("java") + " -jar " + nutsDefinition.getContent().getPath());
                    }
                    return null;
                }
                return map.get(skey);
            }
        };
        for (Map.Entry<String, String> e : map.entrySet()) {
            String k = e.getKey();
            if (!CoreStringUtils.isBlank(k)) {
                k = k.replace('.', '_');
                if (!CoreStringUtils.isBlank(e.getValue())) {
                    envmap.put(k, e.getValue());
                }
            }
        }
        List<String> args2 = new ArrayList<>();
        for (String arg : args) {
            String s = CoreStringUtils.trim(CoreStringUtils.replaceDollarPlaceHolders(arg, mapper));
            if (s.startsWith("<::expand::>")) {
                Collections.addAll(args2, NutsMinimalCommandLine.parseCommandLine(s));
            } else {
                args2.add(s);
            }
        }
        args = args2.toArray(new String[0]);

        Path path = workspace.config().getWorkspaceLocation().resolve(args[0]).normalize();
        if (Files.exists(path)) {
            setExecutable(path);
        }
        Path pdirectory = null;
        if (CoreStringUtils.isBlank(directory)) {
            pdirectory = workspace.config().getWorkspaceLocation();
        } else {
            pdirectory = workspace.config().getWorkspaceLocation().resolve(directory);
        }
        return execAndWait(workspace, args, envmap, pdirectory, terminal, showCommand, failFast);
    }

    public static String resolveJavaCommand(String requestedJavaVersion, NutsWorkspace workspace) {
        String bestJavaPath = resolveJdkLocation(requestedJavaVersion, workspace).getPath();
        if (bestJavaPath.contains("/") || bestJavaPath.contains("\\")) {
            Path file = workspace.config().getWorkspaceLocation().resolve(bestJavaPath);
            if (Files.isDirectory(file) && Files.isDirectory(file.resolve("bin"))) {
                bestJavaPath = file.resolve("bin" + File.separatorChar + "java").toString();
            }
        }
        return bestJavaPath;
    }

    public static NutsSdkLocation resolveJdkLocation(String requestedJavaVersion, NutsWorkspace workspace) {
        requestedJavaVersion = CoreStringUtils.trim(requestedJavaVersion);
        NutsSdkLocation bestJava = workspace.config().getSdk("java", requestedJavaVersion);
        if (bestJava == null) {
            NutsSdkLocation current = new NutsSdkLocation(
                    "java",
                    "java.home",
                    System.getProperty("java.home"),
                    System.getProperty("java.version")
            );
            NutsVersionFilter requestedJavaVersionFilter = workspace.parser().parseVersionFilter(requestedJavaVersion);
            if (requestedJavaVersionFilter == null || requestedJavaVersionFilter.accept(DefaultNutsVersion.valueOf(current.getVersion()))) {
                bestJava = current;
            }
            if (bestJava == null) {
                if (!CoreStringUtils.isBlank(requestedJavaVersion)) {
                    if (log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, "No valid JRE found. recommended {0} . Using default java.home at {1}", new Object[]{requestedJavaVersion, System.getProperty("java.home")});
                    }
                } else {
                    if (log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, "No valid JRE found. Using default java.home at {0}", System.getProperty("java.home"));
                    }
                }
                bestJava = current;
            }
        }
        return bestJava;
    }

    public static int execAndWait(NutsWorkspace ws, String[] args, Map<String, String> env, Path directory, NutsSessionTerminal terminal, boolean showCommand, boolean failFast) {
        PrintStream out = terminal.getOut();
        PrintStream err = terminal.getErr();
        InputStream in = terminal.getIn();
        if (ws.getSystemTerminal().isStandardOutputStream(out)) {
            out = null;
        }
        if (ws.getSystemTerminal().isStandardErrorStream(err)) {
            err = null;
        }
        if (ws.getSystemTerminal().isStandardInputStream(in)) {
            in = null;
        }
        ProcessBuilder2 pb = new ProcessBuilder2()
                .setCommand(args)
                .setEnv(env)
                .setIn(in)
                .setOutput(out)
                .setErr(err)
                .setDirectory(directory == null ? null : directory.toFile())
                .setFailFast(failFast);
        if (out == null && err == null && in == null) {
            pb.inheritIO();
        }

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "[exec] {0}", pb.getCommandString());
        }
        if (showCommand) {
            if (terminal.getOut() instanceof NutsFormattedPrintStream) {
                terminal.getOut().print("==[exec]== ");
            } else {
                terminal.getOut().print("exec ");
            }
            terminal.getOut().printf("%s\n", pb.getCommandString());
        }
        try {
            return pb.start().waitFor().getResult();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static NutsURLHeader getURLHeader(URL url) throws UncheckedIOException {
        if (url.getProtocol().equals("file")) {
            File f = toFile(url);
            NutsURLHeader info = new NutsURLHeader(url.toString());
            info.setContentLength(f.length());
            info.setLastModified(new Date(f.lastModified()));
            return info;
        }
        URLConnection conn = null;
        try {
            try {
                conn = url.openConnection();
                if (conn instanceof HttpURLConnection) {
                    ((HttpURLConnection) conn).setRequestMethod("HEAD");
                }
                conn.getInputStream();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            final String f = conn.getHeaderField("Last-Modified");
            final NutsURLHeader info = new NutsURLHeader(url.toString());
            info.setContentType(conn.getContentType());
            info.setContentEncoding(conn.getContentEncoding());
            info.setContentLength(conn.getContentLengthLong());
//            info.setLastModified(parseHttpDate(f));
            long m = conn.getLastModified();
            info.setLastModified(m == 0 ? null : new Date(m));
            return info;
        } finally {
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).disconnect();
            }
        }
    }

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

//    public static void copy(URL url, File to) throws IOException {
//        try {
//            InputStream in = url.openStream();
//            if (in == null) {
//                throw new IOException("Empty Stream " + url);
//            }
//            if (to.getParentFile() != null) {
//                if (!to.getParentFile().isDirectory()) {
//                    boolean mkdirs = to.getParentFile().mkdirs();
//                    if (!mkdirs) {
//                        log.log(Level.CONFIG, "[ERROR  ] Error creating folder {0}", new Object[]{url});
//                    }
//                }
//            }
//            ReadableByteChannel rbc = Channels.newChannel(in);
//            FileOutputStream fos = new FileOutputStream(to);
//            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
//        } catch (IOException ex) {
//            log.log(Level.CONFIG, "[ERROR  ] Error copying {0} to {1} : {2}", new Object[]{url, to, ex.toString()});
//            throw ex;
//        }
//    }
    public static URL[] toURL(String[] all) throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        if (all != null) {
            for (String s : all) {
                if (!CoreStringUtils.isBlank(s)) {
                    try {
                        URL u = new URL(s);
                        urls.add(u);
                    } catch (MalformedURLException e) {
                        //
                        urls.add(new File(s).toURI().toURL());
                    }
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    public static URL[] toURL(File[] all) throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        if (all != null) {
            for (File s : all) {
                if (s != null) {
                    urls.add(s.toURI().toURL());
                }
            }
        }
        return urls.toArray(new URL[0]);
    }

    public static File toFile(String url) {
        if (CoreStringUtils.isBlank(url)) {
            return null;
        }
        URL u;
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

    public static boolean setExecutable(Path path) {
        if (Files.exists(path) && !Files.isExecutable(path)) {
            PosixFileAttributeView p = Files.getFileAttributeView(path, PosixFileAttributeView.class);
            if (p != null) {
                try {
                    Set<PosixFilePermission> old = new HashSet<>(p.readAttributes().permissions());
                    old.add(PosixFilePermission.OWNER_EXECUTE);
                    Files.setPosixFilePermissions(path, old);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                return true;
            }
        }
        return false;
    }

//    public static Path pathOf(String... s) {
//        return Paths.get(s[0], Arrays.copyOfRange(s, 1, s.length));
//    }
//    public static Path pathOf(String s) {
//        if (CoreStringUtils.isEmpty(s)) {
//            return null;
//        }
//        return Paths.get(s);
//    }
//    public static InputStreamSource toInputStreamSource(Object anyObject) {
//        try {
//            if (anyObject instanceof Path) {
//                // IOUtils.toInputStreamSource does not support Path
//                anyObject = ((Path) anyObject).toFile();
//            }
//            return IOUtils.toInputStreamSource(anyObject, null, null, null);
//        } catch (IOException ex) {
//            throw new UncheckedIOException(ex);
//        }
//    }
    public static boolean mkdirs(Path p) {
        if (p != null) {
            try {
                Files.createDirectories(p);
                return true;
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return false;
    }

    public static Path toPathFile(String s) {
        if (CoreStringUtils.isBlank(s)) {
            return null;
        }
        if (s.startsWith("file:")) {
            try {
                return Paths.get(new URL(s).toURI());
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException("Not a file Path");
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        if (s.startsWith("http://")
                || s.startsWith("https://")
                || s.startsWith("ftp://")
                || s.startsWith("jar:")
                || s.startsWith("zip:")
                || s.startsWith("ssh:")) {
            throw new IllegalArgumentException("Not a file Path");
        }
        if (isURL(s)) {
            throw new IllegalArgumentException("Not a file Path");
        }
        return Paths.get(s);
    }

    public static boolean isPathFile(String s) {
        if (CoreStringUtils.isBlank(s)) {
            return false;
        }
        if (s.startsWith("file:")) {
            return true;
        }
        if (s.startsWith("http://")
                || s.startsWith("https://")
                || s.startsWith("ftp://")
                || s.startsWith("jar:")
                || s.startsWith("zip:")
                || s.startsWith("ssh:")) {
            return false;
        }
        if (isURL(s)) {
            return false;
        }
        return true;
    }

    public static boolean isPathLocal(String s) {
        if (CoreStringUtils.isBlank(s)) {
            return false;
        }
        if (s.startsWith("file:")
                || s.startsWith("jar:")
                || s.startsWith("zip:")) {
            return true;
        }
        if (s.startsWith("http://")
                || s.startsWith("https://")
                || s.startsWith("ftp://")
                || s.startsWith("ssh://")) {
            return false;
        }
        if (isURL(s)) {
            return false;
        }
        return true;
    }

    public static boolean isPathHttp(String s) {
        if (CoreStringUtils.isBlank(s)) {
            return false;
        }
        if (s.startsWith("http://")
                || s.startsWith("https://")) {
            return true;
        }
        return false;
    }

    public static boolean isPathURL(String s) {
        if (CoreStringUtils.isBlank(s)) {
            return false;
        }
        if (CoreStringUtils.isBlank(s)
                || s.startsWith("file:")
                || s.startsWith("jar:")
                || s.startsWith("zip:")) {
            return true;
        }
        if (s.startsWith("http://")
                || s.startsWith("https://")
                || s.startsWith("ftp://")
                || s.startsWith("ssh://")) {
            return true;
        }
        if (isURL(s)) {
            return true;
        }
        return false;
    }

    public static CharacterizedFile characterize(NutsWorkspace ws, SourceItem contentFile, NutsFetchCommand options, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, ws);
        CharacterizedFile c = new CharacterizedFile();
        try {
            c.contentFile = contentFile;
            if (c.contentFile.getSource() instanceof Path) {
                //okkay
            } else if (c.contentFile.getSource() instanceof File) {
                c.contentFile = createSource(((File) c.contentFile.getSource()).toPath());
            } else {
                Path temp = ws.io().createTempFile(contentFile.getName());
                contentFile.copyTo(temp);
                c.contentFile = createSource(temp).toMultiReadSourceItem();
                c.addTemp(temp);
                return characterize(ws, createSource(temp).toMultiReadSourceItem(), options, session);
            }
            Path fileSource = (Path) c.contentFile.getSource();
            if (!Files.exists(fileSource)) {
                throw new NutsIllegalArgumentException("File does not exists " + fileSource);
            }
            if (Files.isDirectory(fileSource)) {
                Path ext = fileSource.resolve(NutsConstants.DESCRIPTOR_FILE_NAME);
                if (Files.exists(ext)) {
                    c.descriptor = ws.parser().parseDescriptor(ext);
                } else {
                    c.descriptor = resolveNutsDescriptorFromFileContent(ws, c.contentFile, options, session);
                }
                if (c.descriptor != null) {
                    if ("zip".equals(c.descriptor.getPackaging())) {
                        Path zipFilePath = ws.io().path(ws.io().expandPath(fileSource.toString() + ".zip"));
                        ZipUtils.zip(fileSource.toString(), new ZipOptions(), zipFilePath.toString());
                        c.contentFile = createSource(zipFilePath).toMultiReadSourceItem();
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                    }
                }
            } else if (Files.isRegularFile(fileSource)) {
                File ext = new File(ws.io().expandPath(fileSource.toString() + "." + NutsConstants.DESCRIPTOR_FILE_NAME));
                if (ext.exists()) {
                    c.descriptor = ws.parser().parseDescriptor(ext);
                } else {
                    c.descriptor = resolveNutsDescriptorFromFileContent(ws, c.contentFile, options, session);
                }
            } else {
                throw new NutsIllegalArgumentException("Path does not denote a valid file or folder " + c.contentFile);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return c;
    }

    public static String syspath(String s) {
        return s.replace('/', File.separatorChar);
    }

    public static String resolveRepositoryPath(NutsCreateRepositoryOptions options, Path rootFolder, NutsWorkspace ws) {
        String loc = options.getLocation();
        if (CoreStringUtils.isBlank(loc)) {
            loc = options.getName();
        }
        if (options.getConfig() != null) {
            if (CoreStringUtils.isBlank(loc)) {
                loc = options.getConfig().getName();
            }
        }
        return ws.io().expandPath(loc, rootFolder.toString());
    }

    public static String trimSlashes(String repositoryIdPath) {
        StringBuilder sb = new StringBuilder(repositoryIdPath);
        boolean updated = true;
        while (updated) {
            updated = false;
            if (sb.length() > 0) {
                if (sb.charAt(0) == '/' || sb.charAt(0) == '\\') {
                    sb.delete(0, 1);
                    updated = true;
                } else if (sb.charAt(sb.length() - 1) == '/' || sb.charAt(sb.length() - 1) == '\\') {
                    sb.delete(sb.length() - 1, sb.length());
                    updated = true;
                }
            }
        }
        return sb.toString();
    }

    public static Path resolveNutsDefaultPath(NutsId id, Path storeLocation) {
        if (CoreStringUtils.isBlank(id.getGroup())) {
            throw new NutsElementNotFoundException("Missing group for " + id);
        }
        if (CoreStringUtils.isBlank(id.getName())) {
            throw new NutsElementNotFoundException("Missing name for " + id.toString());
        }
        if (id.getVersion().isEmpty()) {
            throw new NutsElementNotFoundException("Missing version for " + id.toString());
        }
        Path groupFolder = storeLocation.resolve(id.getGroup().replace('.', File.separatorChar));
        Path artifactFolder = groupFolder.resolve(id.getName());
        return artifactFolder.resolve(id.getVersion().getValue());
    }

    public static NutsRepositoryConfig loadNutsRepositoryConfig(Path file, NutsWorkspace ws) {
        NutsRepositoryConfig conf = null;
        if (Files.isRegularFile(file)) {
            try {
                conf = ws.io().readJson(file, NutsRepositoryConfig.class);
            } catch (RuntimeException ex) {
                log.log(Level.SEVERE, "Erroneous config file. Unable to load file {0} : {1}", new Object[]{file, ex.toString()});
                if (!ws.config().isReadOnly()) {
                    Path newfile = file.getParent().resolve("nuts-repository-" + new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date()) + ".json");
                    log.log(Level.SEVERE, "Erroneous config file will replace by fresh one. Old config is copied to {0}", newfile.toString());
                    try {
                        Files.move(file, newfile);
                    } catch (IOException e) {
                        throw new UncheckedIOException("Unable to load and re-create config file " + file.toString() + " : " + e.toString(), new IOException(ex));
                    }
                } else {
                    throw new UncheckedIOException("Unable to load config file " + file.toString(), new IOException(ex));
                }
            }
        }
        return conf;
    }

    public static String resolveJavaCommand(String javaHome) {
        String exe = CoreIOUtils.getPlatformOsFamily().equals("windows") ? "java.exe" : "java";
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
            if (CoreStringUtils.isBlank(javaHome) || "null".equals(javaHome)) {
                //this may happen is using a precompiled image (such as with graalvm)
                return exe;
            }
        }
        return javaHome + File.separator + "bin" + File.separator + exe;
    }

    public static PrintStream resolveOut(NutsWorkspace ws, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, ws);
        return (session == null || session.getTerminal() == null) ? ws.io().nullPrintStream() : session.getTerminal().getOut();
    }

    public static NutsDescriptor resolveNutsDescriptorFromFileContent(NutsWorkspace ws, CoreIOUtils.SourceItem localPath, NutsFetchCommand queryOptions, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, ws);
        if (localPath != null) {
            List<NutsDescriptorContentParserComponent> allParsers = ws.extensions().createAllSupported(NutsDescriptorContentParserComponent.class, ws);
            if (allParsers.size() > 0) {
                String fileExtension = CoreIOUtils.getFileExtension(localPath.getName());
                NutsDescriptorContentParserContext ctx = new DefaultNutsDescriptorContentParserContext(ws, session, localPath, fileExtension, null, null, queryOptions);
                for (NutsDescriptorContentParserComponent parser : allParsers) {
                    NutsDescriptor desc = null;
                    try {
                        desc = parser.parse(ctx);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (desc != null) {
                        return desc;
                    }
                }
            }
        }
        return null;
    }

    public static String getPath(NutsId id, String ext, char sep) {
        StringBuilder sb = new StringBuilder();
        sb.append(id.getGroup().replace('.', sep));
        sb.append(sep);
        sb.append(id.getName());
        sb.append(sep);
        sb.append(id.getVersion().toString());
        sb.append(sep);
        String name = id.getName() + "-" + id.getVersion().getValue();
        sb.append(name);
        sb.append(ext);
        return sb.toString();
    }

    /**
     * copy le flux d'entree dans le lux de sortie
     *
     * @param in entree
     * @param out sortie
     * @throws IOException when IO error
     */
    public static void copy(InputStream in, OutputStream out) throws IOException {
        copy(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * copy le flux d'entree dans le lux de sortie
     *
     * @param in entree
     * @param out sortie
     * @throws IOException when IO error
     */
    public static void copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int len;

        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }

    }

    public static String loadString(InputStream is, boolean close) {
        try {
            try {
                byte[] bytes = loadByteArray(is);
                return new String(bytes);
            } finally {
                if (is != null && close) {
                    is.close();
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static byte[] loadByteArray(InputStream r) throws IOException {
        ByteArrayOutputStream out = null;

        try {
            out = new ByteArrayOutputStream();
            copy(r, out);
            out.flush();
            return out.toByteArray();
        } finally {
            if (out != null) {
                out.close();
            }
        }

    }

    public static byte[] loadByteArray(InputStream r, boolean close) throws IOException {
        ByteArrayOutputStream out = null;

        try {
            out = new ByteArrayOutputStream();
            copy(r, out);
            out.flush();
            return out.toByteArray();
        } finally {
            if (out != null) {
                out.close();
            }
            if (r != null && close) {
                r.close();
            }
        }

    }

    public static byte[] loadByteArray(InputStream stream, int maxSize, boolean close) throws IOException {
        try {
            if (maxSize > 0) {
                ByteArrayOutputStream to = new ByteArrayOutputStream();
                byte[] bytes = new byte[Math.max(maxSize, 10240)];
                int count;
                int all = 0;
                while ((count = stream.read(bytes)) > 0) {
                    if (all + count < maxSize) {
                        to.write(bytes, 0, count);
                        all += count;
                    } else {
                        int count2 = maxSize - all;
                        to.write(bytes, 0, count2);
                        all += count2;
                        break;
                    }
                }
                return to.toByteArray();
            } else {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                copy(stream, os, close, true);
                return os.toByteArray();
            }
        } finally {
            if (close) {
                stream.close();
            }
        }
    }

    public static long copy(InputStream from, OutputStream to, boolean closeInput, boolean closeOutput) throws IOException {
        byte[] bytes = new byte[1024];//
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

    public static InputStream monitor(URL from, InputStreamMonitor monitor) throws IOException {
        return monitor(from.openStream(), from, getURLName(from), CoreIOUtils.getURLHeader(from).getContentLength(), monitor);
    }

    public static InputStream monitor(InputStream from, Object source, String sourceName, long length, InputStreamMonitor monitor) {
        return new MonitoredInputStream(from, source, sourceName, length, monitor);
    }

    public static void delete(File file) throws IOException {
        delete(file.toPath());
    }

    public static void delete(Path file) throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        if (Files.isRegularFile(file)) {
            try {
                Files.delete(file);
            } catch (IOException e) {
                return;
            }
        }
        final int[] deleted = new int[]{0, 0, 0};

        Files.walkFileTree(file, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    Files.delete(file);
                    log.log(Level.FINEST, "Delete file " + file);
                    deleted[0]++;
                } catch (IOException e) {
                    log.log(Level.FINEST, "Delete file Failed : " + file);
                    deleted[2]++;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                try {
                    Files.delete(dir);
                    log.log(Level.FINEST, "Delete folder " + dir);
                    deleted[1]++;
                } catch (IOException e) {
                    log.log(Level.FINEST, "Delete folder Failed : " + dir);
                    deleted[2]++;
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static String getNativePath(String path) {
        return path.replace('/', File.separatorChar);
    }
//    public static String getAbsoluteFile(File s) {
//        return s.toPath().normalize().toAbsolutePath().toString();
//    }
//    

    public static String getFileExtension(String s) {
        int i = s.lastIndexOf('.');
        if (i == 0) {
            return s.substring(1);
        } else if (i > 0) {
            if (i < (s.length() - 1)) {
                return s.substring(i + 1);
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public static String buildUrl(String url, String path) {
        if (!url.endsWith("/")) {
            if (path.startsWith("/")) {
                return url + path;
            } else {
                return url + "/" + path;
            }
        } else {
            if (path.startsWith("/")) {
                return url + path.substring(1);
            } else {
                return url + path;
            }
        }
    }

    public static boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    public static String getURLName(URL url) {
        String path = url.getFile();
        String name;
        int index = path.lastIndexOf('/');
        if (index < 0) {
            name = path;
        } else {
            name = path.substring(index + 1);
        }
        index = name.indexOf('?');
        if (index >= 0) {
            name = name.substring(0, index);
        }
        name = name.trim();
        return name;
    }

    public static SourceItem createSource(InputStream source) {
        if (source == null) {
            return null;
        }
        return new AbstractSourceItem(String.valueOf(source), source, false, false) {

            @Override
            public InputStream open() {
                return (InputStream) getSource();
            }

            @Override
            public void copyTo(Path path) {
                try {
                    Files.copy(open(), path, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }

        };
    }

    public static SourceItem createSource(Path source) {
        if (source == null) {
            return null;
        }
        return new AbstractMultiReadSourceItem(source.getFileName().toString(), source, true, true) {
            @Override
            public Path getPath() {
                return (Path) getSource();
            }

            @Override
            public URL getURL() throws MalformedURLException {
                return getPath().toUri().toURL();
            }

            @Override
            public InputStream open() {
                try {
                    return Files.newInputStream(getPath());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }

            @Override
            public void copyTo(Path path) {
                try {
                    Files.copy(getPath(), path, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
    }

    public static SourceItem createSource(File source) {
        if (source == null) {
            return null;
        }
        return new AbstractMultiReadSourceItem(source.getName(), source, true, true) {
            @Override
            public Path getPath() {
                return ((File) getSource()).toPath();
            }

            @Override
            public URL getURL() throws MalformedURLException {
                return getPath().toUri().toURL();
            }

            @Override
            public InputStream open() {
                try {
                    return Files.newInputStream(getPath());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }

            @Override
            public void copyTo(Path path) {
                try {
                    Files.copy(getPath(), path, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
    }

    public static SourceItem createSource(byte[] source) {
        if (source == null) {
            return null;
        }
        return new AbstractMultiReadSourceItem(String.valueOf(source), source, false, false) {
            @Override
            public Path getPath() {
                throw new IllegalArgumentException("Unsupported");
            }

            @Override
            public URL getURL() throws MalformedURLException {
                throw new IllegalArgumentException("Unsupported");
            }

            @Override
            public InputStream open() {
                return new ByteArrayInputStream(source);
            }

            @Override
            public void copyTo(Path path) {
                try {
                    Files.copy(open(), path, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
    }

    public static SourceItem createSource(URL source) {
        if (source == null) {
            return null;
        }
        Path basePath = null;
        try {
            basePath = Paths.get(((URL) source).toURI());
        } catch (Exception ex) {
            //
        }
        if (basePath != null) {
            Path finalPath = basePath;
            return new AbstractMultiReadSourceItem(
                    finalPath.getFileName().toString(), source,
                    true, true) {
                @Override
                public Path getPath() {
                    return finalPath;
                }

                @Override
                public URL getURL() throws MalformedURLException {
                    return (URL) getSource();
                }

                @Override
                public InputStream open() {
                    try {
                        return Files.newInputStream(finalPath);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }

                @Override
                public void copyTo(Path path) {
                    try {
                        Files.copy(open(), path, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            };
        } else {
            return new AbstractSourceItem(
                    source.toString(),
                    source, false, true) {
                @Override
                public URL getURL() throws MalformedURLException {
                    return (URL) getSource();
                }

                @Override
                public InputStream open() {
                    try {
                        return ((URL) getSource()).openStream();
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }

                @Override
                public void copyTo(Path path) {
                    try {
                        Files.copy(open(), path, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            };
        }
    }

    public static SourceItem createSource(String source) {
        if (source == null) {
            return null;
        }
        Path basePath = null;
        try {
            basePath = Paths.get(source);
        } catch (Exception ex) {
            //
        }
        if (basePath != null) {
            return createSource(basePath);
        }

        try {
            basePath = Paths.get(new URL(source).toURI());
        } catch (Exception ex) {
            //
        }
        if (basePath != null) {
            return createSource(basePath);
        }

        URL baseURL = null;
        try {
            baseURL = new URL(source);
        } catch (Exception ex) {
            //
        }
        if (baseURL != null) {
            return createSource(baseURL);
        }

        throw new IllegalArgumentException("Unsuported source : " + source);
    }

    public static boolean isValidInputStreamSource(Class type) {
        return URL.class.isAssignableFrom(type)
                || File.class.isAssignableFrom(type)
                || Path.class.isAssignableFrom(type)
                || byte[].class.isAssignableFrom(type)
                || InputStream.class.isAssignableFrom(type)
                || String.class.isAssignableFrom(type);
    }

    public static SourceItem createSource(Object source) {
        if (source == null) {
            return null;
        } else if (source instanceof InputStream) {
            return createSource((InputStream) source);
        } else if (source instanceof Path) {
            return createSource((Path) source);
        } else if (source instanceof File) {
            return createSource((File) source);
        } else if (source instanceof URL) {
            return createSource((URL) source);
        } else if (source instanceof byte[]) {
            return createSource(new ByteArrayInputStream((byte[]) source));
        } else if (source instanceof String) {
            return createSource((String) source);
        } else {
            throw new IllegalArgumentException("Unsupported type " + source.getClass().getName());
        }
    }

    public static TargetItem createTarget(OutputStream target) {
        if (target == null) {
            return null;
        }
        return new TargetItem(target, false) {
            @Override
            public OutputStream getStream() {
                return (OutputStream) getValue();
            }
        };
    }

    public static TargetItem createTarget(String target) {
        if (target == null) {
            return null;
        }
        Path basePath = null;
        try {
            basePath = Paths.get(target);
        } catch (Exception ex) {
            //
        }
        if (basePath != null) {
            return createTarget(target);
        }

        try {
            basePath = Paths.get(new URL(target).toURI());
        } catch (Exception ex) {
            //
        }
        if (basePath != null) {
            return createTarget(target);
        }

        URL baseURL = null;
        try {
            baseURL = new URL(target);
        } catch (Exception ex) {
            //
        }
        if (baseURL != null) {
            return createTarget(baseURL);
        }
        throw new IllegalArgumentException("Unsuported source : " + target);
    }

    public static TargetItem createTarget(Path target) {
        if (target == null) {
            return null;
        }
        return new TargetItem(target, true) {
            @Override
            public Path getPath() {
                return (Path) getValue();
            }

            @Override
            public OutputStream getStream() {
                try {
                    return Files.newOutputStream(getPath());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
    }

    public static TargetItem createTarget(File target) {
        if (target == null) {
            return null;
        }
        return new TargetItem(target, true) {
            @Override
            public Path getPath() {
                return ((File) getValue()).toPath();
            }

            @Override
            public OutputStream getStream() {
                try {
                    return Files.newOutputStream(getPath());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
    }

    public static TargetItem createTarget(Object target) {
        if (target == null) {
            return null;
        } else if (target instanceof OutputStream) {
            return createTarget((OutputStream) target);
        } else if (target instanceof Path) {
            return createTarget((Path) target);
        } else if (target instanceof File) {
            return createTarget((File) target);
        } else if (target instanceof String) {
            return createTarget((String) target);
        } else {
            throw new IllegalArgumentException("Unsupported type " + target.getClass().getName());
        }
    }

    public static NutsHttpConnectionFacade getHttpClientFacade(NutsWorkspace ws, String url) throws UncheckedIOException {
        //        System.out.println("getHttpClientFacade "+url);
        NutsTransportComponent best = ws.extensions().createSupported(NutsTransportComponent.class, url);
        if (best == null) {
            best = DefaultHttpTransportComponent.INSTANCE;
        }
        try {
            return best.open(url);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static String urlEncodeString(String s) {
        if (s == null || s.trim().length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Path resolveLocalPathFromURL(URL url) {
        try {
            return new File(url.toURI()).toPath();
        } catch (URISyntaxException e) {
            return new File(url.getPath()).toPath();
        }
    }

    public static URL resolveURLFromResource(Class cls, String urlPath) throws MalformedURLException {
        if (!urlPath.startsWith("/")) {
            throw new NutsIllegalArgumentException("Unable to resolve url from " + urlPath);
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
            throw new NutsIllegalArgumentException("Unable to resolve url from " + urlPath);
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
                    throw new NutsIllegalArgumentException("Unable to encode " + t, ex);
                }
            }
        }
        return encoded.toString();
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

    public static interface MultiReadSourceItem extends SourceItem {

    }

    public static class DefaultMultiReadSourceItem implements MultiReadSourceItem {

        private SourceItem base;
        private byte[] content;

        public DefaultMultiReadSourceItem(SourceItem base) {
            try {
                content = CoreIOUtils.loadByteArray(base.open());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        @Override
        public String getName() {
            return base.getName();
        }

        @Override
        public boolean isPath() {
            return base.isPath();
        }

        @Override
        public boolean isURL() {
            return base.isURL();
        }

        @Override
        public Path getPath() {
            return base.getPath();
        }

        @Override
        public URL getURL() throws MalformedURLException {
            return base.getURL();
        }

        @Override
        public Object getSource() {
            return base.getSource();
        }

        @Override
        public InputStream open() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public MultiReadSourceItem toMultiReadSourceItem() {
            return this;
        }

        @Override
        public void copyTo(Path path) {
            base.copyTo(path);
        }

    }

    public static interface SourceItem {

        String getName();

        boolean isPath();

        boolean isURL();

        Path getPath();

        URL getURL() throws MalformedURLException;

        Object getSource();

        InputStream open();

        void copyTo(Path path);

        MultiReadSourceItem toMultiReadSourceItem();
    }

    public static abstract class AbstractMultiReadSourceItem
            extends AbstractSourceItem
            implements MultiReadSourceItem {

        public AbstractMultiReadSourceItem(String name, Object value, boolean path, boolean url) {
            super(name, value, path, url);
        }

    }

    public static abstract class AbstractSourceItem implements SourceItem {

        private Object value;
        private boolean path;
        private boolean url;
        private String name;

        public AbstractSourceItem(String name, Object value, boolean path, boolean url) {
            this.name = name;
            this.value = value;
            this.path = path;
            this.url = url;
        }

        @Override
        public String getName() {
            return name;
        }

        public boolean isPath() {
            return path;
        }

        public boolean isURL() {
            return url;
        }

        public Path getPath() {
            throw new UnsupportedOperationException("Not supported");
        }

        public URL getURL() throws MalformedURLException {
            throw new UnsupportedOperationException("Not supported");
        }

        public Object getSource() {
            return value;
        }

        public abstract InputStream open();

        @Override
        public MultiReadSourceItem toMultiReadSourceItem() {
            if (this instanceof MultiReadSourceItem) {
                return (MultiReadSourceItem) this;
            }
            return new DefaultMultiReadSourceItem(this);
        }

    }

    public static abstract class TargetItem {

        private Object value;
        private boolean path;

        public TargetItem(Object value, boolean path) {
            this.value = value;
            this.path = path;
        }

        public boolean isPath() {
            return path;
        }

        public Object getValue() {
            return value;
        }

        public Path getPath() {
            throw new UnsupportedOperationException("Not supported");
        }

        public abstract OutputStream getStream();
    }
}
