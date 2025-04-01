package net.thevpc.nuts.runtime.standalone.io.inputstream;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.io.NContentMetadata;
import net.thevpc.nuts.io.NInputSourceBuilder;
import net.thevpc.nuts.io.NInterruptible;
import net.thevpc.nuts.io.NNonBlockingInputStream;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamExt;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamTee;
import net.thevpc.nuts.runtime.standalone.io.util.NNonBlockingInputStreamAdapter;
import net.thevpc.nuts.io.NullInputStream;
import net.thevpc.nuts.time.NProgressListener;

import java.io.InputStream;
import java.io.OutputStream;

import net.thevpc.nuts.io.NInputSource;

public class DefaultNInputSourceBuilder implements NInputSourceBuilder {

    private InputStream baseInputStream;
    private boolean closeBase = true;
    private Runnable closeAction;
    private boolean interruptible;
    private NContentMetadata metadata;
    private Object source;
    private NMsg sourceName;
    private Long expectedLength;
    private NProgressListener monitoringListener;
    private boolean nonBlocking;
    private OutputStream tee;

    public DefaultNInputSourceBuilder() {
    }

    @Override
    public NInputSourceBuilder setBase(InputStream baseInputStream) {
        this.baseInputStream = baseInputStream;
        return this;
    }

    @Override
    public boolean isCloseBase() {
        return closeBase;
    }

    @Override
    public NInputSourceBuilder setCloseBase(boolean closeBase) {
        this.closeBase = closeBase;
        return this;
    }

    @Override
    public Runnable getCloseAction() {
        return closeAction;
    }

    @Override
    public NInputSourceBuilder setCloseAction(Runnable closeAction) {
        this.closeAction = closeAction;
        return this;
    }

    @Override
    public boolean isInterruptible() {
        return interruptible;
    }

    @Override
    public NInputSourceBuilder setInterruptible(boolean interruptible) {
        this.interruptible = interruptible;
        return this;
    }

    @Override
    public NContentMetadata getMetadata() {
        return metadata;
    }

    @Override
    public NInputSourceBuilder setMetadata(NContentMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public NInputSourceBuilder setSource(Object source) {
        this.source = source;
        return this;
    }

    @Override
    public NMsg getSourceName() {
        return sourceName;
    }

    @Override
    public NInputSourceBuilder setSourceName(NMsg sourceName) {
        this.sourceName = sourceName;
        return this;
    }

    @Override
    public Long getExpectedLength() {
        return expectedLength;
    }

    @Override
    public NInputSourceBuilder setExpectedLength(Long expectedLength) {
        this.expectedLength = expectedLength;
        return this;
    }

    @Override
    public NProgressListener getMonitoringListener() {
        return monitoringListener;
    }

    @Override
    public NInputSourceBuilder setMonitoringListener(NProgressListener monitoringListener) {
        this.monitoringListener = monitoringListener;
        return this;
    }

    @Override
    public boolean isNonBlocking() {
        return nonBlocking;
    }

    @Override
    public NInputSourceBuilder setNonBlocking(boolean nonBlocking) {
        this.nonBlocking = nonBlocking;
        return this;
    }

    @Override
    public OutputStream getTee() {
        return tee;
    }

    @Override
    public NInputSourceBuilder setTee(OutputStream tee) {
        this.tee = tee;
        return this;
    }

    @Override
    public NNonBlockingInputStream createNonBlockingInputStream() {
        InputStream u = createInputStream();
        if (u instanceof NNonBlockingInputStream) {
            return (NNonBlockingInputStream) u;
        }
        return new NNonBlockingInputStreamAdapter(
                u, metadata, sourceName
        );
    }

    @Override
    public NInterruptible<InputStream> createInterruptibleInputStream() {
        InputStream u = createInputStream();
        return (NInterruptible<InputStream>) u;
    }

    @Override
    public InputStream createInputStream() {
        if (baseInputStream == null) {
            InputStream b = NullInputStream.INSTANCE;
            if (nonBlocking) {
                return new NNonBlockingInputStreamAdapter(b, metadata, sourceName);
            }
        }
        InputStream a = baseInputStream;
        Runnable currentOnClose = closeAction;
        if (tee != null) {
            a = new InputStreamTee(a, tee, currentOnClose, metadata);
            currentOnClose = null;
        }
        a = new InputStreamExt(a, metadata, closeBase, currentOnClose, monitoringListener, source,
                sourceName, expectedLength);
        currentOnClose = null;
        if (nonBlocking) {
            a = new NNonBlockingInputStreamAdapter(a, metadata, sourceName);
        }
        return a;
    }

    @Override
    public NInputSource createInputSource() {
        return NInputSource.of(createInputStream());
    }

}
