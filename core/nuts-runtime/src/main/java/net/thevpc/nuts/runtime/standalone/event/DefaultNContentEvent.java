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
package net.thevpc.nuts.runtime.standalone.event;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NRepositoryCmd;

/**
 *
 * @author thevpc
 * @since 0.5.3
 */
public class DefaultNContentEvent implements NContentEvent {

    private final NRepositoryCmd command;
    /**
     * stored deployment Path, this is Repository dependent
     */
    private final NPath location;
    private final NSession session;
    private final NRepository repository;

    public DefaultNContentEvent(NPath location, NRepositoryCmd command, NSession session, NRepository repository) {
        this.location = location;
        this.command = command;
        this.session = session;
        this.repository = repository;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NPath getLocation() {
        return location;
    }

//    @Override
    public NRepositoryCmd getCommand() {
        return command;
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
