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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Command execution type.
 *
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public enum NExecutionType implements NEnum {
    /**
     * command will be resolved as an external command/artifact. Nuts will resolve
     * relevant executor to run it
     */
    SPAWN,

    /**
     * command will be resolved as an external native command. Nuts will
     * delegate running to underlining operating system using standard
     * ProcessBuilder
     */
    SYSTEM,

    /**
     * command will be resolved as a class to run within the current Virtual
     * Machine by means of classloading
     */
    EMBEDDED,

    /**
     * command will be resolved as a list of documents to open using
     * system mapping
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
