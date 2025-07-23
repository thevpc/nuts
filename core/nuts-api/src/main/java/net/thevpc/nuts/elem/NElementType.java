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

import java.util.function.Function;

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
    INT,
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


    OP_PLUS,
    OP_PLUS2,
    OP_PLUS3,
    OP_MINUS,
    OP_MINUS2,
    OP_MINUS3,
    OP_MUL,
    OP_MUL2,
    OP_MUL3,
    OP_DIV,
    OP_HAT,
    OP_HAT2,
    OP_HAT3,
    OP_REM,
    OP_REM2,
    OP_REM3,
    OP_EQ,
    OP_EQ2,
    OP_EQ3,
    OP_TILDE,
    OP_TILDE2,
    OP_TILDE3,
    OP_LT,
    OP_LT2,
    OP_LT3,
    OP_GT,
    OP_GT2,
    OP_GT3,
    OP_LTE,
    OP_GTE,
    OP_ASSIGN,
    OP_ASSIGN_EQ,
    OP_MINUS_GT,
    OP_MINUS2_GT,
    OP_EQ_GT,
    OP_EQ2_GT,
    OP_LT_MINUS2,
    OP_LT_EQ2,
    OP_PIPE,
    OP_PIPE2,
    OP_PIPE3,
    OP_AND,
    OP_AND2,
    OP_AND3,
    OP_HASH,
    OP_HASH2,
    OP_HASH3,
    OP_HASH4,
    OP_HASH5,
    OP_HASH6,
    OP_HASH7,
    OP_HASH8,
    OP_HASH9,
    OP_HASH10,
    OP_AT2,

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
        return NEnumUtils.parseEnum(value, NElementType.class, new Function<NEnumUtils.EnumValue, NOptional<NElementType>>() {
            @Override
            public NOptional<NElementType> apply(NEnumUtils.EnumValue enumValue) {
                switch (enumValue.getValue().trim()) {
                    case "+":
                        return NOptional.of(OP_PLUS);
                    case "++":
                        return NOptional.of(OP_PLUS2);
                    case "+++":
                        return NOptional.of(OP_PLUS3);
                    case "-":
                        return NOptional.of(OP_MINUS);
                    case "--":
                        return NOptional.of(OP_MINUS2);
                    case "---":
                        return NOptional.of(OP_MINUS3);
                    case "*":
                        return NOptional.of(OP_MUL);
                    case "**":
                        return NOptional.of(OP_MUL2);
                    case "***":
                        return NOptional.of(OP_MUL3);
                    case "/":
                        return NOptional.of(OP_DIV);
                    case "^":
                        return NOptional.of(OP_HAT);
                    case "^^":
                        return NOptional.of(OP_HAT2);
                    case "^^^":
                        return NOptional.of(OP_HAT3);
                    case "%":
                        return NOptional.of(OP_REM);
                    case "%%":
                        return NOptional.of(OP_REM2);
                    case "%%%":
                        return NOptional.of(OP_REM3);
                    case "=":
                        return NOptional.of(OP_EQ);
                    case "==":
                        return NOptional.of(OP_EQ2);
                    case "===":
                        return NOptional.of(OP_EQ3);
                    case "~":
                        return NOptional.of(OP_TILDE);
                    case "~~":
                        return NOptional.of(OP_TILDE2);
                    case "~~~":
                        return NOptional.of(OP_TILDE3);
                    case "<":
                        return NOptional.of(OP_LT);
                    case "<<":
                        return NOptional.of(OP_LT2);
                    case "<<<":
                        return NOptional.of(OP_LT3);
                    case ">":
                        return NOptional.of(OP_GT);
                    case ">>":
                        return NOptional.of(OP_GT2);
                    case ">>>":
                        return NOptional.of(OP_GT3);
                    case "<=":
                        return NOptional.of(OP_LTE);
                    case ">=":
                        return NOptional.of(OP_GTE);
                    case ":=":
                        return NOptional.of(OP_ASSIGN);
                    case ":==":
                        return NOptional.of(OP_ASSIGN_EQ);
                    case "->":
                        return NOptional.of(OP_MINUS_GT);
                    case "-->":
                        return NOptional.of(OP_MINUS2_GT);
                    case "=>":
                        return NOptional.of(OP_EQ_GT);
                    case "==>":
                        return NOptional.of(OP_EQ2_GT);
                    case "<--":
                        return NOptional.of(OP_LT_MINUS2);
                    case "<==":
                        return NOptional.of(OP_LT_EQ2);
                    case "|":
                        return NOptional.of(OP_PIPE);
                    case "||":
                        return NOptional.of(OP_PIPE2);
                    case "|||":
                        return NOptional.of(OP_PIPE3);
                    case "&":
                        return NOptional.of(OP_AND);
                    case "&&":
                        return NOptional.of(OP_AND2);
                    case "&&&":
                        return NOptional.of(OP_AND3);
                    case "#":
                        return NOptional.of(OP_HASH);
                    case "##":
                        return NOptional.of(OP_HASH2);
                    case "###":
                        return NOptional.of(OP_HASH3);
                    case "#####":
                        return NOptional.of(OP_HASH4);
                    case "######":
                        return NOptional.of(OP_HASH5);
                    case "#######":
                        return NOptional.of(OP_HASH6);
                    case "########":
                        return NOptional.of(OP_HASH7);
                    case "#########":
                        return NOptional.of(OP_HASH8);
                    case "##########":
                        return NOptional.of(OP_HASH9);
                    case "###########":
                        return NOptional.of(OP_HASH10);
                    case "@@":
                        return NOptional.of(OP_AT2);
                }
                switch (enumValue.getNormalizedValue()) {
                    case "PLUS":
                        return NOptional.of(OP_PLUS);
                    case "PLUS2":
                        return NOptional.of(OP_PLUS2);
                    case "PLUS3":
                        return NOptional.of(OP_PLUS3);
                    case "MINUS":
                        return NOptional.of(OP_MINUS);
                    case "MINUS2":
                        return NOptional.of(OP_MINUS2);
                    case "MINUS3":
                        return NOptional.of(OP_MINUS3);
                    case "MUL":
                        return NOptional.of(OP_MUL);
                    case "MUL2":
                        return NOptional.of(OP_MUL2);
                    case "MUL3":
                        return NOptional.of(OP_MUL3);
                    case "DIV":
                        return NOptional.of(OP_DIV);
                    case "HAT":
                        return NOptional.of(OP_HAT);
                    case "HAT2":
                        return NOptional.of(OP_HAT2);
                    case "HAT3":
                        return NOptional.of(OP_HAT3);
                    case "REM":
                        return NOptional.of(OP_REM);
                    case "REM2":
                        return NOptional.of(OP_REM2);
                    case "REM3":
                        return NOptional.of(OP_REM3);
                    case "EQ":
                        return NOptional.of(OP_EQ);
                    case "EQ2":
                        return NOptional.of(OP_EQ2);
                    case "EQ3":
                        return NOptional.of(OP_EQ3);
                    case "TILDE":
                        return NOptional.of(OP_TILDE);
                    case "TILDE2":
                        return NOptional.of(OP_TILDE2);
                    case "TILDE3":
                        return NOptional.of(OP_TILDE3);
                    case "LT":
                        return NOptional.of(OP_LT);
                    case "LT2":
                        return NOptional.of(OP_LT2);
                    case "LT3":
                        return NOptional.of(OP_LT3);
                    case "GT":
                        return NOptional.of(OP_GT);
                    case "GT2":
                        return NOptional.of(OP_GT2);
                    case "GT3":
                        return NOptional.of(OP_GT3);
                    case "LTE":
                        return NOptional.of(OP_LTE);
                    case "GTE":
                        return NOptional.of(OP_GTE);
                    case "ASSIGN":
                        return NOptional.of(OP_ASSIGN);
                    case "ASSIGN_EQ":
                        return NOptional.of(OP_ASSIGN_EQ);
                    case "MINUS_GT":
                        return NOptional.of(OP_MINUS_GT);
                    case "MINUS2_GT":
                        return NOptional.of(OP_MINUS2_GT);
                    case "EQ_GT":
                        return NOptional.of(OP_EQ_GT);
                    case "EQ2_GT":
                        return NOptional.of(OP_EQ2_GT);
                    case "LT_MINUS2":
                        return NOptional.of(OP_LT_MINUS2);
                    case "LT_EQ2":
                        return NOptional.of(OP_LT_EQ2);
                    case "PIPE":
                        return NOptional.of(OP_PIPE);
                    case "PIPE2":
                        return NOptional.of(OP_PIPE2);
                    case "PIPE3":
                        return NOptional.of(OP_PIPE3);
                    case "AND":
                        return NOptional.of(OP_AND);
                    case "AND2":
                        return NOptional.of(OP_AND2);
                    case "AND3":
                        return NOptional.of(OP_AND3);
                    case "HASH":
                        return NOptional.of(OP_HASH);
                    case "HASH2":
                        return NOptional.of(OP_HASH2);
                    case "HASH3":
                        return NOptional.of(OP_HASH3);
                    case "HASH4":
                        return NOptional.of(OP_HASH4);
                    case "HASH5":
                        return NOptional.of(OP_HASH5);
                    case "HASH6":
                        return NOptional.of(OP_HASH6);
                    case "HASH7":
                        return NOptional.of(OP_HASH7);
                    case "HASH8":
                        return NOptional.of(OP_HASH8);
                    case "HASH9":
                        return NOptional.of(OP_HASH9);
                    case "HASH10":
                        return NOptional.of(OP_HASH10);
                    case "AT2":
                        return NOptional.of(OP_AT2);
                }
                return null;
            }
        });
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
            case OP_PLUS:
            case OP_PLUS2:
            case OP_PLUS3:
            case OP_MINUS:
            case OP_MINUS2:
            case OP_MINUS3:
            case OP_MUL:
            case OP_MUL2:
            case OP_MUL3:
            case OP_DIV:
            case OP_HAT:
            case OP_HAT2:
            case OP_HAT3:
            case OP_REM:
            case OP_REM2:
            case OP_REM3:
            case OP_EQ:
            case OP_EQ2:
            case OP_EQ3:
            case OP_TILDE:
            case OP_TILDE2:
            case OP_TILDE3:
            case OP_LT:
            case OP_LT2:
            case OP_LT3:
            case OP_GT:
            case OP_GT2:
            case OP_GT3:
            case OP_LTE:
            case OP_GTE:
            case OP_ASSIGN:
            case OP_ASSIGN_EQ:
            case OP_MINUS_GT:
            case OP_MINUS2_GT:
            case OP_EQ_GT:
            case OP_EQ2_GT:
            case OP_LT_MINUS2:
            case OP_LT_EQ2:
            case OP_PIPE:
            case OP_PIPE2:
            case OP_PIPE3:
            case OP_AND:
            case OP_AND2:
            case OP_AND3:
            case OP_HASH:
            case OP_HASH2:
            case OP_HASH3:
            case OP_HASH4:
            case OP_HASH5:
            case OP_HASH6:
            case OP_HASH7:
            case OP_HASH8:
            case OP_HASH9:
            case OP_HASH10:
            case OP_AT2:
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
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING:
                return true;
        }
        return false;
    }

    public boolean isAnyStringOrName() {
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
            case NAMED_MATRIX:
            case NAMED_PARAMETRIZED_MATRIX: {
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
            case PARAMETRIZED_MATRIX:
            case NAMED_PARAMETRIZED_MATRIX: {
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
            case NAMED_PARAMETRIZED_MATRIX:
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
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING:
            case CHAR:
                return NElementTypeGroup.STRING;
            case REGEX:
                return NElementTypeGroup.REGEX;
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
            case MATRIX:
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
                return NElementTypeGroup.CONTAINER;
            case BINARY_STREAM:
            case CHAR_STREAM:
                return NElementTypeGroup.STREAM;
            case OP_PLUS:
            case OP_PLUS2:
            case OP_PLUS3:
            case OP_MINUS:
            case OP_MINUS2:
            case OP_MINUS3:
            case OP_MUL:
            case OP_MUL2:
            case OP_MUL3:
            case OP_DIV:
            case OP_HAT:
            case OP_HAT2:
            case OP_HAT3:
            case OP_REM:
            case OP_REM2:
            case OP_REM3:
            case OP_EQ:
            case OP_EQ2:
            case OP_EQ3:
            case OP_TILDE:
            case OP_TILDE2:
            case OP_TILDE3:
            case OP_LT:
            case OP_LT2:
            case OP_LT3:
            case OP_GT:
            case OP_GT2:
            case OP_GT3:
            case OP_LTE:
            case OP_GTE:
            case OP_ASSIGN:
            case OP_ASSIGN_EQ:
            case OP_MINUS_GT:
            case OP_MINUS2_GT:
            case OP_EQ_GT:
            case OP_EQ2_GT:
            case OP_LT_MINUS2:
            case OP_LT_EQ2:
            case OP_PIPE:
            case OP_PIPE2:
            case OP_PIPE3:
            case OP_AND:
            case OP_AND2:
            case OP_AND3:
            case OP_HASH:
            case OP_HASH2:
            case OP_HASH3:
            case OP_HASH4:
            case OP_HASH5:
            case OP_HASH6:
            case OP_HASH7:
            case OP_HASH8:
            case OP_HASH9:
            case OP_HASH10:
            case OP_AT2:
                return NElementTypeGroup.OPERATOR;
            case CUSTOM:
                return NElementTypeGroup.CUSTOM;
            case ALIAS:
                return NElementTypeGroup.OTHER;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String opSymbol() {
        NAssert.requireTrue(isAnyOp(), "type as op");
        switch (this) {
            case OP_PLUS:
                return "+";
            case OP_PLUS2:
                return "++";
            case OP_PLUS3:
                return "+++";
            case OP_MINUS:
                return "-";
            case OP_MINUS2:
                return "--";
            case OP_MINUS3:
                return "---";
            case OP_MUL:
                return "*";
            case OP_MUL2:
                return "**";
            case OP_MUL3:
                return "***";
            case OP_DIV:
                return "/";
            case OP_HAT:
                return "^";
            case OP_HAT2:
                return "^^";
            case OP_HAT3:
                return "^^^";
            case OP_REM:
                return "%";
            case OP_REM2:
                return "%%";
            case OP_REM3:
                return "%%%";
            case OP_EQ:
                return "=";
            case OP_EQ2:
                return "==";
            case OP_EQ3:
                return "===";
            case OP_TILDE:
                return "~";
            case OP_TILDE2:
                return "~~";
            case OP_TILDE3:
                return "~~~";
            case OP_LT:
                return "<";
            case OP_LT2:
                return "<<";
            case OP_LT3:
                return "<<<";
            case OP_GT:
                return ">";
            case OP_GT2:
                return ">>";
            case OP_GT3:
                return ">>>";
            case OP_LTE:
                return "<=";
            case OP_GTE:
                return ">=";
            case OP_ASSIGN:
                return ":=";
            case OP_ASSIGN_EQ:
                return ":==";
            case OP_MINUS_GT:
                return "->";
            case OP_MINUS2_GT:
                return "-->";
            case OP_EQ_GT:
                return "=>";
            case OP_EQ2_GT:
                return "==>";
            case OP_LT_MINUS2:
                return "<--";
            case OP_LT_EQ2:
                return "<==";
            case OP_PIPE:
                return "|";
            case OP_PIPE2:
                return "||";
            case OP_PIPE3:
                return "|||";
            case OP_AND:
                return "&";
            case OP_AND2:
                return "&&";
            case OP_AND3:
                return "&&&";
            case OP_HASH:
                return "#";
            case OP_HASH2:
                return "##";
            case OP_HASH3:
                return "###";
            case OP_HASH4:
                return "#####";
            case OP_HASH5:
                return "######";
            case OP_HASH6:
                return "#######";
            case OP_HASH7:
                return "########";
            case OP_HASH8:
                return "#########";
            case OP_HASH9:
                return "#########";
            case OP_HASH10:
                return "##########";
            case OP_AT2:
                return "@@";
        }
        NAssert.requireTrue(false, "type as op");
        return "";//never happens
    }
}
