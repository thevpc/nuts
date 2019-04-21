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
package net.vpc.app.nuts.core.util.io;

import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.common.DefaultPersistentMap;
import net.vpc.app.nuts.core.util.common.PersistentMap;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsVersion;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultHttpTransportComponent;
import net.vpc.app.nuts.core.DefaultNutsDescriptorContentParserContext;
import net.vpc.app.nuts.core.util.CharacterizedFile;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreIOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final Logger LOG = Logger.getLogger(CoreIOUtils.class.getName());
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
    private static final char[] HEX_ARR = "0123456789ABCDEF".toCharArray();

    public static int execAndWait(NutsDefinition nutMainFile, NutsWorkspace workspace, NutsSession session, Properties execProperties, String[] args, Map<String, String> env, String directory, NutsSessionTerminal terminal, boolean showCommand, boolean failFast) throws NutsExecutionException {
        NutsId id = nutMainFile.getId();
        Path installerFile = nutMainFile.getPath();
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
        map.put("nuts.file", nutMainFile.getPath().toString());
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
                    if (nutsDefinition.getPath() != null) {
                        return ("<::expand::> " + apply("java") + " -jar " + nutsDefinition.getPath());
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
                Collections.addAll(args2, NutsCommandLine.parseCommandLine(s));
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
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "No valid JRE found. recommended {0} . Using default java.home at {1}", new Object[]{requestedJavaVersion, System.getProperty("java.home")});
                    }
                } else {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "No valid JRE found. Using default java.home at {0}", System.getProperty("java.home"));
                    }
                }
                bestJava = current;
            }
        }
        return bestJava;
    }

    public static int execAndWait(NutsWorkspace ws, String[] args, Map<String, String> env, Path directory, NutsSessionTerminal terminal, boolean showCommand, boolean failFast) {
        PrintStream out = terminal.out();
        PrintStream err = terminal.err();
        InputStream in = terminal.in();
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

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "[exec] {0}", pb.getCommandString());
        }
        if (showCommand) {
            if (terminal.out() instanceof NutsFormattedPrintStream) {
                terminal.out().print("==[exec]== ");
            } else {
                terminal.out().print("exec ");
            }
            terminal.out().printf("%s\n", pb.getCommandString());
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
                URI uri = new URL(s).toURI();
                if (uri.getAuthority() != null && uri.getAuthority().length() > 0) {
                    // Hack for UNC Path
                    uri = (new URL("file://" + s.substring("file:".length()))).toURI();
                }

                return Paths.get(uri);
            } catch (URISyntaxException ex) {
                throw new NutsParseException("Not a file Path : " + s);
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
            throw new NutsParseException("Not a file Path");
        }
        if (isURL(s)) {
            throw new NutsParseException("Not a file Path");
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

    public static CharacterizedFile characterize(NutsWorkspace ws, InputSource contentFile, NutsFetchCommand options, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(ws, session);
        CharacterizedFile c = new CharacterizedFile();
        try {
            c.contentFile = contentFile;
            if (c.contentFile.getSource() instanceof Path) {
                //okkay
            } else if (c.contentFile.getSource() instanceof File) {
                c.contentFile = createInputSource(((File) c.contentFile.getSource()).toPath());
            } else {
                Path temp = ws.io().createTempFile(contentFile.getName());
                contentFile.copyTo(temp);
                c.contentFile = createInputSource(temp).multi();
                c.addTemp(temp);
                return characterize(ws, createInputSource(temp).multi(), options, session);
            }
            Path fileSource = (Path) c.contentFile.getSource();
            if (!Files.exists(fileSource)) {
                throw new NutsIllegalArgumentException("File does not exists " + fileSource);
            }
            if (Files.isDirectory(fileSource)) {
                Path ext = fileSource.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                if (Files.exists(ext)) {
                    c.descriptor = ws.parser().parseDescriptor(ext);
                } else {
                    c.descriptor = resolveNutsDescriptorFromFileContent(ws, c.contentFile, options, session);
                }
                if (c.descriptor != null) {
                    if ("zip".equals(c.descriptor.getPackaging())) {
                        Path zipFilePath = ws.io().path(ws.io().expandPath(fileSource.toString() + ".zip"));
                        ZipUtils.zip(fileSource.toString(), new ZipOptions(), zipFilePath.toString());
                        c.contentFile = createInputSource(zipFilePath).multi();
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                    }
                }
            } else if (Files.isRegularFile(fileSource)) {
                File ext = new File(ws.io().expandPath(fileSource.toString() + "." + NutsConstants.Files.DESCRIPTOR_FILE_NAME));
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
        if (id.getVersion().isBlank()) {
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
                LOG.log(Level.SEVERE, "Erroneous config file. Unable to load file {0} : {1}", new Object[]{file, ex.toString()});
                if (!ws.config().isReadOnly()) {
                    Path newfile = file.getParent().resolve("nuts-repository-" + new SimpleDateFormat("yyyy-MM-dd-HHmmss").format(new Date()) + ".json");
                    LOG.log(Level.SEVERE, "Erroneous config file will replace by fresh one. Old config is copied to {0}", newfile.toString());
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
        session = NutsWorkspaceUtils.validateSession(ws, session);
        return (session == null || session.getTerminal() == null) ? ws.io().nullPrintStream() : session.getTerminal().out();
    }

    public static NutsDescriptor resolveNutsDescriptorFromFileContent(NutsWorkspace ws, InputSource localPath, NutsFetchCommand queryOptions, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(ws, session);
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
     * copy input stream to output stream using the buffer size in bytes
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

    public static InputStream monitor(InputStream from, Object source, InputStreamMonitor monitor) {
        String sourceName = null;
        long length = -1;
        if (from instanceof InputStreamMetadataAware) {
            final InputStreamMetadataAware m = (InputStreamMetadataAware) from;
            sourceName = m.getMetaData().getName();
            length = m.getMetaData().getLength();
        }
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
                    LOG.log(Level.FINEST, "Delete file " + file);
                    deleted[0]++;
                } catch (IOException e) {
                    LOG.log(Level.FINEST, "Delete file Failed : " + file);
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
                    LOG.log(Level.FINEST, "Delete folder " + dir);
                    deleted[1]++;
                } catch (IOException e) {
                    LOG.log(Level.FINEST, "Delete folder Failed : " + dir);
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

    public static InputSource createInputSource(InputStream source) {
        if (source == null) {
            return null;
        }
        return new InputStreamSource(String.valueOf(source), source);
    }

    public static InputSource createInputSource(Path source) {
        if (source == null) {
            return null;
        }
        return new PathInputSource(source.getFileName().toString(), source);
    }

    public static InputSource createInputSource(File source) {
        if (source == null) {
            return null;
        }
        return createInputSource(source.toPath());
    }

    public static InputSource createInputSource(byte[] source) {
        if (source == null) {
            return null;
        }
        return new ByteArrayInputSource(String.valueOf(source), source);
    }

    public static InputSource createInputSource(URL source) {
        if (source == null) {
            return null;
        }
        if (isPathFile(source.toString())) {
            return createInputSource(toPathFile(source.toString()));
        }
        return new URLInputSource(source.toString(), source);
    }

    public static InputSource createInputSource(String source) {
        if (source == null) {
            return null;
        }
        if (isPathFile(source)) {
            return createInputSource(toPathFile(source));
        }
        URL baseURL = null;
        try {
            baseURL = new URL(source);
        } catch (Exception ex) {
            //
        }
        if (baseURL != null) {
            return new URLInputSource(source, baseURL);
        }

        throw new NutsUnsupportedArgumentException("Unsupported source : " + source);
    }

    public static boolean isValidInputStreamSource(Class type) {
        return URL.class.isAssignableFrom(type)
                || File.class.isAssignableFrom(type)
                || Path.class.isAssignableFrom(type)
                || byte[].class.isAssignableFrom(type)
                || InputStream.class.isAssignableFrom(type)
                || String.class.isAssignableFrom(type)
                || InputSource.class.isAssignableFrom(type);
    }

    public static InputSource createInputSource(Object source) {
        if (source == null) {
            return null;
        } else if (source instanceof InputSource) {
            return (InputSource) source;
        } else if (source instanceof InputStream) {
            return createInputSource((InputStream) source);
        } else if (source instanceof Path) {
            return createInputSource((Path) source);
        } else if (source instanceof File) {
            return createInputSource((File) source);
        } else if (source instanceof URL) {
            return createInputSource((URL) source);
        } else if (source instanceof byte[]) {
            return createInputSource(new ByteArrayInputStream((byte[]) source));
        } else if (source instanceof String) {
            return createInputSource((String) source);
        } else {
            throw new NutsUnsupportedArgumentException("Unsupported type " + source.getClass().getName());
        }
    }

    public static TargetItem createTarget(OutputStream target) {
        if (target == null) {
            return null;
        }
        return new TargetItem(target, false) {
            @Override
            public OutputStream open() {
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
        throw new NutsUnsupportedArgumentException("Unsuported source : " + target);
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
            public OutputStream open() {
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
            public OutputStream open() {
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
        } else if (target instanceof TargetItem) {
            return (TargetItem) target;
        } else if (target instanceof OutputStream) {
            return createTarget((OutputStream) target);
        } else if (target instanceof Path) {
            return createTarget((Path) target);
        } else if (target instanceof File) {
            return createTarget((File) target);
        } else if (target instanceof String) {
            return createTarget((String) target);
        } else {
            throw new NutsUnsupportedArgumentException("Unsupported type " + target.getClass().getName());
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

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = HEX_ARR[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARR[v & 15];
        }
        return new String(hexChars);
    }

    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            sb.append(toHex(aByte >> 4));
            sb.append(toHex(aByte));
        }
        return sb.toString();
    }

    private static char toHex(int nibble) {
        return HEX_ARR[nibble & 15];
    }

    public static byte[] evalMD5(String input) {
        try {
            byte[] bytesOfMessage = input.getBytes("UTF-8");
            return evalMD5(bytesOfMessage);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] evalMD5(byte[] bytesOfMessage) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("MD5");
            return md.digest(bytesOfMessage);
        } catch (NoSuchAlgorithmException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    public static String evalSHA1(Path file) {
        try {
            return evalSHA1(Files.newInputStream(file), true);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String evalSHA1(File file) {
        try {
            return evalSHA1(new FileInputStream(file), true);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String evalSHA1(String input) {
        return evalSHA1(new ByteArrayInputStream(input.getBytes()), true);
    }

    public static String evalSHA1(InputStream input, boolean closeStream) {
        try {
            MessageDigest sha1 = null;
            try {
                sha1 = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException ex) {
                throw new UncheckedIOException(new IOException(ex));
            }
            byte[] buffer = new byte[8192];
            int len = 0;
            try {
                len = input.read(buffer);
                while (len != -1) {
                    sha1.update(buffer, 0, len);
                    len = input.read(buffer);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return toHexString(sha1.digest());
        } finally {
            if (closeStream) {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
        }
    }

    public static class DefaultMultiReadSourceItem implements MultiInputSource {

        private InputSource base;
        private byte[] content;

        public DefaultMultiReadSourceItem(InputSource base) {
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
        public URL getURL() {
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
        public MultiInputSource multi() {
            return this;
        }

        @Override
        public void copyTo(Path path) {
            base.copyTo(path);
        }

        @Override
        public void close() {
            base.close();
        }

    }

    public static abstract class AbstractMultiReadSourceItem
            extends AbstractSourceItem
            implements MultiInputSource {

        public AbstractMultiReadSourceItem(String name, Object value, boolean path, boolean url) {
            super(name, value, path, url);
        }

    }

    public static abstract class AbstractSourceItem implements InputSource {

        Object value;
        boolean path;
        boolean url;
        String name;

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

        @Override
        public Path getPath() {
            throw new NutsUnsupportedOperationException();
        }

        @Override
        public URL getURL() {
            throw new NutsUnsupportedOperationException();
        }

        @Override
        public Object getSource() {
            return value;
        }

        @Override
        public abstract InputStream open();

        @Override
        public MultiInputSource multi() {
            if (this instanceof MultiInputSource) {
                return (MultiInputSource) this;
            }
            return new DefaultMultiReadSourceItem(this);
        }

        @Override
        public void close() {
        }

        @Override
        public void copyTo(Path path) {
            try {
                Files.copy(open(), path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
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

        public abstract OutputStream open();
    }

    public static InputSource getCachedUrlWithSHA1(NutsWorkspace ws, String path, NutsSession session) {
        final Path cacheBasePath = ws.config().getStoreLocation(ws.config().getRuntimeId(), NutsStoreLocation.CACHE);
        final Path urlContent = cacheBasePath.resolve("urls-content");
        ByteArrayOutputStream t = new ByteArrayOutputStream();
        ws.io().copy().from(path + ".sha1").to(t).run();
        String sha1 = new String(t.toByteArray()).trim();
        final PersistentMap<String, String> cu = getCachedUrls(ws);
        String cachedSha1 = cu.get("sha1://" + path);
        if (cachedSha1 != null && sha1.equalsIgnoreCase(cachedSha1)) {
            String cachedID = cu.get("id://" + path);
            if (cachedID != null) {
                Path p = urlContent.resolve(cachedID);
                if (Files.exists(p)) {
                    return createInputSource(p);
                }
            }
        }
        try {
            final String s = UUID.randomUUID().toString();
            final Path outPath = urlContent.resolve(s + "~");
            Files.createDirectories(urlContent);
            final OutputStream p = Files.newOutputStream(outPath);
            NutsURLHeader header = null;
            long size = -1;
            NutsHttpConnectionFacade f = CoreIOUtils.getHttpClientFacade(ws, path);
            try {

                header = f.getURLHeader();
                size = header.getContentLength();
            } catch (Exception ex) {
                //ignore error
            }

            InputStreamTee ist = new InputStreamTee(f.open(), p, () -> {
                if (Files.exists(outPath)) {
                    cu.put("id://" + path, s);
                    cu.put("sha1://" + path, evalSHA1(outPath));
                    try {
                        Files.move(outPath, urlContent.resolve(s), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                    cu.flush();
                }
            });
            return createInputSource(new InputStreamMetadataAwareImpl(ist, new FixedInputStreamMetadata(path, size)));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static PersistentMap<String, String> getCachedUrls(NutsWorkspace ws) {
        final String k = PersistentMap.class.getName() + ":getCachedUrls";
        PersistentMap<String, String> m = (PersistentMap<String, String>) ws.getUserProperties().get(k);
        if (m == null) {
            m = new DefaultPersistentMap<String, String>(String.class, String.class, ws.config().getStoreLocation(
                    ws.config().getRuntimeId(),
                    NutsStoreLocation.CACHE
            ).resolve("urls-db").toFile());
            ws.getUserProperties().put(k, m);
        }
        return m;
    }

    private static class ByteArrayInputSource extends AbstractMultiReadSourceItem {

        public ByteArrayInputSource(String name, byte[] value) {
            super(name, value, false, false);
        }

        @Override
        public InputStream open() {
            byte[] bytes = (byte[]) this.getSource();
            return new InputStreamMetadataAwareImpl(new ByteArrayInputStream(bytes), new FixedInputStreamMetadata(name, bytes.length));
        }
    }

    private static class URLInputSource extends AbstractSourceItem {

        public URLInputSource(String name, URL value) {
            super(name, value, false, true);
        }

        @Override
        public URL getURL() {
            return (URL) getSource();
        }

        @Override
        public InputStream open() {
            try {
                URL u = getURL();
                if (CoreIOUtils.isPathHttp(u.toString())) {
                    try {
                        NutsHttpConnectionFacade hf = DefaultHttpTransportComponent.INSTANCE.open(u.toString());
                        long len = hf.getURLHeader().getContentLength();
                        return new InputStreamMetadataAwareImpl(u.openStream(), new FixedInputStreamMetadata(u.toString(), len));
                    } catch (Exception ex) {
                        //ignore
                    }
                }
                return u.openStream();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    private static class PathInputSource extends AbstractMultiReadSourceItem {

        public PathInputSource(String name, Path value) {
            super(name, value, true, true);
        }

        @Override
        public Path getPath() {
            return (Path) getSource();
        }

        @Override
        public URL getURL() {
            try {
                return getPath().toUri().toURL();
            } catch (MalformedURLException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        @Override
        public InputStream open() {
            try {
                Path p = getPath();
                return new InputStreamMetadataAwareImpl(Files.newInputStream(p), new FixedInputStreamMetadata(p.toString(),
                        Files.size(p)));
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
    }

    private static class InputStreamSource extends AbstractSourceItem {

        public InputStreamSource(String name, Object value) {
            super(name, value, false, false);
        }

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
    }

    public static void storeProperties(Properties props, OutputStream out) {
        OutputStreamWriter w = new OutputStreamWriter(out);
        try {
            props.store(w, null);
            w.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
