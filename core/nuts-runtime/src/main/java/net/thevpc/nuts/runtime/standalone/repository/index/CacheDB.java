package net.thevpc.nuts.runtime.standalone.repository.index;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NStoreLocation;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;

public class CacheDB {
    public static NanoDB of(NSession session) {
        synchronized (session.getWorkspace()) {
            NanoDB o = (NanoDB) session.env().getProperties().get(CacheDB.class.getName());
            if (o == null) {
                o = new NanoDB(
                                session.locations().getStoreLocation(
                                        session.getWorkspace().getApiId().builder().setVersion("SHARED").build()
                                ,
                                NStoreLocation.CACHE
                        ).resolve("cachedb").toFile().toFile()
                );
                o.getSerializers().setSerializer(NId.class,()->new NanoDBNIdSerializer(session));
                session.env().getProperties().put(CacheDB.class.getName(), o);
            }
            return o;
        }
    }
}
