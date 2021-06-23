/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.progress.DefaultNutsProgressEvent;
import net.thevpc.nuts.runtime.bundles.io.InputStreamMetadataAware;
import net.thevpc.nuts.runtime.core.util.CoreBooleanUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

/**
 * @author thevpc
 */
public class DefaultNutsMonitorAction implements NutsMonitorAction {

    //    private final NutsLogger LOG;
    private final NutsWorkspace ws;
    private String sourceTypeName;
    private String sourceKind;
    private Object source;
    private Object sourceOrigin;
    private String sourceName;
    private long length = -1;
    private NutsSession session;
    private boolean logProgress;
    private NutsProgressFactory progressFactory;

    public DefaultNutsMonitorAction(NutsWorkspace ws) {
        this.ws = ws;
//        LOG = ws.log().of(DefaultNutsMonitorAction.class);
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
    public NutsMonitorAction setName(String name) {
        this.sourceName = name;
        return this;
    }

    @Override
    public String getName() {
        return sourceName;
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
    public NutsMonitorAction setLength(long len) {
        this.length = len;
        return this;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public NutsMonitorAction setSource(String path) {
        this.source = path;
        this.sourceKind = "string";
        return this;
    }

    @Override
    public NutsMonitorAction setSource(NutsInput inputSource) {
        this.source = inputSource;
        this.sourceKind = "inputSource";
        return this;
    }

    @Override
    public NutsMonitorAction setSource(Path path) {
        this.source = path;
        this.sourceKind = "path";
        return this;
    }

    @Override
    public NutsMonitorAction setSource(File path) {
        this.source = path;
        this.sourceKind = "file";
        return this;
    }

    @Override
    public NutsMonitorAction setSource(InputStream path) {
        this.source = path;
        this.sourceKind = "stream";
        return this;
    }

    @Override
    public InputStream create() {
        checkSession();
        if (source == null || sourceKind == null) {
            throw new NutsIllegalArgumentException(getSession(), "missing Source");
        }
        switch (sourceKind) {
            case "inputSource": {
                return monitorInputStream(((NutsInput) source), sourceOrigin, sourceName).open();
            }
            case "stream": {
                return monitorInputStream((InputStream) source, sourceOrigin, length, sourceName);
            }
            case "string": {
                return monitorInputStream((String) source, sourceOrigin, sourceName);
            }
            case "path": {
                return monitorInputStream(((Path) source).toString(), sourceOrigin, sourceName);
            }
            case "file": {
                return monitorInputStream(((File) source).getPath(), sourceOrigin, sourceName);
            }
            default:
                throw new NutsUnsupportedArgumentException(getSession(), sourceKind);
        }
    }

    @Override
    public String getSourceTypeName() {
        return sourceTypeName;
    }

    @Override
    public NutsMonitorAction setSourceTypeName(String sourceType) {
        this.sourceTypeName = sourceType;
        return this;
    }

    @Override
    public NutsInput createSource() {
        checkSession();
        NutsInput base = getSession().getWorkspace().io().input().of(source);
        boolean isPath = base.isPath();
        boolean isUrl = base.isURL();
        String sourceKind0 = sourceKind;
        String sourceTypeName0 = sourceTypeName;
        String sourceName0 = sourceName;
        if (sourceTypeName0 == null && getOrigin() instanceof NutsInput) {
            sourceTypeName0 = ((NutsInput) getOrigin()).getTypeName();
        }
        if (sourceTypeName0 == null && source instanceof NutsInput) {
            sourceTypeName0 = ((NutsInput) source).getTypeName();
        }
        if (sourceName0 == null && getOrigin() instanceof NutsInput) {
            sourceName0 = ((NutsInput) getOrigin()).getName();
        }
        if (sourceName0 == null && source instanceof NutsInput) {
            sourceName0 = ((NutsInput) source).getName();
        }
        if (sourceKind0.equalsIgnoreCase("inputSource")) {
            return monitorInputStream(((NutsInput) source), sourceOrigin, sourceName0);
        }
        if (sourceName0 == null) {
            sourceName0 = NutsCompressedPath.compressPath(source.toString());
        }
        switch (sourceKind0) {
            case "string": {
                return new InputFromString(sourceName0, base, isPath, isUrl, sourceTypeName0, getSession(), base);
            }
            case "path": {
                return new InputFromPath(sourceName0, base, isPath, isUrl, sourceTypeName0, getSession(), base);
            }
            case "file": {
                return new InputFromFile(sourceName0, base, isPath, isUrl, sourceTypeName0, getSession(), base);
            }
            case "stream": {
                return new InputFromStream(sourceName0, base, isPath, isUrl, sourceTypeName0, getSession(), base);
            }
            default:
                throw new NutsUnsupportedArgumentException(getSession(), sourceKind);
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

    public InputStream monitorInputStream(String path, Object source, String sourceName) {
        checkSession();
        if (CoreStringUtils.isBlank(path)) {
            throw new UncheckedIOException(new IOException("missing path"));
        }
        if (CoreStringUtils.isBlank(sourceName)) {
            sourceName = String.valueOf(path);
        }
        NutsProgressMonitor monitor = CoreIOUtils.createProgressMonitor(CoreIOUtils.MonitorType.STREAM, path, source, session, isLogProgress(), getProgressFactory());
        boolean verboseMode
                = CoreBooleanUtils.getSysBoolNutsProperty("monitor.start", false);
        NutsInput stream = null;
        long size = -1;
        try {
            if (verboseMode && monitor != null) {
                monitor.onStart(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, size, null, session, true));
            }
            stream = getSession().getWorkspace().io().input().setTypeName(getSourceTypeName()).of(path);
            size = stream.length();
        } catch (UncheckedIOException | NutsIOException e) {
            if (verboseMode && monitor != null) {
                monitor.onComplete(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, size, e, session, true));
            }
            throw e;
        }
        if (size < 0) {
            size = getLength();
        }
//        if (path.toLowerCase().startsWith("file://")) {
//            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.START).log( "Downloading file {0}", new Object[]{path});
//        } else {
//            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.START).log( "Download url {0}", new Object[]{path});
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

    public NutsInput monitorInputStream(NutsInput inputSource, Object source, String sourceName) {
        checkSession();
        if (inputSource == null) {
            throw new UncheckedIOException(new IOException("missing inputSource"));
        }
        if (CoreStringUtils.isBlank(sourceName)) {
            sourceName = inputSource.getName();
        }
        NutsProgressMonitor monitor = CoreIOUtils.createProgressMonitor(CoreIOUtils.MonitorType.STREAM, inputSource, source, session, isLogProgress(), getProgressFactory());
        boolean verboseMode
                = CoreBooleanUtils.getSysBoolNutsProperty("monitor.start", false);
        long size = -1;
        try {
            if (verboseMode && monitor != null) {
                monitor.onStart(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, size, null, session, true));
            }
            size = inputSource.length();
        } catch (UncheckedIOException | NutsIOException e) {
            if (verboseMode && monitor != null) {
                monitor.onComplete(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, size, e, session, true));
            }
            throw e;
        }
        if (size < 0) {
            size = getLength();
        }
//        if (path.toLowerCase().startsWith("file://")) {
//            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.START).log( "Downloading file {0}", new Object[]{path});
//        } else {
//            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.START).log( "Download url {0}", new Object[]{path});
//        }

        if (monitor == null) {
            return inputSource;
        }
        InputStream openedStream = inputSource.open();
        if (!verboseMode) {
            monitor.onStart(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, size, null, session, size < 0));
        }
        String sourceTypeName = getSourceTypeName();
        if (sourceTypeName == null) {
            sourceTypeName = inputSource.getTypeName();
        }
        return getSession().getWorkspace().io().input()
                .setTypeName(sourceTypeName)
                .of(CoreIOUtils.monitor(openedStream, source, sourceName, size, new SilentStartNutsInputStreamProgressMonitorAdapter(ws, monitor, inputSource.toString()), session))
                ;

    }

    public InputStream monitorInputStream(InputStream stream, Object sourceOrigin, long length, String name) {
        checkSession();
        if (length > 0) {
            NutsProgressMonitor m = CoreIOUtils.createProgressMonitor(CoreIOUtils.MonitorType.STREAM, stream, sourceOrigin, session, isLogProgress(), getProgressFactory());
            if (m == null) {
                return stream;
            }
            return CoreIOUtils.monitor(stream, sourceOrigin, (name == null ? "Stream" : name), length, m, session);
        } else {
            if (stream instanceof InputStreamMetadataAware) {
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

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
    }

    private class InputFromStream extends CoreIOUtils.AbstractItem {

        private final NutsInput base;

        public InputFromStream(String string, Object o, boolean bln, boolean bln1, String string1, NutsSession ns, NutsInput base) {
            super(string, o, bln, bln1, string1, ns);
            this.base = base;
        }

        @Override
        public InputStream open() {
            return monitorInputStream((InputStream) source, sourceOrigin, length, sourceName);
        }

        @Override
        public String toString() {
            return ((InputStream) source).toString();
        }

        @Override
        public long length() {
            return base.length();
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public String getContentEncoding() {
            return null;
        }

        @Override
        public Instant getLastModified() {
            return null;
        }
    }

    private class InputFromFile extends CoreIOUtils.AbstractItem {

        private final NutsInput base;

        public InputFromFile(String string, Object o, boolean bln, boolean bln1, String string1, NutsSession ns, NutsInput base) {
            super(string, o, bln, bln1, string1, ns);
            this.base = base;
        }

        @Override
        public InputStream open() {
            return monitorInputStream(((File) source).getPath(), sourceOrigin, sourceName);
        }

        @Override
        public Path getPath() {
            return ((File) source).toPath();
        }

        @Override
        public String toString() {
            return ((File) source).getPath();
        }

        @Override
        public long length() {
            return base.length();
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public String getContentEncoding() {
            return null;
        }

        @Override
        public Instant getLastModified() {
            FileTime r = null;
            try {
                r = Files.getLastModifiedTime(getPath());
                if (r != null) {
                    return r.toInstant();
                }
            } catch (IOException e) {
                //
            }
            return null;
        }
    }

    private class InputFromPath extends CoreIOUtils.AbstractItem {

        private final NutsInput base;

        public InputFromPath(String string, Object o, boolean bln, boolean bln1, String string1, NutsSession ns, NutsInput base) {
            super(string, o, bln, bln1, string1, ns);
            this.base = base;
        }

        @Override
        public InputStream open() {
            return monitorInputStream(((Path) source).toString(), sourceOrigin, sourceName);
        }

        @Override
        public String toString() {
            return ((Path) source).toString();
        }

        @Override
        public long length() {
            return base.length();
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public String getContentEncoding() {
            return null;
        }

        @Override
        public Instant getLastModified() {
            FileTime r = null;
            try {
                r = Files.getLastModifiedTime(getPath());
                if (r != null) {
                    return r.toInstant();
                }
            } catch (IOException e) {
                //
            }
            return null;
        }
    }

    private class InputFromString extends CoreIOUtils.AbstractItem {

        private final NutsInput base;

        public InputFromString(String string, Object o, boolean bln, boolean bln1, String string1, NutsSession ns, NutsInput base) {
            super(string, o, bln, bln1, string1, ns);
            this.base = base;
        }

        @Override
        public InputStream open() {
            return monitorInputStream((String) source, sourceOrigin, sourceName);
        }

        @Override
        public String toString() {
            return (String) source;
        }

        @Override
        public long length() {
            return base.length();
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public String getContentEncoding() {
            return null;
        }

        @Override
        public Instant getLastModified() {
            return null;
        }
    }

}
