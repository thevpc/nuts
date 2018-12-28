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
package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.core.NutsVersionImpl;
import net.vpc.common.io.*;
import net.vpc.common.strings.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreIOUtils {

    private static final Logger log = Logger.getLogger(CoreIOUtils.class.getName());


    public static int execAndWait(NutsDefinition nutMainFile, NutsWorkspace workspace, NutsSession session, Properties execProperties, String[] args, Map<String, String> env, File directory, NutsTerminal terminal, boolean showCommand, boolean failFast) throws NutsExecutionException {
        NutsId id = nutMainFile.getId();
        File installerFile = nutMainFile.getFile() == null ? null : new File(nutMainFile.getFile());
        File storeFolder = nutMainFile.getInstallFolder() == null ? null : new File(nutMainFile.getInstallFolder());
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> envmap = new HashMap<>();
//        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
//            map.put((String) entry.getKey(), (String) entry.getValue());
//        }
        for (Map.Entry<Object, Object> entry : execProperties.entrySet()) {
            map.put((String) entry.getKey(), (String) entry.getValue());
        }
        String nutsJarFile = workspace.fetchBootFile(session).getFile();
        if (nutsJarFile != null) {
            map.put("nuts.jar", new File(nutsJarFile).getAbsolutePath());
        }
        map.put("nuts.id", id.toString());
        map.put("nuts.id.version", id.getVersion().getValue());
        map.put("nuts.id.name", id.getName());
        map.put("nuts.id.fullName", id.getSimpleName());
        map.put("nuts.id.group", id.getGroup());
        map.put("nuts.file", nutMainFile.getFile());
        String defaultJavaCommand = resolveJavaCommand("", workspace);

        map.put("nuts.java", defaultJavaCommand);
        if (map.containsKey("nuts.jar")) {
            map.put("nuts.cmd", map.get("nuts.java") + " -jar " + map.get("nuts.jar"));
        }
        map.put("nuts.workspace", workspace.getConfigManager().getWorkspaceLocation());
        map.put("nuts.version", id.getVersion().getValue());
        map.put("nuts.name", id.getName());
        map.put("nuts.group", id.getGroup());
        map.put("nuts.face", id.getFace());
        map.put("nuts.namespace", id.getNamespace());
        map.put("nuts.id", id.toString());
        if (installerFile != null) {
            map.put("nuts.installer", installerFile.getPath());
        }
        if (storeFolder == null && installerFile != null) {
            map.put("nuts.store", installerFile.getParentFile().getPath());
        } else if (storeFolder != null) {
            map.put("nuts.store", storeFolder.getPath());
        }
        if (env != null) {
            map.putAll(env);
        }
        MapStringMapper mapper = new MapStringMapper(map) {
            @Override
            public String convert(String skey) {
                if (skey.equals("java") || skey.startsWith("java#")) {
                    String javaVer = skey.substring(4);
                    if (StringUtils.isEmpty(javaVer)) {
                        return defaultJavaCommand;
                    }
                    return resolveJavaCommand(javaVer, workspace);
                } else if (skey.equals("nuts")) {
                    NutsDefinition nutsDefinition = null;
                    nutsDefinition = workspace.fetch(NutsConstants.NUTS_ID_BOOT_API, session);
                    if (nutsDefinition.getFile() != null) {
                        return ("<::expand::> " + convert("java") + " -jar " + nutsDefinition.getFile());
                    }
                    return null;
                }
                return super.convert(skey);
            }
        };
        for (Map.Entry<String, String> e : map.entrySet()) {
            String k = e.getKey();
            if(!StringUtils.isEmpty(k)) {
                k = k.replace('.', '_');
                if (!StringUtils.isEmpty(e.getValue())) {
                    envmap.put(k, e.getValue());
                }
            }
        }
        List<String> args2 = new ArrayList<>();
        for (String arg : args) {
            String s = StringUtils.trim(CoreStringUtils.replaceVars(arg, mapper));
            if (s.startsWith("<::expand::>")) {
                Collections.addAll(args2, StringUtils.parseCommandline(s));
            } else {
                args2.add(s);
            }
        }
        args = args2.toArray(new String[0]);

        File file = FileUtils.getAbsoluteFile(new File(workspace.getConfigManager().getCwd()), args[0]);
        if (file.exists() && !file.canExecute()) {
            if (!file.setExecutable(true)) {
                if (log.isLoggable(Level.WARNING)) {
                    log.log(Level.WARNING, "Unable to set file executable " + file);
                }
            } else {
                if (log.isLoggable(Level.WARNING)) {
                    log.log(Level.WARNING, "Success to set file executable " + file);
                }
            }
        }
        if (directory == null) {
            directory = new File(workspace.getConfigManager().getCwd());
        } else {
            directory = FileUtils.getAbsoluteFile(new File(workspace.getConfigManager().getCwd()), directory.getPath());
        }
        return execAndWait(workspace, args, envmap, directory, terminal, showCommand, failFast);
    }

    public static String resolveJavaCommand(String requestedJavaVersion, NutsWorkspace workspace) {
        String bestJavaPath = resolveJdkLocation(requestedJavaVersion, workspace).getPath();
        if (bestJavaPath.contains("/") || bestJavaPath.contains("\\")) {
            File file = FileUtils.getAbsoluteFile(new File(workspace.getConfigManager().getCwd()), bestJavaPath);
            if (file.isDirectory() && CoreIOUtils.createFile(file, "bin").isDirectory()) {
                bestJavaPath = CoreIOUtils.createFile(bestJavaPath, "bin/java").getPath();
            }
        }
        return bestJavaPath;
    }

    public static NutsSdkLocation resolveJdkLocation(String requestedJavaVersion, NutsWorkspace workspace) {
        requestedJavaVersion = StringUtils.trim(requestedJavaVersion);
        NutsSdkLocation bestJava = workspace.getConfigManager().getSdk("java", requestedJavaVersion);
        if (bestJava == null) {
            NutsSdkLocation current = new NutsSdkLocation(
                    "java.home",
                    System.getProperty("java.home"),
                    System.getProperty("java.version")
            );
            NutsVersionFilter requestedJavaVersionFilter = CoreVersionUtils.createNutsVersionFilter(requestedJavaVersion);
            if (requestedJavaVersionFilter == null || requestedJavaVersionFilter.accept(new NutsVersionImpl(current.getVersion()))) {
                bestJava = current;
            }
            if (bestJava == null) {
                if (!StringUtils.isEmpty(requestedJavaVersion)) {
                    if (log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, "No valid JRE found. recommended " + requestedJavaVersion + " . Using default java.home at " + System.getProperty("java.home"));
                    }
                } else {
                    if (log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, "No valid JRE found. Using default java.home at " + System.getProperty("java.home"));
                    }
                }
                bestJava = current;
            }
        }
        return bestJava;
    }

    public static int execAndWait(NutsWorkspace ws, String[] args, Map<String, String> env, File directory, NutsTerminal terminal, boolean showCommand, boolean failFast) {
        PrintStream out = terminal.getOut();
        PrintStream err = terminal.getErr();
        InputStream in = terminal.getIn();
        if (ws.isStandardOutputStream(out)) {
            out = null;
        }
        if (ws.isStandardErrorStream(err)) {
            err = null;
        }
        if (ws.isStandardInputStream(in)) {
            in = null;
        }
        ProcessBuilder2 pb = new ProcessBuilder2()
                .setCommand(args)
                .setEnv(env)
                .setIn(in)
                .setOutput(out)
                .setErr(err)
                .setDirectory(directory)
                .setFailFast(failFast)
                ;
        if(out==null && err==null && in==null){
            pb.inheritIO();
        }

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "[exec] "+pb.getCommandString());
        }
        if (showCommand) {
            if (terminal.getOut() instanceof NutsFormattedPrintStream) {
                terminal.getOut().print("==[exec]== ");
            } else {
                terminal.getOut().print("exec ");
            }
            terminal.getOut().println(pb.getCommandString());
        }
        return pb.start().waitFor().getResult();
    }


    public static File[] nonNullArray(File[] array1) {
        return array1 == null ? new File[0] : array1;
    }


    public static String readPassword(String prompt, InputStream in, PrintStream out) {
        Console cons = null;
        char[] passwd = null;
        if (in == null) {
            in = System.in;
        }
        if (out == null) {
            out = System.out;
        }
        if (in == System.in && ((cons = System.console()) != null)) {
            if ((passwd = cons.readPassword("[%s]", prompt)) != null) {
                String pwd = new String(passwd);
                Arrays.fill(passwd, ' ');
                return pwd;
            } else {
                return null;
            }
        } else {
            out.print(prompt);
            out.flush();
            Scanner s = new Scanner(in);
            return s.nextLine();
        }

    }

    public static File createFile(String path) {
        return new File(FileUtils.getAbsolutePath(path));
    }

    public static File createFile(File parent, String path) {
        return new File(parent, path);
    }

    public static File createFile(String parent, String path) {
        return new File(FileUtils.getAbsolutePath(parent), path);
    }


    public static URLHeader getURLHeader(URL url) {
        URLHeaderInfo ii = URLUtils.getURLHeader(url);
        URLHeader x = new URLHeader(ii.getUrl());
        x.setContentEncoding(ii.getContentEncoding());
        x.setContentLength(ii.getContentLength());
        x.setContentType(ii.getContentType());
        x.setLastModified(ii.getLastModified());
        return x;
    }


    public static File resolvePath(String path, File baseFolder, String workspaceRoot) {
        if (StringUtils.isEmpty(workspaceRoot)) {
            workspaceRoot = NutsConstants.DEFAULT_NUTS_HOME;
        }
        if (path != null && path.length() > 0) {
            if (path.startsWith("~")) {
                if (path.equals("~~")) {
                    return createFile(workspaceRoot);
                } else if (path.startsWith("~~") && path.length() > 2 && (path.charAt(2) == '/' || path.charAt(2) == '\\')) {
                    return createFile(workspaceRoot, path.substring(3));
                } else if (path.equals("~")) {
                    return new File(System.getProperty("user.home"));
                } else if (path.startsWith("~") && path.length() > 1 && (path.charAt(1) == '/' || path.charAt(1) == '\\')) {
                    return createFile(System.getProperty("user.home"), path.substring(2));
                } else if (baseFolder != null) {
                    return createFile(baseFolder, path);
                } else {
                    return CoreIOUtils.createFile(path);
                }
            } else if (FileUtils.isAbsolutePath(path)) {
                return new File(path);
            } else if (baseFolder != null) {
                return createFile(baseFolder, path);
            } else {
                return CoreIOUtils.createFile(path);
            }
        }
        return baseFolder;
    }

    public static File createTempFile(NutsDescriptor descriptor, boolean desc, File directory) {
        String prefix = "temp-";
        String ext = null;
        if (descriptor != null) {
            ext = StringUtils.trim(descriptor.getExt());
            prefix = StringUtils.trim(descriptor.getId().getGroup()) + "-" + StringUtils.trim(descriptor.getId().getName()) + "-" + StringUtils.trim(descriptor.getId().getVersion().getValue());
            if (prefix.length() < 3) {
                prefix = prefix + "tmp";
            }
            if (!ext.isEmpty()) {
                ext = "." + ext;
                if (ext.length() < 3) {
                    ext = ".tmp" + ext;
                }
            } else {
                ext = "-nuts";
            }
        }
        if (desc) {
            ext = ext + ".nuts";
        }
        try {
            return File.createTempFile(prefix, "-nuts" + (ext != null ? ("." + ext) : ""), directory);
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    public static File createTempFile(String name, boolean desc, File directory) {
        String prefix = "temp-";
        String ext = null;
        if (name != null) {
            ext = FileUtils.getFileExtension(name);
            prefix = name;
            if (prefix.length() < 3) {
                prefix = prefix + "tmp";
            }
            if (!ext.isEmpty()) {
                ext = "." + ext;
                if (ext.length() < 3) {
                    ext = ".tmp" + ext;
                }
            } else {
                ext = "-nuts";
            }
        }
        if (desc) {
            ext = ext + ".nuts";
        }
        try {
            return File.createTempFile(prefix, "-nuts" + (ext != null ? ("." + ext) : ""), directory);
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    public static File createTempFile(NutsDescriptor descriptor, boolean descFile) {
        return createTempFile(descriptor, descFile, null);
    }

    public static File fileByPath(String path) {
        if (path == null) {
            return null;
        }
        return new File(path);
    }

    public static void downloadPath(String from, File to, Object source, NutsWorkspace workspace, NutsSession session) {
        IOUtils.copy(openStream(from, source, workspace, session), to, true, true);
    }

    public static InputStream openStream(String path, Object source, NutsWorkspace workspace, NutsSession session) {
        InputStream stream = null;
        URLHeader header = null;
        long size = -1;
        try {
            NutsHttpConnectionFacade f = CoreHttpUtils.getHttpClientFacade(workspace, path);
            try {

                header = f.getURLHeader();
                size = header.getContentLength();
            } catch (Exception ex) {
                //ignore error
            }
            stream = f.open();
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
        if (stream != null) {
            if (!path.toLowerCase().startsWith("file://")) {
                log.log(Level.FINE, "downloading file {0}", new Object[]{path});
            } else {
                log.log(Level.FINEST, "downloading url {0}", new Object[]{path});
            }
        } else {
            log.log(Level.FINEST, "downloading url failed : {0}", new Object[]{path});
        }
        boolean monitorable = true;
        if (source instanceof NutsId) {
            NutsId d = (NutsId) source;
//            if (CoreNutsUtils.FACE_CATALOG.equals(d.getFace())) {
//                monitorable = false;
//            }
            if (CoreNutsUtils.FACE_PACKAGE_HASH.equals(d.getFace())) {
                monitorable = false;
            }
            if (CoreNutsUtils.FACE_DESC_HASH.equals(d.getFace())) {
                monitorable = false;
            }
//            if (CoreNutsUtils.FACE_DESC.equals(d.getFace())) {
//                monitorable = false;
//            }
//            if ("archetype-catalog".equals(d.getFace())) {
//                monitorable = false;
//            }
//            if ("descriptor".equals(d.getFace())) {
//                monitorable = false;
//            }
//            if ("main-sha1".equals(d.getFace())) {
//                monitorable = false;
//            }
//            if ("descriptor-sha1".equals(d.getFace())) {
//                monitorable = false;
//            }
        }
        if (!monitorable) {
            return stream;
        }
        if (log.isLoggable(Level.INFO)) {
            return IOUtils.monitor(stream, source, String.valueOf(path), size, new DefaultInputStreamMonitor(session.getTerminal().getOut()));
        }
        return stream;

    }
}
