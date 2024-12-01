package net.thevpc.nuts.runtime.standalone.repository.index;

import net.thevpc.nuts.*;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;

public class CacheDB {
    public static NanoDB of() {
        NWorkspace workspace = NWorkspace.of().get();
        synchronized (workspace) {
            NanoDB o = (NanoDB) NWorkspace.get().getProperties().get(CacheDB.class.getName());
            if (o == null) {
                o = new NanoDB(
                        NWorkspace.get().getStoreLocation(
                                        workspace.getApiId().builder().setVersion("SHARED").build()
                                ,
                                NStoreType.CACHE
                        ).resolve("cachedb").toFile().get()
                );
                o.getSerializers().setSerializer(NId.class,()->new NanoDBNIdSerializer(workspace));
                NWorkspace.get().getProperties().put(CacheDB.class.getName(), o);
            }
            return o;
        }
    }
}
