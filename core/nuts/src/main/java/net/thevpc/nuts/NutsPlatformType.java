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
package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

/**
 * uniform platform
 *
 * @author thevpc
 * @app.category Base
 * @since 0.8.1
 */
public enum NutsPlatformType implements NutsEnum {
    JAVA,
    DOTNET,
    PYTHON,
    JAVASCRIPT,
    UNKNOWN;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsPlatformType() {
        this.id = name().toLowerCase();//.replace('_', '-');
    }


    /**
     * parse string and return null if parse fails
     *
     * @param value value to parse
     * @return parsed instance or null
     */
    public static NutsPlatformType parseLenient(String value) {
        return parseLenient(value, null);
    }

    /**
     * parse string and return {@code emptyOrErrorValue} if parse fails
     *
     * @param value value to parse
     * @param emptyOrErrorValue emptyOrErrorValue
     * @return parsed instance or {@code emptyOrErrorValue}
     */
    public static NutsPlatformType parseLenient(String value, NutsPlatformType emptyOrErrorValue) {
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
    public static NutsPlatformType parseLenient(String value, NutsPlatformType emptyValue, NutsPlatformType errorValue) {
        value = value == null ? "" : value.toLowerCase().replace('-', '_').trim();
        switch (value) {
            case "java":
            case "jre":
            case "jdk":
                return JAVA;

            case ".net":
            case "dotnet":
                return DOTNET;

            case "python":
                return PYTHON;

            case "js":
            case "javascript":
                return JAVASCRIPT;

            case "unknown":
                return UNKNOWN;
            case "": {
                return emptyValue;
            }
            default: {
                return errorValue;
            }
        }
    }

    public static NutsPlatformType parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsPlatformType parse(String value, NutsPlatformType emptyValue, NutsSession session) {
        NutsPlatformType v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v,value,NutsPlatformType.class,session);
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
