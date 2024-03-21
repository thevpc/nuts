package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NLocations;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NBlankable;

public class NWsConfDB {
    public void storeStringNonBlank(NLocationKey k, String value, NSession session) {
        storeString(k, value, true, session);
    }

    public void storeString(NLocationKey k, String value, boolean deleteIfBlank, NSession session) {
        NPath path = NLocations.of(session).getStoreLocation(k.getId(), k.getStoreType(), k.getRepoUuid())
                .resolve(k.getName());
        if (NBlankable.isBlank(value) && deleteIfBlank) {
            if (path.isRegularFile()) {
                path.delete();
            }
        } else {
            path.writeString(value);
        }
    }
}
