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
 *
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
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NLiteral;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a parsed command-line argument with a dual-source value model.
 *
 * <p>An argument has two distinct value sources, reflected in three method families:</p>
 *
 * <ul>
 *   <li><b>{@code asXxx()}</b> — interprets the <em>full image</em> (raw string) as a typed value.
 *       Works on non-option args like {@code "3"} or {@code "true"}.
 *       Example: {@code NArg.of("3").asInt()} → {@code NOptional.of(3)}
 *       Example: {@code NArg.of("--count=3").asInt()} → {@code NOptional.ofError()}
 *   </li>
 *   <li><b>{@code getXxxValue()}</b> — interprets the <em>value segment</em> (after {@code =}),
 *       with option-aware semantics (negation, flag presence).
 *       Example: {@code NArg.of("--count=3").getIntValue()} → {@code NOptional.of(3)}
 *       Example: {@code NArg.of("--distinct").getBooleanValue()} → {@code NOptional.of(true)}
 *       Example: {@code NArg.of("--!distinct").getBooleanValue()} → {@code NOptional.of(false)}
 *   </li>
 *   <li><b>{@code xxxValue()}</b> — same as {@code getXxxValue()} but throws instead of
 *       returning {@code NOptional}.
 *   </li>
 * </ul>
 *
 * <p>The two literal anchors are:</p>
 * <ul>
 *   <li>{@link #asLiteral()} — the full raw image, feeds {@code asXxx()}</li>
 *   <li>{@link #literalValue()} — the value segment only, feeds {@code getXxxValue()}</li>
 * </ul>
 *
 * <p>Note: the value segment may be implicit (no {@code =} present) depending on how
 * the argument was produced by {@link NCmdLine}:
 * <ul>
 *   <li>{@code nextEntry("--a")} → value segment is {@code null}</li>
 *   <li>{@code nextFlag("--a")} → value segment is implicitly {@code true}</li>
 * </ul>
 * </p>
 *
 * @author thevpc
 * @app.category Command Line
 * @since 0.5.5
 */
public interface NArg extends NBlankable /*extends NLiteral*/ {

    /**
     * create instance for the given value and with the given session
     *
     * @param value value
     * @return new instance
     */
    static NArg of(String value) {
        return NUtilsRPI.of().createCmdlineArg(value,null);
    }

    /**
     * true if the argument starts with '-' or '+'
     *
     * @return true if the argument starts with '-'
     */
    boolean isOption();

    /**
     * true if the argument do not start with '-' or '+' or is blank. this is
     * equivalent to {@code !isOption()}.
     *
     * @return true if the argument do not start with '-' or '+'
     */
    boolean isNonOption();

    /**
     * equivalent to getStringKey().orElse("")
     *
     * @return non null key as string
     */
    String key();

    /**
     * equivalent to getStringValue().orElse(null)
     *
     * @return string as string
     */
    String value();

    /**
     * Returns the string key.
     *
     * @return get string key result
     */
    NOptional<String> getStringKey();

    /**
     * Returns the string value.
     *
     * @return get string value result
     */
    NOptional<String> getStringValue();

    /**
     * equivalent to <code>getStringValue().get()</code>
     * @return non null string value, or error
     */
    String stringValue();

    /**
     * true if option is in one of the following forms :
     * <ul>
     * <li>-!name[=...]</li>
     * <li>--!name[=...]</li>
     * <li>!name[=...]</li>
     * </ul>
     * where name is any valid identifier
     *
     * @return true if the argument is negated
     */
    boolean isNegated();

    /**
     * true if not negated
     *
     * @return true if not negated
     */
    boolean isEnabled();

    /**
     * false if option is in one of the following forms :
     * <ul>
     * <li>-//name</li>
     * <li>--//name</li>
     * </ul>
     * where name is any valid identifier
     *
     * @return true if the argument is enable and false if It's commented
     */
    boolean isUncommented();

    /**
     * true if option is in one of the following forms :
     * <ul>
     * <li>-//name</li>
     * <li>--//name</li>
     * </ul>
     * where name is any valid identifier
     *
     * @return true if the argument is enable and false if It's commented
     */
    boolean isCommented();

    /**
     * Throw an exception if the argument is null
     *
     * @return {@code this} instance
     */
    NArg required();

    /**
     * true if the argument is in the form key=value
     *
     * @return true if the argument is in the form key=value
     */
    boolean isKeyValue();

    /**
     * return option prefix part ('-' and '--')
     *
     * @return option prefix part ('-' and '--')
     * @since 0.5.7
     */
    NLiteral getOptionPrefix();

    /**
     * return query value separator
     *
     * @return query value separator
     * @since 0.5.7
     */
    String getSeparator();

    /**
     * return option key part excluding prefix ('-' and '--')
     *
     * @return option key part excluding prefix ('-' and '--')
     * @since 0.5.7
     */
    NLiteral getOptionName();

    /**
     * return new instance (never null) of the value part of the argument (after
     * =). However Argument's value may be null (
     * {@code getArgumentValue().getString() == null}). Here are some examples
     * of getArgumentValue() result for some common arguments
     * <ul>
     * <li>Argument("key").getArgumentValue() ==&gt; Argument(null) </li>
     * <li>Argument("key=value").getArgumentValue() ==&gt; Argument("value")
     * </li>
     * <li>Argument("key=").getArgumentValue() ==&gt; Argument("") </li>
     * <li>Argument("--key=value").getArgumentValue() ==&gt; Argument("value")
     * </li>
     * <li>Argument("--!key=value").getArgumentValue() ==&gt; Argument("value")
     * </li>
     * <li>Argument("--!//key=value").getArgumentValue() ==&gt;
     * Argument("value") </li>
     * </ul>
     *
     * @return new instance (never null) of the value part of the argument
     * (after =)
     */
    NLiteral literalValue();

    /**
     * As literal.
     *
     * @return as literal result
     */
    NLiteral asLiteral();

    /**
     * Returns the boolean value.
     *
     * @return get boolean value result
     */
    NOptional<Boolean> getBooleanValue();

    /**
     * Boolean value.
     *
     * @return boolean value result
     */
    boolean booleanValue();

    /**
     * @return value
     * @since 0.8.7
     */
    NOptional<Integer> getIntValue();

    /**
     * @return value
     * @since 0.8.7
     */
    int intValue();

    /**
     * Returns the byte value.
     *
     * @return get byte value result
     */
    NOptional<Byte> getByteValue();

    /**
     * Returns the short value.
     *
     * @return get short value result
     */
    NOptional<Short> getShortValue();

    /**
     * Returns the char value.
     *
     * @return get char value result
     */
    NOptional<Character> getCharValue();

    /**
     * Returns the number value.
     *
     * @return get number value result
     */
    NOptional<Number> getNumberValue();

    /**
     * @return value
     * @since 0.8.7
     */
    NOptional<Long> getLongValue();

    /**
     * Byte value.
     *
     * @return byte value result
     */
    byte byteValue();

    /**
     * Short value.
     *
     * @return short value result
     */
    short shortValue();

    /**
     * Char value.
     *
     * @return char value result
     */
    char charValue();

    /**
     * Number value.
     *
     * @return number value result
     */
    Number numberValue();

    /**
     * @return value
     * @since 0.8.7
     */
    double doubleValue();

    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<Double> getDoubleValue();

    /**
     * @return value
     * @since 0.8.6
     */
    float floatValue();

    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<Float> getFloatValue();


    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<BigInteger> getBigIntValue();

    /**
     * @return value
     * @since 0.8.6
     */
    BigInteger bigIntValue();


    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<BigDecimal> getBigDecimalValue();

    /**
     * @return value
     * @since 0.8.6
     */
    BigDecimal bigDecimalValue();

    /**
     * @return value
     * @since 0.8.6
     */
    long longValue();

    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<LocalDate> getLocalDateValue();

    /**
     * @return value
     * @since 0.8.6
     */
    LocalDate localDateValue();

    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<LocalTime> getLocalTimeValue();

    /**
     * @return value
     * @since 0.8.6
     */
    LocalTime localTimeValue();

    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<LocalDateTime> getLocalDateTimeValue();

    /**
     * @return value
     * @since 0.8.6
     */
    LocalDateTime localDateTimeValue();

    /**
     * @return value
     * @since 0.8.6
     */
    NOptional<Instant> getInstantValue();

    /**
     * @return value
     * @since 0.8.6
     */
    Instant instantValue();

    /**
     * return key part (never null) of the argument. The key does not include
     * neither ! nor // or = argument parts as they are parsed separately. Here
     * are some examples of getStringKey() result for some common arguments
     * <ul>
     * <li>Argument("key").getKey() ==&gt; "key" </li>
     * <li>Argument("key=value").getKey() ==&gt; "key" </li>
     * <li>Argument("--key=value").getKey() ==&gt; "--key"
     * </li>
     * <li>Argument("--!key=value").getKey() ==&gt; "--key"
     * </li>
     * <li>Argument("--!//key=value").getKey() ==&gt; "--key"
     * </li>
     * <li>Argument("--//!key=value").getKey() ==&gt; "--key"
     * </li>
     * </ul>
     * equivalent to {@code getKey().getString()}
     *
     * @return string key
     */
    NLiteral getKey();

    /**
     * Image.
     *
     * @return image result
     */
    String image();

    /**
     * Checks if is flag option.
     *
     * @return is flag option result
     */
    boolean isFlagOption();

    /**
     * Checks if is float.
     *
     * @return is float result
     */
    boolean isFloat();

    /**
     * Checks if is double.
     *
     * @return is double result
     */
    boolean isDouble();

    /**
     * Checks if is instant.
     *
     * @return is instant result
     */
    boolean isInstant();

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    boolean isEmpty();

    /**
     * Checks if is number.
     *
     * @return is number result
     */
    boolean isNumber();

    /**
     * As string.
     *
     * @return as string result
     */
    NOptional<String> asString();

    /**
     * As instant.
     *
     * @return as instant result
     */
    NOptional<Instant> asInstant();

    /**
     * As local date.
     *
     * @return as local date result
     */
    NOptional<LocalDate> asLocalDate();

    /**
     * As local date time.
     *
     * @return as local date time result
     */
    NOptional<LocalDateTime> asLocalDateTime();

    /**
     * As local time.
     *
     * @return as local time result
     */
    NOptional<LocalTime> asLocalTime();

    /**
     * As big complex.
     *
     * @return as big complex result
     */
    NOptional<NBigComplex> asBigComplex();

    /**
     * As double complex.
     *
     * @return as double complex result
     */
    NOptional<NDoubleComplex> asDoubleComplex();

    /**
     * As float complex.
     *
     * @return as float complex result
     */
    NOptional<NFloatComplex> asFloatComplex();

    /**
     * As number.
     *
     * @return as number result
     */
    NOptional<Number> asNumber();

    /**
     * As boolean.
     *
     * @return as boolean result
     */
    NOptional<Boolean> asBoolean();

    /**
     * As big int.
     *
     * @return as big int result
     */
    NOptional<BigInteger> asBigInt();

    /**
     * As big decimal.
     *
     * @return as big decimal result
     */
    NOptional<BigDecimal> asBigDecimal();

    /**
     * Checks if is boolean.
     *
     * @return is boolean result
     */
    boolean isBoolean();

    /**
     * Checks if is string.
     *
     * @return is string result
     */
    boolean isString();

    /**
     * Checks if is complex number.
     *
     * @return is complex number result
     */
    boolean isComplexNumber();

    /**
     * Checks if is temporal.
     *
     * @return is temporal result
     */
    boolean isTemporal();

    /**
     * Checks if is local temporal.
     *
     * @return is local temporal result
     */
    boolean isLocalTemporal();

    /**
     * Checks if is null.
     *
     * @return is null result
     */
    boolean isNull();

    /**
     * Checks if is byte.
     *
     * @return is byte result
     */
    boolean isByte();

    /**
     * Checks if is decimal number.
     *
     * @return is decimal number result
     */
    boolean isDecimalNumber();

    /**
     * Checks if is big number.
     *
     * @return is big number result
     */
    boolean isBigNumber();

    /**
     * Checks if is big decimal.
     *
     * @return is big decimal result
     */
    boolean isBigDecimal();

    /**
     * Checks if is big int.
     *
     * @return is big int result
     */
    boolean isBigInt();

    /**
     * Checks if is int.
     *
     * @return is int result
     */
    boolean isInt();

    /**
     * As double.
     *
     * @return as double result
     */
    NOptional<Double> asDouble();

    /**
     * As float.
     *
     * @return as float result
     */
    NOptional<Float> asFloat();

    /**
     * As byte.
     *
     * @return as byte result
     */
    NOptional<Byte> asByte();

    /**
     * As short.
     *
     * @return as short result
     */
    NOptional<Short> asShort();

    /**
     * As int.
     *
     * @return as int result
     */
    NOptional<Integer> asInt();

    /**
     * Checks if is long.
     *
     * @return is long result
     */
    boolean isLong();

    /**
     * As long.
     *
     * @return as long result
     */
    NOptional<Long> asLong();

    /**
     * Converts to string literal.
     *
     * @return to string literal result
     */
    String toStringLiteral();

    /**
     * As char.
     *
     * @return as char result
     */
    NOptional<Character> asChar();

    /**
     * As type.
     *
     * @param expectedType expected type
     * @return as type result
     */
    <ET> NOptional<ET> asType(Class<ET> expectedType);

    /**
     * As type.
     *
     * @param expectedType expected type
     * @return as type result
     */
    <ET> NOptional<ET> asType(Type expectedType);

    /**
     * As string at.
     *
     * @param index index
     * @return as string at result
     */
    NOptional<String> asStringAt(int index);

    /**
     * As long at.
     *
     * @param index index
     * @return as long at result
     */
    NOptional<Long> asLongAt(int index);

    /**
     * As int at.
     *
     * @param index index
     * @return as int at result
     */
    NOptional<Integer> asIntAt(int index);

    /**
     * As double at.
     *
     * @param index index
     * @return as double at result
     */
    NOptional<Double> asDoubleAt(int index);

    /**
     * Checks if is null at.
     *
     * @param index index
     * @return is null at result
     */
    boolean isNullAt(int index);

    /**
     * As literal at.
     *
     * @param index index
     * @return as literal at result
     */
    NLiteral asLiteralAt(int index);

    /**
     * As object at.
     *
     * @param index index
     * @return as object at result
     */
    NOptional<Object> asObjectAt(int index);

    /**
     * Checks if is stream.
     *
     * @return is stream result
     */
    boolean isStream();

    /**
     * Checks if is ordinal number.
     *
     * @return is ordinal number result
     */
    boolean isOrdinalNumber();

    /**
     * Checks if is floating number.
     *
     * @return is floating number result
     */
    boolean isFloatingNumber();

    /**
     * returns the owner commandline
     * @return the owner commandline
     */
    NCmdLine getCommandLine();
}
