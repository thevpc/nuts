package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.boot.NutsApiUtils;

public interface NutsRepositoryDB {
    String getRepositoryNameByURL(String url);

    boolean isDefaultRepository(String name);

    String getRepositoryURLByName(String name);

    default NutsRepositoryURL parseNutsRepositoryURL(String expression, NutsSession session) {
        return NutsApiUtils.parseRepositoryURL(expression,this,session);
    }
}
