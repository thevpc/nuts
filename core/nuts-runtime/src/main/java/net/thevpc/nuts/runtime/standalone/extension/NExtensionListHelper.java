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
package net.thevpc.nuts.runtime.standalone.extension;

import java.util.*;
import java.util.stream.Collectors;

import net.thevpc.nuts.NDependency;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceConfigBoot;

/**
 *
 * @author thevpc
 */
public class NExtensionListHelper {

    private List<NWorkspaceConfigBoot.ExtensionConfig> initial = new ArrayList<>();
    private List<NWorkspaceConfigBoot.ExtensionConfig> list = new ArrayList<>();
    private NId apiId;

    public NExtensionListHelper(NId apiId, List<NWorkspaceConfigBoot.ExtensionConfig> old) {
        this.apiId=apiId;
        if (old != null) {
            for (NWorkspaceConfigBoot.ExtensionConfig a : old) {
                if (a != null) {
                    this.list.add(a);
                }
            }
        }
    }

    public NExtensionListHelper save() {
        initial = new ArrayList<>(list);
        compress();
        return this;
    }

    public boolean hasChanged() {
        return !initial.equals(list);
    }

    public NExtensionListHelper copy() {
        return new NExtensionListHelper(apiId,list);
    }

    public NExtensionListHelper compress() {
        LinkedHashMap<String, NWorkspaceConfigBoot.ExtensionConfig> m = new LinkedHashMap<>();
        for (NWorkspaceConfigBoot.ExtensionConfig id : list) {
            m.put(id.getId().getShortName(),
                    new NWorkspaceConfigBoot.ExtensionConfig(id.getId().getLongId(), id.getDependencies(), id.isEnabled())
            );
        }
        list.clear();
        list.addAll(m.values());
        return this;
    }

    public boolean add(NId id, List<NDependency> dependencies) {
        String dependenciesString= dependencies.stream()
                .map(Object::toString).collect(Collectors.joining(";"));
        for (int i = 0; i < list.size(); i++) {
            NWorkspaceConfigBoot.ExtensionConfig a = list.get(i);
            if (a.getId().getShortName().equals(id.getShortName())) {
                NWorkspaceConfigBoot.ExtensionConfig o=list.get(i);
                NWorkspaceConfigBoot.ExtensionConfig z = new NWorkspaceConfigBoot.ExtensionConfig(id, dependenciesString, true);
                if(!Objects.equals(o,z)){
                    list.set(i, z);
                    return true;
                }
            }
        }
        NWorkspaceConfigBoot.ExtensionConfig z = new NWorkspaceConfigBoot.ExtensionConfig(id, dependenciesString, true);
        list.add(z);
        return true;
    }

    public boolean remove(NId id) {
        for (int i = 0; i < list.size(); i++) {
            NWorkspaceConfigBoot.ExtensionConfig a = list.get(i);
            if (a.getId().getShortName().equals(id.getShortName())) {
                list.remove(i);
                return true;
            }
        }
        return false;
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
        final NExtensionListHelper other = (NExtensionListHelper) obj;
        if (!Objects.equals(this.list, other.list)) {
            return false;
        }
        return true;
    }

    public List<NId> getIds() {
        List<NId> ids = new ArrayList<>();
        for (NWorkspaceConfigBoot.ExtensionConfig i : list) {
            ids.add(i.getId());
        }
        return ids;
    }

    public List<NWorkspaceConfigBoot.ExtensionConfig> getConfs() {
        List<NWorkspaceConfigBoot.ExtensionConfig> copy = new ArrayList<>();
        for (NWorkspaceConfigBoot.ExtensionConfig i : list) {
            copy.add(new NWorkspaceConfigBoot.ExtensionConfig(i.getId(), i.getDependencies(), i.isEnabled()));
        }
        return copy;
    }

}
