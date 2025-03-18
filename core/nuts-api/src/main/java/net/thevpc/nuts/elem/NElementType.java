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
    NULL,
    /**
     * integer/long (number) element
     */
    LONG,
    /**
     * integer/long (number) element
     */
    INTEGER,
    /**
     * integer/long (number) element
     */
    SHORT,
    /**
     * integer/long (number) element
     */
    BYTE,
    /**
     * float/double (number) element
     */
    FLOAT,
    /**
     * float/double (number) element
     */
    BIG_INTEGER,
    /**
     * float/double (number) element
     */
    BIG_DECIMAL,
    /**
     * float/double (number) element
     */
    DOUBLE,
    /**
     * string element
     */
    STRING,
    REGEX,
    NAME,
    CHAR,

    /**
     * date element
     */
    INSTANT,
    /**
     * boolean element
     */
    BOOLEAN,
    /**
     * array element
     */
    ARRAY,
    /**
     * object (list of key/val) element
     */
    OBJECT,
    /**
     * custom object that is not destructed. Cannot be null or primitive
     */
    CUSTOM,
    PAIR,
    DATETIME,
    DATE,
    TIME,
    BINARY_STREAM,
    CHAR_STREAM,
    DOUBLE_COMPLEX,
    FLOAT_COMPLEX,
    BIG_COMPLEX,
    UPLET,
    MATRIX,
    ALIAS,


    // Missing types to support TSON types
    // will be added incrementally
    // these would be supported as primitives

    // this zould not be supported (will be removed from tson)

    // these are compund types
    OP,

    NAMED_ARRAY,
    NAMED_PARAMETRIZED_ARRAY,
    NAMED_OBJECT,
    NAMED_PARAMETRIZED_OBJECT,
    NAMED_UPLET,
    NAMED_MATRIX,
    NAMED_PARAMETRIZED_MATRIX,
    PARAMETRIZED_MATRIX,
    PARAMETRIZED_ARRAY,
    PARAMETRIZED_OBJECT,

    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NElementType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NElementType> parse(String value) {
        return NEnumUtils.parseEnum(value, NElementType.class);
    }

    public boolean isNumber() {
        switch (this) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case BIG_INTEGER:
            case BIG_COMPLEX:
            case BIG_DECIMAL:
                return true;
            case NULL:
            case BINARY_STREAM:
            case CHAR_STREAM:
            case STRING:
            case CHAR:
            case BOOLEAN:
            case NAME:
            case ALIAS:
            case DATETIME:
            case DATE:
            case TIME:
            case REGEX:
            case MATRIX:
            case PAIR:
            case OP:
            case ARRAY:
            case OBJECT:
            case UPLET:
            case CUSTOM:
            case NAMED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case NAMED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case NAMED_UPLET:
            case NAMED_MATRIX:
            case NAMED_PARAMETRIZED_MATRIX:
            case PARAMETRIZED_MATRIX:
            case PARAMETRIZED_ARRAY:
            case PARAMETRIZED_OBJECT:
                return false;
        }
        return false;
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
        switch (this) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case BIG_INTEGER:
            case BIG_COMPLEX:
            case BIG_DECIMAL:
            case NULL:
            case BINARY_STREAM:
            case CHAR_STREAM:
            case STRING:
            case CHAR:
            case BOOLEAN:
            case NAME:
            case ALIAS:
            case DATETIME:
            case DATE:
            case TIME:
            case REGEX:
                return true;
            case MATRIX:
            case PAIR:
            case OP:
            case ARRAY:
            case OBJECT:
            case UPLET:
            case CUSTOM:
            case NAMED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case NAMED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case NAMED_UPLET:
            case NAMED_MATRIX:
            case NAMED_PARAMETRIZED_MATRIX:
            case PARAMETRIZED_MATRIX:
            case PARAMETRIZED_ARRAY:
            case PARAMETRIZED_OBJECT:
                return false;
        }
        return false;
    }

}
