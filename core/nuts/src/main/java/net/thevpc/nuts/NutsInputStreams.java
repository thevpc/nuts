package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;
import net.thevpc.nuts.spi.NutsComponent;

import java.io.InputStream;

public interface NutsInputStreams extends NutsComponent<Object> {
    static NutsInputStreams of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsInputStreams.class, true, session);
    }

    static InputStream ofNull(NutsSession session) {
        return of(session).ofNull();
    }

    InputStream ofNull();

    boolean isStdin(InputStream in);

    InputStream stdin();
}
