package net.vpc.app.nuts.extensions.util;

import org.objectweb.asm.*;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.util.*;

import java.io.*;
import java.lang.reflect.Modifier;
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
 * Created by vpc on 5/16/17.
 */
public class CoreIOUtils {
    public static final PrintStream NULL_PRINT_STREAM = new PrintStream(IOUtils.NULL_OUTPUT_STREAM) {
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
    private static final Logger log = Logger.getLogger(CoreIOUtils.class.getName());
    public static boolean visitZipFile(InputStream zipFile, ObjectFilter<String> possiblePaths, StreamVisitor visitor) throws IOException {
        byte[] buffer = new byte[4 * 1024];

        //get the zip file content
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(zipFile);
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            ZipInputStream finalZis = zis;
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
                        if(!visitor.visit(fileName,entryInputStream)){
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
    public static void unzip(File zipFile, File outputFolder,File cwd) throws IOException {

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
                File newFile = createFileByCwd(outputFolder + File.separator + fileName,cwd);
                newFile.mkdirs();
            } else {
                File newFile = createFileByCwd(outputFolder + File.separator + fileName,cwd);
                log.log(Level.FINEST, "file unzip : " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                createFileByCwd(newFile.getParent(),cwd).mkdirs();

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

    public static int execAndWait(NutsFile nutMainFile, NutsWorkspace workspace, NutsSession session, Properties execProperties, String[] args, Map<String, String> env, File directory, NutsTerminal terminal,boolean showCommand) throws NutsExecutionError, IOException {
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
        map.put("nuts.id",nutMainFile.getId().toString());
        map.put("nuts.id.version",nutMainFile.getId().getVersion().getValue());
        map.put("nuts.id.name",nutMainFile.getId().getName());
        map.put("nuts.id.fullName",nutMainFile.getId().getFullName());
        map.put("nuts.id.group",nutMainFile.getId().getGroup());
        map.put("nuts.file",nutMainFile.getFile().getPath());

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
                        if (nutsFile.getFile() != null) {
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
                Collections.addAll(args2, CoreStringUtils.parseCommandline(s));
            } else {
                args2.add(s);
            }
        }
        args = args2.toArray(new String[args2.size()]);

        File file = createFileByCwd(args[0],workspace.getCwd());
        if (file.exists() && !file.canExecute()) {
            if (!file.setExecutable(true)) {
                log.log(Level.WARNING, "Unable to set file executable " + file);
            } else {
                log.log(Level.WARNING, "Success to set file executable " + file);
            }
        }
        if(directory==null){
            directory=workspace.getCwd();
        }else{
            directory=CoreIOUtils.createFileByCwd(directory.getPath(),workspace.getCwd());
        }
        int x = Integer.MIN_VALUE;
        try {
            x = execAndWait(args, envmap, directory, terminal,showCommand);
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
            File file = createFileByCwd(bestJavaPath,workspace.getCwd());
            if (file.isDirectory() && IOUtils.createFile(file, "bin").isDirectory()) {
                bestJavaPath = IOUtils.createFile(bestJavaPath, "bin/java").getPath();
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
            pipes.add(pipe("pipe-out-proc-"+proc.toString(),new NutsNonBlockingInputStreamAdapter("pipe-out-proc-"+proc.toString(),proc.getInputStream()), out));
        }
        if (serr != null) {
            pipes.add(pipe("pipe-err-proc-"+proc.toString(),new NutsNonBlockingInputStreamAdapter("pipe-err-proc-"+proc.toString(),proc.getErrorStream()), err));
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
        String ext = IOUtils.getFileExtension(name);
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

    /**
     * @throws IOException
     */
    public static boolean isMainClass(InputStream stream) throws IOException {
        List<Boolean> ref=new ArrayList<>(1);
        ClassVisitor cl = new ClassVisitor(Opcodes.ASM4) {

            /**
             * Called when a class is visited. This is the method called first
             */
            @Override
            public void visit(int version, int access, String name,
                              String signature, String superName, String[] interfaces) {
//                System.out.println("Visiting class: "+name);
//                System.out.println("Class Major Version: "+version);
//                System.out.println("Super class: "+superName);
                super.visit(version, access, name, signature, superName, interfaces);
            }

            /**
             * Invoked only when the class being visited is an inner class
             */
            @Override
            public void visitOuterClass(String owner, String name, String desc) {
                super.visitOuterClass(owner, name, desc);
            }

            /**
             *Invoked when a class level annotation is encountered
             */
            @Override
            public AnnotationVisitor visitAnnotation(String desc,
                                                     boolean visible) {
                return super.visitAnnotation(desc, visible);
            }

            /**
             * When a class attribute is encountered
             */
            @Override
            public void visitAttribute(Attribute attr) {
                super.visitAttribute(attr);
            }

            /**
             *When an inner class is encountered
             */
            @Override
            public void visitInnerClass(String name, String outerName,
                                        String innerName, int access) {
                super.visitInnerClass(name, outerName, innerName, access);
            }

            /**
             * When a field is encountered
             */
            @Override
            public FieldVisitor visitField(int access, String name,
                                           String desc, String signature, Object value) {
                return super.visitField(access, name, desc, signature, value);
            }


            @Override
            public void visitEnd() {
                super.visitEnd();
            }

            /**
             * When a method is encountered
             */
            @Override
            public MethodVisitor visitMethod(int access, String name,
                                             String desc, String signature, String[] exceptions) {
                if(name.equals("main") && desc.equals("([Ljava/lang/String;)V")
                        && Modifier.isPublic(access)
                        && Modifier.isStatic(access)
                        ) {
                    ref.add(true);
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }

            /**
             * When the optional source is encountered
             */
            @Override
            public void visitSource(String source, String debug) {
                super.visitSource(source, debug);
            }


        };
        ClassReader classReader = new ClassReader(stream);
        classReader.accept(cl, 0);
        return !ref.isEmpty();
    }

    public static List<String> resolveMainClasses(InputStream jarStream) throws IOException {
        List<String> classes=new ArrayList<>();
        visitZipFile(jarStream, new ObjectFilter<String>() {
            @Override
            public boolean accept(String value) {
                return value.endsWith(".class");
            }
        }, new StreamVisitor() {
            @Override
            public boolean visit(String path, InputStream inputStream) throws IOException {
                boolean mainClass = isMainClass(inputStream);
                if(mainClass){
                    classes.add(path.replace('/','.').substring(0,path.length()-".class".length()));
                }
                return true;
            }
        });
        return classes;
    }

    public static String[] expandPath(String path,File cwd) {
        return isFilePath(path) ? findFilePathsOrError(path,cwd) : new String[]{path};
    }

    public static String[] findFilePathsOrError(String path,File cwd) {
        File[] files = findFilesOrError(path,cwd);
        String[] strings = new String[files.length];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = files[i].getPath();
        }
        return strings;
    }

    public static File[] findFilesOrError(String path,File cwd) {
        File[] all = findFiles(path,cwd);
        if (all.length == 0) {
            throw new IllegalArgumentException("No file found " + path);
        }
        return all;
    }

    public static File[] findFiles(String path,File cwd) {
        File f = createFileByCwd(path,cwd);
        if (f.isAbsolute()) {
            File f0 = f;
            while (f0.getParentFile() != null && f0.getParentFile().getParent() != null) {
                f0 = f0.getParentFile();
            }
            return findFiles(f.getPath().substring(f0.getParent().length()), f0.getParent(),cwd);
        } else {
            return findFiles(path, ".",cwd);
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
            for (File file : findFiles(parent, base,cwd)) {
                Collections.addAll(all, findFiles(child, file.getPath(),cwd));
            }
            return all.toArray(new File[all.size()]);
        } else {
            if (path.contains("*") || path.contains("?")) {
                Pattern s = Pattern.compile(StringUtils.simpexpToRegexp(path, false));
                File[] files = createFileByCwd(base,cwd).listFiles(new FilenameFilter() {
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

    public static File createFileByCwd(String path, File cwd) {
        return new File(getAbsolutePath(path,cwd));
    }

    public static String getAbsolutePath(String path,File cwd) {
        try {
            return getAbsoluteFile(new File(path),cwd).getCanonicalPath();
        } catch (IOException e) {
            return getAbsoluteFile(new File(path),cwd).getAbsolutePath();
        }
    }

    public static File getAbsoluteFile(File path,File cwd) {
        if (path.isAbsolute()) {
            return path;
        }
        if(cwd==null){
            cwd=new File(".");
        }
        return new File(cwd, path.getPath());
    }

    public static PipeThread pipe(String name, final NutsNonBlockingInputStream in, final OutputStream out) {
        PipeThread p = new PipeThread(name,in, out);
        p.start();
        return p;
    }

    public static int execAndWait(String[] args, Map<String, String> env, File directory, NutsTerminal terminal,boolean showCommand) throws InterruptedException, IOException {
        if(showCommand){
            NutsPrintStream out = terminal.getOut();
            out.draw("==[exec]==");
            for (String arg : args) {
                out.print(" "+arg);
            }
            out.println();
        }
        ProcessBuilder b = new ProcessBuilder(args);
        if (env != null) {
            Map<String, String> environment = b.environment();
            for (Map.Entry<String, String> e : env.entrySet()) {
                String k=e.getKey();
                String v=e.getValue();
                if(k==null){
                    k="";
                }
                if(v==null){
                    v="";
                }
                environment.put(k,v);
            }
        }
        if (directory != null) {
            b.directory(directory);
        }
        Process proc = b.start();
        List<PipeThread> pipes = new ArrayList<>();
        NutsNonBlockingInputStreamAdapter procInput=null;
        NutsNonBlockingInputStreamAdapter procError=null;
        NutsNonBlockingInputStreamAdapter termIn=null;
        if (terminal.getOut() != null) {
            procInput = new NutsNonBlockingInputStreamAdapter("pipe-out-proc-"+proc.toString(),proc.getInputStream());
            pipes.add(pipe("pipe-out-proc-"+proc.toString(), procInput, terminal.getOut()));
        }
        if (terminal.getErr() != null) {
            procError = new NutsNonBlockingInputStreamAdapter("pipe-err-proc-"+proc.toString(),proc.getErrorStream());
            pipes.add(pipe("pipe-err-proc-"+proc.toString(), procError, terminal.getErr()));
        }
        if (terminal.getIn() != null) {
            termIn = new NutsNonBlockingInputStreamAdapter("pipe-in-proc-"+proc.toString(),terminal.getIn());
            pipes.add(pipe("pipe-in-proc-"+proc.toString(), termIn, proc.getOutputStream()));
        }
        while(proc.isAlive()){
            if(termIn!=null) {
                if (!termIn.hasMoreBytes() && termIn.available() == 0) {
                    termIn.close();
                }
            }
            boolean allFinished=true;
            for (PipeThread pipe : pipes) {
                if(!pipe.isStopped()){
                    allFinished=false;
                }else{
                    pipe.getOut().close();
                }
            }
            if(allFinished){
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
                    if(from.hasMoreBytes()) {
                        count = from.readNonBlocking(bytes, 500);
                        all+=count;
                        to.write(bytes, 0, count);
//                        System.out.println("push "+count);
                    }else{
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
        return IOUtils.readStreamAsBytes(new FileInputStream(stream), true);
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
        IOUtils.copy(from.openStream(), to, mkdirs, true);
    }

    public static void copy(File from, OutputStream to, boolean closeOutput) throws IOException {
        IOUtils.copy(new FileInputStream(from), to, true, closeOutput);
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

//    public static void main(String[] args) {
//        String f="/data/vpc/Data/xprojects/net/vpc/apps/nuts/nuts/target/nuts-0.3.3.jar";
//        try {
//            try(InputStream is=new FileInputStream(f)){
//                System.out.println(resolveMainClasses(is));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
