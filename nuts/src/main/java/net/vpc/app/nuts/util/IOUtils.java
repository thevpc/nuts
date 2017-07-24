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
package net.vpc.app.nuts.util;

import net.vpc.app.nuts.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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
 * Created by vpc on 1/15/17.
 */
public class IOUtils {
    public static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
        @Override
        public void write(int b) throws IOException {
        }

        @Override
        public void write(byte[] b) throws IOException {
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    };
    public static final PrintStream NULL_PRINT_STREAM = new PrintStream(NULL_OUTPUT_STREAM) {
        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        @Override
        public boolean checkError() {
            return false;
        }

        @Override
        public void write(int b) {

        }

        @Override
        public void write(byte[] buf, int off, int len) {
        }

        @Override
        public void print(boolean b) {
        }

        @Override
        public void print(char c) {
        }

        @Override
        public void print(int i) {
        }

        @Override
        public void print(long l) {
        }

        @Override
        public void print(float f) {
        }

        @Override
        public void print(double d) {
        }

        @Override
        public void print(char[] s) {
        }

        @Override
        public void print(String s) {
        }

        @Override
        public void print(Object obj) {
        }

        @Override
        public void println() {
        }

        @Override
        public void println(boolean x) {
        }

        @Override
        public void println(char x) {
        }

        @Override
        public void println(int x) {
        }

        @Override
        public void println(long x) {
        }

        @Override
        public void println(float x) {
        }

        @Override
        public void println(double x) {
        }

        @Override
        public void println(char[] x) {
        }

        @Override
        public void println(String x) {
        }

        @Override
        public void println(Object x) {
        }

        @Override
        public PrintStream printf(String format, Object... args) {
            return this;
        }

        @Override
        public PrintStream printf(Locale l, String format, Object... args) {
            return this;
        }

        @Override
        public PrintStream append(CharSequence csq) {
            return this;
        }

        @Override
        public PrintStream append(CharSequence csq, int start, int end) {
            return this;
        }

        @Override
        public PrintStream append(char c) {
            return this;
        }
    };
    private static final Logger log = Logger.getLogger(IOUtils.class.getName());
    public static String PWD = System.getProperty("user.dir");

    public static String getCwd() {
        return PWD;
    }

    public static void setCwd(String pwd) {
        if (pwd == null) {
            throw new IllegalArgumentException("Invalid Path");
        }
        File dir = new File(pwd);
        if (!dir.isAbsolute()) {
            try {
                dir = new File(PWD, dir.getPath()).getCanonicalFile();
            } catch (IOException e) {
                //
            }
        }
        if (!dir.isDirectory() || !dir.exists()) {
            throw new IllegalArgumentException("Invalid Path");
        }
        System.getProperty("user.dir", PWD = dir.getPath());
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
        return new File(getCwd(), path.getPath());
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

    public static String readStreamAsString(InputStream stream, boolean close) throws IOException {
        return new String(readStreamAsBytes(stream, close));
    }

    public static byte[] readStreamAsBytes(File stream) throws IOException {
        return readStreamAsBytes(new FileInputStream(stream), true);
    }

    public static byte[] readStreamAsBytes(InputStream stream, boolean close) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        copy(stream, os, close, true);
        return os.toByteArray();
    }

    public static byte[] readStreamAsBytes(File stream, int maxSize) throws IOException {
        return readStreamAsBytes(new FileInputStream(stream), maxSize, true);
    }

    public static byte[] readStreamAsBytes(InputStream stream, int maxSize, boolean close) throws IOException {
        try {
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
        } finally {
            if (close) {
                stream.close();
            }
        }
    }

    public static void copy(URL from, File to, boolean mkdirs) throws IOException {
        copy(from.openStream(), to, mkdirs, true);
    }

    public static void copy(File from, File to, boolean mkdirs) throws IOException {
        copy(new FileInputStream(from), to, mkdirs, true);
    }

    public static void copy(File from, OutputStream to, boolean closeOutput) throws IOException {
        copy(new FileInputStream(from), to, true, closeOutput);
    }

    public static void copy(String from, File to, boolean mkdirs) throws IOException {
        if (from == null) {
            from = "";
        }
        copy(new ByteArrayInputStream(from.getBytes()), to, mkdirs, true);
    }

