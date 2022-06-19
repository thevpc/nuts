/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.progress;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.*;

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
    private NutsInputSource source;
    private Object sourceOrigin;
    private NutsMessage sourceName;
    private long length = -1;
    private NutsSession session;
    private boolean logProgress;
    private boolean traceProgress;
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
    public NutsMessage getName() {
        return sourceName;
    }

    @Override
    public NutsInputStreamMonitor setName(NutsMessage name) {
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
    public NutsInputStreamMonitor setSource(NutsInputSource source) {
        this.source = source;
        return this;
    }

    @Override
    public NutsInputStreamMonitor setSource(NutsPath inputSource) {
        this.source = inputSource;
        return this;
    }

    @Override
    public NutsInputStreamMonitor setSource(Path path) {
        checkSession();
        this.source = path == null ? null : NutsPath.of(path, getSession());
        return this;
    }

    @Override
    public NutsInputStreamMonitor setSource(File path) {
        checkSession();
        this.source = path == null ? null : NutsPath.of(path, getSession());
        return this;
    }

    @Override
    public NutsInputStreamMonitor setSource(InputStream path) {
        this.source = path == null ? null : NutsIO.of(session).createInputSource(path);
        return this;
    }

    @Override
    public InputStream create() {
        NutsUtils.requireNonNull(source, getSession(), "source");
        NutsMessage sourceName = this.sourceName;
        if (sourceName == null && source != null) {
            sourceName = NutsMessage.ofNtf(NutsTexts.of(session).ofText(source));
        }
        if (sourceName == null) {
            sourceName = NutsMessage.ofNtf(NutsTexts.of(session).ofText(source.getInputMetaData().getName()));
        }
        NutsProgressListener monitor = NutsProgressUtils.createProgressMonitor(NutsProgressUtils.MonitorType.STREAM, source, sourceOrigin, session
                , isLogProgress()
                , isTraceProgress()
                , getProgressFactory());
        boolean verboseMode
                = CoreNutsUtils.isCustomFalse("---monitor-start", getSession());
        long size = -1;
        try {
            if (verboseMode && monitor != null) {
                monitor.onProgress(NutsProgressEvent.ofStart(source, sourceName, size, session));
            }
            size = source.getInputMetaData().getContentLength().orElse(-1L);
        } catch (UncheckedIOException | NutsIOException e) {
            if (verboseMode && monitor != null) {
                monitor.onProgress(NutsProgressEvent.ofComplete(source, sourceName, 0, 0,
                        null, 0, 0, size, e, session));
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
            monitor.onProgress(NutsProgressEvent.ofStart(source, sourceName, size, session));
        }
        String sourceTypeName = getSourceTypeName();
        if (sourceTypeName == null) {
            sourceTypeName = source.getInputMetaData().getKind().orElse("nuts-Path");
        }
        return (InputStream) NutsIO.of(session).createInputSource(
                NutsProgressUtils.monitor(openedStream, source, sourceName, size, new SilentStartNutsProgressListenerAdapter(monitor, sourceName), session),
                new DefaultNutsInputSourceMetadata(source.getInputMetaData())
                        .setKind(sourceTypeName)
        );
    }

    @Override
    public NutsInputSource getSource() {
        return source;
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

    @Override
    public boolean isTraceProgress() {
        return traceProgress;
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

    public NutsInputStreamMonitor setTraceProgress(boolean value) {
        this.traceProgress = value;
        return this;
    }

    /**
     * return progress factory responsible for creating progress monitor
     *
     * @return progress factory responsible for creating progress monitor
     * @since 0.5.8
     */
    @Override
    public NutsProgressFactory getProgressFactory() {
        return progressFactory;
    }

    /**
     * set progress factory responsible for creating progress monitor
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
    public NutsInputStreamMonitor setProgressMonitor(NutsProgressListener value) {
        this.progressFactory = value == null ? null : new SingletonNutsInputStreamProgressFactory(value);
        return this;
    }


    protected void checkSession() {
        NutsSessionUtils.checkSession(ws, session);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
