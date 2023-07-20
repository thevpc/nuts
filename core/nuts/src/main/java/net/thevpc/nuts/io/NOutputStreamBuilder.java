package net.thevpc.nuts.io;

import net.thevpc.nuts.NSession;

import java.io.InputStream;
import java.io.OutputStream;

public interface NOutputStreamBuilder {
    static NOutputStreamBuilder of(OutputStream outputStream, NSession session) {
        return NIO.of(session).ofOutputStreamBuilder(outputStream);
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
