package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.time.NProgressListener;

import java.io.InputStream;
import java.io.OutputStream;

public interface NInputStreamBuilder {
    static NInputStreamBuilder of(InputStream is, NSession session) {
        return NIO.of(session).ofInputStreamBuilder(is);
    }


    NInputStreamBuilder setBase(InputStream baseInputStream);

    boolean isCloseBase();

    NInputStreamBuilder setCloseBase(boolean closeBase);

    Runnable getCloseAction();

    NInputStreamBuilder setCloseAction(Runnable closeAction);

    boolean isInterruptible();

    NInputStreamBuilder setInterruptible(boolean interruptible);

    NContentMetadata getMetadata();

    NInputStreamBuilder setMetadata(NContentMetadata metadata);

    Object getSource();

    NInputStreamBuilder setSource(Object source);

    NMsg getSourceName();

    NInputStreamBuilder setSourceName(NMsg sourceName);

    Long getExpectedLength();

    NInputStreamBuilder setExpectedLength(Long expectedLength);

    NProgressListener getMonitoringListener();

    NInputStreamBuilder setMonitoringListener(NProgressListener monitoringListener);

    boolean isNonBlocking();

    NInputStreamBuilder setNonBlocking(boolean nonBlocking);

    OutputStream getTee();

    NInputStreamBuilder setTee(OutputStream tee);

    NNonBlockingInputStream createNonBlockingInputStream();

    NInterruptible createInterruptibleInputStream();

    InputStream createInputStream();
}
