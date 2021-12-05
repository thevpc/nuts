/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import java.time.Instant;

/**
 * Nuts Element types are generic JSON like parsable objects. elements are a superset of JSON actually
 * that support multiple structured elements including json, xml, etc...
 * Elements are used to provide a convenient way to manipulate structured elements regardless of the underlying
 * format. Hence it is used for converting from json to xml as an example among many other use cases in the NAF
 * (Nuts Application Framework)
 *
 * @author thevpc
 * @app.category Elements
 * @since 0.5.6
 */
public interface NutsElement extends NutsDescribable, NutsBlankable {

    /**
     * element type
     *
     * @return element type
     */
    NutsElementType type();

    /**
     * convert this element to {@link NutsPrimitiveElement} or throw
     * ClassCastException
     *
     * @return {@link NutsPrimitiveElement}
     */
    NutsPrimitiveElement asPrimitive();

    /**
     * cast this element to {@link NutsObjectElement} or throw
     * ClassCastException
     *
     * @return {@link NutsObjectElement}
     */
    NutsObjectElement asObject();

    /**
     * cast this element to {@link NutsCustomElement} or throw
     * ClassCastException
     *
     * @return {@link NutsObjectElement}
     */
    NutsCustomElement asCustom();

    /**
     * true if can be cast to a custom element
     * @return true if can be cast to a custom element
     */
    boolean isCustom();

    /**
     * convert this element to {@link NutsArrayElement} or throw
     * ClassCastException
     *
     * @return {@link NutsArrayElement}
     */
    NutsArrayElement asArray();

    /**
     * return true if this element can be cast to {@link NutsPrimitiveElement}
     * @return true if this element can be cast to {@link NutsPrimitiveElement}
     */
    boolean isPrimitive();

    /**
     * return true if this element can be cast to {@link NutsPrimitiveElement} of type number
     * @return true if this element can be cast to {@link NutsPrimitiveElement} of type number
     */
    boolean isNumber();

    /**
     * return true if this element can be cast to {@link NutsPrimitiveElement} of type null
     * @return true if this element can be cast to {@link NutsPrimitiveElement} of type null
     */
    boolean isNull();

    /**
     * return true if this element can be cast to {@link NutsPrimitiveElement} of type string
     * @return true if this element can be cast to {@link NutsPrimitiveElement} of type string
     */
    boolean isString();

    /**
     * return true if this element can be cast to {@link NutsPrimitiveElement} of type byte or can be converted to
     * @return true if this element can be cast to {@link NutsPrimitiveElement} of type byte or can be converted to
     */
    boolean isByte();

    /**
     * return true if this element can be cast to {@link NutsPrimitiveElement} of type int or can be converted to
     * @return true if this element can be cast to {@link NutsPrimitiveElement} of type int or can be converted to
     */
    boolean isInt();

    /**
     * return true if this element can be cast to {@link NutsPrimitiveElement} of type long or can be converted to
     * @return true if this element can be cast to {@link NutsPrimitiveElement} of type long or can be converted to
     */
    boolean isLong();

    /**
     * return true if this element can be cast to {@link NutsPrimitiveElement} of type short or can be converted to
     * @return true if this element can be cast to {@link NutsPrimitiveElement} of type short or can be converted to
     */
    boolean isShort();

    /**
     * return true if this element can be cast to {@link NutsPrimitiveElement} of type float or can be converted to
     * @return true if this element can be cast to {@link NutsPrimitiveElement} of type float or can be converted to
     */
    boolean isFloat();

    /**
     * return true if this element can be cast to {@link NutsPrimitiveElement} of type double or can be converted to
     * @return true if this element can be cast to {@link NutsPrimitiveElement} of type double or can be converted to
     */
    boolean isDouble();

    /**
     * return true if this element can be cast to {@link NutsPrimitiveElement} of type Instant or can be converted to
     * @return true if this element can be cast to {@link NutsPrimitiveElement} of type Instant or can be converted to
     */
    boolean isInstant();

    /**
     * return true if this element can be cast to {@link NutsObjectElement}
     * @return true if this element can be cast to {@link NutsObjectElement}
     */
    boolean isObject();

    /**
     * return true if this element can be cast to {@link NutsArrayElement}
     * @return true if this element can be cast to {@link NutsArrayElement}
     */
    boolean isArray();


    /**
     * return string or null if this element is a valid primitive.
     *
     * numbers and any other primitive value will be converted to string.
     * A class cast exception is thrown, though, if the element is not primitive.
     *
     * this is equivalent to {@code asPrimitive().getString()}
     * @return string or null if this element is a valid primitive. numbers and any other primitive value
     */
    String asString();

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
     * return boolean if this element is a valid boolean or can be converted to, or false if not
     *
     * this is equivalent to {@code asPrimitive().getString()}
     * @return boolean if this element is a valid boolean or can be converted to, or false if not
     */
    boolean asBoolean();

    /**
     * return byte if this element is a valid byte or can be converted to, or zero if not
     *
     * this is equivalent to {@code asPrimitive().getByte()}
     * @return byte if this element is a valid byte or can be converted to, or zero if not
     */
    byte asByte();

    /**
     * return double if this element is a valid double or can be converted to, or zero if not
     *
     * this is equivalent to {@code asPrimitive().getDouble()}
     * @return double if this element is a valid double or can be converted to, or zero if not
     */
    double asDouble();

