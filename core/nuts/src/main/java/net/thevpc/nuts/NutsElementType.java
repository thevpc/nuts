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
 * Element type. this an extension of json element types.
 *
 * @author thevpc
 * @app.category Elements
 */
public enum NutsElementType implements NutsEnum {
    /**
     * null element
     */
    NULL(true, false),
    /**
     * integer/long (number) element
     */
    LONG(true, true),
    /**
     * integer/long (number) element
     */
    INTEGER(true, true),
    /**
     * integer/long (number) element
     */
    SHORT(true, true),
    /**
     * integer/long (number) element
     */
    BYTE(true, true),
    /**
     * float/double (number) element
     */
    FLOAT(true, true),
    /**
     * float/double (number) element
     */
    BIG_INTEGER(true, true),
    /**
     * float/double (number) element
     */
    BIG_DECIMAL(true, true),
    /**
     * float/double (number) element
     */
    DOUBLE(true, true),
    /**
     * string element
     */
    STRING(true, false),
    //    /**
    //     * nuts string element
    //     */
    //    NUTS_STRING(true,false),

    /**
     * date element
     */
    INSTANT(true, false),
    /**
     * boolean element
     */
    BOOLEAN(true, false),
    /**
     * array element
     */
    ARRAY(false, false),
    /**
     * object (list of key/val) element
     */
    OBJECT(false, false);

    /**
     * true if private type
     */
    private final boolean primitive;
    private final boolean nbr;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsElementType(boolean primitive, boolean nbr) {
        this.id = name().toLowerCase().replace('_', '-');
        this.primitive = primitive;
        this.nbr = nbr;
    }

    public static NutsElementType parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsElementType parseLenient(String value, NutsElementType emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsElementType parseLenient(String value, NutsElementType emptyValue, NutsElementType errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsElementType.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }

    public static NutsElementType parse(String value, NutsSession session) {
        return parse(value, null, session);
    }

    public static NutsElementType parse(String value, NutsElementType emptyValue, NutsSession session) {
        NutsElementType v = parseLenient(value, emptyValue, null);
        NutsApiUtils.checkNonNullEnum(v, value, NutsElementType.class, session);
        return v;
    }

    public boolean isNumber() {
        return nbr;
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }

    /**
     * true if private type
     *
     * @return true if private type
     */
    public boolean isPrimitive() {
        return primitive;
    }
}
