package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NWorkspace;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NBlankable;

public class NWsConfDB {
    private NWorkspace ws;

    public NWsConfDB(NWorkspace ws) {
        this.ws = ws;
    }

    public void storeStringNonBlank(NLocationKey k, String value) {
        storeString(k, value, true);
    }

    public void storeString(NLocationKey k, String value, boolean deleteIfBlank) {
        NPath path = NWorkspace.get().getStoreLocation(k.getId(), k.getStoreType(), k.getRepoUuid())
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
