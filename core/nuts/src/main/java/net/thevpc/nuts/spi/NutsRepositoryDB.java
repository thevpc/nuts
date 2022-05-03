package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.util.NutsUtils;

import java.util.Set;

@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public interface NutsRepositoryDB extends NutsComponent{
    static NutsRepositoryDB of(NutsSession session){
        NutsUtils.requireSession(session);
        return session.extensions().createSupported(NutsRepositoryDB.class, true, session);
    }

    Set<String> getAllNames(String name);

    String getRepositoryNameByLocation(String location);

    boolean isDefaultRepositoryName(String name);

    String getRepositoryLocationByName(String name);
}
