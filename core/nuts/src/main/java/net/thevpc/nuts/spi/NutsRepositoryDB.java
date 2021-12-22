package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.boot.NutsApiUtils;

import java.util.Set;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public interface NutsRepositoryDB extends NutsComponent{
    static NutsRepositoryDB of(NutsSession session){
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsRepositoryDB.class, true, session);
    }

    Set<String> getAllNames(String name);

    String getRepositoryNameByURL(String url);

    boolean isDefaultRepositoryName(String name);

    String getRepositoryURLByName(String name);
}
