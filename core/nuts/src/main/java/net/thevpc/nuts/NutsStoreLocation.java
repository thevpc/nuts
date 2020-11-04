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
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts;

/**
 *
 * @author vpc
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
     * Contains local libraries/nuts/packages.
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
