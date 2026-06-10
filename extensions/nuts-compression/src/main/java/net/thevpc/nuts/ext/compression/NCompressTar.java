package net.thevpc.nuts.ext.compression;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.spi.NCompressPackaging;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.util.*;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;

public class NCompressTar implements NCompressPackaging {

    public NCompressTar() {
    }

    /**
     * Factory method to allow NCompressTarGz to wrap the stream in GZIP.
     */
    protected TarArchiveOutputStream createTarOutputStream(OutputStream out) throws IOException {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(out);
        // Enable POSIX mode to support long file names and large files (> 8GB)
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        return taos;
    }

    @Override
    public void compressPackage(NCompress compress) {
        List<NInputSource> sources = compress.sources();
        NAssert.requireNamedNonBlank(sources, "source");
        NOutputTarget target = compress.target();
        NAssert.requireNamedNonBlank(target, "target");
        NChronometer chronometer = NChronometer.of();
        _LOG().log(NMsg.ofC("compress %s to %s", sources, target).asFinest().withIntent(NMsgIntent.START).withLevel(Level.FINEST));

        try {
            OutputStream fW = null;
            if (target instanceof NPath) {
                Path tempPath = null;
                if (compress.isSafe()) {
                    tempPath = NPath.ofTempFile("tar").toPath().get();
                }
                ((NPath) target).mkParentDirs();

                fW = (tempPath == null) ? target.outputStream() : Files.newOutputStream(tempPath);
                try {
                    try (TarArchiveOutputStream taos = createTarOutputStream(fW)) {
                        if (compress.isSkipRoot()) {
                            for (NInputSource s : sources) {
                                DefaultNCompressItem1 file1 = new DefaultNCompressItem1(s, compress);
                                if (file1.isSourceDirectory()) {
                                    for (DefaultNCompressItem1 c : file1.list()) {
                                        add("", c, taos);
                                    }
                                } else {
                                    add("", file1, taos);
                                }
                            }
                        } else {
                            for (NInputSource s : sources) {
                                add("", new DefaultNCompressItem1(s, compress), taos);
                            }
                        }
                    }
                } finally {
                    if (fW != null) fW.close();
                }

                if (tempPath != null) {
                    if (target instanceof NPath) {
                        Files.move(tempPath, ((NPath) target).toPath().get(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        try (InputStream ii = Files.newInputStream(tempPath); OutputStream jj = target.outputStream()) {
                            NIOUtils.copy(ii, jj);
                        }
                    }
                }
            } else {
                throw new NIllegalArgumentException(NMsg.ofC("unsupported target %s", target));
            }

            _LOG().log(NMsg.ofC("compressed %s to %s", sources, target).asFinest().withIntent(NMsgIntent.SUCCESS).withDurationMillis(chronometer.durationMs()));
        } catch (IOException ex) {
            _LOG().log(NMsg.ofC("error compressing %s to %s : %s", sources, target, ex).asConfig().withIntent(NMsgIntent.FAIL).withDurationMillis(chronometer.durationMs()));
            throw new NIOException(ex);
        } catch (RuntimeException ex) {
            _LOG().log(NMsg.ofC("error compressing %s to %s : %s", sources, target, ex).asConfig().withIntent(NMsgIntent.FAIL).withDurationMillis(chronometer.durationMs()));
            throw ex;
        }
    }

    private void add(String path, DefaultNCompressItem1 srcFolder, TarArchiveOutputStream taos) {
        if (srcFolder.isSourceDirectory()) {
            addFolderToTar(path, srcFolder, taos);
        } else {
            addFileToTar(path, srcFolder, taos, false);
        }
    }

    private void addFolderToTar(String path, DefaultNCompressItem1 srcFolder, TarArchiveOutputStream taos) {
        DefaultNCompressItem1[] dirChildren = srcFolder.list();
        if (dirChildren.length == 0) {
            addFileToTar(path, srcFolder, taos, true);
        } else {
            for (DefaultNCompressItem1 c : dirChildren) {
                if (path.equals("")) {
                    addFileToTar(srcFolder.getName(), c, taos, false);
                } else {
                    addFileToTar(concatPath(path, srcFolder.getName()), c, taos, false);
                }
            }
        }
    }

    private String stripTarPath(String path) {
        if (path.length() > 1 && path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    private void addFileToTar(String path, DefaultNCompressItem1 srcFile, TarArchiveOutputStream taos, boolean isDir) {
        String pathPrefix = path;
        if (!pathPrefix.endsWith("/")) pathPrefix = pathPrefix + "/";
        if (!pathPrefix.startsWith("/")) pathPrefix = "/" + pathPrefix;

        String entryName = stripTarPath(pathPrefix + srcFile.getName());

        try {
            if (isDir || srcFile.isSourceDirectory()) {
                if (!entryName.endsWith("/")) entryName += "/";
                TarArchiveEntry entry = new TarArchiveEntry(entryName);
                taos.putArchiveEntry(entry);
                taos.closeArchiveEntry();
                if (srcFile.isSourceDirectory()) {
                    addFolderToTar(pathPrefix, srcFile, taos);
                }
            } else {
                // TAR requires exact file size in the header.
                // We buffer the content to guarantee we know the size, regardless of NInputSource implementation.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int len;
                try (InputStream in = srcFile.open()) {
                    while ((len = in.read(buf)) > 0) {
                        baos.write(buf, 0, len);
                    }
                }
                byte[] fileContent = baos.toByteArray();

                TarArchiveEntry entry = new TarArchiveEntry(entryName);
                entry.setSize(fileContent.length);
                taos.putArchiveEntry(entry);
                taos.write(fileContent);
                taos.closeArchiveEntry();
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    private static String concatPath(String a, String b) {
        if (a.endsWith("/")) {
            return b.startsWith("/") ? a + b.substring(1) : a + b;
        } else {
            return b.startsWith("/") ? a + b : a + "/" + b;
        }
    }

    protected NLog _LOG() {
        return NLog.of(NCompressTar.class);
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String z = NStringUtils.trim(context.criteria(String.class)).toLowerCase();
        if (z.equals("tar")) {
            return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }
}