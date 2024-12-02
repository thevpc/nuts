package net.thevpc.nuts.runtime.standalone.repository.index;

import net.thevpc.nuts.*;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;

public class CacheDB {
    public static NanoDB of() {
        NWorkspace workspace = NWorkspace.get().get();
        synchronized (workspace) {
            NanoDB o = (NanoDB) NWorkspace.of().getProperties().get(CacheDB.class.getName());
            if (o == null) {
                o = new NanoDB(
                        NWorkspace.of().getStoreLocation(
                                        workspace.getApiId().builder().setVersion("SHARED").build()
                                ,
                                NStoreType.CACHE
                        ).resolve("cachedb").toFile().get()
                );
                o.getSerializers().setSerializer(NId.class,()->new NanoDBNIdSerializer(workspace));
                NWorkspace.of().getProperties().put(CacheDB.class.getName(), o);
            }
            return o;
        }
    }
}
