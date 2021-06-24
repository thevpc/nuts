/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.NutsLogVerb;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 * @author thevpc
 */
public class DefaultNutsIOUncompressAction implements NutsIOUncompressAction {

    private NutsLogger LOG;

    private boolean skipRoot = false;
    private boolean safe = true;
    private boolean logProgress = false;
    private String format = "zip";
    private NutsWorkspace ws;
    private NutsInput source;
    private NutsOutput target;
    private NutsSession session;
    private NutsProgressFactory progressMonitorFactory;

    public DefaultNutsIOUncompressAction(NutsWorkspace ws) {
        this.ws = ws;
//        LOG = ws.log().of(DefaultNutsIOUncompressAction.class);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = this.ws.log().setSession(session).of(DefaultNutsIOUncompressAction.class);
        }
        return LOG;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public NutsIOUncompressAction setFormat(String format) {
        checkSession();
        if (CoreStringUtils.isBlank(format)) {
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
                throw new NutsUnsupportedArgumentException(getSession(), "unsupported compression format " + format);
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

    private NutsInputAction _input() {
        checkSession();
        return ws.io().input().setSession(getSession());
    }

    @Override
    public NutsIOUncompressAction setSource(InputStream source) {
        this.source = _input().of(source);
        return this;
    }

    @Override
    public NutsIOUncompressAction setSource(File source) {
        this.source = _input().of(source);
        return this;
    }

    @Override
    public NutsIOUncompressAction setSource(Path source) {
        this.source = _input().of(source);
        return this;
    }

    @Override
    public NutsIOUncompressAction setSource(URL source) {
        this.source = _input().of(source);
        return this;
    }

    @Override
    public NutsIOUncompressAction setTarget(Path target) {
        this.target = _output().of(target);
        return this;
    }

    @Override
    public NutsIOUncompressAction setTarget(String target) {
        this.target = _output().of(target);
        return this;
    }

    @Override
    public NutsIOUncompressAction setTarget(File target) {
        this.target = _output().of(target);
        return this;
    }

    public DefaultNutsIOUncompressAction setSource(Object source) {
        this.source = _input().of(source);
        return this;
    }

    @Override
    public NutsIOUncompressAction from(String source) {
        this.source = _input().of(source);
        return this;
    }

    protected NutsOutputAction _output() {
        checkSession();
        return ws.io().output().setSession(getSession());
    }

    @Override
    public NutsIOUncompressAction to(String target) {
        this.target = _output().of(target);
        return this;
    }

    @Override
    public NutsIOUncompressAction from(Object source) {
        this.source = _input().of(source);
        return this;
    }

    @Override
    public NutsIOUncompressAction to(Object target) {
        this.target = _output().of(target);
        return this;
    }

    @Override
    public NutsIOUncompressAction to(NutsOutput target) {
        this.target = _output().of(target);
        return this;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    //    @Override
    public DefaultNutsIOUncompressAction setTarget(Object target) {
        this.target = _output().of(target);
        return this;
    }

    @Override
    public NutsIOUncompressAction setTarget(NutsOutput target) {
        this.target = _output().of(target);
        return this;
    }

    @Override
    public boolean isLogProgress() {
        return logProgress;
    }

    @Override
    public DefaultNutsIOUncompressAction setLogProgress(boolean value) {
        this.logProgress = value;
        return this;
    }

    @Override
    public NutsIOUncompressAction from(InputStream source) {
        return setSource(source);
    }

    @Override
    public NutsIOUncompressAction from(File source) {
        return setSource(source);
    }

    @Override
    public NutsIOUncompressAction from(Path source) {
        return setSource(source);
    }

    @Override
    public NutsIOUncompressAction from(URL source) {
        return setSource(source);
    }

    @Override
    public NutsIOUncompressAction to(File target) {
        return setTarget(target);
    }

    @Override
    public NutsIOUncompressAction to(Path target) {
        return setTarget(target);
    }

    @Override
    public boolean isSafe() {
        return safe;
    }

    @Override
    public DefaultNutsIOUncompressAction setSafe(boolean value) {
        this.safe = value;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsIOUncompressAction setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsIOUncompressAction run() {
        checkSession();
        String format = getFormat();
        if (CoreStringUtils.isBlank(format)) {
            format = "zip";
        }
        NutsInput _source = source;
        if (_source == null) {
            throw new NutsIllegalArgumentException(getSession(), "missing source");
        }
        if (target == null) {
            throw new NutsIllegalArgumentException(getSession(), "missing target");
        }
        if (isLogProgress() || getProgressMonitorFactory() != null) {
            _source = ws.io().monitor().setSource(_source).setSession(session)
                    .setProgressFactory(getProgressMonitorFactory())
                    .setLogProgress(isLogProgress())
                    .createSource();
        }
        //boolean _source_isPath = _source.isPath();
//        if (!path.toLowerCase().startsWith("file://")) {
//            LOG.log(Level.FINE, "downloading url {0} to file {1}", new Object[]{path, file});
//        } else {
        _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.START).log("uncompress {0} to {1}", _source, target);
        Path folder = target.getFilePath();
        CoreIOUtils.mkdirs(folder);

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
                throw new NutsUnsupportedArgumentException(getSession(), "unsupported format " + format);
            }
        }
        return this;
    }
    @Override
    public NutsIOUncompressAction visit(NutsIOUncompressVisitor visitor) {
        checkSession();
        String format = getFormat();
        if (CoreStringUtils.isBlank(format)) {
            format = "zip";
        }
        NutsInput _source = source;
        if (_source == null) {
            throw new NutsIllegalArgumentException(getSession(), "missing source");
        }
        if (target == null) {
            throw new NutsIllegalArgumentException(getSession(), "missing target");
        }
        if (isLogProgress() || getProgressMonitorFactory() != null) {
            _source = ws.io().monitor().setSource(_source).setSession(session)
                    .setProgressFactory(getProgressMonitorFactory())
                    .setLogProgress(isLogProgress())
                    .createSource();
        }
        //boolean _source_isPath = _source.isPath();
//        if (!path.toLowerCase().startsWith("file://")) {
//            LOG.log(Level.FINE, "downloading url {0} to file {1}", new Object[]{path, file});
//        } else {
        _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.START).log("uncompress {0} to {1}", _source, target);
        Path folder = target.getFilePath();
        CoreIOUtils.mkdirs(folder);

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
                throw new NutsUnsupportedArgumentException(getSession(), "unsupported format " + format);
            }
        }
        return this;
    }

    private void runZip() {
        checkSession();
        NutsInput _source = source;
//        }
        try {

            byte[] buffer = new byte[1024];

            //create output directory is not exists
            Path folder = target.getFilePath();
            //get the zip file content
            InputStream _in = _source.open();
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
                            CoreIOUtils.mkdirs(newFile);
                        } else {
                            Path newFile = folder.resolve(fileName);
                            _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).log("file unzip : " + newFile);
                            //create all non exists folders
                            //else you will hit FileNotFoundException for compressed folder
                            if (newFile.getParent() != null) {
                                CoreIOUtils.mkdirs(newFile.getParent());
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
            _LOGOP(session).level(Level.CONFIG).verb(NutsLogVerb.FAIL).log("error uncompressing {0} to {1} : {2}",
                    _source.getSource(), target.getSource(), ex);
            throw new UncheckedIOException(ex);
        }
    }
    private void visitZip(NutsIOUncompressVisitor visitor) {
        checkSession();
        NutsInput _source = source;
//        }
        try {

            byte[] buffer = new byte[1024];

            //create output directory is not exists
//            Path folder = target.getPath();
            //get the zip file content
            InputStream _in = _source.open();
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
            _LOGOP(session).level(Level.CONFIG).verb(NutsLogVerb.FAIL).log("error uncompressing {0} to {1} : {2}",
                    _source.getSource(), target.getSource(), ex);
            throw new UncheckedIOException(ex);
        }
    }

    private void runGZip() {
        NutsInput _source = source;
        try {
            String baseName = _source.getName();
            byte[] buffer = new byte[1024];

            //create output directory is not exists
            Path folder = target.getFilePath();

            //get the zip file content
            InputStream _in = _source.open();
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
                    _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).log("file unzip : " + newFile);
                    //create all non exists folders
                    //else you will hit FileNotFoundException for compressed folder
                    if (newFile.getParent() != null) {
                        CoreIOUtils.mkdirs(newFile.getParent());
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
                    .log("error uncompressing {0} to {1} : {2}", _source.getSource(),
                            target.getSource(), ex);
            throw new UncheckedIOException(ex);
        }
    }
    private void visitGZip(NutsIOUncompressVisitor visitor) {
        NutsInput _source = source;
        try {
            String baseName = _source.getName();
            byte[] buffer = new byte[1024];

            //get the zip file content
            InputStream _in = _source.open();
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
                    .log("error uncompressing {0} to {1} : {2}", _source.getSource(),
                            target.getSource(), ex);
            throw new UncheckedIOException(ex);
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
    public NutsIOUncompressAction setProgressMonitorFactory(NutsProgressFactory value) {
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
    public NutsIOUncompressAction setProgressMonitor(NutsProgressMonitor value) {
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
    public NutsIOUncompressAction progressMonitor(NutsProgressMonitor value) {
        return setProgressMonitor(value);
    }

    @Override
    public boolean isSkipRoot() {
        return skipRoot;
    }

    @Override
    public NutsIOUncompressAction setSkipRoot(boolean value) {
        this.skipRoot = true;
        return this;
    }

    @Override
    public NutsIOUncompressAction setFormatOption(String option, Object value) {
        return this;
    }

    @Override
    public Object getFormatOption(String option) {
        return null;
    }
}
