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

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

public enum NIsolationLevel implements NEnum {

    /**
     * The User Workspace Isolation Level runs a workspace in a dedicated location under the regular user's privileges.
     * Only the current user has access to the workspace configuration.
     */
    USER,

    /**
     * The System Workspace Isolation Level is used to share a workspace among all users on the same machine.
     */
    SYSTEM,

    /**
     * The Confined Workspace Isolation Level runs a workspace in a specific location as a regular user,
     * without interfering with other workspaces belonging to the same user.
     * Global shortcuts, .bashrc, and main workspace configurations remain unmodified.
     * However, it may still access user-level resources (e.g., Maven repositories) and system-level workspaces.
     */
    CONFINED,

    /**
     * The Sandbox Isolation Level runs the workspace in a temporary location,
     * spawning a fresh instance each time. It does not interfere with other workspaces,
     * and does not modify global shortcuts, `.bashrc`, or other user environment configurations.
     * However, it may still access user-level resources (e.g., Maven repositories) and system-level workspaces.
     */
    SANDBOX,

    /**
     * The Memory Isolation Level runs the workspace entirely in memory, without storing its configuration on disk.
     * Similar to the Sandbox level, but avoids any disk allocation for configuration.
     * Temporary files may still be used for downloading resources.
     * User-level resources (e.g., Maven repositories) and system-level workspaces remain accessible.
     */
    MEMORY
    ;
    private final String id;

    NIsolationLevel() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NIsolationLevel> parse(String value) {
        return NEnumUtils.parseEnum(value, NIsolationLevel.class);
    }

    @Override
    public String id() {
        return id;
    }

}
