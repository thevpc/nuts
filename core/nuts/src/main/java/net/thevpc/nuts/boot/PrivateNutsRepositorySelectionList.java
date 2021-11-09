/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
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
