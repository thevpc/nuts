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

import net.thevpc.nuts.util.*;

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

    BIG_COMPLEX,
    DOUBLE_COMPLEX,
    /**
     * float/double (number) element
     */
    FLOAT_COMPLEX,
    BIG_DECIMAL,

    /**
     * float/double (number) element
     */
    BIG_INT,

    /**
     * float/double (number) element
     */
    DOUBLE,
    /**
     * float/double (number) element
     */
    FLOAT,
    /**
     * integer/long (number) element
     */
    LONG,
    /**
     * integer/long (number) element
     */
    INT,
    /**
     * integer/long (number) element
     */
    SHORT,
    /**
     * integer/long (number) element
     */
    BYTE,

    DOUBLE_QUOTED_STRING,
    SINGLE_QUOTED_STRING,
    BACKTICK_STRING,
    TRIPLE_DOUBLE_QUOTED_STRING,
    TRIPLE_SINGLE_QUOTED_STRING,
    TRIPLE_BACKTICK_STRING,
    LINE_STRING,
    BLOCK_STRING,

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
    UPLET,


    OPERATOR_SYMBOL,
    BINARY_OPERATOR,
    TERNARY_OPERATOR,
    NARY_OPERATOR,
    UNARY_OPERATOR,
    FLAT_EXPR,

    NAMED_ARRAY,
    NAMED_PARAMETRIZED_ARRAY,
    NAMED_OBJECT,
    NAMED_PARAMETRIZED_OBJECT,
    NAMED_UPLET,
    PARAMETRIZED_ARRAY,
    PARAMETRIZED_OBJECT,
    ORDERED_LIST,
    UNORDERED_LIST,
    EMPTY,
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

    public boolean isAnyFloatingNumber() {
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

    public boolean isAnyOrdinalNumber() {
        switch (this) {
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case BIG_INT:
                return true;
        }
        return false;
    }

    public boolean isAnyNumber() {
        switch (this) {
            case BYTE:
            case SHORT:
            case INT:
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

    public boolean isAnyOp() {
        switch (this) {
            case BINARY_OPERATOR:
            case UNARY_OPERATOR:
            case OPERATOR_SYMBOL:
                return true;
        }
        return false;
    }

    public boolean isAnyPrimitive() {
        switch (this) {
            case BYTE:
            case SHORT:
            case INT:
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
            case BACKTICK_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_BACKTICK_STRING:
            case LINE_STRING:
            case BLOCK_STRING:
            case CHAR:
            case BOOLEAN:
            case NAME:
            case LOCAL_DATETIME:
            case LOCAL_DATE:
            case LOCAL_TIME:
            case INSTANT:
                return true;
        }
        return false;
    }


    public boolean isAnyStream() {
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
            case BACKTICK_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_BACKTICK_STRING:
            case LINE_STRING:
                return true;
        }
        return false;
    }

    public boolean isAnyStringOrName() {
        switch (this) {
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case BACKTICK_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_BACKTICK_STRING:
            case LINE_STRING:
            case NAME:
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

    public boolean isAnyDecimalNumber() {
        switch (this) {
            case FLOAT:
            case DOUBLE:
            case BIG_DECIMAL: {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyBigNumber() {
        switch (this) {
            case BIG_DECIMAL:
            case BIG_INT:
            case BIG_COMPLEX: {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyNamed() {
        switch (this) {
            case PAIR: // key may be the name
            case NAMED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case NAMED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case NAMED_UPLET:
            {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyParametrized() {
        switch (this) {
            case PARAMETRIZED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyLocalTemporal() {
        switch (this) {
            case LOCAL_DATE:
            case LOCAL_DATETIME:
            case LOCAL_TIME: {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyTemporal() {
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

    public boolean isAnyComplexNumber() {
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

    public boolean isAnyArray() {
        switch (this) {
            case ARRAY:
            case NAMED_ARRAY:
            case PARAMETRIZED_ARRAY:
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

    public boolean isAnyUplet() {
        switch (this) {
            case UPLET:
            case NAMED_UPLET:
                return true;
        }
        return false;
    }

    public boolean isAnyListOrParametrizedContainer() {
        switch (this) {
            case UPLET:
            case NAMED_UPLET:
            case OBJECT:
            case NAMED_OBJECT:
            case PARAMETRIZED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case ARRAY:
            case NAMED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
                return true;
        }
        return false;
    }

    public boolean isAnyListContainer() {
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
                return true;
        }
        return false;
    }

    public NElementTypeGroup typeGroup() {
        switch (this) {
            case NULL:
                return NElementTypeGroup.NULL;
            case BOOLEAN:
                return NElementTypeGroup.BOOLEAN;
            case LONG:
            case INT:
            case SHORT:
            case BYTE:
            case FLOAT:
            case BIG_INT:
            case BIG_DECIMAL:
            case DOUBLE:
            case DOUBLE_COMPLEX:
            case FLOAT_COMPLEX:
            case BIG_COMPLEX:
                return NElementTypeGroup.NUMBER;
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case BACKTICK_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_BACKTICK_STRING:
            case LINE_STRING:
            case CHAR:
                return NElementTypeGroup.STRING;
            case NAME:
                return NElementTypeGroup.NAME;
            case INSTANT:
            case LOCAL_DATETIME:
            case LOCAL_DATE:
            case LOCAL_TIME:
                return NElementTypeGroup.TEMPORAL;
            case ARRAY:
            case OBJECT:
            case PAIR:
            case UPLET:
            case NAMED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case NAMED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case NAMED_UPLET:
            case PARAMETRIZED_ARRAY:
            case PARAMETRIZED_OBJECT:
                return NElementTypeGroup.CONTAINER;
            case BINARY_STREAM:
            case CHAR_STREAM:
                return NElementTypeGroup.STREAM;
            case OPERATOR_SYMBOL:
            case BINARY_OPERATOR:
            case UNARY_OPERATOR:
                return NElementTypeGroup.OPERATOR;
            case CUSTOM:
                return NElementTypeGroup.CUSTOM;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
