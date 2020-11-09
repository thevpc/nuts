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

import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsUpdateEvent;
import net.thevpc.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 * @since 0.5.3
 */
public class DefaultNutsUpdateEvent implements NutsUpdateEvent {

    private final NutsDefinition oldDefinition;
    private final NutsDefinition definition;
    private final NutsSession session;
    private final boolean force;

    public DefaultNutsUpdateEvent(NutsDefinition oldDefinition,NutsDefinition definition, NutsSession session, boolean force) {
        this.oldDefinition = oldDefinition;
        this.definition = definition;
        this.session = session;
        this.force = force;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    @Override
    public NutsDefinition getNewValue() {
        return definition;
    }

    @Override
    public NutsDefinition getOldValue() {
        return oldDefinition;
    }

    @Override
    public boolean isForce() {
        return force;
    }

}
