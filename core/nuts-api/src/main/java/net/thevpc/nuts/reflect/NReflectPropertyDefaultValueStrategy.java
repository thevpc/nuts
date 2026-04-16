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
package net.thevpc.nuts.reflect;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Defines the strategy for determining if a property value is considered a "default"
 * and should be omitted during serialization to reduce output verbosity.
 * <p>
 * This strategy is used by {@link net.thevpc.nuts.elem.NElementFactory} and {@link NReflectProperty}
 * to decide whether a specific field or method value contributes to the
 * generated {@link net.thevpc.nuts.elem.NElement}.
 * * @author Taha Ben Salah
 * @since 0.8.9
 */
public enum NReflectPropertyDefaultValueStrategy implements NEnum {
    /**
     * Compares the value against the standard Java Language defaults.
     * <ul>
     * <li>Object: {@code null}</li>
     * <li>Numeric: {@code 0}</li>
     * <li>Boolean: {@code false}</li>
     * </ul>
     * Use this for strict, minimal serialization that ignores uninitialized fields.
     */
    BASE,

    /**
     * Compares the value against a "Clean Instance" (template) of the containing class.
     * <p>
     * If the current value matches the value found in a freshly instantiated
     * object of the same type, it is considered a default. This is particularly
     * useful for {@code Builder} classes where fields may have non-null initial values
     * (e.g., empty lists or default version strings).
     */
    PROTOTYPE,

    /**
     * No value is ever considered a default.
     * <p>
     * Every discovered property will be serialized regardless of its value.
     * This ensures the highest level of data fidelity but results in more
     * verbose output.
     */
    NONE;
    private final String id;

    NReflectPropertyDefaultValueStrategy() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NReflectPropertyDefaultValueStrategy> parse(String value) {
        return NEnumUtils.parseEnum(value, NReflectPropertyDefaultValueStrategy.class);
    }


    @Override
    public String id() {
        return id;
    }
}
