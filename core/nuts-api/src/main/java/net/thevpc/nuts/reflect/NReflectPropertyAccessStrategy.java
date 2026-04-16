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
 * Strategy controlling how properties are discovered and accessed during reflection.
 * When multiple strategies apply, they are applied in the following priority order:
 * BEAN first, then FLUENT, then FIELD — meaning a property discovered by an earlier
 * strategy will not be overridden by a later one.
 */
public enum NReflectPropertyAccessStrategy implements NEnum {
    /**
     * access using fields
     */
    FIELD,

    /**
     * Discovers properties via JavaBean conventions: {@code getX()}/{@code isX()} as getters
     * and {@code setX(value)} as setters. A getter alone yields a read-only property.
     * When combined with {@link #FIELD} (i.e. {@link #ALL}), a bean getter with no matching
     * setter may fall back to a field of the same name and type for write access.
     */
    BEAN,

    /**
     * Discovers properties via fluent accessor conventions: {@code x()} as getter
     * and {@code x(value)} as setter. A getter alone yields a read-only property.
     * A setter without a matching getter is always rejected.
     * Methods matching the JavaBean prefix pattern ({@code get}, {@code set}, {@code is})
     * are excluded from fluent discovery to avoid double-registration.
     */
    FLUENT,

    /**
     * Applies all strategies: {@link #BEAN}, {@link #FLUENT}, and {@link #FIELD}.
     * A property discovered by an earlier strategy is never overridden by a later one.
     */
    ALL;
    private final String id;

    NReflectPropertyAccessStrategy() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    /**
     * Parses a string value into a {@link NReflectPropertyAccessStrategy}.
     *
     * @param value the string to parse
     * @return an {@link NOptional} containing the matched strategy, or empty if not found
     */
    public static NOptional<NReflectPropertyAccessStrategy> parse(String value) {
        return NEnumUtils.parseEnum(value, NReflectPropertyAccessStrategy.class);
    }

    /**
     * Returns the normalized identifier for this strategy, formatted according to
     * {@link NNameFormat#ID_NAME}.
     *
     * @return the string id of this strategy
     */
    @Override
    public String id() {
        return id;
    }
}
