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

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.core.NutsVersionImpl;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreIOUtils {

    public static final OutputStream NULL_OUTPUT_STREAM = NullOutputStream.INSTANCE;
    public static final PrintStream NULL_PRINT_STREAM = NullPrintStream.INSTANCE;
    private static final Logger log = Logger.getLogger(CoreIOUtils.class.getName());

    public static boolean visitZipFile(InputStream zipFile, ObjectFilter<String> possiblePaths, StreamVisitor visitor) throws NutsIOException {
        byte[] buffer = new byte[4 * 1024];

        //get the zip file content
        ZipInputStream zis = null;
        try {
            try {
                zis = new ZipInputStream(zipFile);
                //get the zipped file list entry
                ZipEntry ze = zis.getNextEntry();
                final ZipInputStream finalZis = zis;
                InputStream entryInputStream = new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return finalZis.read();
                    }

                    @Override
                    public int read(byte[] b) throws IOException {
                        return finalZis.read(b);
                    }

                    @Override
                    public int read(byte[] b, int off, int len) throws IOException {
                        return finalZis.read(b, off, len);
                    }

                    @Override
                    public void close() throws IOException {
                        finalZis.closeEntry();
                    }
                };

                while (ze != null) {

                    String fileName = ze.getName();
                    if (!fileName.endsWith("/")) {
                        if (possiblePaths.accept(fileName)) {
                            if (!visitor.visit(fileName, entryInputStream)) {
                                break;
                            }
                        }
                    }
                    ze = zis.getNextEntry();
                }
            } finally {
                if (zis != null) {
                    zis.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(ex);
        }
        return false;
    }

    public static boolean extractFirstPath(InputStream zipFile, Set<String> possiblePaths, OutputStream output, boolean closeOutput) throws IOException {
        byte[] buffer = new byte[4 * 1024];

        //get the zip file content
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(zipFile);
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                if (!fileName.endsWith("/")) {
                    if (possiblePaths.contains(fileName)) {
                        int len;
                        try {
                            while ((len = zis.read(buffer)) > 0) {
                                output.write(buffer, 0, len);
                            }
                            zis.closeEntry();
                        } finally {
                            if (closeOutput) {
                                output.close();
                            }
                        }
                        return true;
                    }
                }
                ze = zis.getNextEntry();
            }
        } finally {
            if (zis != null) {
                zis.close();
            }
        }
        return false;
    }

    public static void zip(final File _folder, final File _zipFilePath) {
        final Path folder = _folder.toPath();
        Path zipFilePath = _zipFilePath.toPath();
        try (
                FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
                ZipOutputStream zos = new ZipOutputStream(fos)) {
            Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    zos.putNextEntry(new ZipEntry(folder.relativize(file).toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    zos.putNextEntry(new ZipEntry(folder.relativize(dir).toString() + "/"));
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    /**
     * Unzip it
     *
     * @param zipFile input zip file
     * @param outputFolder zip file output folder
     */
    public static void unzip(File zipFile, File outputFolder, File cwd) throws IOException {

        byte[] buffer = new byte[1024];

        //create output directory is not exists
        File folder = outputFolder;
        if (!folder.exists()) {
            folder.mkdir();
        }

        //get the zip file content
        ZipInputStream zis
                = new ZipInputStream(new FileInputStream(zipFile));
        //get the zipped file list entry
        ZipEntry ze = zis.getNextEntry();

        while (ze != null) {

            String fileName = ze.getName();
            if (fileName.endsWith("/")) {
                File newFile = createFileByCwd(outputFolder + File.separator + fileName, cwd);
                newFile.mkdirs();
            } else {
                File newFile = createFileByCwd(outputFolder + File.separator + fileName, cwd);
                log.log(Level.FINEST, "file unzip : " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                createFileByCwd(newFile.getParent(), cwd).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
            }
            ze = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    public static int execAndWait(NutsFile nutMainFile, final NutsWorkspace workspace, final NutsSession session, Properties execProperties, String[] args, Map<String, String> env, File directory, NutsTerminal terminal, boolean showCommand) throws NutsExecutionException {
        NutsId id = nutMainFile.getId();
        File installerFile = nutMainFile.getFile();
        File storeFolder = nutMainFile.getInstallFolder();
        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> envmap = new HashMap<>();
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            map.put((String) entry.getKey(), (String) entry.getValue());
        }
        for (Map.Entry<Object, Object> entry : execProperties.entrySet()) {
            map.put((String) entry.getKey(), (String) entry.getValue());
        }
        File nutsJarFile = workspace.fetchBootFile(session).getFile();
        if (nutsJarFile != null) {
            map.put("nuts.jar", nutsJarFile.getAbsolutePath());
        }
        map.put("nuts.id", nutMainFile.getId().toString());
        map.put("nuts.id.version", nutMainFile.getId().getVersion().getValue());
        map.put("nuts.id.name", nutMainFile.getId().getName());
        map.put("nuts.id.fullName", nutMainFile.getId().getFullName());
        map.put("nuts.id.group", nutMainFile.getId().getGroup());
        map.put("nuts.file", nutMainFile.getFile().getPath());

        map.put("nuts.java", resolveJavaCommand("", workspace));
        map.put("nuts.cmd", map.get("nuts.java") + " -jar " + map.get("nuts.jar"));
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
            public String get(String skey) {
                if (skey.equals("java") || skey.startsWith("java#")) {
                    return resolveJavaCommand(skey.substring(4), workspace);
                } else if (skey.equals("nuts")) {
                    NutsFile nutsFile = null;
                    nutsFile = workspace.fetch(NutsConstants.NUTS_ID_BOOT, session);
                    if (nutsFile.getFile() != null) {
                        return ("<::expand::> " + get("java") + " -jar " + nutsFile.getFile().getPath());
                    }
                    return null;
                }
                return super.get(skey);
            }
        };
        for (Map.Entry<String, String> e : map.entrySet()) {
            String k = e.getKey();
            k = k.replace('.', '_');
            envmap.put(k, e.getValue());
        }
        List<String> args2 = new ArrayList<>();
        for (String arg : args) {
            String s = CoreStringUtils.trim(CoreStringUtils.replaceVars(arg, mapper));
            if (s.startsWith("<::expand::>")) {
                Collections.addAll(args2, CoreStringUtils.parseCommandline(s));
            } else {
                args2.add(s);
            }
        }
        args = args2.toArray(new String[args2.size()]);

        File file = createFileByCwd(args[0], workspace.getConfigManager().getCwd());
        if (file.exists() && !file.canExecute()) {
            if (!file.setExecutable(true)) {
                log.log(Level.WARNING, "Unable to set file executable " + file);
            } else {
                log.log(Level.WARNING, "Success to set file executable " + file);
            }
        }
        if (directory == null) {
            directory = workspace.getConfigManager().getCwd();
        } else {
            directory = CoreIOUtils.createFileByCwd(directory.getPath(), workspace.getConfigManager().getCwd());
        }
        int x = Integer.MIN_VALUE;
        try {
            x = execAndWait(args, envmap, directory, terminal, showCommand);
            if (x != 0) {
                throw new NutsExecutionException(x);
            }
            return x;
        } catch (InterruptedException ex) {
            throw new NutsExecutionException(ex.getMessage(), ex, x);
        } catch (IOException ex) {
            throw new NutsExecutionException(ex.getMessage(), ex, x);
        }
    }

    public static String resolveJavaCommand(String requestedJavaVersion, NutsWorkspace workspace) {
        requestedJavaVersion = CoreStringUtils.trim(requestedJavaVersion);
        NutsVersionFilter javaVersionFilter = CoreVersionUtils.createNutsVersionFilter(requestedJavaVersion);
        String bestJavaPath = null;
        String bestJavaVersion = null;
        for (Map.Entry<Object, Object> entry : workspace.getConfigManager().getEnv().entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith("rt.java.")) {
                String javaVersion = key.substring("rt.java.".length());
                if (javaVersionFilter.accept(new NutsVersionImpl(javaVersion))) {
                    if (bestJavaVersion == null || CoreVersionUtils.compareVersions(bestJavaVersion, javaVersion) < 0) {
                        bestJavaVersion = javaVersion;
                        bestJavaPath = (String) entry.getValue();
                    }
                }
            }
        }
        if (CoreStringUtils.isEmpty(bestJavaPath)) {
            if (CoreStringUtils.isEmpty(requestedJavaVersion)) {
                log.log(Level.FINE, "No valid JRE found. recommended " + requestedJavaVersion + " . using default");
            } else {
                log.log(Level.FINE, "No valid JRE found. using default.");
            }
            bestJavaPath = "java";
        }
        if (bestJavaPath.contains("/") || bestJavaPath.contains("\\")) {
            File file = createFileByCwd(bestJavaPath, workspace.getConfigManager().getCwd());
            if (file.isDirectory() && CoreIOUtils.createFile(file, "bin").isDirectory()) {
                bestJavaPath = CoreIOUtils.createFile(bestJavaPath, "bin/java").getPath();
            }
        }
        return bestJavaPath;
    }

    public static String getMainClass(File jarFile) throws IOException {
        JarFile jarfile = new JarFile(jarFile);
        Manifest manifest = jarfile.getManifest();
        Attributes attrs = manifest.getMainAttributes();
        for (Object o : attrs.keySet()) {
            Attributes.Name attrName = (Attributes.Name) o;
            if ("Main-Class".equals(attrName.toString())) {
                return attrs.getValue(attrName);
            }
        }
        return null;
    }

    public static int execAndEcho(String[] args, Map<String, String> env, File directory, StringBuilder sout, StringBuilder serr, long sleep) throws InterruptedException, IOException {
        ProcessBuilder b = new ProcessBuilder(args);
        if (env != null) {
            b.environment().putAll(env);
        }
        if (directory != null) {
            b.directory(directory);
        }
        Process proc = b.start();
        List<PipeThread> pipes = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        if (sout != null) {
            pipes.add(pipe("pipe-out-proc-" + proc.toString(), new NutsNonBlockingInputStreamAdapter("pipe-out-proc-" + proc.toString(), proc.getInputStream()), out));
        }
        if (serr != null) {
            pipes.add(pipe("pipe-err-proc-" + proc.toString(), new NutsNonBlockingInputStreamAdapter("pipe-err-proc-" + proc.toString(), proc.getErrorStream()), err));
        }
        int ret = proc.waitFor();
        if (sleep > 0) {
            Thread.sleep(sleep);
        }
        for (PipeThread pipe : pipes) {
            pipe.requestStop();
        }
        if (sout != null) {
            sout.append(new String(out.toByteArray()));
        }
        if (serr != null) {
            serr.append(new String(err.toByteArray()));
        }
        return ret;

    }

    public static File createTempFile(URL url) throws IOException {
        String contentType = null;
        if ("http".equals(url.getProtocol()) || "https".equals(url.getProtocol())) {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            contentType = connection.getContentType();
            connection.disconnect();
        }
        String name = getURLName(url);
        String ext = getFileExtension(name);
        if (ext.isEmpty()) {
            if (CoreStringUtils.isEmpty(contentType)) {
                ext = ".unknown";
            } else {
                switch (contentType) {
                    case "application/zip":
                        ext = ".zip";
                        break;
                    case "application/x-rar-compressed": //not supported yet
                        ext = ".rar";
                        break;
                    case "application/java-archive":
                        ext = ".jar";
                        break;
                    default:
                        ext = ".unknown";
                        break;
                }
            }

        } else {
            ext = "." + ext;
        }

        String prefix = name;
        if (ext.length() < 3) {
            ext = "___" + ext;
        }
        if (prefix.length() < 3) {
            prefix = prefix + "___";
        }
        File tempFile = null;
        tempFile = File.createTempFile(prefix, ext);
        copy(url, tempFile, true);
        return tempFile;
    }

    public static String getURLName(URL url) {
        String p = url.getPath();
        int sep = p.lastIndexOf('/');
        if (sep < 0) {
            sep = p.lastIndexOf(':');
        }
        p = sep < 0 ? p : p.substring(sep + 1);
        sep = p.indexOf('?');
        if (sep >= 0) {
            p = p.substring(0, sep);
        }
        return p;
    }

    public static int[] delete(File file) throws IOException {
        final int[] deleted = new int[]{0, 0};
        Files.walkFileTree(file.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                log.log(Level.FINEST, "Delete file " + file);
                deleted[1]++;
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                log.log(Level.FINEST, "Delete folder " + dir);
                deleted[0]++;
                return FileVisitResult.CONTINUE;
            }
        });
        return deleted;
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
        return clsAndLibs.toArray(new String[clsAndLibs.size()]);
    }

    public static String[] expandPath(String path, File cwd) {
        return isFilePath(path) ? findFilePathsOrError(path, cwd) : new String[]{path};
    }

    public static String[] findFilePathsOrError(String path, File cwd) {
        File[] files = findFilesOrError(path, cwd);
        String[] strings = new String[files.length];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = files[i].getPath();
        }
        return strings;
    }

    public static File[] findFilesOrError(String path, File cwd) {
        File[] all = findFiles(path, cwd);
        if (all.length == 0) {
            throw new NutsIllegalArgumentException("No file found " + path);
        }
        return all;
    }

    public static File[] findFiles(String path, File cwd) {
        File f = createFileByCwd(path, cwd);
        if (f.isAbsolute()) {
            File f0 = f;
            while (f0.getParentFile() != null && f0.getParentFile().getParent() != null) {
                f0 = f0.getParentFile();
            }
            return findFiles(f.getPath().substring(f0.getParent().length()), f0.getParent(), cwd);
        } else {
            return findFiles(path, ".", cwd);
        }
    }

    public static boolean isFilePath(String path) {
        return path != null && path.indexOf('/') >= 0 && !path.contains("://");
    }

    public static byte[] readStreamAsBytes(File stream, int maxSize) throws IOException {
        return readStreamAsBytes(new FileInputStream(stream), maxSize, true);
    }

    public static File[] findFiles(String path, String base, File cwd) {
        int x = path.indexOf('/');
        if (x > 0) {
            String parent = path.substring(0, x);
            String child = path.substring(x + 1);
            List<File> all = new ArrayList<>();
            for (File file : findFiles(parent, base, cwd)) {
                Collections.addAll(all, findFiles(child, file.getPath(), cwd));
            }
            return all.toArray(new File[all.size()]);
        } else {
            if (path.contains("*") || path.contains("?")) {
                final Pattern s = Pattern.compile(CoreStringUtils.simpexpToRegexp(path));
                File[] files = createFileByCwd(base, cwd).listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return s.matcher(name).matches();
                    }
                });
                if (files == null) {
                    return new File[0];
                }
                return files;
            } else {
                File f = CoreIOUtils.createFile(base, path);
                if (f.exists()) {
                    return new File[]{f};
                }
                return new File[0];
            }
        }
    }

    public static File createFileByCwd(String path, File cwd) {
        return new File(getAbsolutePath(path, cwd));
    }

    public static String getAbsolutePath(String path, File cwd) {
        try {
            return getAbsoluteFile(new File(path), cwd).getCanonicalPath();
        } catch (IOException e) {
            return getAbsoluteFile(new File(path), cwd).getAbsolutePath();
        }
    }

    public static File getAbsoluteFile(File path, File cwd) {
        if (path.isAbsolute()) {
            return path;
        }
        if (cwd == null) {
            cwd = new File(".");
        }
        return new File(cwd, path.getPath());
    }

    public static PipeThread pipe(String name, final NutsNonBlockingInputStream in, final OutputStream out) {
        PipeThread p = new PipeThread(name, in, out);
        p.start();
        return p;
    }

    public static int execAndWait(String[] args, Map<String, String> env, File directory, NutsTerminal terminal, boolean showCommand) throws InterruptedException, IOException {
        if (showCommand) {
            NutsPrintStream out = terminal.getOut();
            out.print("==[exec]==");
            for (String arg : args) {
                out.print(" " + arg);
            }
            out.println();
        }
        ProcessBuilder b = new ProcessBuilder(args);
        if (env != null) {
            Map<String, String> environment = b.environment();
            for (Map.Entry<String, String> e : env.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                if (k == null) {
                    k = "";
                }
                if (v == null) {
                    v = "";
                }
                environment.put(k, v);
            }
        }
        if (directory != null) {
            b.directory(directory);
        }
        Process proc = b.start();
        List<PipeThread> pipes = new ArrayList<>();
        NutsNonBlockingInputStreamAdapter procInput = null;
        NutsNonBlockingInputStreamAdapter procError = null;
        NutsNonBlockingInputStreamAdapter termIn = null;
        if (terminal.getOut() != null) {
            procInput = new NutsNonBlockingInputStreamAdapter("pipe-out-proc-" + proc.toString(), proc.getInputStream());
            pipes.add(pipe("pipe-out-proc-" + proc.toString(), procInput, terminal.getOut()));
        }
        if (terminal.getErr() != null) {
            procError = new NutsNonBlockingInputStreamAdapter("pipe-err-proc-" + proc.toString(), proc.getErrorStream());
            pipes.add(pipe("pipe-err-proc-" + proc.toString(), procError, terminal.getErr()));
        }
        if (terminal.getIn() != null) {
            termIn = new NutsNonBlockingInputStreamAdapter("pipe-in-proc-" + proc.toString(), terminal.getIn());
            pipes.add(pipe("pipe-in-proc-" + proc.toString(), termIn, proc.getOutputStream()));
        }
        while (proc.isAlive()) {
            if (termIn != null) {
                if (!termIn.hasMoreBytes() && termIn.available() == 0) {
                    termIn.close();
                }
            }
            boolean allFinished = true;
            for (PipeThread pipe : pipes) {
                if (!pipe.isStopped()) {
                    allFinished = false;
                } else {
                    pipe.getOut().close();
                }
            }
            if (allFinished) {
                break;
            }
            Thread.sleep(1000);
        }
        proc.getInputStream().close();
        proc.getErrorStream().close();
        proc.getOutputStream().close();

        int ret = proc.waitFor();
        for (PipeThread pipe : pipes) {
            pipe.requestStop();
        }
        return ret;

    }

    public static long copy(InputStream from, OutputStream to, boolean closeInput, boolean closeOutput, StopMonitor monitor) throws IOException {
        byte[] bytes = new byte[10240];
        int count;
        long all = 0;
        try {
            try {
                while (true) {
                    if (monitor.shouldStop()) {
                        break;
                    }
                    if (from.available() > 0) {
                        count = from.read(bytes);
                        if (count > 0) {
                            to.write(bytes, 0, count);
                            all += count;
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            //
                        }
                        if (from.available() > 0) {
                            count = from.read(bytes);
                            if (count > 0) {
                                to.write(bytes, 0, count);
                                all += count;
                            }
                        }
                    }
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

    public static long copy(NutsNonBlockingInputStream from, OutputStream to, boolean closeInput, boolean closeOutput, StopMonitor monitor) throws IOException {
        byte[] bytes = new byte[10240];
        int count;
        long all = 0;
        try {
            try {
                while (true) {
                    if (monitor.shouldStop()) {
                        break;
                    }
                    if (from.hasMoreBytes()) {
                        count = from.readNonBlocking(bytes, 500);
                        all += count;
                        to.write(bytes, 0, count);
//                        System.out.println("push "+count);
                    } else {
                        break;
                    }
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

    public static byte[] readStreamAsBytes(File stream) throws IOException {
        return readStreamAsBytes(new FileInputStream(stream), true);
    }

    public static byte[] readStreamAsBytes(InputStream stream, int maxSize, boolean close) throws IOException {
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
                CoreIOUtils.copy(stream, os, close, true);
                return os.toByteArray();
            }
        } finally {
            if (close) {
                stream.close();
            }
        }
    }

    public static void copy(URL from, File to, boolean mkdirs) {
        try {
            CoreIOUtils.copy(from.openStream(), to, mkdirs, true);
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    public static void copy(File from, OutputStream to, boolean closeOutput) throws IOException {
        CoreIOUtils.copy(new FileInputStream(from), to, true, closeOutput);
    }

    public static void copy(Reader from, OutputStream to, boolean closeInput, boolean closeOutput) throws IOException {
        char[] bytes = new char[10240];
        int count;
        try {
            try {
                to.flush();
                OutputStreamWriter ps = new OutputStreamWriter(to);
                while ((count = from.read(bytes)) > 0) {
                    ps.write(bytes, 0, count);
                }
                ps.flush();
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

    public static File[] nonNullArray(File[] array1) {
        return array1 == null ? new File[0] : array1;
    }

    public static String readStreamAsString(InputStream stream, boolean close) {
        return new String(readStreamAsBytes(stream, close));
    }

    public static byte[] readStreamAsBytes(InputStream stream, boolean close) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        copy(stream, os, close, true);
        return os.toByteArray();
    }

    public static void copy(File from, File to, boolean mkdirs) throws IOException {
        CoreIOUtils.copy(new FileInputStream(from), to, mkdirs, true);
    }

    public static String getFileExtension(File f) {
        return getFileExtension(f.getName());
    }

    public static String getFileExtension(String n) {
        int i = n.lastIndexOf('.');
        if (i >= 0) {
            return n.substring(i + 1);
        }
        return "";
    }

    public static void copy(String from, File to, boolean mkdirs) throws IOException {
        if (from == null) {
            from = "";
        }
        CoreIOUtils.copy(new ByteArrayInputStream(from.getBytes()), to, mkdirs, true);
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

    public static int getURLSize(URL url) {
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).setRequestMethod("HEAD");
            }
            conn.getInputStream();
            return conn.getContentLength();
        } catch (IOException e) {
            throw new NutsIOException(e);
        } finally {
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).disconnect();
            }
        }
    }

    public static InputStream monitor(URL from, InputStreamMonitor monitor) throws NutsIOException {
        try {
            return monitor(from.openStream(), from, getURLName(from), getURLSize(from), monitor);
        } catch (IOException ex) {
            throw new NutsIOException(ex);
        }
    }

    public static InputStream monitor(InputStream from, Object source, String sourceName, long length, InputStreamMonitor monitor) {
        return new MonitoredInputStream(from, source, sourceName, length, monitor);
    }

    public static void copy(InputStream from, File to, boolean mkdirs, boolean closeInput) {
        try {
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
        } catch (IOException ex) {
            throw new NutsIOException(ex);
        }
    }

    public static boolean isAbsolutePath(String location) {
        return new File(location).isAbsolute();
    }

    public static long copy(InputStream from, OutputStream to, boolean closeInput, boolean closeOutput) {
        byte[] bytes = new byte[1024];//
        int count;
        long all = 0;
        try {
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
        } catch (IOException ex) {
            throw new NutsIOException(ex);
        }
    }

    public static File resolvePath(String path, File baseFolder, String workspaceRoot) {
        if (CoreStringUtils.isEmpty(workspaceRoot)) {
            workspaceRoot = NutsConstants.DEFAULT_WORKSPACE_ROOT;
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
                return resolvePath(workspaceRoot + "/" + path.substring(2), null, workspaceRoot);
            } else if (firstItem.equals("~")) {
                return new File(System.getProperty("user.home"), path.substring(1));
            } else if (isAbsolutePath(path)) {
                return new File(path);
            } else if (baseFolder != null) {
                return CoreIOUtils.createFile(baseFolder, path);
            } else {
                return CoreIOUtils.createFile(path);
            }
        }
        return null;
    }

    public static File createTempFile(NutsDescriptor descriptor, boolean desc, File directory) {
        String prefix = "temp-";
        String ext = null;
        if (descriptor != null) {
            ext = CoreStringUtils.trim(descriptor.getExt());
            prefix = CoreStringUtils.trim(descriptor.getId().getGroup()) + "-" + CoreStringUtils.trim(descriptor.getId().getName()) + "-" + CoreStringUtils.trim(descriptor.getId().getVersion().getValue());
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
            ext = getFileExtension(name);
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

    public static Properties loadProperties(URL url) {
        Properties props = new Properties();
        InputStream inputStream = null;
        try {
            try {
                if (url != null) {
                    inputStream = url.openStream();
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

    public static boolean isValidInputStreamSource(Class type) {
        return URL.class.isAssignableFrom(type)
                || File.class.isAssignableFrom(type) || byte[].class.isAssignableFrom(type)
                || InputStream.class.isAssignableFrom(type)
                || String.class.isAssignableFrom(type);
    }

    public static InputStreamSource createInputStreamSource(Object anyObject, String variant, String optionalName, File cwd) {
        if (anyObject instanceof URL) {
            return createInputStreamSource((URL) anyObject);
        }
        if (anyObject instanceof File) {
            return createInputStreamSource((File) anyObject, cwd);
        }
        if (anyObject instanceof byte[]) {
            return createInputStreamSource((byte[]) anyObject, optionalName);
        }
        if (anyObject instanceof InputStream) {
            return createInputStreamSource((InputStream) anyObject, optionalName);
        }
        if (anyObject instanceof String) {
            return createInputStreamSource((String) anyObject, variant, optionalName, cwd);
        }
        throw new NutsException("Unexpected stream source");
    }

    public static InputStreamSource createInputStreamSource(byte[] bytes, String name) {
        return new ByteArrayInputStreamSource(bytes,name,bytes);
    }

    public static InputStreamSource createInputStreamSource(String path, String variant, String name, File cwd) {
        if ("path".equals(variant)) {
            if (path.contains("://")) {
                try {
                    return createInputStreamSource(new URL(path));
                } catch (MalformedURLException e) {
                    throw new NutsIOException();
                }
            }
            return createInputStreamSource(new File(path), cwd);
        }
        if ("url".equals(variant)) {
            try {
                return createInputStreamSource(new URL(path));
            } catch (MalformedURLException e) {
                throw new NutsIOException();
            }
        }
        if ("file".equals(variant)) {
            try {
                return createInputStreamSource(new URL(path));
            } catch (MalformedURLException e) {
                throw new NutsIOException();
            }
        }
        throw new NutsIllegalArgumentException("Unsupported variant " + variant);
    }

    public static InputStreamSource createInputStreamSource(InputStream inputStream, String name) {
        byte[] bytes = readStreamAsBytes(inputStream, true);
        return new ByteArrayInputStreamSource(bytes, name, inputStream);
    }

    public static InputStreamSource createInputStreamSource(URL url) {
        return new URLInputStreamSource(url);
    }

    public static InputStreamSource createInputStreamSource(File file, File cwd) {
        return new FileInputStreamSource(file);
    }

    public static URL getURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new NutsParseException(e);
        }
    }

    public static URL getURL(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new NutsIOException(e);
        }
    }

    public static String getCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    public static boolean storeProperties(Properties p, File file) {
        Writer writer = null;
        try {
            try {
                p.store(writer = new FileWriter(file), null);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
            return true;
        } catch (IOException e) {
            log.log(Level.SEVERE, "Unable to store {0}", file);
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

    public static Properties loadURLProperties(String url) {
        try {
            if (url != null) {
                return loadURLProperties(new URL(url));
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return new Properties();
    }

    public static Properties loadURLProperties(URL url) {
        Properties props = new Properties();
        InputStream inputStream = null;
        try {
            try {
                if (url != null) {
                    inputStream = url.openStream();
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

    public static boolean isRemoteURL(String url) {
        if (url == null) {
            return false;
        }
        url = url.toLowerCase();
        return (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://"));
    }

    public static String nativePath(String path) {
        return path.replace('/', File.separatorChar);
    }



}
