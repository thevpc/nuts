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

    DefaultNPrimitiveElement(NElementType type, Object value, NElementAnnotation[] annotations,NElementComments comments) {
        super(type, annotations, comments);
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
        return value.asObjectValue();
    }

    @Override
    public Object asObjectValue() {
        return value.asObjectValue();
    }

    @Override
    public NOptional<Instant> asInstantValue() {
        return value.asInstantValue();
    }

    @Override
    public NOptional<Number> asNumberValue() {
        return value.asNumberValue();
    }

    @Override
    public NOptional<Boolean> asBooleanValue() {
        return value.asBooleanValue();
    }

    @Override
    public NOptional<Long> asLongValue() {
        return value.asLongValue();
    }

    @Override
    public NOptional<Double> asDoubleValue() {
        return value.asDoubleValue();
    }

    @Override
    public NOptional<Float> asFloatValue() {
        return value.asFloatValue();
    }

    @Override
    public NOptional<LocalDate> asLocalDateValue() {
        return value.asLocalDateValue();
    }

    @Override
    public NOptional<LocalTime> asLocalTimeValue() {
        return value.asLocalTimeValue();
    }

    @Override
    public NOptional<NBigComplex> asBigComplexValue() {
        return value.asBigComplexValue();
    }

    @Override
    public NOptional<NDoubleComplex> asDoubleComplexValue() {
        return value.asDoubleComplexValue();
    }

    @Override
    public NOptional<NFloatComplex> asFloatComplexValue() {
        return value.asFloatComplexValue();
    }

    @Override
    public NOptional<LocalDateTime> asLocalDateTimeValue() {
        return value.asLocalDateTimeValue();
    }

    @Override
    public NOptional<Byte> asByteValue() {
        return value.asByteValue();
    }

    @Override
    public NOptional<Short> asShortValue() {
        return value.asShortValue();
    }

    @Override
    public NOptional<Character> asCharValue() {
        return value.asCharValue();
    }

    @Override
    public NOptional<Integer> asIntValue() {
        return value.asIntValue();
    }

    @Override
    public NOptional<String> asStringValue() {
        return value.asStringValue();
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
    public NOptional<BigInteger> asBigIntValue() {
        return value.asBigIntValue();
    }

    @Override
    public NOptional<BigDecimal> asBigDecimalValue() {
        return value.asBigDecimalValue();
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
            case STRING:
                sb.append(NStringUtils.formatStringLiteral(String.valueOf(value), NQuoteType.DOUBLE));
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
                sb.append(String.valueOf(this.asNumberValue().get()));
                break;
            case INSTANT:
            case LOCAL_TIME:
            case LOCAL_DATE:
            case LOCAL_DATETIME:
                sb.append(NStringUtils.formatStringLiteral(this.asInstantValue().get().toString(), NQuoteType.DOUBLE));
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
    public NOptional<Object> asObjectValueAt(int index) {
        return value.asObjectValueAt(index);
    }

    @Override
    public NPrimitiveElementBuilder builder() {
        return new DefaultNPrimitiveElementBuilder()
                .copyFrom(this);
    }
}
