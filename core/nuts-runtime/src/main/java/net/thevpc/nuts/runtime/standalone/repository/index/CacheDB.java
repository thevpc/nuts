package net.thevpc.nuts.runtime.standalone.repository.index;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;

public class CacheDB {
    public static NanoDB of() {
        NWorkspace workspace = NWorkspace.of().get();
        synchronized (workspace) {
            NanoDB o = (NanoDB) NEnvs.of().getProperties().get(CacheDB.class.getName());
            if (o == null) {
                o = new NanoDB(
                        NLocations.of().getStoreLocation(
                                        workspace.getApiId().builder().setVersion("SHARED").build()
                                ,
                                NStoreType.CACHE
                        ).resolve("cachedb").toFile().get()
                );
                o.getSerializers().setSerializer(NId.class,()->new NanoDBNIdSerializer(workspace));
                NEnvs.of().getProperties().put(CacheDB.class.getName(), o);
            }
            return o;
        }
    }
}
