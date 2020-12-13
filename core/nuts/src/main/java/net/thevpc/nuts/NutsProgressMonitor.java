/**
 * ====================================================================
 *            vpc-common-io : common reusable library for
 *                          input/output
 *
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
 * Monitor handles events from copy, compress and delete actions
 * @author thevpc
 * @since 0.5.8
 * %category Base
 */
public interface NutsProgressMonitor {

    /**
     * called when the action starts
     * @param event event
     */
    void onStart(NutsProgressEvent event);

    /**
     * called when the action terminates
     * @param event event
     */
    void onComplete(NutsProgressEvent event);

    /**
     * called when the action does a step forward and return
     * true if the progress was handled of false otherwise.
     *
     * @param event event
     * @return true if the progress was handled. In that case, a
     * mark point is registered to compute partial time and speed.
     */
    boolean onProgress(NutsProgressEvent event);

}
