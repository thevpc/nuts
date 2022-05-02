/**
 * ====================================================================
 * vpc-common-io : common reusable library for
 * input/output
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
package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.util.NutsLoggerVerb;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author taha.bensalah@gmail.com
 */
public class ZipUtils {

    //    private static final Logger LOG = Logger.getLogger(ZipUtils.class.getName());
    public static void zip(NutsSession ws, String target, ZipOptions options, String... source) throws IOException {
        if (options == null) {
            options = new ZipOptions();
        }
        File targetFile = new File(target);
        File f = options.isTempFile() ? File.createTempFile("zip", ".zip") : targetFile;
        f.getParentFile().mkdirs();
        ZipOutputStream zip = null;
        FileOutputStream fW = null;
        try {
            fW = new FileOutputStream(f);
            try {
                zip = new ZipOutputStream(fW);
                if (options.isSkipRoot()) {
                    for (String s : source) {
                        File file1 = new File(s);
                        if (file1.isDirectory()) {
                            for (File file : file1.listFiles()) {
                                add("", file.getPath(), zip);
                            }
                        } else {
                            add("", file1.getPath(), zip);
                        }
                    }
                } else {
                    for (String s : source) {
                        add("", s, zip);
                    }
                }
            } finally {
                if (zip != null) {
                    zip.close();
                }
            }
        } finally {
            if (fW != null) {
                fW.close();
            }
        }
        if (options.isTempFile()) {
            targetFile.getParentFile().mkdirs();
            if (!f.renameTo(targetFile)) {
                Files.copy(f.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    //    private static void zipDir(String dirName, String nameZipFile) throws IOException {
//        ZipOutputStream zip = null;
//        FileOutputStream fW = null;
//        fW = new FileOutputStream(nameZipFile);
//        zip = new ZipOutputStream(fW);
//        addFolderToZip("", dirName, zip);
//        zip.close();
//        fW.close();
//    }
    private static void add(String path, String srcFolder, ZipOutputStream zip) throws IOException {
        File folder = new File(srcFolder);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFolder, zip);
        } else {
            addFileToZip(path, srcFolder, zip, false);
        }
    }

    private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws IOException {
        File folder = new File(srcFolder);
        if (folder.list().length == 0) {
            addFileToZip(path, srcFolder, zip, true);
        } else {
            for (String fileName : folder.list()) {
                if (path.equals("")) {
                    addFileToZip(folder.getName(), concatPath(srcFolder, fileName), zip, false);
                } else {
                    addFileToZip(concatPath(path, folder.getName()), srcFolder + "/" + fileName, zip, false);
                }
            }
        }
    }

    private static String concatPath(String a, String b) {
        if (a.endsWith("/")) {
            if (b.startsWith("/")) {
                return a + b.substring(1);
            } else {
                return a + b;
            }
        } else {
            if (b.startsWith("/")) {
                return a + b;
            } else {
                return a + "/" + b;
            }
        }
    }

    private static void addFileToZip(String path, String srcFile, ZipOutputStream zip, boolean flag) throws IOException {
        File folder = new File(srcFile);
        String pathPrefix = path;
        if (!pathPrefix.endsWith("/")) {
            pathPrefix = pathPrefix + "/";
        }
        if (!pathPrefix.startsWith("/")) {
            pathPrefix = "/" + pathPrefix;
        }

        if (flag) {
            zip.putNextEntry(new ZipEntry(pathPrefix + folder.getName() + "/"));
        } else {
            if (folder.isDirectory()) {
                addFolderToZip(pathPrefix, srcFile, zip);
            } else {
                byte[] buf = new byte[1024];
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                zip.putNextEntry(new ZipEntry(pathPrefix + folder.getName()));
                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                }
            }
        }
    }

    /**
     * Unzip it
     *
     * @param session      workspace
     * @param zipFile      input zip file
     * @param outputFolder zip file output folder
     * @param options      options
     * @throws IOException io exception
     */
    public static void unzip(NutsSession session, String zipFile, String outputFolder, UnzipOptions options) throws IOException {
        if (options == null) {
            options = new UnzipOptions();
        }
        byte[] buffer = new byte[1024];

        //create output directory is not exists
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }

        //get the zip file content
        try (ZipInputStream zis
                     = new ZipInputStream(new FileInputStream(new File(zipFile)))) {
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            String root = null;
            while (ze != null) {

                String fileName = ze.getName();
                if (options.isSkipRoot()) {
                    if (root == null) {
                        if (fileName.endsWith("/")) {
                            root = fileName;
                            ze = zis.getNextEntry();
                            continue;
                        } else {
                            throw new IOException("not a single root zip");
                        }
                    }
                    if (fileName.startsWith(root)) {
                        fileName = fileName.substring(root.length());
                    } else {
                        throw new IOException("not a single root zip");
                    }
                }
                if (fileName.endsWith("/")) {
                    File newFile = new File(outputFolder + File.separator + fileName);
                    newFile.mkdirs();
                } else {
                    File newFile = new File(outputFolder + File.separator + fileName);
                    NutsLoggerOp.of(ZipUtils.class, session).level(Level.FINEST).verb(NutsLoggerVerb.WARNING)
                            .log(NutsMessage.jstyle("file unzip : {0}", newFile.getAbsoluteFile()));
                    //create all non exists folders
                    //else you will hit FileNotFoundException for compressed folder
                    newFile.getParentFile().mkdirs();

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
        }
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

    //    public static void zip(final File _folder, final File _zipFilePath) {
//        final Path folder = _folder.toPath();
//        Path zipFilePath = _zipFilePath.toPath();
//        try (
//                FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
//                ZipOutputStream zos = new ZipOutputStream(fos)) {
//            Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    zos.putNextEntry(new ZipEntry(folder.relativize(file).toString()));
//                    Files.copy(file, zos);
//                    zos.closeEntry();
//                    return FileVisitResult.CONTINUE;
//                }
//
//                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                    zos.putNextEntry(new ZipEntry(folder.relativize(dir).toString() + "/"));
//                    zos.closeEntry();
//                    return FileVisitResult.CONTINUE;
//                }
//            });
//        } catch (IOException e) {
//            throw new RuntimeIOException(e);
//        }
//    }
    public static boolean visitZipStream(NutsPath zipFile, InputStreamVisitor visitor, NutsSession session) {
        try (InputStream is = zipFile.getInputStream()) {
            return visitZipStream(is, visitor, session);
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static boolean visitZipStream(Path zipFile, InputStreamVisitor visitor, NutsSession session) {
        try (InputStream is = Files.newInputStream(zipFile)) {
            return visitZipStream(is, visitor, session);
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public static boolean visitZipStream(InputStream zipFile, InputStreamVisitor visitor, NutsSession session) {
        //byte[] buffer = new byte[4 * 1024];

        //get the zip file content
        ZipInputStream zis = null;
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
                if (!fileName.equals("/") && fileName.startsWith("/")) {
                    fileName = fileName.substring(1);
                }
                if (!fileName.endsWith("/")) {
                    if (!visitor.visit(fileName, entryInputStream)) {
                        break;
                    }
                }
                ze = zis.getNextEntry();
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        } finally {
            if (zis != null) {
                try {
                    zis.close();
                } catch (IOException ex) {
                    throw new NutsIOException(session, ex);
                }
            }
        }
        return false;
    }
}
