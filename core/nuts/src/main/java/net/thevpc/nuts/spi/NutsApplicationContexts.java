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


import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.boot.NutsApiUtils;

/**
 * Application context that store all relevant information about application
 * execution mode, workspace, etc.
 *
 * @author thevpc
 * @app.category Application
 * @since 0.5.5
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public interface NutsApplicationContexts extends NutsComponent {
    static NutsApplicationContexts of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsApplicationContexts.class, true, null);
    }

    /**
     * create a new instance of {@link NutsApplicationContext}
     *
     * @param args            application arguments
     * @param startTimeMillis application start time
     * @param appClass        application class
     * @param storeId         application store id or null
     * @return new instance of {@link NutsApplicationContext}
     */
    NutsApplicationContext create(String[] args, long startTimeMillis, Class appClass, String storeId);
}
