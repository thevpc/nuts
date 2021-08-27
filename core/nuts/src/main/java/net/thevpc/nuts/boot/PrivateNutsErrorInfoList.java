package net.thevpc.nuts.boot;

import java.util.ArrayList;
import java.util.List;

class PrivateNutsErrorInfoList {
    private final List<PrivateNutsErrorInfo> all = new ArrayList<>();

    public void removeErrorsFor(String nutsId) {
        all.removeIf(x -> x.getNutsId().equals(nutsId));
    }

    public void add(PrivateNutsErrorInfo e) {
        all.add(e);
    }

    public List<PrivateNutsErrorInfo> list() {
        return all;
    }
}
