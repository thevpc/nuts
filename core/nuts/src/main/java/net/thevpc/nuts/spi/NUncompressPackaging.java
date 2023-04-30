package net.thevpc.nuts.spi;

import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NUncompress;
import net.thevpc.nuts.io.NUncompressVisitor;

public interface NUncompressPackaging extends NComponent {
    void visitPackage(NUncompress uncompress, NInputSource source, NUncompressVisitor visitor);

    void uncompressPackage(NUncompress uncompress, NInputSource source);
}
