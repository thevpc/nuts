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
 * Application Life Cycle interface define methods to be overridden to
 * perform specific business for each of the predefined application execution 
 * modes {@link NutsApplicationMode}.
 *
 * @author thevpc
 * @since 0.5.5
 * %category Application
 */
public interface NutsApplicationLifeCycle {

    /**
     * this method should be implemented to perform specific business when
     * application is running (default mode)
     *
     * @param applicationContext context
     */
    default void onRunApplication(NutsApplicationContext applicationContext) {

    }

    /**
     * this method should be implemented to perform specific business when
     * application is installed.
     *
     * @param applicationContext context
     */
    default void onInstallApplication(NutsApplicationContext applicationContext) {

    }

    /**
     * this method should be implemented to perform specific business when
     * application is updated.
     *
     * @param applicationContext context
     */
    default void onUpdateApplication(NutsApplicationContext applicationContext) {

    }

    /**
     * this method should be implemented to perform specific business when
     * application is un-installed.
     *
     * @param applicationContext context
     */
    default void onUninstallApplication(NutsApplicationContext applicationContext) {

    }

    /**
     * this method should be implemented to create specific ApplicationContext
     * implementation or return null to use default one.
     *
     * @param ws workspace
     * @param args application arguments
     * @param startTimeMillis start time in milliseconds
     * @return new NutsApplicationContext instance or null
     */
    default NutsApplicationContext createApplicationContext(NutsWorkspace ws, String[] args, long startTimeMillis) {
        return null;
    }
}
