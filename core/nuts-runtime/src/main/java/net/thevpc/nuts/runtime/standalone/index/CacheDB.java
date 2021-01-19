package net.thevpc.nuts.runtime.standalone.index;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDB;

import java.io.File;

public class CacheDB {
    public static NanoDB of(NutsWorkspace ws) {
        synchronized (ws) {
            NanoDB o = (NanoDB) ws.env().getProperties().get(NanoDB.class.getName());
            if (o == null) {
                o = new NanoDB(
                        new File(
                                ws.locations().getStoreLocation(
                                ws.getApiId().builder().setVersion("SHARED").build()
                                ,
                                NutsStoreLocation.CACHE
                        ),"/cachedb")
                );
                if(o.getSerializers().findSerializer(NutsId.class,true)==null){
                    o.getSerializers().setSerializer(NutsId.class,true,new NanoDBNutsIdSerializer.Null(ws));
                }
                if(o.getSerializers().findSerializer(NutsId.class,false)==null){
                    o.getSerializers().setSerializer(NutsId.class,false,new NanoDBNutsIdSerializer.NonNull(ws));
                }
                ws.env().getProperties().put(NanoDB.class.getName(), o);
            }
            return o;
        }
    }
}
