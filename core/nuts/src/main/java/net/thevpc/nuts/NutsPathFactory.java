package net.thevpc.nuts;

import net.thevpc.nuts.spi.NutsPathSPI;

public interface NutsPathFactory{
    NutsSupplier<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader);
}
