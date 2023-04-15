package net.thevpc.nuts.io;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponent;

public interface NHashName extends NComponent {
    static NHashName of(NSession session) {
        return session.extensions().createComponent(NHashName.class).get();
    }


    String getHashName(Object source);

    String getHashName(Object source, String sourceType);
}
