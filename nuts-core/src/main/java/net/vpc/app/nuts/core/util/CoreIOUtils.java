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
import net.vpc.common.io.*;
import net.vpc.common.strings.StringConverterMap;
import net.vpc.common.strings.StringUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreIOUtils {

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
        Path nutsJarFile = workspace.fetchApiDefinition(session).getContent().getPath();
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
        StringConverterMap mapper = new StringConverterMap(map) {
            @Override
            public String convert(String skey) {
                if (skey.equals("java") || skey.startsWith("java#")) {
                    String javaVer = skey.substring(4);
                    if (StringUtils.isEmpty(javaVer)) {
                        return defaultJavaCommand;
                    }
                    return resolveJavaCommand(javaVer, workspace);
                } else if (skey.equals("nuts")) {
                    NutsDefinition nutsDefinition;
                    nutsDefinition = workspace.fetch(NutsConstants.NUTS_ID_BOOT_API).setSession(session).fetchDefinition();
                    if (nutsDefinition.getContent().getPath() != null) {
                        return ("<::expand::> " + convert("java") + " -jar " + nutsDefinition.getContent().getPath());
                    }
                    return null;
                }
                return super.convert(skey);
            }
        };
        for (Map.Entry<String, String> e : map.entrySet()) {
            String k = e.getKey();
            if (!StringUtils.isEmpty(k)) {
                k = k.replace('.', '_');
                if (!StringUtils.isEmpty(e.getValue())) {
                    envmap.put(k, e.getValue());
                }
            }
        }
        List<String> args2 = new ArrayList<>();
        for (String arg : args) {
            String s = StringUtils.trim(StringUtils.replaceDollarPlaceHolders(arg, mapper));
            if (s.startsWith("<::expand::>")) {
                Collections.addAll(args2, StringUtils.parseCommandline(s));
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
        if (StringUtils.isEmpty(directory)) {
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
        requestedJavaVersion = StringUtils.trim(requestedJavaVersion);
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
                if (!StringUtils.isEmpty(requestedJavaVersion)) {
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

    public static NutsURLHeader getURLHeader(URL url) {
        URLHeaderInfo ii;
        try {
            ii = URLUtils.getURLHeader(url);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        NutsURLHeader x = new NutsURLHeader(ii.getUrl());
        x.setContentEncoding(ii.getContentEncoding());
        x.setContentLength(ii.getContentLength());
        x.setContentType(ii.getContentType());
        x.setLastModified(ii.getLastModified());
        return x;
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
                if (!StringUtils.isEmpty(s)) {
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
        if (StringUtils.isEmpty(url)) {
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
//        if (StringUtils.isEmpty(s)) {
//            return null;
//        }
//        return Paths.get(s);
//    }

    public static InputStreamSource toInputStreamSource(Object anyObject) {
        try {
            if (anyObject instanceof Path) {
                // IOUtils.toInputStreamSource does not support Path
                anyObject = ((Path) anyObject).toFile();
            }
            return IOUtils.toInputStreamSource(anyObject, null, null, null);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }


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
}
