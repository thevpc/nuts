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


import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NClock;

/**
 * Application context that store all relevant information about application
 * execution mode, workspace, etc.
 *
 * @author thevpc
 * @app.category Application
 * @since 0.5.5
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public interface NApplicationContexts extends NComponent {
    static NApplicationContexts of(NSession session) {
       return NExtensions.of(session).createSupported(NApplicationContexts.class);
    }

    /**
     * create a new instance of {@link NApplicationContext}
     *
     * @param args            application arguments
     * @param startTime application start time
     * @param appClass        application class
     * @param storeId         application store id or null
     * @return new instance of {@link NApplicationContext}
     */
    NApplicationContext create(String[] args, NClock startTime, Class appClass, String storeId);
}
