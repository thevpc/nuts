package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsUtilStrings;

import java.util.ArrayList;
import java.util.List;

public class PrivateNutsRepositorySelectorList {

    private final List<PrivateNutsRepositorySelector> all = new ArrayList<>();

    public PrivateNutsRepositorySelectorList() {
    }

    public PrivateNutsRepositorySelectorList(PrivateNutsRepositorySelector[] a) {
        for (PrivateNutsRepositorySelector repoDefString : a) {
            if (repoDefString != null) {
                all.add(repoDefString);
            }
        }
    }

    public PrivateNutsRepositorySelector[] toArray() {
        return all.toArray(new PrivateNutsRepositorySelector[0]);
    }

    public PrivateNutsRepositorySelectorList join(PrivateNutsRepositorySelectorList other) {
        if (other == null || other.all.isEmpty()) {
            return this;
        }
        List<PrivateNutsRepositorySelector> all2 = new ArrayList<>();
        all2.addAll(all);
        all2.addAll(other.all);
        return new PrivateNutsRepositorySelectorList(all2.toArray(new PrivateNutsRepositorySelector[0]));
    }

    public PrivateNutsRepositorySelection[] resolveSelectors(PrivateNutsRepositorySelection[] other) {
        PrivateNutsRepositorySelectionList existing2 = new PrivateNutsRepositorySelectionList();
        if (other != null) {
            for (PrivateNutsRepositorySelection s : other) {
                if (s != null) {
                    s = PrivateNutsRepositorySelector.validateSelection(s);
                    if (s != null) {
                        existing2.add(s);
                    }
                }
            }
        }
        List<PrivateNutsRepositorySelection> all2 = new ArrayList<>();
        for (PrivateNutsRepositorySelector r : all) {
            if (r.getOp() != PrivateNutsRepositorySelectorOp.EXCLUDE) {
//                    boolean accept = true;
                if (!NutsUtilStrings.isBlank(r.getName())) {
                    String u2 = r.getUrl();
                    int i = existing2.indexOfName(r.getName(), 0);
                    if (i >= 0) {
                        PrivateNutsRepositorySelection ss = existing2.removeAt(i);
                        if (ss != null && u2 == null) {
                            u2 = ss.getUrl();
                        }
                    }
                    all2.add(new PrivateNutsRepositorySelection(r.getName(), u2));
                } else if (r.getOp() == PrivateNutsRepositorySelectorOp.EXACT) {
                    all2.add(new PrivateNutsRepositorySelection(r.getName(), r.getUrl()));
                }
            }
        }
        for (PrivateNutsRepositorySelection e : existing2.toArray()) {
            if (acceptExisting(e)) {
                all2.add(e);
            }
        }
        return all2.toArray(new PrivateNutsRepositorySelection[0]);
    }

    public boolean acceptExisting(PrivateNutsRepositorySelection ss) {
        String n = ss.getName();
        String url = ss.getUrl();
        boolean includeOthers = true;
        for (PrivateNutsRepositorySelector s : all) {
            if (s.matches(n, url)) {
                switch (s.getOp()) {
                    case EXACT:
                        return true;
                    case INCLUDE:
                        return true;
                    case EXCLUDE:
                        return false;
                }
            }
            if (s.getOp() == PrivateNutsRepositorySelectorOp.EXACT) {
                includeOthers = false;
            }
        }
        return includeOthers;
    }

}
