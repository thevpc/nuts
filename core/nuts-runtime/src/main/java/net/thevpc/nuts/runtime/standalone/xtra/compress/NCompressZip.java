package net.thevpc.nuts.runtime.standalone.xtra.compress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.spi.NCompressPackaging;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class NCompressZip implements NCompressPackaging {
    public NCompressZip() {
    }

    public void compressPackage(NCompress compress) {
        List<NInputSource> sources = compress.getSources();
        NAssert.requireNonBlank(sources, "source");
        NOutputTarget target = compress.getTarget();
        NAssert.requireNonBlank(target, "target");
        NChronometer chronometer = NChronometer.startNow();
        _LOG().log(NMsg.ofC("compress %s to %s", sources, target).asFinest().withIntent(NMsgIntent.START).withLevel(Level.FINEST).withIntent(NMsgIntent.START));
        try {
            OutputStream fW = null;
            ZipOutputStream zip = null;
            if (target instanceof NPath) {
                Path tempPath = null;
                if (compress.isSafe()) {
                    tempPath = NPath.ofTempFile("zip").toPath().get();
                }
                if (target instanceof NPath) {
                    ((NPath) target).mkParentDirs();
                }
                if (tempPath == null) {
                    fW = target.getOutputStream();
                } else {
                    fW = Files.newOutputStream(tempPath);
                }
                try {
                    try {
                        zip = new ZipOutputStream(fW);
                        if (compress.isSkipRoot()) {
                            for (NInputSource s : sources) {
                                DefaultNCompress.Item file1 = new DefaultNCompress.Item(s, compress);
                                if (file1.isSourceDirectory()) {
                                    for (DefaultNCompress.Item c : file1.list()) {
                                        add("", c, zip);
                                    }
                                } else {
                                    add("", file1, zip);
                                }
                            }
                        } else {
                            for (NInputSource s : sources) {
                                add("", new DefaultNCompress.Item(s, compress), zip);
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
                if (tempPath != null) {
                    if (target instanceof NPath/* && ((NPath) target).isFile()*/) {
                        Files.move(tempPath, ((NPath) target).toPath().get(),
                                StandardCopyOption.REPLACE_EXISTING);
                    } else if (target instanceof NPath) {
                        try (InputStream ii = Files.newInputStream(tempPath)) {
                            try (OutputStream jj = target.getOutputStream()) {
                                NIOUtils.copy(ii, jj);
                            }
                        }
                    } else {
                        NIOUtils.copy(
                                Files.newInputStream(tempPath),
                                target.getOutputStream()
                        );
                    }
                }
            } else {
                throw new NIllegalArgumentException(NMsg.ofC("unsupported target %s", target));
            }
            _LOG()
                    .log(NMsg.ofC("compressed %s to %s", sources, target).asFinest().withIntent(NMsgIntent.SUCCESS).withDurationMillis(chronometer.getDurationMs()));
        } catch (IOException ex) {
            _LOG()
                    .log(NMsg.ofC("error compressing %s to %s : %s",
                            sources, target, ex).asConfig().withIntent(NMsgIntent.FAIL).withDurationMillis(chronometer.getDurationMs()));
            throw new NIOException(ex);
        } catch (RuntimeException ex) {
            _LOG()
                    .log(NMsg.ofC("error compressing %s to %s : %s",
                            sources, target, ex).asConfig().withIntent(NMsgIntent.FAIL).withDurationMillis(chronometer.getDurationMs()));
            throw ex;
        }
    }

    protected NLog _LOG() {
        return NLog.of(NCompressZip.class);
    }


    private void add(String path, DefaultNCompress.Item srcFolder, ZipOutputStream zip) {
        if (srcFolder.isSourceDirectory()) {
            addFolderToZip(path, srcFolder, zip);
        } else {
            addFileToZip(path, srcFolder, zip, false);
        }
    }

    private void addFolderToZip(String path, DefaultNCompress.Item srcFolder, ZipOutputStream zip) throws UncheckedIOException {
        DefaultNCompress.Item[] dirChildren = srcFolder.list();
        if (dirChildren.length == 0) {
            addFileToZip(path, srcFolder, zip, true);
        } else {
            for (DefaultNCompress.Item c : dirChildren) {
                if (path.equals("")) {
                    addFileToZip(srcFolder.getName(), c, zip, false);
                } else {
                    addFileToZip(
                            concatPath(path, srcFolder.getName()),
                            c, zip, false);
                }
            }
        }
    }

    private String stripZipPath(String path) {
        if (path.length() > 1) {
            if (path.charAt(0) == '/') {
                return path.substring(1);
            }
        }
        return path;
    }

    private void addFileToZip(String path, DefaultNCompress.Item srcFile, ZipOutputStream zip, boolean flag) throws UncheckedIOException {
//        File folder = new File(srcFile);
        String pathPrefix = path;
        if (!pathPrefix.endsWith("/")) {
            pathPrefix = pathPrefix + "/";
        }
        if (!pathPrefix.startsWith("/")) {
            pathPrefix = "/" + pathPrefix;
        }
        try {

            if (flag) {
                zip.putNextEntry(new ZipEntry(stripZipPath(pathPrefix + srcFile.getName() + "/")));
            } else {
                if (srcFile.isSourceDirectory()) {
                    addFolderToZip(pathPrefix, srcFile, zip);
                } else {
                    byte[] buf = new byte[1024];
                    int len;
                    InputStream in = srcFile.open();
                    zip.putNextEntry(new ZipEntry(stripZipPath(pathPrefix + srcFile.getName())));
                    while ((len = in.read(buf)) > 0) {
                        zip.write(buf, 0, len);
                    }
                }
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
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


    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        NCompress c = context.getConstraints(NCompress.class);
        String z = NStringUtils.trim(c.getPackaging()).toLowerCase();
        if (z.isEmpty()
                || z.equals("zip")
                || z.equals("gzip")
                || z.equals("gz")
        ) {
            return NConstants.Support.DEFAULT_SUPPORT;
        }
        return NConstants.Support.NO_SUPPORT;
    }
}
