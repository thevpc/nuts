package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.runtime.bundles.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.repository.index.NanoDBNutsIdSerializer;

public class InstallLogDB {
    static NanoDB of(NutsSession session) {
        synchronized (session.getWorkspace()) {
            NanoDB o = (NanoDB) session.env().getProperties().get(NanoDB.class.getName());
            if (o == null) {
                o = new NanoDB(
                                session.locations().getStoreLocation(
                                        session.getWorkspace().getApiId().builder().setVersion("SHARED").build(),
                                        NutsStoreLocation.VAR
                                ).resolve("install-log").toFile().toFile()
                );
                o.getSerializers().setSerializer(NutsId.class, () -> new NanoDBNutsIdSerializer(session));
                session.env().getProperties().put(NanoDB.class.getName(), o);
            }
            return o;
        }
    }
}
