/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.io;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsLogger;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.io.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * @author vpc
 */
public class DefaultNutsMonitorAction implements NutsMonitorAction {

    private final NutsLogger LOG;
    private final NutsWorkspace ws;
    private String sourceType;
    private Object source;
    private Object sourceOrigin;
    private String sourceName;
    private long length = -1;
    private NutsSession session;
    private boolean logProgress;
    private NutsProgressFactory progressFactory;

    public DefaultNutsMonitorAction(NutsWorkspace ws) {
        this.ws = ws;
        LOG = ws.log().of(DefaultNutsMonitorAction.class);
    }

    @Override
    public NutsMonitorAction session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsMonitorAction setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsMonitorAction name(String name) {
        return setName(name);
    }

    @Override
    public NutsMonitorAction setName(String name) {
        this.sourceName = name;
        return this;
    }

    @Override
    public String getName() {
        return sourceName;
    }

    @Override
    public NutsMonitorAction origin(Object origin) {
        return setOrigin(origin);
    }

    @Override
    public NutsMonitorAction setOrigin(Object origin) {
        this.sourceOrigin = origin;
        return this;
    }

    @Override
    public Object getOrigin() {
        return sourceOrigin;
    }

    @Override
    public NutsMonitorAction length(long len) {
        return setLength(len);
    }

    @Override
    public NutsMonitorAction setLength(long len) {
        this.length = len;
        return this;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public NutsMonitorAction source(String path) {
        return setSource(path);
    }

    @Override
    public NutsMonitorAction source(Path path) {
        return setSource(path);
    }

    @Override
    public NutsMonitorAction source(File path) {
        return setSource(path);
    }

    @Override
    public NutsMonitorAction setSource(String path) {
        this.source = path;
        this.sourceType = "string";
        return this;
    }

    @Override
    public NutsMonitorAction setSource(Path path) {
        this.source = path;
        this.sourceType = "path";
        return this;
    }

    @Override
    public NutsMonitorAction setSource(File path) {
        this.source = path;
        this.sourceType = "file";
        return this;
    }

    @Override
    public NutsMonitorAction source(InputStream inputStream) {
        return setSource(inputStream);
    }

    @Override
    public NutsMonitorAction setSource(InputStream path) {
        this.source = path;
        this.sourceType = "stream";
        return this;
    }

    @Override
    public InputStream create() {
        if (source == null || sourceType == null) {
            throw new NutsIllegalArgumentException(ws, "Missing Source");
        }
        switch (sourceType) {
            case "stream": {
                return monitorInputStream((InputStream) source,sourceOrigin, length, sourceName, session);
            }
            case "string": {
                return monitorInputStream((String) source, sourceOrigin, sourceName, session);
            }
            case "path": {
                return monitorInputStream(((Path) source).toString(), sourceOrigin, sourceName, session);
            }
            case "file": {
                return monitorInputStream(((File) source).getPath(), sourceOrigin, sourceName, session);
            }
            default:
                throw new NutsUnsupportedArgumentException(ws, sourceType);
        }
    }

    public InputStream monitorInputStream(String path, Object source, String sourceName, NutsSession session) {
        if (session == null) {
            session = ws.createSession();
        }
        if (CoreStringUtils.isBlank(path)) {
            throw new UncheckedIOException(new IOException("Missing Path"));
        }
        if (CoreStringUtils.isBlank(sourceName)) {
            sourceName = String.valueOf(path);
        }
        if (session == null) {
            session = ws.createSession();
        }
        NutsProgressMonitor monitor = CoreIOUtils.createProgressMonitor(CoreIOUtils.MonitorType.STREAM, path, source, session, isLogProgress(),getProgressFactory());
        boolean verboseMode
                = CoreCommonUtils.getSysBoolNutsProperty("monitor.start", false);
        InputSource stream = null;
        long size = -1;
        try {
            if (verboseMode && monitor != null) {
                monitor.onStart(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, size, null, session, true));
            }
            stream = CoreIOUtils.createInputSource(path);
            size = stream.length();
        } catch (UncheckedIOException e) {
            if (verboseMode && monitor != null) {
                monitor.onComplete(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, size, e, session, true));
            }
            throw e;
        }
        if(size<0){
            size=getLength();
        }
//        if (path.toLowerCase().startsWith("file://")) {
//            LOG.log(Level.FINE, NutsLogVerb.START, "Downloading file {0}", new Object[]{path});
//        } else {
//            LOG.log(Level.FINEST, NutsLogVerb.START, "Download url {0}", new Object[]{path});
//        }

        InputStream openedStream = stream.open();
        if (monitor == null) {
            return openedStream;
        }
        if (!verboseMode) {
            monitor.onStart(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, size, null, session, size < 0));
        }
        return CoreIOUtils.monitor(openedStream, source, sourceName, size, new SilentStartNutsInputStreamProgressMonitorAdapter(ws, monitor, path), session);

    }

    public InputStream monitorInputStream(InputStream stream,Object sourceOrigin, long length, String name, NutsSession session) {
        if (length > 0) {
            if (session == null) {
                session = ws.createSession();
            }
            NutsProgressMonitor m = CoreIOUtils.createProgressMonitor(CoreIOUtils.MonitorType.STREAM, stream, sourceOrigin, session, isLogProgress(), getProgressFactory());
            if (m == null) {
                return stream;
            }
            return CoreIOUtils.monitor(stream, sourceOrigin, (name == null ? "Stream" : name), length, m, session);
        } else {
            if (stream instanceof InputStreamMetadataAware) {
                if (session == null) {
                    session = ws.createSession();
                }
                NutsProgressMonitor m = CoreIOUtils.createProgressMonitor(CoreIOUtils.MonitorType.STREAM, stream, sourceOrigin, session, isLogProgress(), getProgressFactory());
                if (m == null) {
                    return stream;
                }
                return CoreIOUtils.monitor(stream, sourceOrigin, m, session);
            } else {
                return stream;
            }
        }
    }

    /**
     * when true, will include default factory (console) even if progressFactory is defined
     *
     * @return true if always include default factory
     * @since 0.5.8
     */
    @Override
    public boolean isLogProgress() {
        return logProgress;
    }

    /**
     * when true, will include default factory (console) even if progressFactory is defined
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsMonitorAction setLogProgress(boolean value) {
        this.logProgress = value;
        return this;
    }

    /**
     * when true, will include default factory (console) even if progressFactory is defined
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsMonitorAction logProgress(boolean value) {
        return setLogProgress(value);
    }

    /**
     * always include default factory (console) even if progressFactory is defined
     *
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsMonitorAction logProgress() {
        return logProgress(true);
    }

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    @Override
    public NutsProgressFactory getProgressFactory() {
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
    public NutsMonitorAction setProgressFactory(NutsProgressFactory value) {
        this.progressFactory = value;
        return this;
    }

    /**
     * set progress factory responsible of creating progress monitor
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsMonitorAction progressFactory(NutsProgressFactory value) {
        return setProgressFactory(value);
    }

    /**
     * set progress monitor. Will create a singleton progress monitor factory
     *
     * @param value new value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsMonitorAction setProgressMonitor(NutsProgressMonitor value) {
        this.progressFactory = value == null ? null : new SingletonNutsInputStreamProgressFactory(value);
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
    public NutsMonitorAction progressMonitor(NutsProgressMonitor value) {
        return setProgressMonitor(value);
    }

}
