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
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
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
package net.thevpc.nuts.runtime;

/**
 *
 * @author vpc
 */
public class CoreNutsConstants {
     public static final class Versions {

        private Versions() {
        }
        public static final String CHECKED_OUT_EXTENSION = "-CHECKED-OUT";
    }

    public static final class Files {
        public static final String DOT_FILES = ".files";
        public static final String DOT_FOLDERS = ".folder";
        public static final String WORKSPACE_SECURITY_CONFIG_FILE_NAME = "nuts-security-config.json";
        public static final String WORKSPACE_MAIN_CONFIG_FILE_NAME = "nuts-main-config.json";
    }

    /**
     * valid values for Query parameter "face"
     */
    public static final class QueryFaces {
        public static final String CATALOG = "catalog";
    }
}
