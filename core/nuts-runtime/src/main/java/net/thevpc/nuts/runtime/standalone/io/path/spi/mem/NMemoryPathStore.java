package net.thevpc.nuts.runtime.standalone.io.path.spi.mem;

import net.thevpc.nuts.util.*;

import java.util.*;

public class NMemoryPathStore {
    public static final String PREFIX = "mem:";
    NMemStoreItem root;

    public NMemoryPathStore() {
        root= new NMemStoreItem(true, "", null,this);
    }



    public NMemStoreItem findStoreItem(String path) {
        return findStoreItem(NStringUtils.split(path, "/", true, true));
    }

    public NMemStoreItem storeItemMkdirs(List<String> items0) {
        NMemStoreItem last = root;
        List<String> items = new ArrayList<>(items0);
        while (!items.isEmpty()) {
            String n = items.remove(0);
            NMemStoreItem found = last.child(n);
            if (found == null) {
                found = new NMemStoreItem(true, n, last,this);
            }
            last = found;
        }
        return last;
    }

    public NMemStoreItem findStoreItem(List<String> items0) {
        NMemStoreItem last = root;
        List<String> items = new ArrayList<>(items0);
        while (!items.isEmpty()) {
            String n = items.remove(0);
            NMemStoreItem r = last.child(n);
            if (r == null) {
                return null;
            }
            last = r;
        }
        return last;
    }

}