    public static void copy(InputStream from, File to, boolean mkdirs, boolean closeInput) throws IOException {
        try {
            File parentFile = to.getParentFile();
            if (mkdirs && parentFile != null) {
                parentFile.mkdirs();
            }
            File temp = IOUtils.createFile(to.getPath() + "~");
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

    public static File resolvePath(String path, File baseFolder) {
        if (path != null && path.length() > 0) {
            String firstItem = "";
            if ('\\' == File.separatorChar) {
                String[] split = path.split("(/|\\\\)");
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
                return resolvePath(NutsConstants.DEFAULT_WORKSPACE_ROOT + "/" + path.substring(2), null);
            } else if (firstItem.equals("~")) {
                return new File(System.getProperty("user.home"), path.substring(1));
            } else if (isAbsolutePath(path)) {
                return new File(path);
            } else if (baseFolder != null) {
                return IOUtils.createFile(baseFolder, path);
            } else {
                return IOUtils.createFile(path);
            }
        }
        return null;
    }

    public static PipeThread pipe(final InputStream in, final OutputStream out) {
        PipeThread p = new PipeThread(in, out);
        p.start();
        return p;
    }

    public static String resolveJavaCommand(String requestedJavaVersion, NutsWorkspace workspace) {
        requestedJavaVersion = StringUtils.trim(requestedJavaVersion);
        NutsVersionFilter javaVersionFilter = VersionUtils.createFilter(requestedJavaVersion);
        String bestJavaPath = null;
        String bestJavaVersion = null;
        for (Map.Entry<Object, Object> entry : workspace.getConfig().getEnv().entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith("rt.java.")) {
                String javaVersion = key.substring("rt.java.".length());
                if (javaVersionFilter.accept(new NutsVersion(javaVersion))) {
                    if (bestJavaVersion == null || VersionUtils.compareVersions(bestJavaVersion, javaVersion) < 0) {
                        bestJavaVersion = javaVersion;
                        bestJavaPath = (String) entry.getValue();
                    }
                }
            }
        }
        if (StringUtils.isEmpty(bestJavaPath)) {
            if (StringUtils.isEmpty(requestedJavaVersion)) {
                log.log(Level.FINE, "No valid JRE found. recommended " + requestedJavaVersion + " . using default");
            } else {
                log.log(Level.FINE, "No valid JRE found. using default.");
            }
            bestJavaPath = "java";
        }
        if (bestJavaPath.contains("/") || bestJavaPath.contains("\\")) {
            File file = IOUtils.createFile(bestJavaPath);
            if (file.isDirectory() && IOUtils.createFile(file, "bin").isDirectory()) {
                bestJavaPath = IOUtils.createFile(bestJavaPath, "bin/java").getPath();
            }
        }
        return bestJavaPath;
    }

    public static int execAndWait(NutsFile nutMainFile, NutsWorkspace workspace, NutsSession session, Properties execProperties, String[] args, Map<String, String> env, File directory, NutsTerminal terminal) throws NutsExecutionError, IOException {
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
        File nutsJarFile = workspace.fetchNutsId(session).getFile();
        if (nutsJarFile != null) {
            map.put("nuts.jar", nutsJarFile.getAbsolutePath());
        }
        map.put("nuts.java", resolveJavaCommand("", workspace));
        map.put("nuts.cmd", map.get("nuts.java") + " -jar " + map.get("nuts.jar"));
        map.put("nuts.workspace", workspace.getWorkspaceLocation());
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
                    try {
                        nutsFile = workspace.fetch(NutsConstants.NUTS_COMPONENT_ID, session);
                        if(nutsFile.getFile()!=null) {
                            return ("<::expand::> " + get("java") + " -jar " + nutsFile.getFile().getPath());
                        }
                        return null;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
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
            String s = StringUtils.trim(StringUtils.replaceVars(arg, mapper));
            if (s.startsWith("<::expand::>")) {
                Collections.addAll(args2, StringUtils.parseCommandline(s));
            } else {
                args2.add(s);
            }
        }
        args = args2.toArray(new String[args2.size()]);

        File file = IOUtils.createFile(args[0]);
        if (file.exists() && !file.canExecute()) {
            if (!file.setExecutable(true)) {
                log.log(Level.WARNING, "Unable to set file executable " + file);
            } else {
                log.log(Level.WARNING, "Success to set file executable " + file);
            }
        }
        int x = Integer.MIN_VALUE;
        try {
            x = IOUtils.execAndWait(args, envmap, directory, terminal);
            if (x != 0) {
                throw new NutsExecutionError(x);
            }
            return x;
        } catch (InterruptedException ex) {
            throw new NutsExecutionError(ex.getMessage(), ex, x);
        } catch (IOException ex) {
            throw new NutsExecutionError(ex.getMessage(), ex, x);
        }
    }

//    public static void main(String[] args) {
////        for (File file : findFiles("Programs/nuts-bundles/*", "/data/vpc")) {
////            System.out.println("### "+file);
////        }
//        for (File file : findFiles("/data/vpc/Programs/nuts-bundles/*")) {
//            System.out.println("### "+file);
//        }
//    }

    public static boolean isFilePath(String path) {
        return path != null && path.indexOf('/') >= 0 && !path.contains("://");
    }

    public static String[] expandPath(String path) {
        return IOUtils.isFilePath(path) ? IOUtils.findFilePathsOrError(path) : new String[]{path};
    }

    public static String[] findFilePathsOrError(String path) {
        File[] files = findFilesOrError(path);
        String[] strings = new String[files.length];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = files[i].getPath();
        }
        return strings;
    }

    public static File[] findFilesOrError(String path) {
        File[] all = findFiles(path);
        if (all.length == 0) {
            throw new IllegalArgumentException("No file found " + path);
        }
        return all;
    }

