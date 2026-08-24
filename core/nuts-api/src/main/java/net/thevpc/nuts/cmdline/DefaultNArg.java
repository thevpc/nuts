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
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NException;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NLiteral;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author thevpc
 */
public class DefaultNArg implements NArg {

    public static final String KEY_PATTERN_STRING = "[a-zA-Z0-9_.@&^$%][a-zA-Z0-9_.@&^$%+!-]*";
    public static final Pattern PATTERN_OPTION_EQ = Pattern.compile("^((?<optp>[-]+|[+]+)(?<cmt>//)?(?<flg>[!~])?)?(?<optk>" + KEY_PATTERN_STRING + ")?(?<opts>[=](?<optv>.*))?(?<optr>.*)$");
    public static final Pattern PATTERN_OPTION_COL = Pattern.compile("^((?<optp>[-]+|[+]+)(?<cmt>//)?(?<flg>[!~])?)?(?<optk>" + KEY_PATTERN_STRING + ")?(?<opts>[:](?<optv>.*))?(?<optr>.*)$");

    /**
     * Checks if is simple key.
     *
     * @param c c
     * @return is simple key result
     */
    public static boolean isSimpleKey(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z');
    }

    /**
     * Checks if is key start.
     *
     * @param c c
     * @return is key start result
     */
    public static boolean isKeyStart(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9')
                || c == '_'
                || c == '.'
                || c == '@'
                || c == '&'
                || c == '^'
                || c == '$'
                || c == '%';
    }

    /**
     * Checks if is key part.
     *
     * @param c c
     * @return is key part result
     */
    public static boolean isKeyPart(char c) {
        return isKeyStart(c)
                || c == '-'
                || c == '+'
                || c == '!';
    }

    /**
     * equal character
     */
    private final char eq;
    private final String key;
    private final String value;
    private final String optionPrefix;
    private final String optionName;
    private final boolean enabled;
    private final boolean active;
    private final boolean option;
    private final String image;
    private final NCmdLine cmdLine;

    /**
     * Default n arg.
     *
     * @param expression expression
     * @return default n arg result
     */
    public DefaultNArg(String expression) {
      /**
       * This.
       *
       * @param expression expression
       * @param null null
       */
        this(expression, (NCmdLine) null);
    }

    /**
     * Default n arg.
     *
     * @param expression expression
     * @param cmdLine cmd line
     * @return default n arg result
     */
    public DefaultNArg(String expression, NCmdLine cmdLine) {
      /**
       * This.
       *
       * @param expression expression
       * @param '=' '='
       * @param cmdLine cmd line
       */
        this(expression, '=', cmdLine);
    }

    /**
     * Default n arg.
     *
     * @param image image
     * @param eq eq
     * @return default n arg result
     */
    public DefaultNArg(String image, char eq) {
      /**
       * This.
       *
       * @param image image
       * @param eq eq
       * @param null null
       */
        this(image, eq, null);
    }

    /**
     * Constructor
     *
     * @param image image string
     * @param eq    equals
     */
    public DefaultNArg(String image, char eq, NCmdLine cmdLine) {
        this.cmdLine = cmdLine;
        this.eq = (eq == '\0' ? '=' : eq);
        this.image = image;
        Pattern currOptionsPattern;
        switch (this.eq) {
            case '=': {
                currOptionsPattern = PATTERN_OPTION_EQ;
                break;
            }
            case ':': {
                currOptionsPattern = PATTERN_OPTION_COL;
                break;
            }
            default: {
                currOptionsPattern = Pattern.compile("^((?<optp>[-]+|[+]+)(?<cmt>//)?(?<flg>[!~])?)?(?<optk>" + KEY_PATTERN_STRING + "*)?(?<opts>[" + eq + "](?<optv>.*))?(?<optr>.*)$");
            }
        }
        Matcher matcher = currOptionsPattern.matcher(image == null ? "" : image);
        if (matcher.find()) {
            String optp = matcher.group("optp");
            String cmt = matcher.group("cmt");
            String flg = matcher.group("flg");
            String optk = matcher.group("optk");
            String opts = matcher.group("opts");
            String optv = matcher.group("optv");
            String optr = matcher.group("optr");
            if (optp != null && optp.length() > 0) {
                option = true;
                active = !(cmt != null && cmt.length() > 0);
                enabled = !(flg != null && flg.length() > 0);
                optionPrefix = optp;
                if (optr != null && optr.length() > 0) {
                    optionName = (optk == null ? "" : optk) + optr;
                    key = optp + optionName;
                    value = null;
                } else {
                    optionName = (optk == null ? "" : optk);
                    if (opts != null && opts.length() > 0) {
                        key = optp + optionName;
                        value = optv + optr;
                    } else {
                        key = optp + optionName;
                        value = null;
                    }
                }
            } else {
                option = false;
                active = true;
                enabled = true;
                optionPrefix = null;
                optionName = null;
                if (opts != null && opts.length() > 0) {
                    key = image == null ? null : (optk == null ? "" : optk);
                    value = optv;
                } else {
                    key = image == null ? null : ((optk == null ? "" : optk) + optr);
                    value = null;
                }
            }
        } else {
            active = true;
            enabled = true;
            option = false;
            optionName = null;
            key = null;
            value = null;
            optionPrefix = null;
        }
    }

