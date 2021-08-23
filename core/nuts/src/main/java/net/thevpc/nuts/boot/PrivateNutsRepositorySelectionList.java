package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsUtilStrings;

import java.util.ArrayList;
import java.util.List;

public class PrivateNutsRepositorySelectionList {
    private final List<PrivateNutsRepositorySelection> all = new ArrayList<>();

    public PrivateNutsRepositorySelectionList(PrivateNutsRepositorySelection[] all) {
        addAll(all);
    }

    public PrivateNutsRepositorySelectionList() {
    }

    public PrivateNutsRepositorySelection[] toArray() {
        return all.toArray(new PrivateNutsRepositorySelection[0]);
    }

    public boolean containsName(String name) {
        return indexOfName(name, 0) >= 0;
    }

    public boolean containsURL(String url) {
        return indexOfURL(url, 0) >= 0;
    }

    public boolean containsSelection(PrivateNutsRepositorySelection s) {
        return indexOf(s, 0) >= 0;
    }

    public int indexOfName(String name, int offset) {
        String trimmedName = NutsUtilStrings.trim(name);
        for (int i = offset; i < all.size(); i++) {
            if (trimmedName.equals(NutsUtilStrings.trim(all.get(i).getName()))) {
                return i;
            }
        }
        return -1;
    }

    public int indexOfURL(String url, int offset) {
        String trimmedName = NutsUtilStrings.trim(url);
        for (int i = offset; i < all.size(); i++) {
            if (trimmedName.equals(NutsUtilStrings.trim(all.get(i).getUrl()))) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(PrivateNutsRepositorySelection other, int offset) {
        if (other == null) {
            return -1;
        }
        for (int i = offset; i < all.size(); i++) {
            PrivateNutsRepositorySelection o = all.get(i);
            if (NutsUtilStrings.trim(other.getName()).equals(NutsUtilStrings.trim(o.getName()))) {
                if (NutsUtilStrings.trim(other.getUrl()).equals(NutsUtilStrings.trim(o.getUrl()))) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void addAll(PrivateNutsRepositorySelection[] all) {
        if (all != null) {
            for (PrivateNutsRepositorySelection a : all) {
                add(a);
            }
        }
    }

    public void add(PrivateNutsRepositorySelection a) {
        if (a != null) {
            String n = NutsUtilStrings.trim(a.getName());
            if (n.isEmpty()) {
                if (indexOf(a, 0) < 0) {
                    all.add(a);
                }
            } else {
                if (indexOfName(a.getName(), 0) < 0) {
                    all.add(a);
                }
            }
        }
    }

    public PrivateNutsRepositorySelection removeAt(int i) {
        return all.remove(i);
    }
}
