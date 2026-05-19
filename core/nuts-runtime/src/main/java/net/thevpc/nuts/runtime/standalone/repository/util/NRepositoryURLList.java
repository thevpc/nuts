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
package net.thevpc.nuts.runtime.standalone.repository.util;

import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.List;

public class NRepositoryURLList {
    private final List<NRepositorySpec> all = new ArrayList<>();

    public NRepositoryURLList(NRepositorySpec[] all) {
        addAll(all);
    }

    public NRepositoryURLList() {
    }

    public NRepositorySpec[] toArray() {
        return all.toArray(new NRepositorySpec[0]);
    }

    public boolean containsName(String name) {
        return indexOfName(name, 0) >= 0;
    }

    public boolean containsURL(String url) {
        return indexOfURL(url, 0) >= 0;
    }

    public boolean containsSelection(NRepositorySpec s) {
        return indexOf(s, 0) >= 0;
    }

    public int indexOfName(String name, int offset) {
        String trimmedName = NStringUtils.trim(name);
        for (int i = offset; i < all.size(); i++) {
            if (trimmedName.equals(NStringUtils.trim(all.get(i).name()))) {
                return i;
            }
        }
        return -1;
    }
    public int indexOfNames(String[] names, int offset) {
        for (int i = offset; i < all.size(); i++) {
            NRepositorySpec loc = all.get(i);
            String trimmedLocName = NStringUtils.trim(loc.name());
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
            if (trimmedName.equals(NStringUtils.trim(all.get(i).sourceLocation().getPath()))) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(NRepositorySpec other, int offset) {
        if (other == null) {
            return -1;
        }
        for (int i = offset; i < all.size(); i++) {
            NRepositorySpec o = all.get(i);
            if (NStringUtils.trim(other.name()).equals(NStringUtils.trim(o.name()))) {
                if (NStringUtils.trim(other.sourceLocation().getPath()).equals(NStringUtils.trim(o.sourceLocation().getPath()))) {
                    return i;
                }
            }
        }
        return -1;
    }

    public NRepositoryURLList addAll(NRepositorySpec[] all) {
        if (all != null) {
            for (NRepositorySpec a : all) {
                add(a);
            }
        }
        return this;
    }

    public NRepositoryURLList add(NRepositorySpec a) {
        if (a != null) {
            String n = NStringUtils.trim(a.name());
            if (n.isEmpty()) {
                if (indexOf(a, 0) < 0) {
                    all.add(a);
                }
            } else {
                if (indexOfName(a.name(), 0) < 0) {
                    all.add(a);
                }
            }
        }
        return this;
    }

    public NRepositorySpec removeAt(int i) {
        return all.remove(i);
    }

    public NRepositoryURLList clear() {
        all.clear();
        return this;
    }
}
