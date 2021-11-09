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
 * <br>
 * <p>
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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsSession;

/**
 * classes that implement this class will have their method {@link #setSession(NutsSession)}
 * called upon its creation (by factory) with a non {@code null} argument to <strong>initialize</strong>.
 * They <strong>may</strong> accept a call with a {@code null}
 * argument later to <strong>dispose</strong> the instance.
 *
 * @author thevpc
 * @app.category SPI Base
 */
public interface NutsSessionAware {

    /**
     * initialize or dispose the instance.
     * when session is not null, the instance should initialize it values
     * accordingly.
     * when session is null, the instance should dispose resources.
     *
     * @param session session reference or null
     */
    void setSession(NutsSession session);

}
