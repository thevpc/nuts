/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.time;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.time.NProgressEvent;
import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.time.NProgressListener;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * @author thevpc
 */
public class DefaultNInputStreamMonitor implements NInputStreamMonitor {

    //    private final NutsLogger LOG;
    private final NWorkspace workspace;
    private String sourceTypeName;
    private NInputSource source;
    private Object sourceOrigin;
    private NMsg sourceName;
    private long length = -1;
    private boolean logProgress;
    private boolean traceProgress;
    private NProgressFactory progressFactory;

    public DefaultNInputStreamMonitor(NWorkspace workspace) {
        this.workspace = workspace;
    }
    
    @Override
    public NMsg getName() {
        return sourceName;
    }

    @Override
    public NInputStreamMonitor setName(NMsg name) {
        this.sourceName = name;
        return this;
    }

    @Override
    public Object getOrigin() {
        return sourceOrigin;
    }

    @Override
    public NInputStreamMonitor setOrigin(Object origin) {
        this.sourceOrigin = origin;
        return this;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public NInputStreamMonitor setLength(long len) {
        this.length = len;
        return this;
    }

    @Override
    public NInputStreamMonitor setSource(NInputSource source) {
        this.source = source;
        return this;
    }

    @Override
    public NInputStreamMonitor setSource(NPath inputSource) {
        this.source = inputSource;
        return this;
    }

    @Override
    public NInputStreamMonitor setSource(Path path) {
        this.source = path == null ? null : NPath.of(path);
        return this;
    }

    @Override
    public NInputStreamMonitor setSource(File path) {
        this.source = path == null ? null : NPath.of(path);
        return this;
    }

    @Override
    public NInputStreamMonitor setSource(InputStream path) {
        this.source = path == null ? null : NInputSource.of(path);
        return this;
    }

    @Override
    public InputStream create() {
        NAssert.requireNonNull(source, "source");
        NMsg sourceName = this.sourceName;
        if (sourceName == null && source != null) {
            sourceName = NMsg.ofNtf(NText.of(source));
        }
        if (sourceName == null) {
            sourceName = NMsg.ofNtf(NText.of(source.getMetaData().getName()));
        }
        NProgressListener monitor = NProgressUtils.createProgressMonitor(NProgressUtils.MonitorType.STREAM, source, sourceOrigin, workspace
                , isLogProgress()
                , isTraceProgress()
                , getProgressFactory());
        boolean verboseMode
                = CoreNUtils.isCustomFalse("---monitor-start");
        long size = -1;
        try {
            if (verboseMode && monitor != null) {
                monitor.onProgress(NProgressEvent.ofStart(source, sourceName, size));
            }
            size = source.getMetaData().getContentLength().orElse(-1L);
        } catch (UncheckedIOException | NIOException e) {
            if (verboseMode && monitor != null) {
                monitor.onProgress(NProgressEvent.ofComplete(source, sourceName, 0, 0,
                        null, 0, 0, size, e));
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
            monitor.onProgress(NProgressEvent.ofStart(source, sourceName, size));
        }
        String sourceTypeName = getSourceTypeName();
        if (sourceTypeName == null) {
            sourceTypeName = source.getMetaData().getKind().orElse("nuts-Path");
        }

        InputStream z = NInputSourceBuilder.of(openedStream)
                .setSource(source)
                .setMonitoringListener(new SilentStartNProgressListenerAdapter(monitor, sourceName))
                .createInputStream()
                ;
        ((NContentMetadataProvider)z).getMetaData().setKind(sourceTypeName);
        return z;
//        return (InputStream) NIO.of().ofInputSource(
//                NProgressUtils.ofMonitored(openedStream, source, sourceName, size, new SilentStartNProgressListenerAdapter(monitor, sourceName), session),
//                new DefaultNContentMetadata(source.getMetaData())
//                        .setKind(sourceTypeName)
//        );
    }

    @Override
    public NInputSource getSource() {
        return source;
    }

    @Override
    public String getSourceTypeName() {
        return sourceTypeName;
    }

    @Override
    public NInputStreamMonitor setSourceTypeName(String sourceType) {
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
    public NInputStreamMonitor setLogProgress(boolean value) {
        this.logProgress = value;
        return this;
    }

    public NInputStreamMonitor setTraceProgress(boolean value) {
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
    public NProgressFactory getProgressFactory() {
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
    public NInputStreamMonitor setProgressFactory(NProgressFactory value) {
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
    public NInputStreamMonitor setProgressMonitor(NProgressListener value) {
        this.progressFactory = value == null ? null : new SingletonNInputStreamProgressFactory(value);
        return this;
    }


    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
