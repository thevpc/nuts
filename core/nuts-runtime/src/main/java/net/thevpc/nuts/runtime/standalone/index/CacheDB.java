package net.thevpc.nuts.runtime.standalone.index;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDB;

import java.io.File;

public class CacheDB {
    public static NanoDB of(NutsSession session) {
        synchronized (session.getWorkspace()) {
            NanoDB o = (NanoDB) session.env().getProperties().get(NanoDB.class.getName());
            if (o == null) {
                o = new NanoDB(
                        new File(
                                session.locations().getStoreLocation(
                                        session.getWorkspace().getApiId().builder().setVersion("SHARED").build()
                                ,
                                NutsStoreLocation.CACHE
                        ),"/cachedb")
                );
                if(o.getSerializers().findSerializer(NutsId.class,true)==null){
                    o.getSerializers().setSerializer(NutsId.class,true,new NanoDBNutsIdSerializer.Null(session));
                }
                if(o.getSerializers().findSerializer(NutsId.class,false)==null){
                    o.getSerializers().setSerializer(NutsId.class,false,new NanoDBNutsIdSerializer.NonNull(session));
                }
                session.env().getProperties().put(NanoDB.class.getName(), o);
            }
            return o;
        }
    }
}
