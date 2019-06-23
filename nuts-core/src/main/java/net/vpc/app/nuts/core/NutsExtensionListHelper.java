/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import net.vpc.app.nuts.NutsId;

/**
 *
 * @author vpc
 */
public class NutsExtensionListHelper {
    
    private List<NutsId> initial = new ArrayList<>();
    private List<NutsId> list = new ArrayList<>();

    public NutsExtensionListHelper(List<NutsId> old) {
        this.list.addAll(old);
    }

    public NutsExtensionListHelper save() {
        initial=new ArrayList<>(list);
        compress();
        return this;
    }
    
    public boolean hasChanged() {
        return !initial.equals(list);
    }
    
    public NutsExtensionListHelper copy() {
        return new NutsExtensionListHelper(list);
    }

    public NutsExtensionListHelper compress() {
        LinkedHashMap<String, NutsId> m = new LinkedHashMap<>();
        for (NutsId id : list) {
            m.put(id.getSimpleName(), id.getLongNameId());
        }
        list.clear();
        list.addAll(m.values());
        return this;
    }

    public NutsExtensionListHelper add(NutsId id) {
        for (int i = 0; i < list.size(); i++) {
            NutsId a = list.get(i);
            if (a.getSimpleName().equals(id.getSimpleName())) {
                list.set(i, id);
                return this;
            }
        }
        return this;
    }

    public NutsExtensionListHelper remove(NutsId id) {
        for (int i = 0; i < list.size(); i++) {
            NutsId a = list.get(i);
            if (a.getSimpleName().equals(id.getSimpleName())) {
                list.remove(i);
                return this;
            }
        }
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.list);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutsExtensionListHelper other = (NutsExtensionListHelper) obj;
        if (!Objects.equals(this.list, other.list)) {
            return false;
        }
        return true;
    }

    public List<NutsId> getIds() {
        return new ArrayList<>(list);
    }
    
}
