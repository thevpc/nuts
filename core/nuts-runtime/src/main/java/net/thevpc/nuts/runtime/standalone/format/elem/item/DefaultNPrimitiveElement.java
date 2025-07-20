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
package net.thevpc.nuts.runtime.standalone.format.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.format.elem.NElementToStringHelper;
import net.thevpc.nuts.runtime.standalone.format.elem.builder.DefaultNPrimitiveElementBuilder;
import net.thevpc.nuts.util.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author thevpc
 */
public class DefaultNPrimitiveElement extends AbstractNElement implements NPrimitiveElement {
    private final Object value;

    public DefaultNPrimitiveElement(NElementType type, Object value, NElementAnnotation[] annotations, NElementComments comments) {
        super(type, annotations, comments);
        if (type == NElementType.NAME) {
            NAssert.requireTrue(NElementUtils.isValidElementName((String) value), "valid name : " + (String) value);
        }
        this.value = value;
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
        return NLiteral.of(value).asObject().orNull();
    }

    @Override
    public boolean isBoolean() {
        return NLiteral.of(value).isBoolean();
    }

    @Override
    public boolean isNull() {
        return NLiteral.of(value).isNull();
    }

    @Override
    public boolean isByte() {
        return NLiteral.of(value).isByte();
    }

    @Override
    public boolean isInt() {
        return NLiteral.of(value).isInt();
    }

    @Override
    public boolean isDecimalNumber() {
        return NLiteral.of(value).isDecimalNumber();
    }

    @Override
    public boolean isBigNumber() {
        return NLiteral.of(value).isBigNumber();
    }

    @Override
    public boolean isBigDecimal() {
        return NLiteral.of(value).isBigDecimal();
    }

    @Override
    public boolean isBigInt() {
        return NLiteral.of(value).isBigInt();
    }

    @Override
    public boolean isString() {
        return NLiteral.of(value).isString();
    }

    @Override
    public boolean isLong() {
        return NLiteral.of(value).isLong();
    }

    @Override
    public boolean isShort() {
        return NLiteral.of(value).isShort();
    }

    @Override
    public boolean isFloat() {
        return NLiteral.of(value).isFloat();
    }

    @Override
    public boolean isDouble() {
        return NLiteral.of(value).isDouble();
    }

    @Override
    public boolean isInstant() {
        return NLiteral.of(value).isInstant();
    }

    @Override
    public boolean isEmpty() {
        return NLiteral.of(value).isEmpty();
    }

    @Override
    public boolean isBlank() {
        return NLiteral.of(value).isBlank();
    }

    @Override
    public NOptional<BigInteger> asBigIntValue() {
        return NLiteral.of(value).asBigInt();
    }

    @Override
    public NOptional<BigDecimal> asBigDecimalValue() {
        return NLiteral.of(value).asBigDecimal();
    }

    @Override
    public boolean isNumber() {
        return NLiteral.of(value).isNumber();
    }

    @Override
    public boolean isFloatingNumber() {
        return NLiteral.of(value).isFloatingNumber();
    }

    @Override
    public boolean isOrdinalNumber() {
        return NLiteral.of(value).isOrdinalNumber();
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
            case BYTE:
            case LONG:
            case BIG_DECIMAL:
            case BIG_INT:
            case SHORT:
            case INT:
            case FLOAT:
            case DOUBLE:
                NNumberElement r = asNumber().get();
                NNumberLayout layout = r.numberLayout();
                String suffix = r.numberSuffix();
                switch (layout) {
                    case DECIMAL: {
                        sb.append(String.valueOf(this.asNumberValue().get()));
                        break;
                    }
                    case HEXADECIMAL: {
                        sb.append(asBigIntValue().get().toString(16));
                        break;
                    }
                    case OCTAL: {
                        sb.append(asBigIntValue().get().toString(8));
                        break;
                    }
                    case BINARY: {
                        sb.append(asBigIntValue().get().toString(2));
                        break;
                    }
                }
                if (!NBlankable.isBlank(suffix)) {
                    sb.append(suffix);
                }
                break;
            case INSTANT:
            case LOCAL_TIME:
            case LOCAL_DATE:
            case LOCAL_DATETIME:
            case NAME:
            case BOOLEAN:
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

//    @Override
//    public String toStringLiteral() {
//        return value.toStringLiteral();
//    }
//
//
//    @Override
//    public NOptional<Object> asObjectAt(int index) {
//        return value.asObjectAt(index);
//    }

    @Override
    public NPrimitiveElementBuilder builder() {
        return new DefaultNPrimitiveElementBuilder()
                .copyFrom(this);
    }

    @Override
    public NLiteral asLiteral() {
        return NLiteral.of(value);
    }

    @Override
    public NOptional<Temporal> asTemporalValue() {
        if (value != null && value instanceof Temporal) {
            return NOptional.of((Temporal) value);
        }
        return super.asTemporalValue();
    }
}
