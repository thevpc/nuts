package net.thevpc.nuts.spi;

import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.NSession;

import java.util.Set;

@NComponentScope(NComponentScopeType.WORKSPACE)
public interface NRepositoryDB extends NComponent {
    static NRepositoryDB of(NSession session){
       return NExtensions.of(session).createSupported(NRepositoryDB.class, true, session);
    }

    Set<String> getAllNames(String name);

    String getRepositoryNameByLocation(String location);

    boolean isDefaultRepositoryName(String name);

    String getRepositoryLocationByName(String name);
}