    /**
     * true if expression starts with '-' or '+'
     *
     * @return true if expression starts with '-' or '+'
     */
    @Override
    public boolean isOption() {
        return option;
    }

    @Override
    public boolean isNonOption() {
        return !isOption();
    }

    @Override
    public NOptional<String> getStringKey() {
        /**
         * Returns the key.
         *
         * @param ).asString( ).as string(
         * @return get key result
         */
        return getKey().asString();
    }

    @Override
    public String key() {
        /**
         * Returns the string key.
         *
         * @param ).orElse("" ).or else(""
         * @return get string key result
         */
        return getStringKey().orElse("");
    }

    @Override
    public String value() {
        /**
         * Returns the string value.
         *
         * @param ).orNull( ).or null(
         * @return get string value result
         */
        return getStringValue().orNull();
    }

    @Override
    public NOptional<String> getStringValue() {
        return this.literalValue().asString()
                .ifEmptyUse(
                        () -> NOptional.ofEmpty(() -> NMsg.ofC("missing value for : %s", getKey().asString().orElse("")))
                )
                .onErrorUse(
                        () -> NOptional.ofEmpty(() -> NMsg.ofC("erroneous value for : %s", getKey().asString().orElse("")))
                );
    }

    @Override
    public NOptional<Integer> getIntValue() {
        return this.literalValue().asInt();
    }

    @Override
    public NOptional<Byte> getByteValue() {
        return this.literalValue().asByte();
    }

    @Override
    public NOptional<Short> getShortValue() {
        return this.literalValue().asShort();
    }

    @Override
    public NOptional<Character> getCharValue() {
        return this.literalValue().asChar();
    }

    @Override
    public NOptional<Number> getNumberValue() {
        return this.literalValue().asNumber();
    }

    @Override
    public NOptional<Long> getLongValue() {
        return this.literalValue().asLong();
    }

    @Override
    public NOptional<Double> getDoubleValue() {
        return this.literalValue().asDouble();
    }

    @Override
    public NOptional<Float> getFloatValue() {
        return this.literalValue().asFloat();
    }

    @Override
    public NOptional<LocalDate> getLocalDateValue() {
        return this.literalValue().asLocalDate();
    }

    @Override
    public NOptional<LocalTime> getLocalTimeValue() {
        return this.literalValue().asLocalTime();
    }

    @Override
    public NOptional<LocalDateTime> getLocalDateTimeValue() {
        return this.literalValue().asLocalDateTime();
    }

    @Override
    public NOptional<Instant> getInstantValue() {
        return this.literalValue().asInstant();
    }

    @Override
    public int intValue() {
        /**
         * Returns the int value.
         *
         * @param ).get( ).get(
         * @return get int value result
         */
        return getIntValue().get();
    }

    @Override
    public byte byteValue() {
        /**
         * Returns the byte value.
         *
         * @param ).get( ).get(
         * @return get byte value result
         */
        return getByteValue().get();
    }

    @Override
    public short shortValue() {
        /**
         * Returns the short value.
         *
         * @param ).get( ).get(
         * @return get short value result
         */
        return getShortValue().get();
    }

