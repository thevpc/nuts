package net.thevpc.nuts.io;

import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NProgressListener;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.io.InputStream;
import java.io.OutputStream;

public interface NInputSourceBuilder {

    static NInputSourceBuilder of(InputStream is) {
        return NIORPI.of().ofInputSourceBuilder(is);
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

    @NGetter
    Object source();

    @NSetter
    NInputSourceBuilder source(Object source);

    NMsg sourceName();

    NInputSourceBuilder sourceName(NMsg sourceName);

    Long expectedLength();

    NInputSourceBuilder expectedLength(Long expectedLength);

    NProgressListener monitoringListener();

    NInputSourceBuilder monitoringListener(NProgressListener monitoringListener);

    boolean isNonBlocking();

    NInputSourceBuilder nonBlocking(boolean nonBlocking);

    OutputStream tee();

    NInputSourceBuilder tee(OutputStream tee);

    NNonBlockingInputStream createNonBlockingInputStream();

    NInterruptible<InputStream> createInterruptibleInputStream();

    InputStream createInputStream();

    NInputSource createInputSource();
}
