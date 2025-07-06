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
    BIG_INT,
    /**
     * float/double (number) element
     */
    BIG_DECIMAL,
    /**
     * float/double (number) element
     */
    DOUBLE,

    DOUBLE_QUOTED_STRING,
    SINGLE_QUOTED_STRING,
    ANTI_QUOTED_STRING,
    TRIPLE_DOUBLE_QUOTED_STRING,
    TRIPLE_SINGLE_QUOTED_STRING,
    TRIPLE_ANTI_QUOTED_STRING,
    LINE_STRING,

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
    LOCAL_DATETIME,
    LOCAL_DATE,
    LOCAL_TIME,
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

    public boolean isFloatingNumber() {
        switch (this) {
            case FLOAT:
            case DOUBLE:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case BIG_COMPLEX:
            case BIG_DECIMAL:
                return true;
        }
        return false;
    }
    public boolean isOrdinalNumber() {
        switch (this) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case BIG_INT:
                return true;
        }
        return false;
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
            case BIG_INT:
            case BIG_COMPLEX:
            case BIG_DECIMAL:
                return true;
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
            case BIG_INT:
            case BIG_COMPLEX:
            case BIG_DECIMAL:
            case NULL:
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING:
            case CHAR:
            case BOOLEAN:
            case NAME:
            case ALIAS:
            case LOCAL_DATETIME:
            case LOCAL_DATE:
            case LOCAL_TIME:
            case INSTANT:
            case REGEX:
                return true;
            case BINARY_STREAM:
            case CHAR_STREAM:
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

    public boolean isString() {
        switch (this) {
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING:
                return true;
        }
        return false;
    }

    public boolean isStream() {
        switch (this) {
            case BINARY_STREAM:
            case CHAR_STREAM:
                return true;
        }
        return false;
    }

    public boolean isAnyString() {
        switch (this) {
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING:
            case NAME:
            case REGEX:
            case CHAR:
                return true;
        }
        return false;
    }

    public boolean isAnyDate() {
        switch (this) {
            case LOCAL_DATE:
            case LOCAL_DATETIME:
            case INSTANT:
                return true;
        }
        return false;
    }

    public boolean isDecimalNumber() {
        switch (this) {
            case FLOAT:
            case DOUBLE:
            case BIG_DECIMAL: {
                return true;
            }
        }
        return false;
    }

    public boolean isBigNumber() {
        switch (this) {
            case BIG_DECIMAL:
            case BIG_INT:
            case BIG_COMPLEX: {
                return true;
            }
        }
        return false;
    }

    public boolean isNamed() {
        switch (this) {
            case PAIR: // key may be the name
            case NAMED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case NAMED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case NAMED_UPLET:
            case NAMED_MATRIX:
            case NAMED_PARAMETRIZED_MATRIX: {
                return true;
            }
        }
        return false;
    }

    public boolean isParametrized() {
        switch (this) {
            case PARAMETRIZED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case PARAMETRIZED_MATRIX:
            case NAMED_PARAMETRIZED_MATRIX:
            {
                return true;
            }
        }
        return false;
    }

    public boolean isLocalTemporal() {
        switch (this) {
            case LOCAL_DATE:
            case LOCAL_DATETIME:
            case LOCAL_TIME: {
                return true;
            }
        }
        return false;
    }

    public boolean isTemporal() {
        switch (this) {
            case INSTANT:
            case LOCAL_DATE:
            case LOCAL_DATETIME:
            case LOCAL_TIME: {
                return true;
            }
        }
        return false;
    }

    public boolean isComplexNumber() {
        switch (this) {
            case BIG_COMPLEX:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX: {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyNamedObject() {
        switch (this) {
            case NAMED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
                return true;
        }
        return false;
    }

    public boolean isAnyNamedArray() {
        switch (this) {
            case NAMED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
                return true;
        }
        return false;
    }

    public boolean isAnyNamedMatrix() {
        switch (this) {
            case NAMED_MATRIX:
            case NAMED_PARAMETRIZED_MATRIX:
                return true;
        }
        return false;
    }

    public boolean isAnyParametrizedObject() {
        switch (this) {
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
                return true;
        }
        return false;
    }

    public boolean isAnyParametrizedArray() {
        switch (this) {
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
                return true;
        }
        return false;
    }

    public boolean isAnyParametrizedMatrix() {
        switch (this) {
            case PARAMETRIZED_MATRIX:
            case NAMED_PARAMETRIZED_MATRIX:
                return true;
        }
        return false;
    }

    public boolean isAnyArray() {
        switch (this) {
            case ARRAY:
            case NAMED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_PARAMETRIZED_MATRIX:
                return true;
        }
        return false;
    }

    public boolean isAnyObject() {
        switch (this) {
            case OBJECT:
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
                return true;
        }
        return false;
    }

    public boolean isAnyMatrix() {
        switch (this) {
            case MATRIX:
            case NAMED_MATRIX:
            case PARAMETRIZED_MATRIX:
            case NAMED_PARAMETRIZED_MATRIX:
                return true;
        }
        return false;
    }

    public boolean isAnyUplet() {
        switch (this) {
            case UPLET:
            case NAMED_UPLET:
                return true;
        }
        return false;
    }

    public boolean isListContainer() {
        switch (this) {
            case UPLET:
            case NAMED_UPLET:
            case OBJECT:
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case ARRAY:
            case NAMED_ARRAY:
            case PARAMETRIZED_ARRAY:
            case NAMED_PARAMETRIZED_MATRIX:
                return true;
        }
        return false;
    }
}