    @Override
    public char charValue() {
        /**
         * Returns the char value.
         *
         * @param ).get( ).get(
         * @return get char value result
         */
        return getCharValue().get();
    }

    @Override
    public Number numberValue() {
        /**
         * Returns the number value.
         *
         * @param ).get( ).get(
         * @return get number value result
         */
        return getNumberValue().get();
    }

    @Override
    public double doubleValue() {
        /**
         * Returns the double value.
         *
         * @param ).get( ).get(
         * @return get double value result
         */
        return getDoubleValue().get();
    }

    @Override
    public float floatValue() {
        /**
         * Returns the float value.
         *
         * @param ).get( ).get(
         * @return get float value result
         */
        return getFloatValue().get();
    }

    @Override
    public long longValue() {
        /**
         * Returns the long value.
         *
         * @param ).get( ).get(
         * @return get long value result
         */
        return getLongValue().get();
    }

    @Override
    public LocalDate localDateValue() {
        /**
         * Returns the local date value.
         *
         * @param ).get( ).get(
         * @return get local date value result
         */
        return getLocalDateValue().get();
    }

    @Override
    public LocalTime localTimeValue() {
        /**
         * Returns the local time value.
         *
         * @param ).get( ).get(
         * @return get local time value result
         */
        return getLocalTimeValue().get();
    }

    @Override
    public LocalDateTime localDateTimeValue() {
        /**
         * Returns the local date time value.
         *
         * @param ).get( ).get(
         * @return get local date time value result
         */
        return getLocalDateTimeValue().get();
    }

    @Override
    public Instant instantValue() {
        /**
         * Returns the instant value.
         *
         * @param ).get( ).get(
         * @return get instant value result
         */
        return getInstantValue().get();
    }

    @Override
    public NOptional<BigInteger> getBigIntValue() {
        return this.literalValue().asBigInt();
    }

    @Override
    public BigInteger bigIntValue() {
        /**
         * Returns the big int value.
         *
         * @param ).get( ).get(
         * @return get big int value result
         */
        return getBigIntValue().get();
    }

    @Override
    public NOptional<BigDecimal> getBigDecimalValue() {
        return this.literalValue().asBigDecimal();
    }

    @Override
    public BigDecimal bigDecimalValue() {
        /**
         * Returns the big decimal value.
         *
         * @param ).get( ).get(
         * @return get big decimal value result
         */
        return getBigDecimalValue().get();
    }

    /**
     * String value.
     *
     * @return string value result
     */
    public String stringValue() {
        /**
         * Returns the string value.
         *
         * @param ).get( ).get(
         * @return get string value result
         */
        return getStringValue().get();
    }

