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
package net.thevpc.nuts.runtime.standalone.util;

/**
 *
 * @author thevpc
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

        /**
         * workspace boot config file name for a particular nuts-extension version
         */
        public static final String WORKSPACE_RUNTIME_CACHE_FILE_NAME = "nuts-runtime-cache.json";
    }

    /**
     * valid values for Query parameter "face"
     */
    public static final class QueryFaces {
        public static final String CATALOG = "catalog";
    }
}
