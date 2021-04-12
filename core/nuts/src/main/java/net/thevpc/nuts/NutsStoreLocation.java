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
 *
 * @author thevpc
 * @since 0.5.4
 * @category Base
 */
public enum NutsStoreLocation {

    /**
     * defines the base directory relative to which user specific executable
     * files should be stored.
     * Contains (not modifiable ) binaries an executables.
     */
    APPS,
    
    /**
     * defines the base directory relative to which user specific configuration
     * files should be stored. equivalent to $XDG_CONFIG_HOME on LINUX systems.
     * Contains configuration files.
     */
    CONFIG,
    
    /**
     * defines the base directory relative to which user specific data files
     * should be stored. equivalent to $XDG_DATA_HOME on LINUX systems.
     * Contains variable/modifiable data files.
     */
    VAR,
    
    /**
     * defines the base directory relative to which user log files should be
     * stored. equivalent to $XDG_LOG_HOME on LINUX systems.
     * Contains variable log files.
     */
    LOG,
    
    /**
     * defines the base directory relative to which user temp files should be
     * stored.
     * Contains temporary files.
     */
    TEMP,
    
    /**
     * defines the base directory relative to which user specific non-essential
     * data files should be stored. equivalent to $XDG_CACHE_HOME on LINUX.
     * Contains cached files, libraries and applications that could be 
     * downloaded/re-created again when needed.
     */
    CACHE,

    /**
     * defines the base directory relative to which user binary non executable
     * files should be stored.
     * Contains local libraries/packages.
     */
    LIB,

    /**
     * defines the base directory relative to which user-specific non-essential
     * runtime files and other file objects (such as sockets, named pipes, ...)
     * should be stored. equivalent to $XDG_RUNTIME_DIR on LINUX systems.
     * Contains temporary runtime special files (like sockets on LINUX systems).
     */
    RUN;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsStoreLocation() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    /**
     * lower cased identifier.
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}
