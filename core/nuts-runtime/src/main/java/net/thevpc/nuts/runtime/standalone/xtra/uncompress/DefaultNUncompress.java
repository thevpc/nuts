/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.uncompress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.progress.SingletonNInputStreamProgressFactory;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author thevpc
 */
public class DefaultNUncompress implements NUncompress {

    private NLog LOG;

    private boolean skipRoot = false;
    private boolean safe = true;
    private String format = "zip";
    private NWorkspace ws;
    private NInputSource source;
    private NOutputTarget target;
    private NSession session;
    private NProgressFactory progressFactory;
    private Set<NPathOption> options = new LinkedHashSet<>();
    private NUncompressVisitor visitor;

    public DefaultNUncompress(NSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultNUncompress.class, session);
        }
        return LOG;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public NUncompress setFormat(String format) {
        checkSession();
        if (NBlankable.isBlank(format)) {
            format = "zip";
        }
        switch (format) {
            case "zip":
            case "gzip":
            case "gz": {
                this.format = format;
                break;
            }
            default: {
                throw new NUnsupportedArgumentException(getSession(), NMsg.ofC("unsupported compression format %s", format));
            }
        }
        return this;
    }

    @Override
    public NInputSource getSource() {
        return source;
    }

    protected void checkSession() {
        NSessionUtils.checkSession(ws, session);
    }

    @Override
    public NUncompress setSource(NInputSource source) {
        this.source = source;
        return this;
    }

    @Override
    public NUncompress setTarget(NOutputTarget target) {
        this.target = target;
        return this;
    }


    @Override
    public NUncompress setSource(InputStream source) {
        checkSession();
        this.source = source == null ? null : NIO.of(session).ofInputSource(source);
        return this;
    }

    @Override
    public NUncompress setSource(NPath source) {
        checkSession();
        this.source = source;
        return this;
    }

    @Override
    public NUncompress setSource(File source) {
        this.source = source == null ? null : NPath.of(source, session);
        return this;
    }

    @Override
    public NUncompress setSource(Path source) {
        this.source = source == null ? null : NPath.of(source, session);
        return this;
    }

    @Override
    public NUncompress setSource(URL source) {
        this.source = source == null ? null : NPath.of(source, session);
        return this;
    }

    @Override
    public NUncompress setTarget(Path target) {
        this.target = target == null ? null : NPath.of(target, session);
        return this;
    }

    @Override
    public NUncompress setTarget(File target) {
        this.target = target == null ? null : NPath.of(target, session);
        return this;
    }

    public NUncompress setTarget(NPath target) {
        this.target = target;
        return this;
    }

    @Override
    public NUncompress from(NPath source) {
        this.source = source;
        return this;
    }

    @Override
    public NUncompress to(NPath target) {
        this.target = target;
        return this;
    }

    @Override
    public NOutputTarget getTarget() {
        return target;
    }

    //    @Override


    @Override
    public NUncompress from(InputStream source) {
        return setSource(source);
    }

    @Override
    public NUncompress from(File source) {
        return setSource(source);
    }

    @Override
    public NUncompress from(Path source) {
        return setSource(source);
    }

    @Override
    public NUncompress from(URL source) {
        return setSource(source);
    }

    @Override
    public NUncompress to(File target) {
        return setTarget(target);
    }

    @Override
    public NUncompress to(Path target) {
        return setTarget(target);
    }

    @Override
    public boolean isSafe() {
        return safe;
    }

    @Override
    public DefaultNUncompress setSafe(boolean value) {
        this.safe = value;
        return this;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NUncompress setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public NUncompress run() {
        checkSession();
        CompressType compressType = toCompressType(getFormat());
        NAssert.requireNonNull(source, "source", getSession());

        NInputSource _source = source;
        if (options.contains(NPathOption.LOG)
                || options.contains(NPathOption.TRACE)
                || getProgressFactory() != null) {
            NInputStreamMonitor monitor = NInputStreamMonitor.of(session);
            monitor.setOrigin(source);
            monitor.setLogProgress(options.contains(NPathOption.LOG));
            monitor.setTraceProgress(options.contains(NPathOption.TRACE));
            monitor.setProgressFactory(getProgressFactory());
            monitor.setSource(source);
            _source = NIO.of(session).ofInputSource(monitor.create());
        }

        if (visitor == null && target == null) {
            NAssert.requireNonNull(target, "target", getSession());
        } else if (visitor != null && target != null) {
            throw new NIllegalArgumentException(getSession(), NMsg.ofC("invalid target %s when visitor is specified", target));
        }else if(visitor!=null){
            return runVisitor(_source,compressType);
        }


        _LOGOP(session).level(Level.FINEST).verb(NLogVerb.START)
                .log(NMsg.ofJ("uncompress {0} to {1}", _source, target));
        switch (compressType) {
            case ZIP: {
                runUnzipToTarget(_source);
                return this;
            }
            case GZIP:{
                runUngzipToTarget(_source);
                return this;
            }
        }
        throw new NUnsupportedArgumentException(getSession(), NMsg.ofC("unsupported format %s", format));
    }

    private NPath asValidTargetPath() {
        if (target != null) {
            if (target instanceof NPath) {
                NPath p = (NPath) target;
                if (p.isFile()) {
                    return p;
                }
            }
        }
        return null;
    }

    @Override
    public NUncompress visit(NUncompressVisitor visitor) {
        this.visitor = visitor;
        return this;
    }

    private CompressType toCompressType(String format) {
        if (NBlankable.isBlank(format)) {
            format = "zip";
        }
        switch (format) {
            case "zip": {
                return CompressType.ZIP;
            }
            case "gzip":
            case "gz": {
                return CompressType.GZIP;
            }
        }
        throw new NUnsupportedArgumentException(getSession(), NMsg.ofC("unsupported format %s", format));
    }

    private NUncompress runVisitor(NInputSource source,CompressType format) {
        switch (format) {
            case ZIP: {
                visitUnzip(source,visitor);
                break;
            }
            case GZIP:{
                visitUngzip(source,visitor);
                break;
            }
            default: {
                throw new NUnsupportedArgumentException(getSession(), NMsg.ofC("unsupported format %s", format));
            }
        }
        return this;
    }

    private void runUnzipToTarget(NInputSource source) {
        try {
            NPath _target = asValidTargetPath();
            if (_target == null) {
                throw new NIllegalArgumentException(getSession(), NMsg.ofC("invalid target %s", target));
            }
            Path folder = _target.toFile();
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
                        if (skipRoot) {
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

    private void visitUnzip(NInputSource source,NUncompressVisitor visitor) {
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
                        if (skipRoot) {
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

    private void runUngzipToTarget(NInputSource source) {
        try {
            NPath _target = asValidTargetPath();
            if (_target == null) {
                throw new NIllegalArgumentException(getSession(), NMsg.ofC("invalid target %s", target));
            }
            Path folder = _target.toFile();
            NPath.of(folder, session).mkdirs();

            String baseName = source.getInputMetaData().getName().orElse("no-name");
            byte[] buffer = new byte[1024];

            //get the zip file content
            InputStream _in = source.getInputStream();
            try {
                try (GZIPInputStream zis = new GZIPInputStream(_in)) {
                    String n = NPath.of(baseName, session).getName();
                    if (n.endsWith(".gz")) {
                        n = n.substring(0, n.length() - 3);
                    }
                    if (n.isEmpty()) {
                        n = "data";
                    }
                    //get the zipped file list entry
                    Path newFile = folder.resolve(n);
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
            } finally {
                _in.close();
            }
        } catch (IOException ex) {
            _LOGOP(session).level(Level.CONFIG).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("error uncompressing {0} to {1} : {2}", source,
                            target, ex));
            throw new NIOException(session, ex);
        }
    }

    private void visitUngzip(NInputSource source,NUncompressVisitor visitor) {
        try {
            String baseName = source.getInputMetaData().getName().orElse("no-name");
            //get the zip file content
            InputStream _in = source.getInputStream();
            try {
                try (GZIPInputStream zis = new GZIPInputStream(_in)) {
                    String n = NPath.of(baseName, session).getName();
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
            _LOGOP(session).level(Level.CONFIG).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("error uncompressing {0} to {1} : {2}", source,
                            target, ex));
            throw new NIOException(session, ex);
        }
    }

    /**
     * return progress factory responsible for creating progress monitor
     *
     * @return progress factory responsible for creating progress monitor
     * @since 0.5.8
     */
    @Override
    public NProgressFactory getProgressFactory() {
        return progressFactory;
    }

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NUncompress setProgressFactory(NProgressFactory value) {
        this.progressFactory = value;
        return this;
    }

    /**
     * set progress monitor. Will create a singeleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NUncompress setProgressMonitor(NProgressListener value) {
        this.progressFactory = value == null ? null : new SingletonNInputStreamProgressFactory(value);
        return this;
    }

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NUncompress progressMonitor(NProgressListener value) {
        return setProgressMonitor(value);
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NUncompress setSkipRoot(boolean value) {
        this.skipRoot = value;
        return this;
    }

    @Override
    public NUncompress setFormatOption(String option, Object value) {
        return this;
    }

    @Override
    public Object getFormatOption(String option) {
        return null;
    }

    @Override
    public NUncompress addOptions(NPathOption... pathOptions) {
        if (pathOptions != null) {
            for (NPathOption o : pathOptions) {
                if (o != null) {
                    options.add(o);
                }
            }
        }
        return this;
    }

    @Override
    public NUncompress removeOptions(NPathOption... pathOptions) {
        if (pathOptions != null) {
            for (NPathOption o : pathOptions) {
                if (o != null) {
                    options.remove(o);
                }
            }
        }
        return this;
    }

    @Override
    public NUncompress clearOptions() {
        options.clear();
        return this;
    }

    @Override
    public Set<NPathOption> getOptions() {
        return new LinkedHashSet<>(options);
    }

    private enum CompressType{
        ZIP,
        GZIP
    }

}
