package net.thevpc.nuts.runtime.standalone.repository.index;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDB;

public class CacheDB {
    public static NanoDB of(NutsSession session) {
        synchronized (session.getWorkspace()) {
            NanoDB o = (NanoDB) session.env().getProperties().get(CacheDB.class.getName());
            if (o == null) {
                o = new NanoDB(
                                session.locations().getStoreLocation(
                                        session.getWorkspace().getApiId().builder().setVersion("SHARED").build()
                                ,
                                NutsStoreLocation.CACHE
                        ).resolve("cachedb").toFile().toFile()
                );
                o.getSerializers().setSerializer(NutsId.class,()->new NanoDBNutsIdSerializer(session));
                session.env().getProperties().put(CacheDB.class.getName(), o);
            }
            return o;
        }
    }
}
