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
package net.thevpc.nuts.runtime.standalone.event;

import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.NWorkspaceEvent;

/**
 *
 * @author thevpc
 * @since 0.5.4
 */
public class DefaultNWorkspaceEvent implements NWorkspaceEvent {

    private final NSession session;
    private final NRepository repository;
    private final String propertyName;
    private final Object propertyOldValue;
    private final Object propertyValue;

    public DefaultNWorkspaceEvent(NSession session, NRepository repository, String propertyName, Object propertyOldValue, Object propertyValue) {
        this.session = session;
        this.repository = repository;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.propertyOldValue = propertyOldValue;
    }

    @Override
    public NSession getSession() {
        return session;
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

    @Override
    public NWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    @Override
    public NRepository getRepository() {
        return repository;
    }
}
