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
package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author thevpc
 */
class DefaultNPrimitiveElement extends AbstractNElement implements NPrimitiveElement {
    private final NLiteral value;

    DefaultNPrimitiveElement(NElementType type, Object value, NElementAnnotation[] annotations, NElementComments comments) {
        super(type, annotations, comments);
        if (type == NElementType.NAME) {
            NAssert.requireTrue(NElements.isValidName((String) value), "valid name");
        }
        this.value = NLiteral.of(value);
    }

    @Override
    public List<NElement> resolveAll(String pattern) {
        pattern = NStringUtils.trimToNull(pattern);
        if (pattern == null || pattern.equals(".")) {
            return new ArrayList<>(Arrays.asList(this));
        }
        return new ArrayList<>();
    }

    @Override
    public Object value() {
        return value.asRawObject();
    }

    @Override
    public Object asRawObject() {
        return value.asRawObject();
    }

    @Override
    public NOptional<Instant> asInstant() {
        return value.asInstant();
    }

    @Override
    public NOptional<Number> asNumber() {
        return value.asNumber();
    }

    @Override
    public NOptional<Boolean> asBoolean() {
        return value.asBoolean();
    }

    @Override
    public NOptional<Long> asLong() {
        return value.asLong();
    }

    @Override
    public NOptional<Double> asDouble() {
        return value.asDouble();
    }

    @Override
    public NOptional<Float> asFloat() {
        return value.asFloat();
    }

    @Override
    public NOptional<LocalDate> asLocalDate() {
        return value.asLocalDate();
    }

    @Override
    public NOptional<LocalTime> asLocalTime() {
        return value.asLocalTime();
    }

    @Override
    public NOptional<NBigComplex> asBigComplex() {
        return value.asBigComplex();
    }

    @Override
    public NOptional<NDoubleComplex> asDoubleComplex() {
        return value.asDoubleComplex();
    }

    @Override
    public NOptional<NFloatComplex> asFloatComplex() {
        return value.asFloatComplex();
    }

    @Override
    public NOptional<LocalDateTime> asLocalDateTime() {
        return value.asLocalDateTime();
    }

    @Override
    public NOptional<Byte> asByte() {
        return value.asByte();
    }

    @Override
    public NOptional<Short> asShort() {
        return value.asShort();
    }

    @Override
    public NOptional<Character> asChar() {
        return value.asChar();
    }

    @Override
    public NOptional<Integer> asInt() {
        return value.asInt();
    }

    @Override
    public NOptional<String> asString() {
        return value.asString();
    }

    @Override
    public boolean isBoolean() {
        return value.isBoolean();
    }

    @Override
    public boolean isNull() {
        return value.isNull();
    }

    @Override
    public boolean isByte() {
        return value.isByte();
    }

    @Override
    public boolean isInt() {
        return value.isInt();
    }

    @Override
    public boolean isDecimalNumber() {
        return value.isDecimalNumber();
    }

    @Override
    public boolean isBigNumber() {
        return value.isBigNumber();
    }

    @Override
    public boolean isBigDecimal() {
        return value.isBigDecimal();
    }

    @Override
    public boolean isBigInt() {
        return value.isBigInt();
    }

    @Override
    public boolean isString() {
        return value.isString();
    }

    @Override
    public boolean isLong() {
        return value.isLong();
    }

    @Override
    public boolean isShort() {
        return value.isShort();
    }

    @Override
    public boolean isFloat() {
        return value.isFloat();
    }

    @Override
    public boolean isDouble() {
        return value.isDouble();
    }

    @Override
    public boolean isInstant() {
        return value.isInstant();
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public boolean isBlank() {
        return value.isBlank();
    }

    @Override
    public NOptional<BigInteger> asBigInt() {
        return value.asBigInt();
    }

    @Override
    public NOptional<BigDecimal> asBigDecimal() {
        return value.asBigDecimal();
    }

    @Override
    public boolean isNumber() {
        return value.isNumber();
    }

    @Override
    public String toString() {
        return toString(true);
    }

    @Override
    public String toString(boolean compact) {
        NStringBuilder sb = new NStringBuilder();
        sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
        switch (type()) {
            case NULL:
                sb.append("null");
                break;
            case CHAR:
                sb.append(NStringUtils.formatStringLiteral(String.valueOf(value), NElementType.SINGLE_QUOTED_STRING));
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING:
                sb.append(NStringUtils.formatStringLiteral(String.valueOf(value), type()));
                break;
            case NAME:
                sb.append(String.valueOf(value));
                break;
            case BOOLEAN:
                sb.append(String.valueOf(value));
                break;
            case BYTE:
            case LONG:
            case BIG_DECIMAL:
            case BIG_INTEGER:
            case SHORT:
            case INTEGER:
            case FLOAT:
            case DOUBLE:
                sb.append(String.valueOf(this.asNumber().get()));
                break;
            case INSTANT:
            case LOCAL_TIME:
            case LOCAL_DATE:
            case LOCAL_DATETIME:
                sb.append(NStringUtils.formatStringLiteral(this.asInstant().get().toString(), NElementType.DOUBLE_QUOTED_STRING));
                break;
            default: {
                sb.append(String.valueOf(value));
            }
        }
        sb.append(NElementToStringHelper.trailingComments(this, compact));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNPrimitiveElement that = (DefaultNPrimitiveElement) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toStringLiteral() {
        return value.toStringLiteral();
    }


    @Override
    public NOptional<Object> asObjectAt(int index) {
        return value.asObjectAt(index);
    }

    @Override
    public NPrimitiveElementBuilder builder() {
        return new DefaultNPrimitiveElementBuilder()
                .copyFrom(this);
    }
}
