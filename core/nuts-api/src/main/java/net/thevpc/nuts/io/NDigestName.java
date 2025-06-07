package net.thevpc.nuts.io;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

public interface NDigestName extends NComponent {
    static NDigestName of() {
        return NExtensions.of(NDigestName.class);
    }


    String getDigestName(Object source);

    String getDigestName(Object source, String sourceType);
}
