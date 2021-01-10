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
package net.thevpc.nuts;

import java.io.Serializable;

/**
 * Generic Add options
 *
 * author vpc
 * @since 0.5.7
 * @category Commands
 */
public class NutsUpdateOptions implements Serializable {
    private static final long serialVersionUID = 1;

    /**
     * current session
     */
    private NutsSession session;

    public NutsUpdateOptions() {
    }

    public NutsUpdateOptions(NutsSession session) {
        this.session = session;
    }

    /**
     * current session
     *
     * @return current session
     */
    public NutsSession getSession() {
        return session;
    }

    /**
     * update current session
     *
     * @param session session
     * @return {@code this} instance
     */
    public NutsUpdateOptions setSession(NutsSession session) {
        this.session = session;
        return this;
    }

}
