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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.List;

public class NRepositoryURLList {
    private final List<NRepositoryLocation> all = new ArrayList<>();

    public NRepositoryURLList(NRepositoryLocation[] all) {
        addAll(all);
    }

    public NRepositoryURLList() {
    }

    public NRepositoryLocation[] toArray() {
        return all.toArray(new NRepositoryLocation[0]);
    }

    public boolean containsName(String name) {
        return indexOfName(name, 0) >= 0;
    }

    public boolean containsURL(String url) {
        return indexOfURL(url, 0) >= 0;
    }

    public boolean containsSelection(NRepositoryLocation s) {
        return indexOf(s, 0) >= 0;
    }

    public int indexOfName(String name, int offset) {
        String trimmedName = NStringUtils.trim(name);
        for (int i = offset; i < all.size(); i++) {
            if (trimmedName.equals(NStringUtils.trim(all.get(i).getName()))) {
                return i;
            }
        }
        return -1;
    }
    public int indexOfNames(String[] names, int offset) {
        for (int i = offset; i < all.size(); i++) {
            NRepositoryLocation loc = all.get(i);
            String trimmedLocName = NStringUtils.trim(loc.getName());
            for (String name : names) {
                String trimmedName = NStringUtils.trim(name);
                if (trimmedName.equals(trimmedLocName)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int indexOfURL(String url, int offset) {
        String trimmedName = NStringUtils.trim(url);
        for (int i = offset; i < all.size(); i++) {
            if (trimmedName.equals(NStringUtils.trim(all.get(i).getPath()))) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(NRepositoryLocation other, int offset) {
        if (other == null) {
            return -1;
        }
        for (int i = offset; i < all.size(); i++) {
            NRepositoryLocation o = all.get(i);
            if (NStringUtils.trim(other.getName()).equals(NStringUtils.trim(o.getName()))) {
                if (NStringUtils.trim(other.getPath()).equals(NStringUtils.trim(o.getPath()))) {
                    return i;
                }
            }
        }
        return -1;
    }

    public NRepositoryURLList addAll(NRepositoryLocation[] all) {
        if (all != null) {
            for (NRepositoryLocation a : all) {
                add(a);
            }
        }
        return this;
    }

    public NRepositoryURLList add(NRepositoryLocation a) {
        if (a != null) {
            String n = NStringUtils.trim(a.getName());
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

    public NRepositoryLocation removeAt(int i) {
        return all.remove(i);
    }

    public NRepositoryURLList clear() {
        all.clear();
        return this;
    }
}
