package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.NutsId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class NutsIdListBuilder {
    private LinkedHashMap<String, NutsId> all = new LinkedHashMap<>();
    private boolean latestOnly;

    public NutsIdListBuilder(boolean latestOnly) {
        this.latestOnly = latestOnly;
    }

    public void add(NutsId n) {
        NutsId x = n.setNamespace(null).setFace(null);
        if (latestOnly) {
            NutsId b = x.setVersion("");
            String key = b.toString();
            if (!all.containsKey(key)) {
                all.put(key, n);
            }
        } else {
            String key = x.toString();
            NutsId old = all.get(key);
            if (old == null || old.getVersion().isEmpty() || old.getVersion().compareTo(x.getVersion()) < 0) {
                all.put(key, n);
            }
        }
    }

    public List<NutsId> build() {
        return new ArrayList<>(all.values());
    }
}
