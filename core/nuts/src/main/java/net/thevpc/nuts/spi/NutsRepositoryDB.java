package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.boot.NutsApiUtils;

public interface NutsRepositoryDB {
    String getRepositoryNameByURL(String url);

    boolean isDefaultRepositoryName(String name);

    String getRepositoryURLByName(String name);
}
