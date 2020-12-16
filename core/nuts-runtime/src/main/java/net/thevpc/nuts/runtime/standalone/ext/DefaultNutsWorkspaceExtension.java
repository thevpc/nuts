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
package net.thevpc.nuts.runtime.standalone.ext;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsWorkspaceExtension;
import net.thevpc.nuts.runtime.standalone.util.common.ListMap;

/**
 * Created by vpc on 1/15/17.
 */
public class DefaultNutsWorkspaceExtension implements NutsWorkspaceExtension {

    private NutsId id;
    private NutsId wiredId;
    private ClassLoader classLoader;
    private ListMap<String, String> wiredComponents = new ListMap<>();

    public DefaultNutsWorkspaceExtension(NutsId id, NutsId wiredId, ClassLoader classLoader) {
        this.id = id;
        this.wiredId = wiredId;
        this.classLoader = classLoader;
    }

    public NutsId getWiredId() {
        return wiredId;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public NutsId getId() {
        return id;
    }

    public ListMap<String, String> getWiredComponents() {
        return wiredComponents;
    }
}
