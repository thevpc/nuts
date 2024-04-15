package net.thevpc.nuts.io;

import net.thevpc.nuts.reserved.rpi.NIORPI;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.time.NProgressListener;

import java.io.InputStream;
import java.io.OutputStream;

public interface NInputSourceBuilder {

    static NInputSourceBuilder of(InputStream is, NSession session) {
        return NIORPI.of(session).ofInputSourceBuilder(is);
    }

    NInputSourceBuilder setBase(InputStream baseInputStream);

    boolean isCloseBase();

    NInputSourceBuilder setCloseBase(boolean closeBase);

    Runnable getCloseAction();

    NInputSourceBuilder setCloseAction(Runnable closeAction);

    boolean isInterruptible();

    NInputSourceBuilder setInterruptible(boolean interruptible);

    NContentMetadata getMetadata();

    NInputSourceBuilder setMetadata(NContentMetadata metadata);

    Object getSource();

    NInputSourceBuilder setSource(Object source);

    NMsg getSourceName();

    NInputSourceBuilder setSourceName(NMsg sourceName);

    Long getExpectedLength();

    NInputSourceBuilder setExpectedLength(Long expectedLength);

    NProgressListener getMonitoringListener();

    NInputSourceBuilder setMonitoringListener(NProgressListener monitoringListener);

    boolean isNonBlocking();

    NInputSourceBuilder setNonBlocking(boolean nonBlocking);

    OutputStream getTee();

    NInputSourceBuilder setTee(OutputStream tee);

    NNonBlockingInputStream createNonBlockingInputStream();

    NInterruptible<InputStream> createInterruptibleInputStream();

    InputStream createInputStream();

    NInputSource createInputSource();
}
