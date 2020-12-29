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

import java.io.Serializable;
import java.net.URL;
import java.nio.file.Path;

/**
 * Content describes a artifact file location and its characteristics.
 * @author thevpc
 * @since 0.5.4
 * @category Descriptor
 */
public interface NutsContent extends Serializable {

    /**
     * artifact local path
     * @return artifact local path
     */
    Path getPath();

    URL getURL();

    String getLocation();

    /**
     * when true, the content was retrieved from cache rather then from remote location.
     * @return true if content is cached
     */
    boolean isCached();

    /**
     * when true, the path location is temporary and should be deleted after usage
     * @return true if content temporary
     */
    boolean isTemporary();
}
