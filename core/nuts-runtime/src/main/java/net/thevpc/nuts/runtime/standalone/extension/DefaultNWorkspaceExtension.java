/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
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

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NWorkspaceExtension;
import net.thevpc.nuts.runtime.standalone.util.collections.ListMap;
import net.thevpc.nuts.spi.NExtensionLifeCycle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/15/17.
 */
public class DefaultNWorkspaceExtension implements NWorkspaceExtension {

    private NId id;
    private NId wiredId;
    private boolean enabled;
    private ClassLoader classLoader;
    private ListMap<String, String> wiredComponents = new ListMap<>();
    private List<NExtensionLifeCycle> events = new ArrayList<>();

    public DefaultNWorkspaceExtension(NId id, NId wiredId, ClassLoader classLoader) {
        this.id = id;
        this.wiredId = wiredId;
        this.classLoader = classLoader;
        this.enabled = true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public NWorkspaceExtension setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public List<NExtensionLifeCycle> getEvents() {
        return events;
    }

    public NId getId() {
        return wiredId;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public NId getConfigId() {
        return id;
    }

    public ListMap<String, String> getWiredComponents() {
        return wiredComponents;
    }
}
