package net.thevpc.nuts.runtime.standalone.xtra.compress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.NCompressPackaging;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NLogVerb;
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
    private NLog LOG;

    public NCompressZip() {
    }

    public void compressPackage(NCompress compress) {
        //checkSession();
        NSession session = compress.getSession();
        List<NInputSource> sources = compress.getSources();
        NAssert.requireNonBlank(sources, "source", session);
        NOutputTarget target = compress.getTarget();
        NAssert.requireNonBlank(target, "target", session);
        _LOG(session).with().level(Level.FINEST).verb(NLogVerb.START).log(NMsg.ofJ("compress {0} to {1}", sources, target));
        try {
            OutputStream fW = null;
            ZipOutputStream zip = null;
            if (target instanceof NPath) {
                Path tempPath = null;
                if (compress.isSafe()) {
                    tempPath = NPath.ofTempFile("zip", session).toPath().get();
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
                                        add("", c, zip, session);
                                    }
                                } else {
                                    add("", file1, zip, session);
                                }
                            }
                        } else {
                            for (NInputSource s : sources) {
                                add("", new DefaultNCompress.Item(s, compress), zip, session);
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
                                CoreIOUtils.copy(ii, jj, session);
                            }
                        }
                    } else {
                        CoreIOUtils.copy(
                                Files.newInputStream(tempPath),
                                target.getOutputStream(), session
                        );
                    }
                }
            } else {
                throw new NIllegalArgumentException(session, NMsg.ofC("unsupported target %s", target));
            }
        } catch (IOException ex) {
            _LOG(session).with().level(Level.CONFIG).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("error compressing {0} to {1} : {2}",
                            sources, target, ex));
            throw new NIOException(session, ex);
        }
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(NCompressZip.class, session);
        }
        return LOG;
    }


    private void add(String path, DefaultNCompress.Item srcFolder, ZipOutputStream zip, NSession session) {
        if (srcFolder.isSourceDirectory()) {
            addFolderToZip(path, srcFolder, zip, session);
        } else {
            addFileToZip(path, srcFolder, zip, false, session);
        }
    }

    private void addFolderToZip(String path, DefaultNCompress.Item srcFolder, ZipOutputStream zip, NSession session) throws UncheckedIOException {
        DefaultNCompress.Item[] dirChildren = srcFolder.list();
        if (dirChildren.length == 0) {
            addFileToZip(path, srcFolder, zip, true, session);
        } else {
            for (DefaultNCompress.Item c : dirChildren) {
                if (path.equals("")) {
                    addFileToZip(srcFolder.getName(), c, zip, false, session);
                } else {
                    addFileToZip(
                            concatPath(path, srcFolder.getName()),
                            c, zip, false, session);
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

    private void addFileToZip(String path, DefaultNCompress.Item srcFile, ZipOutputStream zip, boolean flag, NSession session) throws UncheckedIOException {
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
                    addFolderToZip(pathPrefix, srcFile, zip, session);
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
            throw new NIOException(session, ex);
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
