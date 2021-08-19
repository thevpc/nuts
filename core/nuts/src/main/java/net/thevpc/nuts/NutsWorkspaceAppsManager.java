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

/**
 * @app.category Application
 */
public interface NutsWorkspaceAppsManager {

    /**
     * create a new instance of {@link NutsApplicationContext}
     *
     * @param session session context session. If null will consider {@code getSession()} that should not be null as well.
     * @param args            application arguments
     * @param startTimeMillis application start time
     * @param appClass        application class
     * @param storeId         application store id or null
     * @return new instance of {@link NutsApplicationContext}
     */
    NutsApplicationContext createApplicationContext(NutsSession session, String[] args, long startTimeMillis, Class appClass, String storeId);

    /**
     * create a new exec entries action
     * @return exec entries action
     */
    NutsExecutionEntryAction execEntries();

    NutsSession getSession();

    NutsWorkspaceAppsManager setSession(NutsSession session);
}
