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
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponent;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ListIterator;

/**
 * Command History
 *
 * @author thevpc
 * @app.category Command Line
 */
public interface NCmdLineHistory extends NComponent {
    static NCmdLineHistory of(NSession session) {
       return NExtensions.of(session).createComponent(NCmdLineHistory.class).get();
    }


    void load();

    void save();

    void load(InputStream in);

    void save(OutputStream out);

    NPath getPath();

    NCmdLineHistory setPath(Path path);

    NCmdLineHistory setPath(File path);

    NCmdLineHistory setPath(NPath path);

    int size();

    void purge();

    NCmdLineHistoryEntry getEntry(int index);

    ListIterator<NCmdLineHistoryEntry> iterator(int index);

    void add(Instant time, String line);

}
