package net.thevpc.nuts.spi;

import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NUncompress;
import net.thevpc.nuts.io.NUncompressVisitor;

/**
 * NUncompressPackaging interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NUncompressPackaging extends NComponent {
    /**
     * Visit package.
     *
     * @param uncompress uncompress
     * @param source source
     * @param visitor visitor
     */
    void visitPackage(NUncompress uncompress, NInputSource source, NUncompressVisitor visitor);

    /**
     * Uncompress package.
     *
     * @param uncompress uncompress
     * @param source source
     */
    void uncompressPackage(NUncompress uncompress, NInputSource source);
}
