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
 *
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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NMsg;

/**
 * Base exception for Repository related exceptions
 *
 * @app.category Exceptions
 * @since 0.5.4
 */
public abstract class NRepositoryException extends NException {

    private final String repository;

    /**
     * Constructs a new NutsRepositoryException exception
     *
     * @param session    workspace
     * @param repository repository
     * @param message    message
     * @param ex         exception
     */
    public NRepositoryException(NSession session, String repository, NMsg message, Throwable ex) {
        super(session,
                message == null ? NMsg.ofC("repository %s has encountered problem", (repository == null ? "<null>" : repository)) : message, ex);
        this.repository = repository;
    }

    /**
     * the repository of this exception
     *
     * @return the repository of this exception
     */
    public String getRepository() {
        return repository;
    }
}
