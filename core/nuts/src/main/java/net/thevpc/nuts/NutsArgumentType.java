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

/**
 * Argument parse Type
 *
 * @author thevpc
 * @since 0.5.5
 * @app.category Command Line
 */
public enum NutsArgumentType implements NutsEnum {
    /**
     * argument that may or may not accept value.
     */
    ANY,
    /**
     * argument that accepts a string as value. Either the string is included in
     * the argument itself (--option=value) or succeeds it (--option value).
     */
    STRING,
    /**
     * argument that accepts a boolean as value. Either the boolean is not
     * defined (--option), is included in the argument itself (--option=true) or
     * succeeds it (--option true). Parsing boolean is also aware of negated
     * options (--!option) that will be interpreted as (--option=false).
     */
    BOOLEAN;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * Default constructor
     */
    NutsArgumentType() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    /**
     * parse string and return null if parse fails
     * @param value value to parse
     * @return parsed instance or null
     */
    public static NutsArgumentType parseLenient(String value) {
        return parseLenient(value, null);
    }

    /**
     * parse string and return {@code emptyOrErrorValue} if parse fails
     * @param value value to parse
     * @return parsed instance or {@code emptyOrErrorValue}
     */
    public static NutsArgumentType parseLenient(String value, NutsArgumentType emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    /**
     *
     * parse string and return {@code emptyValue} when null or {@code errorValue} if parse fails
     * @param value value to parse
     * @param emptyValue value when the value is null or empty
     * @param errorValue value when the value cannot be parsed
     * @return parsed value
     */
    public static NutsArgumentType parseLenient(String value, NutsArgumentType emptyValue, NutsArgumentType errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsArgumentType.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    public static NutsArgumentType parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsArgumentType parse(String value, NutsArgumentType emptyValue, NutsSession session) {
        NutsArgumentType v = parseLenient(value, emptyValue, null);
        if (v == null) {
            if (!NutsBlankable.isBlank(value)) {
                throw new NutsParseEnumException(session, value, NutsArgumentType.class);
            }
        }
        return v;
    }

    /**
     * lower cased identifier.
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }
}
