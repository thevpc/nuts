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
package net.thevpc.nuts.runtime;

import java.nio.file.Path;

import net.thevpc.nuts.*;

/**
 *
 * @author thevpc
 * @since 0.5.3
 */
public class DefaultNutsContentEvent implements NutsContentEvent {

    private final NutsRepositoryCommand command;
    /**
     * stored deployment Path, this is Repository dependent
     */
    private final Path path;
    private final NutsSession session;
    private final NutsRepository repository;

    public DefaultNutsContentEvent(Path path, NutsRepositoryCommand command, NutsSession session, NutsRepository repository) {
        this.path = path;
        this.command = command;
        this.session = session;
        this.repository = repository;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public Path getPath() {
        return path;
    }

//    @Override
    public NutsRepositoryCommand getCommand() {
        return command;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    @Override
    public NutsRepository getRepository() {
        return repository;
    }
}
