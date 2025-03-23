package net.thevpc.nuts.runtime.standalone.xtra.compress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
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
import java.util.zip.GZIPInputStream;

public class NUncompressGzip implements NUncompressPackaging {
    private NWorkspace workspace;

    public NUncompressGzip(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public void visitPackage(NUncompress uncompress, NInputSource source, NUncompressVisitor visitor) {
        try {
            String baseName = source.getMetaData().getName().orElse("no-name");
            //get the zip file content
            InputStream _in = source.getInputStream();
            try {
                try (GZIPInputStream zis = new GZIPInputStream(_in)) {
                    String n = NPath.of(baseName).getName();
                    if (n.endsWith(".gz")) {
                        n = n.substring(0, n.length() - 3);
                    }
                    if (n.isEmpty()) {
                        n = "data";
                    }
                    //get the zipped file list entry
                    visitor.visitFile(n, new InputStream() {
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
                    });
                }
            } finally {
                _in.close();
            }
        } catch (IOException ex) {
            _LOGOP().level(Level.CONFIG).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("error uncompressing {0} to {1} : {2}", source,
                            uncompress.getTarget(), ex));
            throw new NIOException(ex);
        }
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(NUncompressGzip.class);
    }

    @Override
    public void uncompressPackage(NUncompress uncompress, NInputSource source) {
        NOutputTarget target = uncompress.getTarget();
        try {
            NPath _target = asValidTargetPath(target);
            if (_target == null) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid target %s", target));
            }
            Path folder = _target.toPath().get();
            NPath.of(folder).mkdirs();

            String baseName = source.getMetaData().getName().orElse("no-name");
            byte[] buffer = new byte[1024];

            //get the zip file content
            InputStream _in = source.getInputStream();
            try {
                try (GZIPInputStream zis = new GZIPInputStream(_in)) {
                    String n = NPath.of(baseName).getName();
                    if (n.endsWith(".gz")) {
                        n = n.substring(0, n.length() - 3);
                    }
                    if (n.isEmpty()) {
                        n = "data";
                    }
                    //get the zipped file list entry
                    Path newFile = folder.resolve(n);
                    _LOGOP().level(Level.FINEST).verb(NLogVerb.WARNING)
                            .log(NMsg.ofJ("file unzip : {0}", newFile));
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
            } finally {
                _in.close();
            }
        } catch (IOException ex) {
            _LOGOP().level(Level.CONFIG).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("error uncompressing {0} to {1} : {2}", source,
                            target, ex));
            throw new NIOException(ex);
        }
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
        if (
                z.equals("gzip")
                        || z.equals("gz")
        ) {
            return NConstants.Support.DEFAULT_SUPPORT;
        }
        return NConstants.Support.NO_SUPPORT;
    }
}
