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
package net.thevpc.nuts.artifact;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Artifacts are organized according to {@code NutsIdType} to reflect how the artifact
 * should be managed by the workspace.
 * This information is available in {@link NDefinition}
 *
 * @app.category Base
 */
public enum NIdType implements NEnum {
    /**
     * the id denotes a nuts api id artifact.
     * an artifact is a valid nuts api if it corresponds to {@link NConstants.Ids#NUTS_API}
     */
    API,

    /**
     * the id denotes a nuts runtime id artifact.
     * an artifact is a valid nuts api if it corresponds to {@link NConstants.Ids#NUTS_RUNTIME}
     */
    RUNTIME,

    /**
     * the id denotes a nuts known extension artifact.
     * an artifact is a valid nuts extension if it contains the flag "EXTENSION" armed (property "nuts.extension" equal to
     * true) in its descriptor.
     */
    EXTENSION,

    /**
     * the id denotes a nuts companion id artifact.
     * Default companions are
     * <ul>
     *     <li>net.thevpc.nsh:nsh</li>
     * </ul>
     */
    COMPANION,

    /**
     * the id is none of the previous types
     */
    REGULAR;
    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NIdType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NIdType> parse(String value) {
        return NEnumUtils.parseEnum(value, NIdType.class);
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
