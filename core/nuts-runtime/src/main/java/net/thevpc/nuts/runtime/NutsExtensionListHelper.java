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
 * <br>
 *
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
package net.thevpc.nuts.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.runtime.main.config.NutsWorkspaceConfigBoot;

/**
 *
 * @author vpc
 */
public class NutsExtensionListHelper {

    private List<NutsWorkspaceConfigBoot.ExtensionConfig> initial = new ArrayList<>();
    private List<NutsWorkspaceConfigBoot.ExtensionConfig> list = new ArrayList<>();

    public NutsExtensionListHelper(List<NutsWorkspaceConfigBoot.ExtensionConfig> old) {
        if (old != null) {
            for (NutsWorkspaceConfigBoot.ExtensionConfig a : old) {
                if (a != null) {
                    this.list.add(a);
                }
            }
        }
    }

    public NutsExtensionListHelper save() {
        initial = new ArrayList<>(list);
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
        LinkedHashMap<String, NutsWorkspaceConfigBoot.ExtensionConfig> m = new LinkedHashMap<>();
        for (NutsWorkspaceConfigBoot.ExtensionConfig id : list) {
            m.put(id.getId().getShortName(),
                    new NutsWorkspaceConfigBoot.ExtensionConfig(id.getId().getLongNameId(), id.isEnabled())
            );
        }
        list.clear();
        list.addAll(m.values());
        return this;
    }

    public NutsExtensionListHelper add(NutsId id) {
        for (int i = 0; i < list.size(); i++) {
            NutsWorkspaceConfigBoot.ExtensionConfig a = list.get(i);
            if (a.getId().getShortName().equals(id.getShortName())) {
                list.set(i, new NutsWorkspaceConfigBoot.ExtensionConfig(id,true));
                return this;
            }
        }
        return this;
    }

    public NutsExtensionListHelper remove(NutsId id) {
        for (int i = 0; i < list.size(); i++) {
            NutsWorkspaceConfigBoot.ExtensionConfig a = list.get(i);
            if (a.getId().getShortName().equals(id.getShortName())) {
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
        List<NutsId> ids=new ArrayList<>();
        for (NutsWorkspaceConfigBoot.ExtensionConfig i : list) {
            ids.add(i.getId());
        }
        return ids;
    }

    public List<NutsWorkspaceConfigBoot.ExtensionConfig> getConfs() {
        List<NutsWorkspaceConfigBoot.ExtensionConfig> copy=new ArrayList<>();
        for (NutsWorkspaceConfigBoot.ExtensionConfig i : list) {
            copy.add(new NutsWorkspaceConfigBoot.ExtensionConfig(i.getId(),i.isEnabled()));
        }
        return copy;
    }

}
