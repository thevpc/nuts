package net.thevpc.nuts.spi;

import net.thevpc.nuts.io.NCompress;

/**
 * NCompressPackaging interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NCompressPackaging extends NComponent {
    /**
     * Compress package.
     *
     * @param compress compress
     */
    void compressPackage(NCompress compress);
}
