package net.thevpc.nuts.runtime.standalone.repository.index;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;

public class CacheDB {
    public static NanoDB of(NSession session) {
        synchronized (session.getWorkspace()) {
            NanoDB o = (NanoDB) NEnvs.of(session).getProperties().get(CacheDB.class.getName());
            if (o == null) {
                o = new NanoDB(
                                NLocations.of(session).getStoreLocation(
                                        session.getWorkspace().getApiId().builder().setVersion("SHARED").build()
                                ,
                                NStoreType.CACHE
                        ).resolve("cachedb").toFile().toFile()
                );
                o.getSerializers().setSerializer(NId.class,()->new NanoDBNIdSerializer(session));
                NEnvs.of(session).getProperties().put(CacheDB.class.getName(), o);
            }
            return o;
        }
    }
}
