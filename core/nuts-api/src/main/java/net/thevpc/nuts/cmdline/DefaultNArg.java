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

import net.thevpc.nuts.NExceptions;
import net.thevpc.nuts.elem.NBigComplex;
import net.thevpc.nuts.elem.NDoubleComplex;
import net.thevpc.nuts.elem.NFloatComplex;
import net.thevpc.nuts.util.NMsg;
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

    public static boolean isSimpleKey(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z');
    }

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

    public DefaultNArg(String expression) {
        this(expression, '=');
    }

    /**
     * Constructor
     *
     * @param image image string
     * @param eq    equals
     */
    public DefaultNArg(String image, char eq) {
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
        return getKey().asString();
    }

    @Override
    public String key() {
        return getStringKey().orElse("");
    }

    @Override
    public String value() {
        return getStringValue().orNull();
    }

    @Override
    public NOptional<String> getStringValue() {
        return getValue().asString()
                .ifEmptyUse(
                        () -> NOptional.ofEmpty(() -> NMsg.ofC("missing value for : %s", getKey().asString().orElse("")))
                )
                .ifErrorUse(
                        () -> NOptional.ofEmpty(() -> NMsg.ofC("erroneous value for : %s", getKey().asString().orElse("")))
                );
    }

    @Override
    public NOptional<Integer> getIntValue() {
        return getValue().asInt();
    }

    @Override
    public NOptional<Long> getLongValue() {
        return getValue().asLong();
    }

    @Override
    public NOptional<Double> getDoubleValue() {
        return getValue().asDouble();
    }

    @Override
    public NOptional<Float> getFloatValue() {
        return getValue().asFloat();
    }

    @Override
    public NOptional<LocalDate> getLocalDateValue() {
        return getValue().asLocalDate();
    }

    @Override
    public NOptional<LocalTime> getLocalTimeValue() {
        return getValue().asLocalTime();
    }

    @Override
    public NOptional<LocalDateTime> getLocalDateTimeValue() {
        return getValue().asLocalDateTime();
    }

    @Override
    public NOptional<Instant> getInstantValue() {
        return getValue().asInstant();
    }

    @Override
    public int intValue() {
        return getIntValue().get();
    }

    @Override
    public double doubleValue() {
        return getDoubleValue().get();
    }

    @Override
    public float floatValue() {
        return getFloatValue().get();
    }

    @Override
    public long longValue() {
        return getLongValue().get();
    }

    @Override
    public LocalDate localDateValue() {
        return getLocalDateValue().get();
    }

    @Override
    public LocalDateTime localTimeValue() {
        return getLocalDateTimeValue().get();
    }

    @Override
    public LocalDateTime localDateTimeValue() {
        return getLocalDateTimeValue().get();
    }

    @Override
    public Instant instantValue() {
        return getInstantValue().get();
    }

    @Override
    public NOptional<BigInteger> getBigIntValue() {
        return getValue().asBigInt();
    }

    @Override
    public BigInteger bigIntValue() {
        return getBigIntValue().get();
    }

    @Override
    public NOptional<BigDecimal> getBigDecimalValue() {
        return getValue().asBigDecimal();
    }

    @Override
    public BigDecimal bigDecimalValue() {
        return getBigDecimalValue().get();
    }

    public String stringValue() {
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
    public boolean isNonCommented() {
        return active;
    }

    @Override
    public boolean isCommented() {
        return !active;
    }

    @Override
    public NArg required() {
        if (image == null) {
            throw NExceptions.ofSafeNoSuchElementException(NMsg.ofPlain("missing value"));
        }
        return this;
    }

    @Override
    public boolean isKeyValue() {
        return value != null;
    }

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
    public NLiteral getValue() {
        return NLiteral.of(value);
    }

    @Override
    public NOptional<Boolean> getBooleanValue() {
        if (isNegated()) {
            return getValue().asBoolean().ifEmpty(true).map(x -> isNegated() != x)
                    .ifEmptyUse(
                            () -> NOptional.ofEmpty(() -> NMsg.ofC("missing value for : %s", getKey().asString().orElse("")))
                    )
                    .ifErrorUse(
                            () -> NOptional.ofEmpty(() -> NMsg.ofC("erroneous value for : %s", getKey().asString().orElse("")))
                    );
        }
        return getValue().asBoolean()
                .ifEmptyUse(
                        () -> NOptional.ofEmpty(() -> NMsg.ofC("missing value for : %s", getKey().asString().orElse("")))
                )
                .ifErrorUse(
                        () -> NOptional.ofEmpty(() -> NMsg.ofC("erroneous value for : %s", getKey().asString().orElse("")))
                );
    }

    @Override
    public boolean booleanValue() {
        return getBooleanValue().get();
    }

    @Override
    public NLiteral getKey() {
        return NLiteral.of(key == null ? image : key);
    }

    @Override
    public boolean isFlagOption() {
        if (isOption()) {
            if (getValue().isNull()) {
                return true;
            }
        }
        return false;
    }

    public NLiteral toLiteral() {
        return NLiteral.of(image);
    }

    @Override
    public String toString() {
        return String.valueOf(image);
    }

    @Override
    public String getImage() {
        return image;
    }


    public NOptional<Instant> asInstant() {
        return toLiteral().asInstant();
    }


    public NOptional<LocalDate> asLocalDate() {
        return toLiteral().asLocalDate();
    }


    public NOptional<LocalDateTime> asLocalDateTime() {
        return toLiteral().asLocalDateTime();
    }


    public NOptional<LocalTime> asLocalTime() {
        return toLiteral().asLocalTime();
    }


    public NOptional<NBigComplex> asBigComplex() {
        return toLiteral().asBigComplex();
    }


    public NOptional<NDoubleComplex> asDoubleComplex() {
        return toLiteral().asDoubleComplex();
    }


    public NOptional<NFloatComplex> asFloatComplex() {
        return toLiteral().asFloatComplex();
    }


    public NOptional<Number> asNumber() {
        return toLiteral().asNumber();
    }


    public NOptional<Boolean> asBoolean() {
        return toLiteral().asBoolean();
    }


    public NOptional<Long> asLong() {
        return toLiteral().asLong();
    }


    public NOptional<Double> asDouble() {
        return toLiteral().asDouble();
    }


    public NOptional<Float> asFloat() {
        return toLiteral().asFloat();
    }


    public NOptional<Byte> asByte() {
        return toLiteral().asByte();
    }


    public NOptional<Short> asShort() {
        return toLiteral().asShort();
    }


    public NOptional<Integer> asInt() {
        return toLiteral().asInt();
    }


    public NOptional<BigInteger> asBigInt() {
        return toLiteral().asBigInt();
    }


    public NOptional<BigDecimal> asBigDecimal() {
        return toLiteral().asBigDecimal();
    }


    public boolean isBoolean() {
        return toLiteral().isBoolean();
    }


    public boolean isString() {
        return toLiteral().isString();
    }


    public boolean isComplexNumber() {
        return toLiteral().isComplexNumber();
    }


    public boolean isTemporal() {
        return toLiteral().isTemporal();
    }


    public boolean isLocalTemporal() {
        return toLiteral().isLocalTemporal();
    }


    public boolean isNull() {
        return toLiteral().isNull();
    }


    public boolean isByte() {
        return toLiteral().isByte();
    }


    public boolean isDecimalNumber() {
        return toLiteral().isDecimalNumber();
    }


    public boolean isBigNumber() {
        return toLiteral().isBigNumber();
    }


    public boolean isBigDecimal() {
        return toLiteral().isBigDecimal();
    }


    public boolean isBigInt() {
        return toLiteral().isBigInt();
    }


    public boolean isInt() {
        return toLiteral().isInt();
    }


    public boolean isLong() {
        return toLiteral().isLong();
    }


    public boolean isShort() {
        return toLiteral().isShort();
    }


    public boolean isFloat() {
        return toLiteral().isFloat();
    }


    public boolean isDouble() {
        return toLiteral().isDouble();
    }


    public boolean isInstant() {
        return toLiteral().isInstant();
    }


    public boolean isEmpty() {
        return toLiteral().isEmpty();
    }

    @Override
    public boolean isBlank() {
        return toLiteral().isBlank();
    }

    public boolean isNumber() {
        return toLiteral().isNumber();
    }

    @Override
    public NOptional<String> asString() {
        return toLiteral().asString();
    }


    public String toStringLiteral() {
        return toLiteral().toStringLiteral();
    }


    public NOptional<Character> asChar() {
        return toLiteral().asChar();
    }


    public boolean isSupportedType(Class<?> type) {
        return toLiteral().isSupportedType(type);
    }


    public <ET> NOptional<ET> asType(Class<ET> expectedType) {
        return toLiteral().asType(expectedType);
    }


    public <ET> NOptional<ET> asType(Type expectedType) {
        return toLiteral().asType(expectedType);
    }


    public NOptional<String> asStringAt(int index) {
        return toLiteral().asStringAt(index);
    }


    public NOptional<Long> asLongAt(int index) {
        return toLiteral().asLongAt(index);
    }


    public NOptional<Integer> asIntAt(int index) {
        return toLiteral().asIntAt(index);
    }


    public NOptional<Double> asDoubleAt(int index) {
        return toLiteral().asDoubleAt(index);
    }


    public boolean isNullAt(int index) {
        return toLiteral().isNullAt(index);
    }


    public NLiteral asLiteralAt(int index) {
        return toLiteral().asLiteralAt(index);
    }


    public NOptional<Object> asObjectAt(int index) {
        return toLiteral().asObjectAt(index);
    }


    public boolean isStream() {
        return toLiteral().isStream();
    }


    public boolean isOrdinalNumber() {
        return toLiteral().isOrdinalNumber();
    }


    public boolean isFloatingNumber() {
        return toLiteral().isFloatingNumber();
    }
}
