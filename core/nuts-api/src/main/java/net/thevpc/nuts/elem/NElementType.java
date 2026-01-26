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
    NULL(NElementTypeGroup.NULL),

    BIG_COMPLEX(NElementTypeGroup.NUMBER),
    DOUBLE_COMPLEX(NElementTypeGroup.NUMBER),
    /**
     * float/double (number) element
     */
    FLOAT_COMPLEX(NElementTypeGroup.NUMBER),
    BIG_DECIMAL(NElementTypeGroup.NUMBER),

    /**
     * float/double (number) element
     */
    BIG_INT(NElementTypeGroup.NUMBER),

    /**
     * float/double (number) element
     */
    DOUBLE(NElementTypeGroup.NUMBER),
    /**
     * float/double (number) element
     */
    FLOAT(NElementTypeGroup.NUMBER),
    /**
     * integer/long (number) element
     */
    LONG(NElementTypeGroup.NUMBER),
    ULONG(NElementTypeGroup.NUMBER),
    /**
     * integer/long (number) element
     */
    UINT(NElementTypeGroup.NUMBER),
    INT(NElementTypeGroup.NUMBER),
    /**
     * integer/long (number) element
     */
    SHORT(NElementTypeGroup.NUMBER),
    USHORT(NElementTypeGroup.NUMBER),
    /**
     * integer/long (number) element
     */
    UBYTE(NElementTypeGroup.NUMBER),
    BYTE(NElementTypeGroup.NUMBER),

    DOUBLE_QUOTED_STRING(NElementTypeGroup.STRING),
    SINGLE_QUOTED_STRING(NElementTypeGroup.STRING),
    BACKTICK_STRING(NElementTypeGroup.STRING),
    TRIPLE_DOUBLE_QUOTED_STRING(NElementTypeGroup.STRING),
    TRIPLE_SINGLE_QUOTED_STRING(NElementTypeGroup.STRING),
    TRIPLE_BACKTICK_STRING(NElementTypeGroup.STRING),
    LINE_STRING(NElementTypeGroup.STRING),
    BLOCK_STRING(NElementTypeGroup.STRING),
    CHAR(NElementTypeGroup.STRING),
    NAME(NElementTypeGroup.STRING),


    /**
     * boolean element
     */
    BOOLEAN(NElementTypeGroup.BOOLEAN),

    /**
     * date element
     */
    INSTANT(NElementTypeGroup.TEMPORAL),
    LOCAL_DATETIME(NElementTypeGroup.TEMPORAL),
    LOCAL_DATE(NElementTypeGroup.TEMPORAL),
    LOCAL_TIME(NElementTypeGroup.TEMPORAL),

    BINARY_STREAM(NElementTypeGroup.STREAM),
    CHAR_STREAM(NElementTypeGroup.STREAM),


    OPERATOR_SYMBOL(NElementTypeGroup.OPERATOR),
    BINARY_OPERATOR(NElementTypeGroup.OPERATOR),
    TERNARY_OPERATOR(NElementTypeGroup.OPERATOR),
    NARY_OPERATOR(NElementTypeGroup.OPERATOR),
    UNARY_OPERATOR(NElementTypeGroup.OPERATOR),
    FLAT_EXPR(NElementTypeGroup.OPERATOR),

    PAIR(NElementTypeGroup.CONTAINER),
    /**
     * array element
     */
    ARRAY(NElementTypeGroup.CONTAINER),
    NAMED_ARRAY(NElementTypeGroup.CONTAINER),
    PARAM_ARRAY(NElementTypeGroup.CONTAINER),
    FULL_ARRAY(NElementTypeGroup.CONTAINER),

    /**
     * object (list of key/val) element
     */
    OBJECT(NElementTypeGroup.CONTAINER),
    NAMED_OBJECT(NElementTypeGroup.CONTAINER),
    PARAM_OBJECT(NElementTypeGroup.CONTAINER),
    FULL_OBJECT(NElementTypeGroup.CONTAINER),

    UPLET(NElementTypeGroup.CONTAINER),
    NAMED_UPLET(NElementTypeGroup.CONTAINER),

    ORDERED_LIST(NElementTypeGroup.CONTAINER),
    UNORDERED_LIST(NElementTypeGroup.CONTAINER),

    /**
     * custom object that is not destructed. Cannot be null or primitive
     */
    CUSTOM(NElementTypeGroup.CUSTOM),
    EMPTY(NElementTypeGroup.OTHER),
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;
    private final NElementTypeGroup group;

    NElementType(NElementTypeGroup group) {
        this.id = NNameFormat.ID_NAME.format(name());
        this.group=group;
    }

    public static NOptional<NElementType> parse(String value) {
        return NEnumUtils.parseEnum(value, NElementType.class);
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
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
            case UBYTE:
            case USHORT:
            case UINT:
            case ULONG:
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
            case UBYTE:
            case USHORT:
            case UINT:
            case ULONG:
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
     * true if private type
     *
     * @return true if private type
     */

    public boolean isAnyOp() {
        switch (this) {
            case BINARY_OPERATOR:
            case UNARY_OPERATOR:
            case OPERATOR_SYMBOL:
            case FLAT_EXPR:
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
            case FULL_ARRAY:
            case NAMED_OBJECT:
            case FULL_OBJECT:
            case NAMED_UPLET:
            {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyParametrized() {
        switch (this) {
            case PARAM_ARRAY:
            case FULL_ARRAY:
            case PARAM_OBJECT:
            case FULL_OBJECT:
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
            case FULL_OBJECT:
                return true;
        }
        return false;
    }

    public boolean isAnyNamedArray() {
        switch (this) {
            case NAMED_OBJECT:
            case FULL_OBJECT:
                return true;
        }
        return false;
    }

    public boolean isAnyParamObject() {
        switch (this) {
            case PARAM_OBJECT:
            case FULL_OBJECT:
                return true;
        }
        return false;
    }

    public boolean isAnyParamArray() {
        switch (this) {
            case PARAM_OBJECT:
            case FULL_OBJECT:
                return true;
        }
        return false;
    }

    public boolean isAnyArray() {
        switch (this) {
            case ARRAY:
            case NAMED_ARRAY:
            case PARAM_ARRAY:
                return true;
        }
        return false;
    }

    public boolean isAnyObject() {
        switch (this) {
            case OBJECT:
            case NAMED_OBJECT:
            case PARAM_OBJECT:
            case FULL_OBJECT:
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
            case PARAM_OBJECT:
            case FULL_OBJECT:
            case ARRAY:
            case NAMED_ARRAY:
            case FULL_ARRAY:
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
            case PARAM_OBJECT:
            case FULL_OBJECT:
            case ARRAY:
            case NAMED_ARRAY:
            case PARAM_ARRAY:
                return true;
        }
        return false;
    }

    public NElementTypeGroup group() {
        return group;
    }

}
