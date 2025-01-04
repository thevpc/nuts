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

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

import java.io.InputStream;
import java.util.List;

/**
 * @app.category Base
 */
public interface NLibPaths extends NComponent {
    static NLibPaths of() {
        return NExtensions.of(NLibPaths.class);
    }


    List<NExecutionEntry> parseExecutionEntries(NPath file);


//    NExecutionEntry[] parse(NPath file);

    /**
     * parse Execution Entries
     *
     * @param inputStream stream
     * @param type        stream type
     * @param sourceName  stream source name (optional)
     * @return execution entries (class names with main method)
     */
    List<NExecutionEntry> parseExecutionEntries(InputStream inputStream, String type, String sourceName);

    List<NPath> resolveLibPaths(Class<?> clazz);

    NOptional<NPath> resolveLibPath(Class<?> clazz);

    /**
     * detect nuts id from resources containing the given class
     * or null if not found. If multiple resolutions return the first.
     *
     * @param clazz to search for
     * @return nuts id detected from resources containing the given class
     */
    NOptional<NId> resolveId(Class<?> clazz);

    NOptional<NId> resolveId(NPath path);

    /**
     * detect all nuts ids from resources containing the given class.
     *
     * @param clazz to search for
     * @return all nuts ids detected from resources containing the given class
     */
    List<NId> resolveIds(Class<?> clazz);

    List<NId> resolveIds(NPath path);
}
