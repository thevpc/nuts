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

import net.thevpc.nuts.boot.reserved.util.NBootStringUtils;

import java.util.ArrayList;
import java.util.List;

public class NBootRepositoryLocationList {
    private final List<NBootRepositoryLocation> all = new ArrayList<>();

    public NBootRepositoryLocationList(NBootRepositoryLocation[] all) {
        addAll(all);
    }

    public NBootRepositoryLocationList() {
    }

    public NBootRepositoryLocation[] toArray() {
        return all.toArray(new NBootRepositoryLocation[0]);
    }

    public boolean containsName(String name) {
        return indexOfName(name, 0) >= 0;
    }

    public boolean containsURL(String url) {
        return indexOfURL(url, 0) >= 0;
    }

    public boolean containsSelection(NBootRepositoryLocation s) {
        return indexOf(s, 0) >= 0;
    }

    public int indexOfName(String name, int offset) {
        String trimmedName = NBootStringUtils.trim(name);
        for (int i = offset; i < all.size(); i++) {
            if (trimmedName.equals(NBootStringUtils.trim(all.get(i).getName()))) {
                return i;
            }
        }
        return -1;
    }
    public int indexOfNames(String[] names, int offset) {
        for (int i = offset; i < all.size(); i++) {
            NBootRepositoryLocation loc = all.get(i);
            String trimmedLocName = NBootStringUtils.trim(loc.getName());
            for (String name : names) {
                String trimmedName = NBootStringUtils.trim(name);
                if (trimmedName.equals(trimmedLocName)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int indexOfURL(String url, int offset) {
        String trimmedName = NBootStringUtils.trim(url);
        for (int i = offset; i < all.size(); i++) {
            if (trimmedName.equals(NBootStringUtils.trim(all.get(i).getPath()))) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(NBootRepositoryLocation other, int offset) {
        if (other == null) {
            return -1;
        }
        for (int i = offset; i < all.size(); i++) {
            NBootRepositoryLocation o = all.get(i);
            if (NBootStringUtils.trim(other.getName()).equals(NBootStringUtils.trim(o.getName()))) {
                if (NBootStringUtils.trim(other.getPath()).equals(NBootStringUtils.trim(o.getPath()))) {
                    return i;
                }
            }
        }
        return -1;
    }

    public NBootRepositoryLocationList addAll(NBootRepositoryLocation[] all) {
        if (all != null) {
            for (NBootRepositoryLocation a : all) {
                add(a);
            }
        }
        return this;
    }

    public NBootRepositoryLocationList add(NBootRepositoryLocation a) {
        if (a != null) {
            String n = NBootStringUtils.trim(a.getName());
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

    public NBootRepositoryLocation removeAt(int i) {
        return all.remove(i);
    }

    public NBootRepositoryLocationList clear() {
        all.clear();
        return this;
    }
}
