/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.reserved.NScopedWorkspace;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NRunnable;

import java.io.Closeable;

/**
 * Created by vpc on 1/5/17.
 *
 * @author thevpc
 * @app.category Base
 * @since 0.5.4
 */
public interface NWorkspace extends NWorkspaceBase, NComponent, Closeable {
    static NWorkspace get() {
        return of().get();
    }

    static NOptional<NWorkspace> of() {
        return NScopedWorkspace.currentWorkspace();
    }

    static void run(NRunnable runnable) {
        NScopedWorkspace.runWith(runnable);
    }

    static <T> T call(NCallable<T> callable) {
        return NScopedWorkspace.callWith(callable);
    }

    void setSharedInstance();

    boolean isSharedInstance();

    void runWith(NRunnable runnable);

    <T> T callWith(NCallable<T> callable);

    /**
     * Workspace identifier, most likely to be unique cross machines
     *
     * @return uuid
     */
    String getUuid();

    /**
     * Workspace name
     *
     * @return name
     */
    String getName();

    String getHashName();

    NVersion getApiVersion();

    NId getApiId();

    NId getAppId();

    NId getRuntimeId();

    NPath getLocation();

    /// ////////////////// create new session
    NSession createSession();

    NSession currentSession();

    NExtensions extensions();

    void close();
}
