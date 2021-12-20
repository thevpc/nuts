package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.boot.NutsApiUtils;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public interface NutsRepositoryDB extends NutsComponent{
    static NutsRepositoryDB of(NutsSession session){
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsRepositoryDB.class, true, session);
    }

    String getRepositoryNameByURL(String url);

    boolean isDefaultRepositoryName(String name);

    String getRepositoryURLByName(String name);
}
