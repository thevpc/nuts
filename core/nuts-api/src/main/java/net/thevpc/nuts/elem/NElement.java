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

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Nuts Element types are generic JSON like parsable objects. elements are a superset of JSON actually
 * that support multiple structured elements including json, xml, etc...
 * Elements are used to provide a convenient way to manipulate structured elements regardless of the underlying
 * format. Hence It's used for converting from json to xml as an example among many other use cases in the NAF
 * (Nuts Application Framework)
 *
 * @author thevpc
 * @app.category Elements
 * @since 0.5.6
 */
public interface NElement extends NElementDescribable<NElement>, NBlankable/*, NLiteral*/ {

    /**
     * element type
     *
     * @return element type
     */
    NElementType type();

    String toString(boolean compact);

    boolean isStream();

    boolean isNumber();

    boolean isFloatingNumber();

    boolean isOrdinalNumber();

    boolean isNull();

    boolean isString();

    boolean isByte();

    boolean isInt();

    boolean isLong();

    boolean isShort();

    boolean isFloat();

    boolean isDouble();

    boolean isBoolean();

    boolean isDecimalNumber();

    boolean isBigNumber();

    boolean isBigDecimal();

    boolean isBigInt();

    boolean isInstant();

    boolean isComplexNumber();

    boolean isTemporal();

    boolean isLocalTemporal();

    boolean isNamed();

    boolean isParametrized();

    boolean isNamedUplet();

    boolean isNamedUplet(String name);

    boolean isNamedObject();

    boolean isAnyNamedObject();

    boolean isAnyNamedObject(String name);

    boolean isParametrizedObject();

    boolean isNamedParametrizedObject();

    boolean isNamedParametrizedMatrix();

    boolean isNamedParametrizedObject(String name);

    boolean isNamedArray();

    boolean isAnyArray();

    boolean isListContainer();

    boolean isAnyObject();

    boolean isAnyMatrix();

    boolean isAnyUplet();

    boolean isAnyNamedArray();

    boolean isAnyNamedArray(String name);

    boolean isParametrizedArray();

    boolean isNamedParametrizedArray();

    boolean isNamedParametrizedArray(String name);


    boolean isNamedMatrix();

    boolean isAnyNamedMatrix();

    boolean isAnyNamedMatrix(String name);

    boolean isParametrizedMatrix();

    boolean isAnyParametrizedMatrix();

    boolean isNamed(String name);

    boolean isAnyParametrizedMatrix(String name);

    List<NElementAnnotation> annotations();

    /**
     * convert this element to {@link NPrimitiveElement} or throw
     * ClassCastException
     *
     * @return {@link NPrimitiveElement}
     */
    NOptional<NPrimitiveElement> asPrimitive();

    NOptional<NElement> resolve(String pattern);

    List<NElement> resolveAll(String pattern);

    /**
     * cast this element to {@link NObjectElement} or throw
     * ClassCastException
     *
     * @return {@link NObjectElement}
     */
    NOptional<NObjectElement> asObject();

    NOptional<NElement> asElementAt(int index);

    NOptional<NUpletElement> asUplet();

    NOptional<NPairElement> asPair();

    NOptional<NNumberElement> asInt();

    NOptional<NStringElement> asStr();

    /**
     * cast this element to {@link NObjectElement} or throw
     * ClassCastException
     *
     * @return {@link NObjectElement}
     */
    NOptional<NListContainerElement> asListContainer();

    NOptional<NNamedElement> asNamed();

    /**
     * cast this element to {@link NCustomElement} or throw
     * ClassCastException
     *
     * @return {@link NObjectElement}
     */
    NOptional<NCustomElement> asCustom();

    /**
     * true if can be cast to a custom element
     *
     * @return true if can be cast to a custom element
     */
    boolean isCustom();

    /**
     * convert this element to {@link NArrayElement} or throw
     * ClassCastException
     *
     * @return {@link NArrayElement}
     */
    NOptional<NArrayElement> asArray();

    /**
     * return true if this element can be cast to {@link NPrimitiveElement}
     *
     * @return true if this element can be cast to {@link NPrimitiveElement}
     */
    boolean isPrimitive();

    boolean isAnyString();


    /**
     * return true if this element can be cast to {@link NObjectElement}
     *
     * @return true if this element can be cast to {@link NObjectElement}
     */
    boolean isObject();

    /**
     * return true if this element can be cast to {@link NArrayElement}
     *
     * @return true if this element can be cast to {@link NArrayElement}
     */
    boolean isArray();


    /**
     * return true if this element is empty:
     * <ul>
     *     <li>primitives are empty only if they are null or an empty string</li>
     *     <li>objects are empty if they do not have any field</li>
     *     <li>arrays are empty if they do not have any item</li>
     *     <li>customs are NEVER empty</li>
     * </ul>
     *
     * @return return true if this element is empty
     */
    boolean isEmpty();

    /**
     * return true if this element is blank:
     * <ul>
     *     <li>primitives are blank only if they are null or a blank string</li>
     *     <li>objects are blank if they do not have any field</li>
     *     <li>arrays are blank if they do not have any item</li>
     *     <li>customs are NEVER blank</li>
     * </ul>
     *
     * @return return true if this element is blank
     */
    @Override
    boolean isBlank();

    NElementBuilder builder();

    boolean isPair();

    /**
     * true if pair with primitive key
     *
     * @return true if pair with primitive key
     */
    boolean isSimplePair();

    /**
     * true if pair with string like key
     *
     * @return true if pair with string like key
     */
    boolean isNamedPair();

    /**
     * best effort to convert to NListContainerElement
     *
     * @return NListContainerElement
     */
    NOptional<NListContainerElement> toListContainer();

    NElementComments comments();

    boolean isName();

    /**
     * converts the current element to a named uplet is applicable without information loss
     *
     * @return
     */
    NOptional<NUpletElement> toNamedUplet();

    /**
     * converts the current element to a named uplet
     *
     * @return
     */
    NOptional<NPairElement> toNamedPair();

    NOptional<NObjectElement> toNamedObject();

    NOptional<NObjectElement> toObject();

    NOptional<NArrayElement> toNamedArray();

    NOptional<NArrayElement> toArray();

    NArrayElement wrapIntoArray();

    NObjectElement wrapIntoObject();

    NUpletElement wrapIntoUplet();

    NArrayElement wrapIntoNamedArray(String name);

    NObjectElement wrapIntoNamedObject(String name);

    NUpletElement wrapIntoNamedUplet(String name);

    NPairElement wrapIntoNamedPair(String name);

    NLiteral asLiteral();

    NOptional<NStringElement> asString();

    NOptional<String> asStringValue();

    NOptional<LocalTime> asLocalTimeValue();

    NOptional<BigInteger> asBigIntValue();

    NOptional<BigDecimal> asBigDecimalValue();

    NOptional<Boolean> asBooleanValue();

    NOptional<Number> asNumberValue();

    NOptional<Character> asCharValue();

    NOptional<Instant> asInstantValue();

    NOptional<NBigComplex> asBigComplexValue();

    NOptional<NDoubleComplex> asDoubleComplexValue();

    NOptional<NFloatComplex> asFloatComplexValue();

    NOptional<LocalDate> asLocalDateValue();

    NOptional<LocalDateTime> asLocalDateTimeValue();

    NOptional<Byte> asByteValue();

    NOptional<Short> asShortValue();

    NOptional<Integer> asIntValue();

    NOptional<Long> asLongValue();

    NOptional<Float> asFloatValue();

    NOptional<Double> asDoubleValue();

    NOptional<NNumberElement> asNumber();
}
