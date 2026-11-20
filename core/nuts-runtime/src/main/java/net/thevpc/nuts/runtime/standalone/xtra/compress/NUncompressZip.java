package net.thevpc.nuts.runtime.standalone.xtra.compress;

import net.thevpc.nuts.io.*;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.io.util.NArchiveSecurityUtils;
import net.thevpc.nuts.spi.NUncompressPackaging;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.text.NMsg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NUncompressZip implements NUncompressPackaging {
    public NUncompressZip() {
    }

    private String extractRoot(String fileName) {
        int si = fileName.indexOf("/");
        if (si >= 0) {
            return fileName.substring(0, si + 1);
        }
        return "";
    }

    public void uncompressPackage(NUncompress uncompress, NInputSource source) {
        NOutputTarget target = uncompress.target();
        List<Path> createdFiles = new ArrayList<Path>();
        try {
            NPath _target = asValidTargetPath(target);
            if (_target == null) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid target %s", target));
            }
            Path folder = _target.toPath().get();
            NPath.of(folder).mkdirs();
            NArchiveSecurityUtils.Limits limits = NArchiveSecurityUtils.Limits.fromSystemProperties();
            byte[] buffer = new byte[10 * 4 * 1024];
            InputStream _in = source.inputStream();
            try {
                try (ZipInputStream zis = new ZipInputStream(_in)) {
                    //get the zipped file list entry
                    ZipEntry ze = zis.getNextEntry();
                    String root = null;
                    while (ze != null) {
                        limits.startEntry(ze.getSize(), ze.getCompressedSize());
                        String fileName0 = ze.getName();
                        NArchiveSecurityUtils.validateEntryName(fileName0);
                        String fileName = fileName0;
                        if (uncompress.isSkipRoot()) {
                            String currentRoot = extractRoot(fileName0);

                            if (root == null) {
                                root = currentRoot;
                            } else if (!Objects.equals(root, currentRoot)) {
                                throw new IOException("not a single root archive: '" + currentRoot + "' <> '" + root + "'");
                            }

                            // Strip the root from the filename
                            fileName = fileName0.substring(root.length());

                            // Edge case: If the entry IS the root directory itself, its stripped name will be empty.
                            // We skip it to avoid creating an empty directory or triggering weird edge cases.
                            if (fileName.isEmpty() || fileName.equals("/")) {
                                ze = zis.getNextEntry();
                                continue;
                            }
                        }
                        Path newFile = NArchiveSecurityUtils.resolveSafeEntry(folder, fileName);
                        if (fileName.endsWith("/")) {
                            if (!fileName.isEmpty()) { //check the case of skip root
                                Files.createDirectories(newFile);
                            }
                        } else {
                            _LOG()
                                    .log(NMsg.ofJ("file unzip : {0}", newFile)
                                            .asFinestAlert());
                            //create all non exists folders
                            //else you will hit FileNotFoundException for compressed folder
                            if (newFile.getParent() != null) {
                                Files.createDirectories(newFile.getParent());
                            }
                            createdFiles.add(newFile);
                            try (OutputStream fos = Files.newOutputStream(newFile)) {
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
                                    limits.addBytes(len);
                                    fos.write(buffer, 0, len);
                                }
                            }
                        }
                        ze = zis.getNextEntry();
                    }
                    zis.closeEntry();
                }
            } finally {
                _in.close();
            }
        } catch (IOException ex) {
            cleanupCreatedFiles(createdFiles);
            _LOG()
                    .log(NMsg.ofJ("error uncompressing {0} to {1} : {2}",
                            source, target, ex)
                            .withLevel(Level.CONFIG).withIntent(NMsgIntent.FAIL));
            throw new NIOException(ex);
        } catch (RuntimeException ex) {
            cleanupCreatedFiles(createdFiles);
            throw ex;
        }
    }

    private static void cleanupCreatedFiles(List<Path> createdFiles) {
        for (int i = createdFiles.size() - 1; i >= 0; i--) {
            try {
                Files.deleteIfExists(createdFiles.get(i));
            } catch (IOException ex) {
                // best effort cleanup of a failed extraction
            }
        }
    }

    public void visitPackage(NUncompress uncompress, NInputSource source, NUncompressVisitor visitor) {
        try {
            //get the zip file content
            InputStream _in = source.inputStream();
            try {
                try (ZipInputStream zis = new ZipInputStream(_in)) {
                    //get the zipped file list entry
                    ZipEntry ze = zis.getNextEntry();
                    String root = null;
                    while (ze != null) {

                        String fileName = ze.getName();
                        NArchiveSecurityUtils.validateEntryName(fileName);
                        if (uncompress.isSkipRoot()) {
                            String currentRoot = extractRoot(fileName);

                            if (root == null) {
                                root = currentRoot;
                            } else if (!Objects.equals(root, currentRoot)) {
                                throw new IOException("not a single root archive: '" + currentRoot + "' <> '" + root + "'");

                            }

                            // Strip the root from the filename
                            fileName = fileName.substring(root.length());

                            // Edge case: Skip the root directory entry itself
                            if (fileName.isEmpty() || fileName.equals("/")) {
                                continue;
                            }
                        }
                        if (fileName.endsWith("/")) {
                            if (!visitor.visitFolder(fileName)) {
                                break;
                            }
                        } else {
                            if (!visitor.visitFile(fileName, new InputStream() {
                                @Override
                                public int read() throws IOException {
                                    return zis.read();
                                }

                                @Override
                                public int read(byte[] b, int off, int len) throws IOException {
                                    return zis.read(b, off, len);
                                }

                                @Override
                                public int read(byte[] b) throws IOException {
                                    return zis.read(b);
                                }
                            })) {
                                break;
                            }
                        }
                        ze = zis.getNextEntry();
                    }
                    zis.closeEntry();
                }
            } finally {
                _in.close();
            }
        } catch (IOException ex) {
            _LOG()
                    .log(NMsg.ofJ("error visiting {0} : {2}",
                            source, ex).asConfig().withIntent(NMsgIntent.FAIL));
            throw new NIOException(ex);
        }
    }

    protected NLog _LOG() {
        return NLog.of(NUncompressZip.class);
    }

    private NPath asValidTargetPath(NOutputTarget target) {
        if (target != null) {
            if (target instanceof NPath) {
                NPath p = (NPath) target;
                //if (p.isFile()) {
                return p;
                //}
            }
        }
        return null;
    }

    @NScore(fixed = NScorable.DEFAULT_SCORE)
    public static int getScore(NScorableContext context) {
        String z = NStringUtils.strip(context.criteria(String.class)).toLowerCase();
        if (z.isEmpty()
                || z.equals("zip")
                || z.equals("jar")
                || z.equals("war")
        ) {
            return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }
}
