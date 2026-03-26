package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.core.NWorkspace;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NUnused;

@NUnused
public class NWsConfDB {

    public NWsConfDB() {
    }

    public void storeStringNonBlank(NStoreKey k, String value) {
        storeString(k, value, true);
    }

    public void storeString(NStoreKey k, String value, boolean deleteIfBlank) {
        NPath path = NPath.of(k)
                .resolve(k.name());
        if (NBlankable.isBlank(value) && deleteIfBlank) {
            if (path.isRegularFile()) {
                path.delete();
            }
        } else {
            path.mkParentDirs().writeString(value);
        }
    }
}
