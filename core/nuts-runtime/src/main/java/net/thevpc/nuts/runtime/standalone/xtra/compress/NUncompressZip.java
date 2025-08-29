package net.thevpc.nuts.runtime.standalone.xtra.compress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.spi.NUncompressPackaging;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
        NOutputTarget target = uncompress.getTarget();
        try {
            NPath _target = asValidTargetPath(target);
            if (_target == null) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid target %s", target));
            }
            Path folder = _target.toPath().get();
            NPath.of(folder).mkdirs();
            byte[] buffer = new byte[10 * 4 * 1024];
            InputStream _in = source.getInputStream();
            try {
                try (ZipInputStream zis = new ZipInputStream(_in)) {
                    //get the zipped file list entry
                    ZipEntry ze = zis.getNextEntry();
                    String root = null;
                    while (ze != null) {
                        String fileName = ze.getName();
                        if (uncompress.isSkipRoot()) {
                            String root2 = extractRoot(fileName);
                            if (root == null) {
                                root = root2;
                            } else if (!Objects.equals(root2, root)) {
                                throw new IOException("not a single root zip : '" + root2 + "' <> '" + root + "'");
                            }
                            fileName = fileName.substring(root.length());
                        }
                        if (fileName.endsWith("/")) {
                            Path newFile = folder.resolve(fileName);
                            NPath.of(newFile).mkdirs();
                        } else {
                            Path newFile = folder.resolve(fileName);
                            _LOG()
                                    .log(NMsg.ofJ("file unzip : {0}", newFile)
                                            .asFinestAlert());
                            //create all non exists folders
                            //else you will hit FileNotFoundException for compressed folder
                            if (newFile.getParent() != null) {
                                NPath.of(newFile).mkParentDirs();
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
            _LOG()
                    .log(NMsg.ofJ("error uncompressing {0} to {1} : {2}",
                            source, target, ex)
                            .withLevel(Level.CONFIG).withIntent(NMsgIntent.FAIL));
            throw new NIOException(ex);
        }
    }

    public void visitPackage(NUncompress uncompress, NInputSource source, NUncompressVisitor visitor) {
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
