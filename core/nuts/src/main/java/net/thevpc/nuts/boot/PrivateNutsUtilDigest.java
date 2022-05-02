/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.util.NutsUtilStrings;

import java.io.*;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;

class PrivateNutsUtilDigest {
    public static String getURLDigest(URL url, PrivateNutsBootLog bLog) {
        if (url != null) {
            File ff = PrivateNutsUtilIO.toFile(url);
            if (ff != null) {
                return getFileOrDirectoryDigest(ff.toPath());
            }
            InputStream is = null;
            try {
                is = PrivateNutsUtilIO.openStream(url, bLog);
                if (is != null) {
                    return getStreamDigest(is);
                }
            } catch (Exception e) {
                //
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        //
                    }
                }
            }
        }
        return null;
    }

    public static String getFileOrDirectoryDigest(Path p) {
        if (Files.isDirectory(p)) {
            return getDirectoryDigest(p);
        } else if (Files.isRegularFile(p)) {
            try (InputStream is = Files.newInputStream(p)) {
                return getStreamDigest(is);
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }

    private static String getDirectoryDigest(Path p) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            Files.walkFileTree(p, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    incrementalUpdateFileDigest(new ByteArrayInputStream(dir.toString().getBytes()), md);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    incrementalUpdateFileDigest(new ByteArrayInputStream(file.toString().getBytes()), md);
                    incrementalUpdateFileDigest(Files.newInputStream(file), md);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
            byte[] digest = md.digest();
            return NutsUtilStrings.toHexString(digest);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getStreamDigest(InputStream is) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return getStreamDigest(is, md, 2048);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getStreamDigest(InputStream is, MessageDigest md, int byteArraySize) {
        try {
            md.reset();
            byte[] bytes = new byte[byteArraySize];
            int numBytes;
            while ((numBytes = is.read(bytes)) != -1) {
                md.update(bytes, 0, numBytes);
            }
            byte[] digest = md.digest();
            return NutsUtilStrings.toHexString(digest);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void incrementalUpdateFileDigest(InputStream is, MessageDigest md) {
        try {
            byte[] bytes = new byte[4096];
            int numBytes;
            while ((numBytes = is.read(bytes)) != -1) {
                md.update(bytes, 0, numBytes);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
