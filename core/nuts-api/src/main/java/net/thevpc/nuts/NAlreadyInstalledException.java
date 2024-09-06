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
 * Thrown to indicate that the artifact is already installed and should not be
 * reinstalled Created by vpc on 1/15/17.
 *
 * @author thevpc
 * @app.category Exceptions
 * @since 0.5.4
 */
public class NAlreadyInstalledException extends NInstallationException {


    /**
     * Custom Constructor
     *
     * @param session workspace
     * @param id      nuts id
     */
    public NAlreadyInstalledException(NSession session, NId id) {
        this(session, id, null, null);
    }

    /**
     * Custom Constructor
     *
     * @param session session
     * @param id      nuts id
     * @param msg     message
     * @param cause   exception
     */
    public NAlreadyInstalledException(NSession session, NId id, NMsg msg, Exception cause) {
        super(session, id, msg == null ? NMsg.ofC("already installed %s", (id == null ? "<null>" : id)) : msg, cause);
    }

}
