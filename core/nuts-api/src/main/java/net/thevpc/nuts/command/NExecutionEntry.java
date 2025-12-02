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
package net.thevpc.nuts.command;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.util.NAssert;

import java.io.InputStream;
import java.util.List;

/**
 * Represents an executable entry in a Nuts application or descriptor.
 * <p>
 * An execution entry can be a Java class, main entry point, or any
 * runnable component that the Nuts runtime can recognize and execute.
 * This interface provides metadata about the entry and allows
 * parsing from various sources.
 * </p>
 *
 * <p>
 * Typical usage:
 * <pre>{@code
 * List<NExecutionEntry> entries = NExecutionEntry.parse(pathToDescriptor);
 * for (NExecutionEntry entry : entries) {
 *     if (entry.isApp()) {
 *         // execute or inspect
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public interface NExecutionEntry extends Comparable<NExecutionEntry> {

    /**
     * Parses execution entries from the given file path.
     * <p>
     * The file is expected to contain execution entry metadata recognized
     * by the Nuts runtime.
     *
     * @param path the path to parse entries from
     * @return list of parsed execution entries
     * @throws NullPointerException if {@code path} is null
     */
    static List<NExecutionEntry> parse(NPath path) {
        NAssert.requireNonNull(path, "path");
        return NIORPI.of().parseExecutionEntries(path);
    }


    /**
     * Parses execution entries from the given input stream.
     *
     * @param inputStream the input stream containing execution entry data
     * @param type the type/format of the stream (e.g., XML, JSON, TSON)
     * @param sourceName the logical name of the source (used in error messages)
     * @return list of parsed execution entries
     * @throws NullPointerException if {@code inputStream} is null
     */
    static List<NExecutionEntry> parse(InputStream inputStream, String type, String sourceName) {
        return NIORPI.of().parseExecutionEntries(inputStream, type, sourceName);
    }

    /**
     * Returns {@code true} if the entry resolved to a valid Nuts application.
     * <p>
     * An entry may exist in metadata but not actually correspond to a valid executable class.
     *
     * @return {@code true} if this entry represents a valid Nuts application
     */
    boolean isApp();


    /**
     * Returns the class name of this execution entry.
     *
     * @return the fully qualified class name
     */
    String getName();

    /**
     * Returns {@code true} if this class is registered as the default main
     * entry in META-INF.
     * <p>
     * Default entries are typically executed when no explicit entry is specified.
     *
     * @return {@code true} if this is a default entry
     */
    boolean isDefaultEntry();

}