    /**
     * return float if this element is a valid float or can be converted to, or zero if not
     *
     * this is equivalent to {@code asPrimitive().getFloat()}
     * @return float if this element is a valid float or can be converted to, or zero if not
     */
    float asFloat();

    /**
     * return int if this element is a valid int or can be converted to, or zero if not
     *
     * this is equivalent to {@code asPrimitive().getInt()}
     * @return int if this element is a valid int or can be converted to, or zero if not
     */
    int asInt();

    /**
     * return long if this element is a valid long or can be converted to, or zero if not
     *
     * this is equivalent to {@code asPrimitive().getLong()}
     * @return long if this element is a valid long or can be converted to, or zero if not
     */
    long asLong();

    /**
     * return short if this element is a valid short or can be converted to, or zero if not
     *
     * this is equivalent to {@code asPrimitive().getShort()}
     * @return short if this element is a valid short or can be converted to, or zero if not
     */
    short asShort();

    /**
     * return instant if this element is a valid instant or can be converted to, or {@code Instant.MIN} if not
     *
     * this is equivalent to {@code asPrimitive().getInstant()}
     * @return float if this element is a valid float or can be converted to, or zero if not
     */
    Instant asInstant();

    /**
     * return instant if this element is a valid instant or can be converted to, or {@code defaultValue} if not
     *
     * this is equivalent to {@code asPrimitive().getInstant(defaultValue)}
     * @param defaultValue default value
     * @return instant if this element is a valid instant or can be converted to, or {@code defaultValue} if not
     */
    Instant asSafeInstant(Instant defaultValue);

    /**
     * return int if this element is a valid int or can be converted to, or {@code defaultValue} if not
     *
     * this is equivalent to {@code asPrimitive().getInt(defaultValue)}
     * @param defaultValue default value
     * @return int if this element is a valid int or can be converted to, or zero if not
     */
    Integer asSafeInt(Integer defaultValue);

    /**
     * return long if this element is a valid long or can be converted to, or {@code defaultValue} if not
     *
     * this is equivalent to {@code asPrimitive().getLong(defaultValue)}
     * @param defaultValue default value
     * @return long if this element is a valid long or can be converted to, or zero if not
     */
    Long asSafeLong(Long defaultValue);

    /**
     * return short if this element is a valid short or can be converted to, or {@code defaultValue} if not
     *
     * this is equivalent to {@code asPrimitive().getShort(defaultValue)}
     * @param defaultValue default value
     * @return short if this element is a valid short or can be converted to, or zero if not
     */
    Short asSafeShort(Short defaultValue);

    /**
     * return byte if this element is a valid byte or can be converted to, or {@code defaultValue} if not
     *
     * this is equivalent to {@code asPrimitive().getByte(defaultValue)}
     * @param defaultValue default value
     * @return byte if this element is a valid byte or can be converted to, or zero if not
     */
    Byte asSafeByte(Byte defaultValue);

    /**
     * return double if this element is a valid double or can be converted to, or {@code defaultValue} if not
     *
     * this is equivalent to {@code asPrimitive().getDouble(defaultValue)}
     * @param defaultValue default value
     * @return double if this element is a valid double or can be converted to, or zero if not
     */
    Double asSafeDouble(Double defaultValue);

    /**
     * return float if this element is a valid float or can be converted to, or {@code defaultValue} if not
     *
     * this is equivalent to {@code asPrimitive().getFloat(defaultValue)}
     * @param defaultValue default value
     * @return float if this element is a valid float or can be converted to, or zero if not
     */
    Float asSafeFloat(Float defaultValue);

    /**
     * return String if this element is a valid String or can be converted to, or {@code defaultValue} if not
     *
     * this is equivalent to {@code asPrimitive().getString(defaultValue)}
     * @param defaultValue default value
     * @return String if this element is a valid String or can be converted to, or zero if not
     */
    String asSafeString(String defaultValue);

    /**
     * convert this element to {@link NutsObjectElement} or return a new instance.
     *
     * when {@code embed==false} an empty array is returned.
     * when {@code embed==true} the array returned will embed the current value as a {@code {"value": this} } entry, unless it is null.
     *
     * @param embed embed the current value as an item if not null
     * @return {@link NutsObjectElement}
     */
    NutsObjectElement asSafeObject(boolean embed);

    /**
     * convert this element to {@link NutsObjectElement} or return a new empty instance.
     * this is equivalent to {@code asSafeObject(false)}
     * @return convert this element to {@link NutsObjectElement} or return a new empty instance.
     */
    NutsObjectElement asSafeObject();

    /**
     * convert this element to {@link NutsArrayElement} or return a new instance.
     *
     * when {@code embed==false} an empty array is returned.
     * when {@code embed==true} the array returned will embed the current value, unless it is null.
     *
     * @param embed embed the current value as an item if not null
     * @return {@link NutsArrayElement}
     */
    NutsArrayElement asSafeArray(boolean embed);

    /**
     * convert this element to {@link NutsArrayElement} or return a new empty instance.
     * this is equivalent to {@code asSafeArray(false)}
     * @return convert this element to {@link NutsArrayElement} or return a new empty instance.
     */
    NutsArrayElement asSafeArray();

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
}
