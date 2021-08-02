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

import java.io.File;
import java.nio.file.Path;

/**
 * I/O Action that help monitored delete.
 *
 * @author thevpc
 * @since 0.5.8
 * @category Input Output
 */
public interface NutsIODeleteAction {
    /**
     * return target to delete
     * @return return target to delete
     */
    Object getTarget();

    /**
     * update target to delete
     * @param target target
     * @return {@code this} instance
     */
    NutsIODeleteAction setTarget(Object target);

    /**
     * update target to delete
     * @param target target
     * @return {@code this} instance
     */
    NutsIODeleteAction setTarget(File target);

    /**
     * update target to delete
     * @param target target
     * @return {@code this} instance
     */
    NutsIODeleteAction setTarget(Path target);

    /**
     * current session
     * @return current session
     */
    NutsSession getSession();

    /**
     * update session
     * @param session session
     * @return {@code this} instance
     */
    NutsIODeleteAction setSession(NutsSession session);

    /**
     * run delete action and return {@code this}
     * @return {@code this} instance
     */
    NutsIODeleteAction run();

    /**
     * return true if fail fast.
     * When fail fast flag is armed, the first
     * error that occurs will throw an {@link java.io.UncheckedIOException}
     * @return true if fail fast
     */
    boolean isFailFast();

    /**
     * update fail fast flag
     * @param failFast value
     * @return {@code this} instance
     */
    NutsIODeleteAction setFailFast(boolean failFast);

}
