/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.standalone.io.util.NutsStreamOrPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.File;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * @author thevpc
 */
public class DefaultNutsInputStreamMonitor implements NutsInputStreamMonitor {

    //    private final NutsLogger LOG;
    private final NutsWorkspace ws;
    private String sourceTypeName;
    private NutsStreamOrPath source;
    private Object sourceOrigin;
    private NutsString sourceName;
    private long length = -1;
    private NutsSession session;
    private boolean logProgress;
    private NutsProgressFactory progressFactory;

    public DefaultNutsInputStreamMonitor(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
//        LOG = ws.log().of(DefaultNutsInputStreamMonitor.class);
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsInputStreamMonitor setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    @Override
    public NutsString getName() {
        return sourceName;
    }

    @Override
    public NutsInputStreamMonitor setName(NutsString name) {
        this.sourceName = name;
        return this;
    }

    @Override
    public Object getOrigin() {
        return sourceOrigin;
    }

    @Override
    public NutsInputStreamMonitor setOrigin(Object origin) {
        this.sourceOrigin = origin;
        return this;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public NutsInputStreamMonitor setLength(long len) {
        this.length = len;
        return this;
    }

    @Override
    public NutsInputStreamMonitor setSource(String path) {
        this.source = path == null ? null : NutsStreamOrPath.of(path,session);
        return this;
    }

    @Override
    public NutsInputStreamMonitor setSource(NutsPath inputSource) {
        this.source = inputSource == null ? null : NutsStreamOrPath.of(inputSource);
        return this;
    }

    @Override
    public NutsInputStreamMonitor setSource(Path path) {
        checkSession();
        this.source = path == null ? null : NutsStreamOrPath.of(path,session);
        return this;
    }

    @Override
    public NutsInputStreamMonitor setSource(File path) {
        checkSession();
        this.source = path == null ? null : NutsStreamOrPath.of(path,session);
        return this;
    }

    @Override
    public NutsInputStreamMonitor setSource(InputStream path) {
        checkSession();
        this.source = path == null ? null : NutsStreamOrPath.of(path,session);
        return this;
    }

    @Override
    public InputStream create() {
        checkSession();
        if (source == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing Source"));
        }
        checkSession();
        NutsString sourceName = this.sourceName;
        if (sourceName == null || sourceName.isEmpty()) {
            sourceName = NutsTexts.of(session).toText(source);
        }
        if (sourceName == null || sourceName.isEmpty()) {
            sourceName = NutsTexts.of(session).toText(source.getName());
        }
        if (sourceName == null || sourceName.isEmpty()) {
            sourceName = NutsTexts.of(session).toText(String.valueOf(source.getValue()));
        }
        NutsProgressMonitor monitor = CoreIOUtils.createProgressMonitor(CoreIOUtils.MonitorType.STREAM, source.getValue(), sourceOrigin, session, isLogProgress(), getProgressFactory());
        boolean verboseMode
                = getSession().boot().getBootCustomBoolArgument(false,false,false,"---monitor-start");
        long size = -1;
        try {
            if (verboseMode && monitor != null) {
                monitor.onStart(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, size, null, session, true));
            }
            size = source.getContentLength();
        } catch (UncheckedIOException | NutsIOException e) {
            if (verboseMode && monitor != null) {
                monitor.onComplete(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, size, e, session, true));
            }
            throw e;
        }
        if (size < 0) {
            size = getLength();
        }
        if (monitor == null) {
            return source.getInputStream();
        }
        InputStream openedStream = source.getInputStream();
        if (!verboseMode) {
            monitor.onStart(new DefaultNutsProgressEvent(source, sourceName, 0, 0, 0, 0, size, null, session, size < 0));
        }
        String sourceTypeName = getSourceTypeName();
        if (sourceTypeName == null) {
            sourceTypeName = source.getStreamMetaData().getUserKind();
        }
        if (sourceTypeName == null) {
            sourceTypeName = "nuts-Path";//inputSource.getTypeName();
        }
        return InputStreamMetadataAwareImpl.of(
                CoreIOUtils.monitor(openedStream, source, sourceName, size, new SilentStartNutsInputStreamProgressMonitorAdapter(monitor, sourceName.filteredText()), session),
                new NutsDefaultStreamMetadata(source.getStreamMetaData())
                        .setUserKind(sourceTypeName)
        );
    }

    @Override
    public String getSourceTypeName() {
        return sourceTypeName;
    }

    @Override
    public NutsInputStreamMonitor setSourceTypeName(String sourceType) {
        this.sourceTypeName = sourceType;
        return this;
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
    public NutsInputStreamMonitor setLogProgress(boolean value) {
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
    public NutsInputStreamMonitor setProgressFactory(NutsProgressFactory value) {
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
    public NutsInputStreamMonitor setProgressMonitor(NutsProgressMonitor value) {
        this.progressFactory = value == null ? null : new SingletonNutsInputStreamProgressFactory(value);
        return this;
    }


    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
