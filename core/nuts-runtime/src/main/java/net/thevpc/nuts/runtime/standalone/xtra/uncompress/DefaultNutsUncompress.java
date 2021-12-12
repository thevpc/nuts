/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.uncompress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.util.NutsStreamOrPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.NutsLogVerb;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.thevpc.nuts.runtime.standalone.io.progress.SingletonNutsInputStreamProgressFactory;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

/**
 * @author thevpc
 */
public class DefaultNutsUncompress implements NutsUncompress {

    private NutsLogger LOG;

    private boolean skipRoot = false;
    private boolean safe = true;
    private boolean logProgress = false;
    private String format = "zip";
    private NutsWorkspace ws;
    private NutsStreamOrPath source;
    private NutsStreamOrPath target;
    private NutsSession session;
    private NutsProgressFactory progressMonitorFactory;

    public DefaultNutsUncompress(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
//        LOG = ws.log().of(DefaultNutsUncompress.class);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsUncompress.class,session);
        }
        return LOG;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public NutsUncompress setFormat(String format) {
        checkSession();
        if (NutsBlankable.isBlank(format)) {
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
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.cstyle("unsupported compression format %s", format));
            }
        }
        return this;
    }

    @Override
    public Object getSource() {
        return source;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
    }

    @Override
    public NutsUncompress setSource(InputStream source) {
        checkSession();
        this.source = source==null?null:NutsStreamOrPath.of(source,getSession());
        return this;
    }

    @Override
    public NutsUncompress setSource(NutsPath source) {
        checkSession();
        this.source = source==null?null:NutsStreamOrPath.of(source);
        return this;
    }

    @Override
    public NutsUncompress setSource(File source) {
        this.source = source==null?null:NutsStreamOrPath.of(source,session);
        return this;
    }

    @Override
    public NutsUncompress setSource(Path source) {
        this.source = source==null?null:NutsStreamOrPath.of(source,session);
        return this;
    }

    @Override
    public NutsUncompress setSource(URL source) {
        this.source = source==null?null:NutsStreamOrPath.of(source,session);
        return this;
    }

    @Override
    public NutsUncompress setTarget(Path target) {
        this.target = target==null?null:NutsStreamOrPath.of(target,session);
        return this;
    }

    @Override
    public NutsUncompress setTarget(String target) {
        this.target = target==null?null:NutsStreamOrPath.of(target,session);
        return this;
    }

    @Override
    public NutsUncompress setTarget(File target) {
        this.target = target==null?null:NutsStreamOrPath.of(target,session);
        return this;
    }

    public NutsUncompress setTarget(NutsPath target) {
        this.target = target==null?null:NutsStreamOrPath.of(target);
        return this;
    }

    @Override
    public NutsUncompress from(String source) {
        this.source = source==null?null:NutsStreamOrPath.of(source,session);
        return this;
    }

    @Override
    public NutsUncompress to(String target) {
        this.target = target==null?null:NutsStreamOrPath.of(target,session);
        return this;
    }

    @Override
    public NutsUncompress from(NutsPath source) {
        this.source = source==null?null:NutsStreamOrPath.of(source);
        return this;
    }

    @Override
    public NutsUncompress to(NutsPath target) {
        this.target = target==null?null:NutsStreamOrPath.of(target);
        return this;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    //    @Override


    @Override
    public boolean isLogProgress() {
        return logProgress;
    }

    @Override
    public DefaultNutsUncompress setLogProgress(boolean value) {
        this.logProgress = value;
        return this;
    }

    @Override
    public NutsUncompress from(InputStream source) {
        return setSource(source);
    }

    @Override
    public NutsUncompress from(File source) {
        return setSource(source);
    }

    @Override
    public NutsUncompress from(Path source) {
        return setSource(source);
    }

    @Override
    public NutsUncompress from(URL source) {
        return setSource(source);
    }

    @Override
    public NutsUncompress to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsUncompress to(Path target) {
        return setTarget(target);
    }

    @Override
    public boolean isSafe() {
        return safe;
    }

    @Override
    public DefaultNutsUncompress setSafe(boolean value) {
        this.safe = value;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsUncompress setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public NutsUncompress run() {
        checkSession();
        String format = getFormat();
        if (NutsBlankable.isBlank(format)) {
            format = "zip";
        }
        NutsStreamOrPath _source = source;
        if (_source == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing source"));
        }
        if (target == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing target"));
        }
        if (!target.isPath() || !target.getPath().isFile() ) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid target %s",target.getValue()));
        }
        if (isLogProgress() || getProgressMonitorFactory() != null) {
            NutsInputStreamMonitor m = NutsInputStreamMonitor.of(session);
            if(_source.isInputStream()){
                m.setSource(_source.getInputStream());
            }else{
                m.setSource(_source.getPath());
            }
            _source = NutsStreamOrPath.of
                    (m.setProgressFactory(getProgressMonitorFactory())
                    .setLogProgress(isLogProgress())
                    .create(),session);
        }
        //boolean _source_isPath = _source.isPath();
//        if (!path.toLowerCase().startsWith("file://")) {
//            LOG.log(Level.FINE, "downloading url {0} to file {1}", new Object[]{path, file});
//        } else {
        _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.START)
                .log(NutsMessage.jstyle("uncompress {0} to {1}", _source, target));
        Path folder = target.getPath().toFile();
        CoreIOUtils.mkdirs(folder,session);

        switch (format) {
            case "zip": {
                runZip();
                break;
            }
            case "gzip":
            case "gz": {
                runGZip();
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.cstyle("unsupported format %s", format));
            }
        }
        return this;
    }
    @Override
    public NutsUncompress visit(NutsIOUncompressVisitor visitor) {
        checkSession();
        String format = getFormat();
        if (NutsBlankable.isBlank(format)) {
            format = "zip";
        }
        NutsStreamOrPath _source = source;
        if (_source == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing source"));
        }
        if (target == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing target"));
        }
        if (!target.isPath() || !target.getPath().isFile() ) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid target %s",target.getValue()));
        }
        if (isLogProgress() || getProgressMonitorFactory() != null) {
            NutsInputStreamMonitor m = NutsInputStreamMonitor.of(session);
            if(_source.isInputStream()){
                m.setSource(_source.getInputStream());
            }else{
                m.setSource(_source.getPath());
            }
            _source = NutsStreamOrPath.of(m
                    .setProgressFactory(getProgressMonitorFactory())
                    .setLogProgress(isLogProgress())
                    .create(),session);
        }
        //boolean _source_isPath = _source.isPath();
