package net.thevpc.nuts.io;

import net.thevpc.nuts.internal.rpi.NIORPI;

import java.io.OutputStream;

public interface NOutputStreamBuilder {
    static NOutputStreamBuilder of(OutputStream outputStream) {
        return NIORPI.of().ofOutputStreamBuilder(outputStream);
    }

    OutputStream base();

    NOutputStreamBuilder base(OutputStream base);

    NContentMetadata metadata();

    NOutputStreamBuilder metadata(NContentMetadata metadata);

    boolean isCloseBase();

    NOutputStreamBuilder closeBase(boolean closeBase);

    Runnable closeAction();

    NOutputStreamBuilder closeAction(Runnable closeAction);

    OutputStream createOutputStream();
}
