/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.xtra.time;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.text.NMsg;
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
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNInputStreamMonitor implements NInputStreamMonitor {

    //    private final NutsLogger LOG;
    private String sourceTypeName;
    private NInputSource source;
    private Object sourceOrigin;
    private NMsg sourceName;
    private long length = -1;
    private boolean logProgress;
    private boolean traceProgress;
    private NProgressFactory progressFactory;

    public DefaultNInputStreamMonitor() {
    }
    
    @Override
    public NMsg name() {
        return sourceName;
    }

    @Override
    public NInputStreamMonitor name(NMsg name) {
        this.sourceName = name;
        return this;
    }

    @Override
    public Object origin() {
        return sourceOrigin;
    }

    @Override
    public NInputStreamMonitor origin(Object origin) {
        this.sourceOrigin = origin;
        return this;
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public NInputStreamMonitor length(long len) {
        this.length = len;
        return this;
    }

    @Override
    public NInputStreamMonitor source(NInputSource source) {
        this.source = source;
        return this;
    }

    @Override
    public NInputStreamMonitor source(NPath inputSource) {
        this.source = inputSource;
        return this;
    }

    @Override
    public NInputStreamMonitor source(Path path) {
        this.source = path == null ? null : NPath.of(path);
        return this;
    }

    @Override
    public NInputStreamMonitor source(File path) {
        this.source = path == null ? null : NPath.of(path);
        return this;
    }

    @Override
    public NInputStreamMonitor source(InputStream path) {
        this.source = path == null ? null : NInputSource.of(path);
        return this;
    }

    @Override
    public InputStream create() {
        NAssert.requireNamedNonNull(source, "source");
        NMsg sourceName = this.sourceName;
        if (sourceName == null && source != null) {
            sourceName = NMsg.ofNtf(NText.of(source));
        }
        if (sourceName == null) {
            sourceName = NMsg.ofNtf(NText.of(source.metaData().name()));
        }
        NProgressListener monitor = NProgressUtils.createProgressMonitor(NProgressUtils.MonitorType.STREAM, source, sourceOrigin, NWorkspace.of()
                , isLogProgress()
                , isTraceProgress()
                , progressFactory());
        boolean verboseMode
                = CoreNUtils.isCustomFalse("---monitor-start");
        long size = -1;
        try {
            if (verboseMode && monitor != null) {
                monitor.onProgress(NProgressEvent.ofStart(source, sourceName, size));
            }
            size = source.metaData().contentLength().orElse(-1L);
        } catch (UncheckedIOException | NIOException e) {
            if (verboseMode && monitor != null) {
                monitor.onProgress(NProgressEvent.ofComplete(source, sourceName, 0, 0,
                        null, 0, 0, size, e));
            }
            throw e;
        }
        if (size < 0) {
            size = length();
        }
        if (monitor == null) {
            return source.inputStream();
        }
        InputStream openedStream = source.inputStream();
        if (!verboseMode) {
            monitor.onProgress(NProgressEvent.ofStart(source, sourceName, size));
        }
        String sourceTypeName = sourceTypeName();
        if (sourceTypeName == null) {
            sourceTypeName = source.metaData().kind().orElse("nuts-Path");
        }

        InputStream z = NInputSourceBuilder.of(openedStream)
                .source(source)
                .monitoringListener(new SilentStartNProgressListenerAdapter(monitor, sourceName))
                .createInputStream()
                ;
        ((NContentMetadataProvider)z).metaData().kind(sourceTypeName);
        return z;
//        return (InputStream) NIO.of().ofInputSource(
//                NProgressUtils.ofMonitored(openedStream, source, sourceName, size, new SilentStartNProgressListenerAdapter(monitor, sourceName), session),
//                new DefaultNContentMetadata(source.getMetaData())
//                        .setKind(sourceTypeName)
//        );
    }

    @Override
    public NInputSource source() {
        return source;
    }

    @Override
    public String sourceTypeName() {
        return sourceTypeName;
    }

    @Override
    public NInputStreamMonitor sourceTypeName(String sourceType) {
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
    public NInputStreamMonitor logProgress(boolean value) {
        this.logProgress = value;
        return this;
    }

    public NInputStreamMonitor traceProgress(boolean value) {
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
    public NProgressFactory progressFactory() {
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
    public NInputStreamMonitor progressFactory(NProgressFactory value) {
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
    public NInputStreamMonitor progressMonitor(NProgressListener value) {
        this.progressFactory = value == null ? null : new SingletonNInputStreamProgressFactory(value);
        return this;
    }


}
