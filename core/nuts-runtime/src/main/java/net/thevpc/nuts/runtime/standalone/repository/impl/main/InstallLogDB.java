package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NStoreLocation;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.repository.index.NanoDBNIdSerializer;

public class InstallLogDB {
    static NanoDB of(NSession session) {
        synchronized (session.getWorkspace()) {
            NanoDB o = (NanoDB) session.env().getProperties().get(NanoDB.class.getName());
            if (o == null) {
                o = new NanoDB(
                                session.locations().getStoreLocation(
                                        session.getWorkspace().getApiId().builder().setVersion("SHARED").build(),
                                        NStoreLocation.VAR
                                ).resolve("install-log").toFile().toFile()
                );
                o.getSerializers().setSerializer(NId.class, () -> new NanoDBNIdSerializer(session));
                session.env().getProperties().put(NanoDB.class.getName(), o);
            }
            return o;
        }
    }
}
