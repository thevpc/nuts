package net.thevpc.nuts.io;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

/**
 * NDigestName interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDigestName extends NComponent {
    /**
     * Creates a new instance of.
     *
     * @return of result
     */
    static NDigestName of() {
        return NExtensions.of(NDigestName.class);
    }


    /**
     * Digest name.
     *
     * @param source source
     * @return digest name result
     */
    String digestName(Object source);

}
