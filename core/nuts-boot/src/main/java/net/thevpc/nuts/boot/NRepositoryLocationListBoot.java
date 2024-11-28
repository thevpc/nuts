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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.reserved.util.NStringUtilsBoot;

import java.util.ArrayList;
import java.util.List;

public class NRepositoryLocationListBoot {
    private final List<NRepositoryLocationBoot> all = new ArrayList<>();

    public NRepositoryLocationListBoot(NRepositoryLocationBoot[] all) {
        addAll(all);
    }

    public NRepositoryLocationListBoot() {
    }

    public NRepositoryLocationBoot[] toArray() {
        return all.toArray(new NRepositoryLocationBoot[0]);
    }

    public boolean containsName(String name) {
        return indexOfName(name, 0) >= 0;
    }

    public boolean containsURL(String url) {
        return indexOfURL(url, 0) >= 0;
    }

    public boolean containsSelection(NRepositoryLocationBoot s) {
        return indexOf(s, 0) >= 0;
    }

    public int indexOfName(String name, int offset) {
        String trimmedName = NStringUtilsBoot.trim(name);
        for (int i = offset; i < all.size(); i++) {
            if (trimmedName.equals(NStringUtilsBoot.trim(all.get(i).getName()))) {
                return i;
            }
        }
        return -1;
    }
    public int indexOfNames(String[] names, int offset) {
        for (int i = offset; i < all.size(); i++) {
            NRepositoryLocationBoot loc = all.get(i);
            String trimmedLocName = NStringUtilsBoot.trim(loc.getName());
            for (String name : names) {
                String trimmedName = NStringUtilsBoot.trim(name);
                if (trimmedName.equals(trimmedLocName)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int indexOfURL(String url, int offset) {
        String trimmedName = NStringUtilsBoot.trim(url);
        for (int i = offset; i < all.size(); i++) {
            if (trimmedName.equals(NStringUtilsBoot.trim(all.get(i).getPath()))) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(NRepositoryLocationBoot other, int offset) {
        if (other == null) {
            return -1;
        }
        for (int i = offset; i < all.size(); i++) {
            NRepositoryLocationBoot o = all.get(i);
            if (NStringUtilsBoot.trim(other.getName()).equals(NStringUtilsBoot.trim(o.getName()))) {
                if (NStringUtilsBoot.trim(other.getPath()).equals(NStringUtilsBoot.trim(o.getPath()))) {
                    return i;
                }
            }
        }
        return -1;
    }

    public NRepositoryLocationListBoot addAll(NRepositoryLocationBoot[] all) {
        if (all != null) {
            for (NRepositoryLocationBoot a : all) {
                add(a);
            }
        }
        return this;
    }

    public NRepositoryLocationListBoot add(NRepositoryLocationBoot a) {
        if (a != null) {
            String n = NStringUtilsBoot.trim(a.getName());
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
        return this;
    }

    public NRepositoryLocationBoot removeAt(int i) {
        return all.remove(i);
    }

    public NRepositoryLocationListBoot clear() {
        all.clear();
        return this;
    }
}
