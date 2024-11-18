package net.thevpc.nuts.io;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

public interface NHashName extends NComponent {
    static NHashName of() {
        return NExtensions.of().createComponent(NHashName.class).get();
    }


    String getHashName(Object source);

    String getHashName(Object source, String sourceType);
}