    @Override
    public boolean isNegated() {
        return !enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isUncommented() {
        return active;
    }

    @Override
    public boolean isCommented() {
        return !active;
    }

    @Override
    public NArg required() {
        if (image == null) {
            throw NException.ofSafeNoSuchElementException(NMsg.ofPlain("missing value"));
        }
        return this;
    }

    @Override
    public boolean isKeyValue() {
        return value != null;
    }

    /**
     * Returns the option prefix.
     *
     * @return get option prefix result
     */
    public NLiteral getOptionPrefix() {
        return NLiteral.of(optionPrefix);
    }

    @Override
    public String getSeparator() {
        return String.valueOf(eq);
    }

    @Override
    public NLiteral getOptionName() {
        return NLiteral.of(optionName);
    }

    @Override
    public NLiteral literalValue() {
        return NLiteral.of(value);
    }

    @Override
    public NLiteral asLiteral() {
        return NLiteral.of(image);
    }

    @Override
    public NOptional<Boolean> getBooleanValue() {
        return this.literalValue().asBoolean()
                .onEmpty(!isNegated())
                .map(x -> isNegated() != x)
                .ifEmptyUse(
                        () -> NOptional.ofEmpty(() -> NMsg.ofC("missing value for : %s", getKey().asString().orElse("")))
                )
                .onErrorUse(
                        () -> NOptional.ofEmpty(() -> NMsg.ofC("erroneous value for : %s", getKey().asString().orElse("")))
                );
    }

    @Override
    public boolean booleanValue() {
        /**
         * Returns the boolean value.
         *
         * @param ).get( ).get(
         * @return get boolean value result
         */
        return getBooleanValue().get();
    }

    @Override
    public NLiteral getKey() {
        return NLiteral.of(key == null ? image : key);
    }

    @Override
    public boolean isFlagOption() {
        if (isOption()) {
            if (this.literalValue().isNull()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(image);
    }

    @Override
    public String image() {
        return image;
    }


    @Override
    public NOptional<Instant> asInstant() {
        /**
         * As literal.
         *
         * @param ).asInstant( ).as instant(
         * @return as literal result
         */
        return asLiteral().asInstant();
    }


    @Override
    public NOptional<LocalDate> asLocalDate() {
        /**
         * As literal.
         *
         * @param ).asLocalDate( ).as local date(
         * @return as literal result
         */
        return asLiteral().asLocalDate();
    }


    @Override
    public NOptional<LocalDateTime> asLocalDateTime() {
        /**
         * As literal.
         *
         * @param ).asLocalDateTime( ).as local date time(
         * @return as literal result
         */
        return asLiteral().asLocalDateTime();
    }


    @Override
    public NOptional<LocalTime> asLocalTime() {
        /**
         * As literal.
         *
         * @param ).asLocalTime( ).as local time(
         * @return as literal result
         */
        return asLiteral().asLocalTime();
    }


    @Override
    public NOptional<NBigComplex> asBigComplex() {
        /**
         * As literal.
         *
         * @param ).asBigComplex( ).as big complex(
         * @return as literal result
         */
        return asLiteral().asBigComplex();
    }


    @Override
    public NOptional<NDoubleComplex> asDoubleComplex() {
        /**
         * As literal.
         *
         * @param ).asDoubleComplex( ).as double complex(
         * @return as literal result
         */
        return asLiteral().asDoubleComplex();
    }


    @Override
    public NOptional<NFloatComplex> asFloatComplex() {
        /**
         * As literal.
         *
         * @param ).asFloatComplex( ).as float complex(
         * @return as literal result
         */
        return asLiteral().asFloatComplex();
    }


    @Override
    public NOptional<Number> asNumber() {
        /**
         * As literal.
         *
         * @param ).asNumber( ).as number(
         * @return as literal result
         */
        return asLiteral().asNumber();
    }


    /**
     * As boolean.
     *
     * @return as boolean result
     */
    public NOptional<Boolean> asBoolean() {
        /**
         * As literal.
         *
         * @param ).asBoolean( ).as boolean(
         * @return as literal result
         */
        return asLiteral().asBoolean();
    }


    /**
     * As long.
     *
     * @return as long result
     */
    public NOptional<Long> asLong() {
        /**
         * As literal.
         *
         * @param ).asLong( ).as long(
         * @return as literal result
         */
        return asLiteral().asLong();
    }


    @Override
    public NOptional<Double> asDouble() {
        /**
         * As literal.
         *
         * @param ).asDouble( ).as double(
         * @return as literal result
         */
        return asLiteral().asDouble();
    }


    @Override
    public NOptional<Float> asFloat() {
        /**
         * As literal.
         *
         * @param ).asFloat( ).as float(
         * @return as literal result
         */
        return asLiteral().asFloat();
    }


    @Override
    public NOptional<Byte> asByte() {
        /**
         * As literal.
         *
         * @param ).asByte( ).as byte(
         * @return as literal result
         */
        return asLiteral().asByte();
    }


    @Override
    public NOptional<Short> asShort() {
        /**
         * As literal.
         *
         * @param ).asShort( ).as short(
         * @return as literal result
         */
        return asLiteral().asShort();
    }


    /**
     * As int.
     *
     * @return as int result
     */
    public NOptional<Integer> asInt() {
        /**
         * As literal.
         *
         * @param ).asInt( ).as int(
         * @return as literal result
         */
        return asLiteral().asInt();
    }


    @Override
    public NOptional<BigInteger> asBigInt() {
        /**
         * As literal.
         *
         * @param ).asBigInt( ).as big int(
         * @return as literal result
         */
        return asLiteral().asBigInt();
    }


    @Override
    public NOptional<BigDecimal> asBigDecimal() {
        /**
         * As literal.
         *
         * @param ).asBigDecimal( ).as big decimal(
         * @return as literal result
         */
        return asLiteral().asBigDecimal();
    }


    /**
     * Checks if is boolean.
     *
     * @return is boolean result
     */
    public boolean isBoolean() {
        /**
         * As literal.
         *
         * @param ).asBoolean().isPresent( ).as boolean().is present(
         * @return as literal result
         */
        return asLiteral().asBoolean().isPresent();
    }


    @Override
    public boolean isString() {
        /**
         * As literal.
         *
         * @param ).asString().isPresent( ).as string().is present(
         * @return as literal result
         */
        return asLiteral().asString().isPresent();
    }


    @Override
    public boolean isComplexNumber() {
        /**
         * As literal.
         *
         * @param ).isComplexNumber( ).is complex number(
         * @return as literal result
         */
        return asLiteral().isComplexNumber();
    }


    @Override
    public boolean isTemporal() {
        NLiteral t = asLiteral();
        return t.asLocalDate().isPresent()
                || t.asLocalDateTime().isPresent()
                || t.asLocalTime().isPresent()
                || t.asInstant().isPresent()
                ;
    }


    @Override
    public boolean isLocalTemporal() {
        NLiteral t = asLiteral();
        return t.asLocalDate().isPresent()
                || t.asLocalDateTime().isPresent()
                || t.asLocalTime().isPresent()
                ;
    }


    @Override
    public boolean isNull() {
        /**
         * As literal.
         *
         * @param ).isNull( ).is null(
         * @return as literal result
         */
        return asLiteral().isNull();
    }


    @Override
    public boolean isByte() {
        /**
         * As literal.
         *
         * @param ).asByte().isPresent( ).as byte().is present(
         * @return as literal result
         */
        return asLiteral().asByte().isPresent();
    }


    @Override
    public boolean isDecimalNumber() {
        /**
         * As literal.
         *
         * @param ).asBigDecimal().isPresent( ).as big decimal().is present(
         * @return as literal result
         */
        return asLiteral().asBigDecimal().isPresent();
    }


    @Override
    public boolean isBigNumber() {
        /**
         * As literal.
         *
         * @param ).asBigDecimal().isPresent( ).as big decimal().is present(
         * @return as literal result
         */
        return asLiteral().asBigDecimal().isPresent();
    }


    @Override
    public boolean isBigDecimal() {
        /**
         * As literal.
         *
         * @param ).asBigDecimal().isPresent( ).as big decimal().is present(
         * @return as literal result
         */
        return asLiteral().asBigDecimal().isPresent();
    }


    @Override
    public boolean isBigInt() {
        /**
         * As literal.
         *
         * @param ).asBigInt().isPresent( ).as big int().is present(
         * @return as literal result
         */
        return asLiteral().asBigInt().isPresent();
    }


    /**
     * Checks if is int.
     *
     * @return is int result
     */
    public boolean isInt() {
        /**
         * As literal.
         *
         * @param ).asInt().isPresent( ).as int().is present(
         * @return as literal result
         */
        return asLiteral().asInt().isPresent();
    }


    /**
     * Checks if is long.
     *
     * @return is long result
     */
    public boolean isLong() {
        /**
         * As literal.
         *
         * @param ).asLong().isPresent( ).as long().is present(
         * @return as literal result
         */
        return asLiteral().asLong().isPresent();
    }


    /**
     * Checks if is short.
     *
     * @return is short result
     */
    public boolean isShort() {
        /**
         * As literal.
         *
         * @param ).asShort().isPresent( ).as short().is present(
         * @return as literal result
         */
        return asLiteral().asShort().isPresent();
    }


    @Override
    public boolean isFloat() {
        /**
         * As literal.
         *
         * @param ).asFloat().isPresent( ).as float().is present(
         * @return as literal result
         */
        return asLiteral().asFloat().isPresent();
    }


    @Override
    public boolean isDouble() {
        /**
         * As literal.
         *
         * @param ).asDouble().isPresent( ).as double().is present(
         * @return as literal result
         */
        return asLiteral().asDouble().isPresent();
    }


    @Override
    public boolean isInstant() {
        /**
         * As literal.
         *
         * @param ).asInstant().isPresent( ).as instant().is present(
         * @return as literal result
         */
        return asLiteral().asInstant().isPresent();
    }


    @Override
    public boolean isEmpty() {
        /**
         * As literal.
         *
         * @param ).isEmpty( ).is empty(
         * @return as literal result
         */
        return asLiteral().isEmpty();
    }

    @Override
    public boolean isBlank() {
        /**
         * As literal.
         *
         * @param ).isBlank( ).is blank(
         * @return as literal result
         */
        return asLiteral().isBlank();
    }

    @Override
    public boolean isNumber() {
        /**
         * As literal.
         *
         * @param ).asNumber().isPresent( ).as number().is present(
         * @return as literal result
         */
        return asLiteral().asNumber().isPresent();
    }

    @Override
    public NOptional<String> asString() {
        return NOptional.ofNamed(image(),"image");
    }


    @Override
    public String toStringLiteral() {
        /**
         * As literal.
         *
         * @param ).toStringLiteral( ).to string literal(
         * @return as literal result
         */
        return asLiteral().toStringLiteral();
    }


    @Override
    public NOptional<Character> asChar() {
        /**
         * As literal.
         *
         * @param ).asChar( ).as char(
         * @return as literal result
         */
        return asLiteral().asChar();
    }


    @Override
    public <ET> NOptional<ET> asType(Class<ET> expectedType) {
        /**
         * As literal.
         *
         * @param ).asType(expectedType ).as type(expected type
         * @return as literal result
         */
        return asLiteral().asType(expectedType);
    }


    @Override
    public <ET> NOptional<ET> asType(Type expectedType) {
        /**
         * As literal.
         *
         * @param ).asType(expectedType ).as type(expected type
         * @return as literal result
         */
        return asLiteral().asType(expectedType);
    }


    @Override
    public NOptional<String> asStringAt(int index) {
        /**
         * As literal.
         *
         * @param ).asStringAt(index ).as string at(index
         * @return as literal result
         */
        return asLiteral().asStringAt(index);
    }


    @Override
    public NOptional<Long> asLongAt(int index) {
        /**
         * As literal.
         *
         * @param ).asLongAt(index ).as long at(index
         * @return as literal result
         */
        return asLiteral().asLongAt(index);
    }


    @Override
    public NOptional<Integer> asIntAt(int index) {
        /**
         * As literal.
         *
         * @param ).asIntAt(index ).as int at(index
         * @return as literal result
         */
        return asLiteral().asIntAt(index);
    }


    @Override
    public NOptional<Double> asDoubleAt(int index) {
        /**
         * As literal.
         *
         * @param ).asDoubleAt(index ).as double at(index
         * @return as literal result
         */
        return asLiteral().asDoubleAt(index);
    }


    @Override
    public boolean isNullAt(int index) {
        /**
         * As literal.
         *
         * @param ).isNullAt(index ).is null at(index
         * @return as literal result
         */
        return asLiteral().isNullAt(index);
    }


    @Override
    public NLiteral asLiteralAt(int index) {
        /**
         * As literal.
         *
         * @param ).asLiteralAt(index ).as literal at(index
         * @return as literal result
         */
        return asLiteral().asLiteralAt(index);
    }


    @Override
    public NOptional<Object> asObjectAt(int index) {
        /**
         * As literal.
         *
         * @param ).asObjectAt(index ).as object at(index
         * @return as literal result
         */
        return asLiteral().asObjectAt(index);
    }


    @Override
    public boolean isStream() {
        /**
         * As literal.
         *
         * @param ).isStream( ).is stream(
         * @return as literal result
         */
        return asLiteral().isStream();
    }


    @Override
    public boolean isOrdinalNumber() {
        /**
         * As literal.
         *
         * @param ).asBigInt().isPresent( ).as big int().is present(
         * @return as literal result
         */
        return asLiteral().asBigInt().isPresent();
    }


    @Override
    public boolean isFloatingNumber() {
        /**
         * As literal.
         *
         * @param ).asBigDecimal().isPresent( ).as big decimal().is present(
         * @return as literal result
         */
        return asLiteral().asBigDecimal().isPresent();
    }

    @Override
    public NCmdLine getCommandLine() {
        return cmdLine;
    }
}
