package net.thevpc.nuts.spi;

import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.NSession;

import java.util.Set;

public interface NRepositoryDB extends NComponent {
    static NRepositoryDB of(NSession session){
       return NExtensions.of(session).createComponent(NRepositoryDB.class).get();
    }

    Set<String> getAllNames(String name);

    String getRepositoryNameByLocation(String location);

    boolean isDefaultRepositoryName(String name);

    String getRepositoryLocationByName(String name);
}
