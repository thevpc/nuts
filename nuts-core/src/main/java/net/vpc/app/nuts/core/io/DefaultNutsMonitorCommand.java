/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.io;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.CoreNutsConstants;
import net.vpc.app.nuts.NutsLogger;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * @author vpc
 */
public class DefaultNutsMonitorCommand implements NutsMonitorCommand {

    private final NutsLogger LOG;
    private final NutsWorkspace ws;
    private String sourceType;
    private Object source;
    private Object sourceOrigin;
    private String sourceName;
    private long length = -1;
    private NutsSession session;
    private boolean includeDefaultFactory;
    private NutsInputStreamProgressFactory progressFactory;

    public DefaultNutsMonitorCommand(NutsWorkspace ws) {
        this.ws = ws;
        LOG=ws.log().of(DefaultNutsMonitorCommand.class);
    }

    @Override
    public NutsMonitorCommand session(NutsSession s) {
        return setSession(s);
    }

    @Override
    public NutsMonitorCommand setSession(NutsSession s) {
        this.session = s;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsMonitorCommand name(String s) {
        return setName(s);
    }

    @Override
    public NutsMonitorCommand setName(String s) {
        this.sourceName = s;
        return this;
    }

    @Override
    public String getName() {
        return sourceName;
    }

    @Override
    public NutsMonitorCommand origin(Object s) {
        return setOrigin(s);
    }

    @Override
    public NutsMonitorCommand setOrigin(Object s) {
        this.sourceOrigin = s;
        return this;
    }

    @Override
    public Object getOrigin() {
        return sourceOrigin;
    }

    @Override
    public NutsMonitorCommand length(long len) {
        return setLength(len);
    }

    @Override
    public NutsMonitorCommand setLength(long len) {
        this.length = len;
        return this;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public NutsMonitorCommand source(String path) {
        return setSource(path);
    }

    @Override
    public NutsMonitorCommand source(Path path) {
        return setSource(path);
    }

    @Override
    public NutsMonitorCommand source(File path) {
        return setSource(path);
    }

    @Override
    public NutsMonitorCommand setSource(String path) {
        this.source = path;
        this.sourceType = "string";
        return this;
    }

    @Override
    public NutsMonitorCommand setSource(Path path) {
        this.source = path;
        this.sourceType = "path";
        return this;
    }

    @Override
    public NutsMonitorCommand setSource(File path) {
        this.source = path;
        this.sourceType = "file";
        return this;
    }

    @Override
    public NutsMonitorCommand source(InputStream inputStream) {
        return setSource(inputStream);
    }

    @Override
    public NutsMonitorCommand setSource(InputStream path) {
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
            case "string": {
                return monitorInputStream((String) source, sourceOrigin, sourceName, session);
            }
            case "stream": {
                return monitorInputStream((InputStream) source, length, sourceName, session);
            }
            case "path": {
                return monitorInputStream(((Path) source).toString(), length, sourceName, session);
            }
            case "file": {
                return monitorInputStream(((File) source).getPath(), length, sourceName, session);
            }
            default:
                throw new NutsUnsupportedArgumentException(ws, sourceType);
        }
    }

    public boolean acceptMonitoring(String path, Object source, String sourceName, NutsSession session) {
        Object o = session.getProperty("monitor-allowed");
        if (o != null) {
            o = ws.commandLine().create(new String[]{String.valueOf(o)}).next().getBoolean();
        }
        boolean monitorable = true;
        if (o instanceof Boolean) {
            monitorable = ((Boolean) o).booleanValue();
        }
        if (monitorable) {
            if (source instanceof NutsId) {
                NutsId d = (NutsId) source;
                if (NutsConstants.QueryFaces.CONTENT_HASH.equals(d.getFace())) {
                    monitorable = false;
                }
                if (NutsConstants.QueryFaces.DESCRIPTOR_HASH.equals(d.getFace())) {
                    monitorable = false;
                }
            }
            if (monitorable) {
                if (path.endsWith("/" + CoreNutsConstants.Files.DOT_FOLDERS) || path.endsWith("/" + CoreNutsConstants.Files.DOT_FILES)
                        || path.endsWith(".pom") || path.endsWith(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION)
                        || path.endsWith(".xml") || path.endsWith(".json")) {
                    monitorable = false;
                }
            }
        }
        if (!CoreCommonUtils.getSysBoolNutsProperty("monitor.enabled", true)) {
            monitorable = false;
        }
        if (!LOG.isLoggable(Level.INFO)) {
            monitorable = false;
        }
        return monitorable;
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
        NutsProgressMonitor monitor = createProgressMonitor(path, source, sourceName, session);
        boolean verboseMode
                = CoreCommonUtils.getSysBoolNutsProperty("monitor.start", false);
        InputSource stream = null;
        long size = -1;
        try {
            if (verboseMode && monitor != null) {
                monitor.onStart(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, size, null, session,true));
            }
            stream = CoreIOUtils.createInputSource(path);
            size = stream.length();
        } catch (UncheckedIOException e) {
            if (verboseMode && monitor != null) {
                monitor.onComplete(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, size, e, session,true));
            }
            throw e;
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
            monitor.onStart(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, size, null, session,size<0));
        }
        return CoreIOUtils.monitor(openedStream, source, sourceName, size, new SilentStartNutsInputStreamProgressMonitorAdapter(ws,monitor, path), session);

    }

    public InputStream monitorInputStream(InputStream stream, long length, String name, NutsSession session) {
        if (length > 0) {
            if (session == null) {
                session = ws.createSession();
            }
            NutsProgressMonitor m = createProgressMonitor(stream, stream, name, session);
            if (m == null) {
                return stream;
            }
            return CoreIOUtils.monitor(stream, null, (name == null ? "Stream" : name), length, m, session);
        } else {
            if (stream instanceof InputStreamMetadataAware) {
                if (session == null) {
                    session = ws.createSession();
                }
                NutsProgressMonitor m = createProgressMonitor(stream, stream, name, session);
                if (m == null) {
                    return stream;
                }
                return CoreIOUtils.monitor(stream, null, m, session);
            } else {
                return stream;
            }
        }
    }

    private NutsProgressMonitor createProgressMonitor(Object source, Object sourceOrigin, String sourceName, NutsSession session) {
        if (!isIncludeDefaultFactory()) {
            if (progressFactory != null) {
                return progressFactory.create(source, sourceOrigin, sourceName, session);
            }
            return new DefaultNutsInputStreamProgressFactory().create(source, sourceOrigin, sourceName, session);
        } else {
            NutsProgressMonitor m0 = new DefaultNutsInputStreamProgressFactory().create(source, sourceOrigin, sourceName, session);
            NutsProgressMonitor m1 = null;
            if (progressFactory != null) {
                m1 = progressFactory.create(source, sourceOrigin, sourceName, session);
            }
            if (m1 == null) {
                return m0;
            }
            if (m0 == null) {
                return m1;
            }
            ;
            return new NutsProgressMonitorList(new NutsProgressMonitor[]{m0, m1});
        }
    }

    /**
     * when true, will include default factory (console) even if progressFactory is defined
     * @return true if always include default factory
     * @since 0.5.8
     */
    @Override
    public boolean isIncludeDefaultFactory() {
        return includeDefaultFactory;
    }

    /**
     * when true, will include default factory (console) even if progressFactory is defined
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsMonitorCommand setIncludeDefaultFactory(boolean value) {
        this.includeDefaultFactory = value;
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
    public NutsMonitorCommand includeDefaultFactory(boolean value) {
        return setIncludeDefaultFactory(value);
    }

    /**
     *always include default factory (console) even if progressFactory is defined
     *
     * @return {@code this} instance
     * @since 0.5.8
     */
    @Override
    public NutsMonitorCommand includeDefaultFactory() {
        return includeDefaultFactory(true);
    }

    /**
     * return progress factory responsible of creating progress monitor
     *
     * @return progress factory responsible of creating progress monitor
     * @since 0.5.8
     */
    @Override
    public NutsInputStreamProgressFactory getProgressFactory() {
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
    public NutsMonitorCommand setProgressFactory(NutsInputStreamProgressFactory value) {
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
    public NutsMonitorCommand progressFactory(NutsInputStreamProgressFactory value) {
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
    public NutsMonitorCommand setProgressMonitor(NutsProgressMonitor value) {
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
    public NutsMonitorCommand progressMonitor(NutsProgressMonitor value) {
        return setProgressMonitor(value);
    }

}
