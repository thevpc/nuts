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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsEnum;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.boot.NutsApiUtils;

/**
 * Modes Application can run with
 *
 * @app.category Application
 * @since 0.5.5
 */
public enum NutsComponentScopeType implements NutsEnum {
    /**
     * a new instance will be created per workspace reference
     */
    WORKSPACE,
    /**
     * a new instance will be created per session reference
     */
    SESSION,
    /**
     * a new instance will be created at each call
     */
    PROTOTYPE;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NutsComponentScopeType() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    /**
     * parse string and return null if parse fails
     *
     * @param value value to parse
     * @return parsed instance or null
     */
    public static NutsComponentScopeType parseLenient(String value) {
        return parseLenient(value, null);
    }

    /**
     * parse string and return {@code emptyOrErrorValue} if parse fails
     *
     * @param emptyOrErrorValue emptyOrErrorValue
     * @param value             value to parse
     * @return parsed instance or {@code emptyOrErrorValue}
     */
    public static NutsComponentScopeType parseLenient(String value, NutsComponentScopeType emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    /**
     * parse string and return {@code emptyValue} when null or {@code errorValue} if parse fails
     *
     * @param value      value to parse
     * @param emptyValue value when the value is null or empty
     * @param errorValue value when the value cannot be parsed
     * @return parsed value
     */
    public static NutsComponentScopeType parseLenient(String value, NutsComponentScopeType emptyValue, NutsComponentScopeType errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsComponentScopeType.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    public static NutsComponentScopeType parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsComponentScopeType parse(String value, NutsComponentScopeType emptyValue, NutsSession session) {
        NutsComponentScopeType v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v, value, NutsComponentScopeType.class, session);
        return v;
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
