package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.repository.index.NanoDBNIdSerializer;

public class InstallLogDB {
    static NanoDB of(NSession session) {
        synchronized (session.getWorkspace()) {
            NanoDB o = (NanoDB) NEnvs.of(session).getProperties().get(NanoDB.class.getName());
            if (o == null) {
                o = new NanoDB(
                                NLocations.of(session).getStoreLocation(
                                        session.getWorkspace().getApiId().builder().setVersion("SHARED").build(),
                                        NStoreType.VAR
                                ).resolve("install-log").toFile().toFile()
                );
                o.getSerializers().setSerializer(NId.class, () -> new NanoDBNIdSerializer(session));
                NEnvs.of(session).getProperties().put(NanoDB.class.getName(), o);
            }
            return o;
        }
    }
}