//        if (!path.toLowerCase().startsWith("file://")) {
//            LOG.log(Level.FINE, "downloading url {0} to file {1}", new Object[]{path, file});
//        } else {
        _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.START)
                .log(NutsMessage.jstyle("uncompress {0} to {1}", _source, target));
        Path folder = target.getPath().toFile();
        CoreIOUtils.mkdirs(folder,session);

        switch (format) {
            case "zip": {
                visitZip(visitor);
                break;
            }
            case "gzip":
            case "gz": {
                visitGZip(visitor);
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.cstyle("unsupported format %s" + format));
            }
        }
        return this;
    }

    private void runZip() {
        checkSession();
        NutsStreamOrPath _source = source;
//        }
        try {

            byte[] buffer = new byte[1024];

            //create output directory is not exists
            Path folder = target.getPath().toFile();
            //get the zip file content
            InputStream _in = _source.getInputStream();
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
                                    throw new IOException("tot a single root zip");
                                }
                            }
                            if (fileName.startsWith(root)) {
                                fileName = fileName.substring(root.length());
                            } else {
                                throw new IOException("tot a single root zip");
                            }
                        }
                        if (fileName.endsWith("/")) {
                            Path newFile = folder.resolve(fileName);
                            CoreIOUtils.mkdirs(newFile,session);
                        } else {
                            Path newFile = folder.resolve(fileName);
                            _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.WARNING)
                                    .log(NutsMessage.jstyle("file unzip : {0}", newFile));
                            //create all non exists folders
                            //else you will hit FileNotFoundException for compressed folder
                            if (newFile.getParent() != null) {
                                CoreIOUtils.mkdirs(newFile.getParent(),session);
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
            _LOGOP(session).level(Level.CONFIG).verb(NutsLogVerb.FAIL).log(
                    NutsMessage.jstyle("error uncompressing {0} to {1} : {2}",
                    _source.getValue(), target.getValue(), ex));
            throw new NutsIOException(session,ex);
        }
    }
    private void visitZip(NutsIOUncompressVisitor visitor) {
        checkSession();
        NutsStreamOrPath _source = source;
//        }
        try {

            byte[] buffer = new byte[1024];

            //create output directory is not exists
//            Path folder = target.getPath();
            //get the zip file content
            InputStream _in = _source.getInputStream();
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
                                    throw new IOException("tot a single root zip");
                                }
                            }
                            if (fileName.startsWith(root)) {
                                fileName = fileName.substring(root.length());
                            } else {
                                throw new IOException("tot a single root zip");
                            }
                        }
                        if (fileName.endsWith("/")) {
                            if(!visitor.visitFolder(fileName)){
                                break;
                            }
                        } else {
                            if(!visitor.visitFile(fileName,new InputStream(){
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
                            })){
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
            _LOGOP(session).level(Level.CONFIG).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("error uncompressing {0} to {1} : {2}",
                    _source.getValue(), target.getValue(), ex));
            throw new NutsIOException(session,ex);
        }
    }

    private void runGZip() {
        NutsStreamOrPath _source = source;
        try {
            String baseName = _source.getName();
            byte[] buffer = new byte[1024];

            //create output directory is not exists
            Path folder = target.getPath().toFile();

            //get the zip file content
            InputStream _in = _source.getInputStream();
            try {
                try (GZIPInputStream zis = new GZIPInputStream(_in)) {
                    String n = CoreIOUtils.getURLName(baseName == null ? "" : baseName);
                    if (n.endsWith(".gz")) {
                        n = n.substring(0, n.length() - 3);
                    }
                    if (n.isEmpty()) {
                        n = "data";
                    }
                    //get the zipped file list entry
                    Path newFile = folder.resolve(n);
                    _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.WARNING)
                            .log(NutsMessage.jstyle("file unzip : {0}", newFile));
                    //create all non exists folders
                    //else you will hit FileNotFoundException for compressed folder
                    if (newFile.getParent() != null) {
                        CoreIOUtils.mkdirs(newFile.getParent(),session);
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
            _LOGOP(session).level(Level.CONFIG).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("error uncompressing {0} to {1} : {2}", _source.getValue(),
                            target.getValue(), ex));
            throw new NutsIOException(session,ex);
        }
    }
    private void visitGZip(NutsIOUncompressVisitor visitor) {
        NutsStreamOrPath _source = source;
        try {
            String baseName = _source.getName();
            byte[] buffer = new byte[1024];

            //get the zip file content
            InputStream _in = _source.getInputStream();
            try {
                try (GZIPInputStream zis = new GZIPInputStream(_in)) {
                    String n = CoreIOUtils.getURLName(baseName == null ? "" : baseName);
                    if (n.endsWith(".gz")) {
                        n = n.substring(0, n.length() - 3);
                    }
                    if (n.isEmpty()) {
                        n = "data";
                    }
                    //get the zipped file list entry
                    visitor.visitFile(n,new InputStream(){
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
            _LOGOP(session).level(Level.CONFIG).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("error uncompressing {0} to {1} : {2}", _source.getValue(),
                            target.getValue(), ex));
            throw new NutsIOException(session,ex);
        }
    }

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    @Override
    public NutsProgressFactory getProgressMonitorFactory() {
        return progressMonitorFactory;
    }

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsUncompress setProgressMonitorFactory(NutsProgressFactory value) {
        this.progressMonitorFactory = value;
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
    public NutsUncompress setProgressMonitor(NutsProgressMonitor value) {
        this.progressMonitorFactory = value == null ? null : new SingletonNutsInputStreamProgressFactory(value);
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
    public NutsUncompress progressMonitor(NutsProgressMonitor value) {
        return setProgressMonitor(value);
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NutsUncompress setSkipRoot(boolean value) {
        this.skipRoot = true;
        return this;
    }

    @Override
    public NutsUncompress setFormatOption(String option, Object value) {
        return this;
    }

    @Override
    public Object getFormatOption(String option) {
        return null;
    }
}
