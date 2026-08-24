/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
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
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NCmdLineHistory of() {
       return NExtensions.of(NCmdLineHistory.class);
    }


    /**
     * Load.
     */
    void load();

    /**
     * Save.
     */
    void save();

    /**
     * Load.
     *
     * @param in in
     */
    void load(InputStream in);

    /**
     * Save.
     *
     * @param out out
     */
    void save(OutputStream out);

    /**
     * Path.
     *
     * @return path result
     */
    NPath path();

    /**
     * Path.
     *
     * @param path path
     * @return path result
     */
    NCmdLineHistory path(Path path);

    /**
     * Path.
     *
     * @param path path
     * @return path result
     */
    NCmdLineHistory path(File path);

    /**
     * Path.
     *
     * @param path path
     * @return path result
     */
    NCmdLineHistory path(NPath path);

    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * Purge.
     */
    void purge();

    /**
     * Returns the entry.
     *
     * @param index index
     * @return get entry result
     */
    NCmdLineHistoryEntry getEntry(int index);

    /**
     * Iterator.
     *
     * @param index index
     * @return iterator result
     */
    ListIterator<NCmdLineHistoryEntry> iterator(int index);

    /**
     * Adds add.
     *
     * @param time time
     * @param line line
     */
    void add(Instant time, String line);

}
