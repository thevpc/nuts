package net.thevpc.nuts.runtime.standalone.xtra.compress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.spi.NUncompressPackaging;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NUncompressZip implements NUncompressPackaging {
    private NLog LOG;

    public void uncompressPackage(NUncompress uncompress, NInputSource source) {
        NSession session = uncompress.getSession();
        NOutputTarget target = uncompress.getTarget();
        try {
            NPath _target = asValidTargetPath(target);
            if (_target == null) {
                throw new NIllegalArgumentException(session, NMsg.ofC("invalid target %s", target));
            }
            Path folder = _target.toPath().get();
            NPath.of(folder, session).mkdirs();
            byte[] buffer = new byte[1024];
            InputStream _in = source.getInputStream();
            try {
                try (ZipInputStream zis = new ZipInputStream(_in)) {
                    //get the zipped file list entry
                    ZipEntry ze = zis.getNextEntry();
                    String root = null;
                    while (ze != null) {

                        String fileName = ze.getName();
                        if (uncompress.isSkipRoot()) {
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
                            Path newFile = folder.resolve(fileName);
                            NPath.of(newFile, session).mkdirs();
                        } else {
                            Path newFile = folder.resolve(fileName);
                            _LOGOP(session).level(Level.FINEST).verb(NLogVerb.WARNING)
                                    .log(NMsg.ofJ("file unzip : {0}", newFile));
                            //create all non exists folders
                            //else you will hit FileNotFoundException for compressed folder
                            if (newFile.getParent() != null) {
                                NPath.of(newFile, session).mkParentDirs();
                            }
                            try (OutputStream fos = Files.newOutputStream(newFile)) {
                                int len;
                                while ((len = zis.read(buffer)) > 0) {
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
            _LOGOP(session).level(Level.CONFIG).verb(NLogVerb.FAIL).log(
                    NMsg.ofJ("error uncompressing {0} to {1} : {2}",
                            source, target, ex));
            throw new NIOException(session, ex);
        }
    }

    public void visitPackage(NUncompress uncompress, NInputSource source, NUncompressVisitor visitor) {
        NSession session = uncompress.getSession();
        try {
            //get the zip file content
            InputStream _in = source.getInputStream();
            try {
                try (ZipInputStream zis = new ZipInputStream(_in)) {
                    //get the zipped file list entry
                    ZipEntry ze = zis.getNextEntry();
                    String root = null;
                    while (ze != null) {

                        String fileName = ze.getName();
                        if (uncompress.isSkipRoot()) {
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
            _LOGOP(session).level(Level.CONFIG).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("error visiting {0} : {2}",
                            source, ex));
            throw new NIOException(session, ex);
        }
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(NUncompressZip.class, session);
        }
        return LOG;
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

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        NUncompress c = context.getConstraints(NUncompress.class);
        String z = NStringUtils.trim(c.getPackaging()).toLowerCase();
        if (z.isEmpty()
                || z.equals("zip")
                || z.equals("jar")
                || z.equals("war")
        ) {
            return NConstants.Support.DEFAULT_SUPPORT;
        }
        return NConstants.Support.NO_SUPPORT;
    }
}
