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
package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;

/**
 * Element type. this an extension of json element types.
 *
 * @author thevpc
 * @app.category Elements
 */
public enum NElementType implements NEnum {
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
    REGEX(true, false),
    NAME(true, false),
    CHAR(true, false),

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
    ENTRY(false, false),
    /**
     * object (list of key/val) element
     */
    OBJECT(false, false),
    /**
     * custom object that is not destructed. Cannot be null or primitive
     */
    CUSTOM(false, false),
    PAIR(false, false),
    DATETIME(true, false),
    DATE(true, false),
    TIME(true, false),
    BINARY_STREAM(true, false),
    CHAR_STREAM(true, false),
    DOUBLE_COMPLEX(true, true),
    FLOAT_COMPLEX(true, true),
    BIG_COMPLEX(true, true),
    UPLET(false, true),
    MATRIX(false, false),
    ALIAS(false, false),


    // Missing types to support TSON types
    // will be added incrementally
    // these would be supported as primitives

    // this zould not be supported (will be removed from tson)

    // these are compund types
    OP(false, false),

    ;

    /**
     * true if private type
     */
    private final boolean primitive;
    private final boolean nbr;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NElementType(boolean primitive, boolean nbr) {
        this.id = NNameFormat.ID_NAME.format(name());
        this.primitive = primitive;
        this.nbr = nbr;
    }

    public static NOptional<NElementType> parse(String value) {
        return NEnumUtils.parseEnum(value, NElementType.class);
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
