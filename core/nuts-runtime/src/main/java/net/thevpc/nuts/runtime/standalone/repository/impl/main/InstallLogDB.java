package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.*;
import net.thevpc.nuts.env.NEnvs;
import net.thevpc.nuts.env.NLocations;
import net.thevpc.nuts.env.NStoreType;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.repository.index.NanoDBNIdSerializer;

public class InstallLogDB {
    static NanoDB of(NWorkspace workspace) {
        synchronized (workspace) {
            NanoDB o = (NanoDB) NEnvs.of().getProperties().get(NanoDB.class.getName());
            if (o == null) {
                o = new NanoDB(
                        NLocations.of().getStoreLocation(
                                        workspace.getApiId().builder().setVersion("SHARED").build(),
                                        NStoreType.VAR
                                ).resolve("install-log").toFile().get()
                );
                o.getSerializers().setSerializer(NId.class, () -> new NanoDBNIdSerializer(workspace));
                NEnvs.of().getProperties().put(NanoDB.class.getName(), o);
            }
            return o;
        }
    }
}
