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

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * @app.category Base
 */
public interface NExecutionEntries extends NComponent {
    static NExecutionEntries of(NSession session) {
       return NExtensions.of(session).createSupported(NExecutionEntries.class);
    }

    /**
     * parse Execution Entries
     *
     * @param file jar file
     * @return execution entries (class names with main method)
     */
    List<NExecutionEntry> parse(File file);

    List<NExecutionEntry> parse(NPath file);

    /**
     * parse Execution Entries
     *
     * @param file jar file
     * @return execution entries (class names with main method)
     */
    List<NExecutionEntry> parse(Path file);

//    NutsExecutionEntry[] parse(NPath file);

    /**
     * parse Execution Entries
     *
     * @param inputStream stream
     * @param type        stream type
     * @param sourceName  stream source name (optional)
     * @return execution entries (class names with main method)
     */
    List<NExecutionEntry> parse(InputStream inputStream, String type, String sourceName);

    NSession getSession();

    NExecutionEntries setSession(NSession session);
}