    public static File[] findFiles(String path) {
        File f = IOUtils.createFile(path);
        if (f.isAbsolute()) {
            File f0 = f;
            while (f0.getParentFile() != null && f0.getParentFile().getParent() != null) {
                f0 = f0.getParentFile();
            }
            return findFiles(f.getPath().substring(f0.getParent().length()), f0.getParent());
        } else {
            return findFiles(path, ".");
        }
    }

    public static File[] findFiles(String path, String base) {
        int x = path.indexOf('/');
        if (x > 0) {
            String parent = path.substring(0, x);
            String child = path.substring(x + 1);
            List<File> all = new ArrayList<>();
            for (File file : findFiles(parent, base)) {
                Collections.addAll(all, findFiles(child, file.getPath()));
            }
            return all.toArray(new File[all.size()]);
        } else {
            if (path.contains("*") || path.contains("?")) {
                Pattern s = Pattern.compile(StringUtils.simpexpToRegexp(path, false));
                File[] files = IOUtils.createFile(base).listFiles(new FilenameFilter() {
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
                File f = IOUtils.createFile(base, path);
                if (f.exists()) {
                    return new File[]{f};
                }
                return new File[0];
            }
        }
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

    public static int execAndWait(String[] args, Map<String, String> env, File directory, NutsTerminal terminal) throws InterruptedException, IOException {
        ProcessBuilder b = new ProcessBuilder(args);
        if (env != null) {
            b.environment().putAll(env);
        }
        if (directory != null) {
            b.directory(directory);
        }
        Process proc = b.start();
        List<PipeThread> pipes = new ArrayList<>();
        if (terminal.getOut() != null) {
            pipes.add(IOUtils.pipe(proc.getInputStream(), terminal.getOut()));
        }
        if (terminal.getErr() != null) {
            pipes.add(IOUtils.pipe(proc.getErrorStream(), terminal.getErr()));
        }
        if (terminal.getIn() != null) {
            pipes.add(IOUtils.pipe(terminal.getIn(), proc.getOutputStream()));
        }
        int ret = proc.waitFor();
        for (PipeThread pipe : pipes) {
            pipe.requestStop();
        }
        return ret;

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
            pipes.add(IOUtils.pipe(proc.getInputStream(), out));
        }
        if (serr != null) {
            pipes.add(IOUtils.pipe(proc.getErrorStream(), err));
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

    public static int[] delete(File file) throws IOException {
        int[] deleted = new int[]{0, 0};
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
                java.util.Arrays.fill(passwd, ' ');
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

    public static void zip(final File _folder, final File _zipFilePath) throws IOException {
        Path folder = _folder.toPath();
        Path zipFilePath = _zipFilePath.toPath();
        try (
                FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
                ZipOutputStream zos = new ZipOutputStream(fos)
        ) {
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
        }
    }

    /**
     * Unzip it
     *
     * @param zipFile      input zip file
     * @param outputFolder zip file output folder
     */
    public static void unzip(File zipFile, File outputFolder) throws IOException {

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
                File newFile = IOUtils.createFile(outputFolder + File.separator + fileName);
                newFile.mkdirs();
            } else {
                File newFile = IOUtils.createFile(outputFolder + File.separator + fileName);
                log.log(Level.FINEST, "file unzip : " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                IOUtils.createFile(newFile.getParent()).mkdirs();

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

    public static File createTempFile(NutsDescriptor descriptor, File directory) throws IOException {
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
        return File.createTempFile(prefix, "-nuts" + (ext != null ? ("." + ext) : ""), directory);
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
            if (StringUtils.isEmpty(contentType)) {
                ext = ".unknown";
            } else {
                if (contentType.equals("application/zip")) {
                    ext = ".zip";
                } else if (contentType.equals("application/x-rar-compressed")) {//not supported yet
                    ext = ".rar";
                } else if (contentType.equals("application/java-archive")) {
                    ext = ".jar";
                } else {
                    ext = ".unknown";
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

    public static File createTempFile(NutsDescriptor descriptor) throws IOException {
        return createTempFile(descriptor, null);
    }

    public static String getURLName(URL url) {
        String p = url.getPath();
        int sep = p.lastIndexOf('/');
        if (sep < 0) {
            sep = p.lastIndexOf(':');
        }
        p = sep == 0 ? p : p.substring(sep);
        sep = p.indexOf('?');
        if (sep >= 0) {
            p = p.substring(0, sep);
        }
        return p;
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

    public interface StopMonitor {

        boolean shouldStop();
    }

    private static class PipeThread extends Thread implements StopMonitor {

        private final InputStream in;
        private final OutputStream out;
        private final Object lock = new Object();
        private boolean requestStop = false;
        private boolean stopped = false;

        public PipeThread(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public boolean shouldStop() {
            return requestStop;
        }

        public void requestStop() {
            requestStop = true;
            if (!stopped) {
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
//            for (int i = 0; i < 100; i++) {
//                if (stopped) {
//                    return;
//                }
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    //e.printStackTrace();
//                }
//            }
//            throw new RuntimeException("Unable to stop");
        }

        @Override
        public void run() {
            try {
                copy(in, out, false, false, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            stopped = true;
            synchronized (lock) {
                lock.notify();
            }
        }
    }

}
