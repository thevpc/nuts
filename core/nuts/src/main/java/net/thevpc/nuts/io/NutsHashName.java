package net.thevpc.nuts.io;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsComponent;

public interface NutsHashName extends NutsComponent {
    static NutsHashName of(NutsSession session) {
        return session.extensions().createSupported(NutsHashName.class, true, session);
    }


    String getHashName(Object source);

    String getHashName(Object source, String sourceType);
}
