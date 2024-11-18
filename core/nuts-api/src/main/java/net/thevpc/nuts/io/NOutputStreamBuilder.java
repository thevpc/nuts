package net.thevpc.nuts.io;

import net.thevpc.nuts.reserved.rpi.NIORPI;

import java.io.OutputStream;

public interface NOutputStreamBuilder {
    static NOutputStreamBuilder of(OutputStream outputStream) {
        return NIORPI.of().ofOutputStreamBuilder(outputStream);
    }

    OutputStream getBase();

    NOutputStreamBuilder setBase(OutputStream base);

    NContentMetadata getMetadata();

    NOutputStreamBuilder setMetadata(NContentMetadata metadata);

    boolean isCloseBase();

    NOutputStreamBuilder setCloseBase(boolean closeBase);

    Runnable getCloseAction();

    NOutputStreamBuilder setCloseAction(Runnable closeAction);

    OutputStream createOutputStream();
}
