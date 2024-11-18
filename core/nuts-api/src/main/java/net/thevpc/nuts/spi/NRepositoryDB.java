package net.thevpc.nuts.spi;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.reserved.boot.NReservedBootRepositoryDB;

import java.util.Set;

public interface NRepositoryDB extends NComponent {
    static NRepositoryDB ofDefault(){
        return new NReservedBootRepositoryDB();
    }
    static NRepositoryDB of(){
        if(!NSession.of().isPresent()){
            return ofDefault();
        }
       return NExtensions.of().createComponent(NRepositoryDB.class).get();
    }

    Set<String> getAllNames(String name);

    String getRepositoryNameByLocation(String location);

    boolean isDefaultRepositoryName(String name);

    /**
     * @since  0.8.5
     * @param nameOrUrl nameOrUrl
     * @return true if preview
     */
    boolean isPreviewRepository(String nameOrUrl);

    String getRepositoryLocationByName(String name);
}
