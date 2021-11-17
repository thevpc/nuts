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
package net.thevpc.nuts.runtime.standalone.repository.config;

import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.NutsRepositoryEvent;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

/**
 *
 * @author thevpc
 * @since 0.5.4
 */
public class DefaultNutsRepositoryEvent implements NutsRepositoryEvent {

    private final NutsSession session;
    private final NutsRepository parent;
    private final NutsRepository repository;
    private final String propertyName;
    private final Object propertyOldValue;
    private final Object propertyValue;

    public DefaultNutsRepositoryEvent(NutsSession session, NutsRepository parent, NutsRepository repository, String propertyName, Object propertyOldValue, Object propertyValue) {
        this.session = session;
        this.parent = parent;
        this.repository = repository;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.propertyOldValue = propertyOldValue;
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
    public NutsRepository getParent() {
        return parent;
    }

    @Override
    public NutsRepository getRepository() {
        return repository;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public Object getPropertyOldValue() {
        return propertyOldValue;
    }

    @Override
    public Object getPropertyValue() {
        return propertyValue;
    }
}
