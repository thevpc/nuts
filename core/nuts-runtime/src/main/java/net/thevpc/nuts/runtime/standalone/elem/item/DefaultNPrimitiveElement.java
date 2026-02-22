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
package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.builder.DefaultNPrimitiveElementBuilder;
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
    public static final DefaultNPrimitiveElement NULL = new DefaultNPrimitiveElement(NElementType.NULL, null);

    private final Object value;

    public DefaultNPrimitiveElement(NElementType type, Object value) {
        this(type, value, null, null, null);
    }

    public DefaultNPrimitiveElement(NElementType type, Object value, List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics, NElementMetadata metadata) {
        super(type, affixes, diagnostics, metadata);
        if (type == NElementType.NAME) {
            NAssert.requireNamedTrue(NElementUtils.isValidElementName((String) value), "valid name : " + (String) value);
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
        return value;
    }

    @Override
    public boolean isEmpty() {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return NStringUtils.isEmpty((String) value);
        }
        if (value instanceof CharSequence) {
            return NStringUtils.isEmpty(((CharSequence) value).toString());
        }
        return NLiteral.of(value).isEmpty();
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(value);
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
