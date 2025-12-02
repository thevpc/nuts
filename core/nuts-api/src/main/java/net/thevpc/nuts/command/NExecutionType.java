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

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Specifies the type of command execution.
 * <p>
 * This enum defines how a command or executable is resolved and run
 * by the Nuts framework. Each type determines the execution strategy,
 * whether running in-process, via the OS, or as a document opener.
 * </p>
 *
 * <ul>
 *     <li>{@link #SPAWN} – Resolves the command as an external artifact or executable.
 *         Nuts selects the appropriate executor to run it.</li>
 *     <li>{@link #SYSTEM} – Executes a native command using the underlying
 *         operating system, typically via {@link ProcessBuilder}.</li>
 *     <li>{@link #EMBEDDED} – Executes a Java class in the current JVM
 *         using classloading, without spawning a new process.</li>
 *     <li>{@link #OPEN} – Opens documents or files using the system's default
 *         application for the given file type.</li>
 * </ul>
 *
 * <p>
 * Use the appropriate type to control how commands are executed and how
 * Nuts resolves their environment and dependencies.
 * </p>
 *
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public enum NExecutionType implements NEnum {
    /**
     * Command will be resolved as an external artifact or executable.
     * Nuts will determine the appropriate executor to run it.
     */
    SPAWN,

    /**
     * Command will be executed as a native system command.
     * Nuts delegates execution to the operating system using standard
     * {@link ProcessBuilder}.
     */
    SYSTEM,

    /**
     * Command will be executed as a Java class within the current JVM.
     * Execution happens via classloading, without spawning a separate process.
     */
    EMBEDDED,

    /**
     * Command will be treated as a file or document to open.
     * The system's default application mapping will be used.
     */
    OPEN,
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NExecutionType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NExecutionType> parse(String value) {
        return NEnumUtils.parseEnum(value, NExecutionType.class);
    }


    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}
