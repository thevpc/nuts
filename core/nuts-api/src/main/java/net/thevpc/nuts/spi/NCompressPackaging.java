package net.thevpc.nuts.spi;

import net.thevpc.nuts.io.NCompress;

public interface NCompressPackaging extends NComponent {
    void compressPackage(NCompress compress);
}
