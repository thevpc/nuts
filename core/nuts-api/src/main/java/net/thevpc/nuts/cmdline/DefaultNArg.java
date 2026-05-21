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

import net.thevpc.nuts.util.NExceptions;
import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;
import net.thevpc.nuts.text.NMsg;
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
    private final NCmdLine cmdLine;

    public DefaultNArg(String expression) {
        this(expression, (NCmdLine) null);
    }

    public DefaultNArg(String expression, NCmdLine cmdLine) {
        this(expression, '=', cmdLine);
    }

    public DefaultNArg(String image, char eq) {
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
        return getIntValue().get();
    }

    @Override
    public byte byteValue() {
        return getByteValue().get();
    }

    @Override
    public short shortValue() {
        return getShortValue().get();
    }

    @Override
    public char charValue() {
        return getCharValue().get();
    }

    @Override
    public Number numberValue() {
        return getNumberValue().get();
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
    public LocalTime localTimeValue() {
        return getLocalTimeValue().get();
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
        return this.literalValue().asBigInt();
    }

    @Override
    public BigInteger bigIntValue() {
        return getBigIntValue().get();
    }

    @Override
    public NOptional<BigDecimal> getBigDecimalValue() {
        return this.literalValue().asBigDecimal();
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
        return asLiteral().asInstant();
    }


    @Override
    public NOptional<LocalDate> asLocalDate() {
        return asLiteral().asLocalDate();
    }


    @Override
    public NOptional<LocalDateTime> asLocalDateTime() {
        return asLiteral().asLocalDateTime();
    }


    @Override
    public NOptional<LocalTime> asLocalTime() {
        return asLiteral().asLocalTime();
    }


    @Override
    public NOptional<NBigComplex> asBigComplex() {
        return asLiteral().asBigComplex();
    }


    @Override
    public NOptional<NDoubleComplex> asDoubleComplex() {
        return asLiteral().asDoubleComplex();
    }


    @Override
    public NOptional<NFloatComplex> asFloatComplex() {
        return asLiteral().asFloatComplex();
    }


    @Override
    public NOptional<Number> asNumber() {
        return asLiteral().asNumber();
    }


    public NOptional<Boolean> asBoolean() {
        return asLiteral().asBoolean();
    }


    public NOptional<Long> asLong() {
        return asLiteral().asLong();
    }


    @Override
    public NOptional<Double> asDouble() {
        return asLiteral().asDouble();
    }


    @Override
    public NOptional<Float> asFloat() {
        return asLiteral().asFloat();
    }


    @Override
    public NOptional<Byte> asByte() {
        return asLiteral().asByte();
    }


    @Override
    public NOptional<Short> asShort() {
        return asLiteral().asShort();
    }


    public NOptional<Integer> asInt() {
        return asLiteral().asInt();
    }


    @Override
    public NOptional<BigInteger> asBigInt() {
        return asLiteral().asBigInt();
    }


    @Override
    public NOptional<BigDecimal> asBigDecimal() {
        return asLiteral().asBigDecimal();
    }


    public boolean isBoolean() {
        return asLiteral().asBoolean().isPresent();
    }


    @Override
    public boolean isString() {
        return asLiteral().asString().isPresent();
    }


    @Override
    public boolean isComplexNumber() {
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
        return asLiteral().isNull();
    }


    @Override
    public boolean isByte() {
        return asLiteral().asByte().isPresent();
    }


    @Override
    public boolean isDecimalNumber() {
        return asLiteral().asBigDecimal().isPresent();
    }


    @Override
    public boolean isBigNumber() {
        return asLiteral().asBigDecimal().isPresent();
    }


    @Override
    public boolean isBigDecimal() {
        return asLiteral().asBigDecimal().isPresent();
    }


    @Override
    public boolean isBigInt() {
        return asLiteral().asBigInt().isPresent();
    }


    public boolean isInt() {
        return asLiteral().asInt().isPresent();
    }


    public boolean isLong() {
        return asLiteral().asLong().isPresent();
    }


    public boolean isShort() {
        return asLiteral().asShort().isPresent();
    }


    @Override
    public boolean isFloat() {
        return asLiteral().asFloat().isPresent();
    }


    @Override
    public boolean isDouble() {
        return asLiteral().asDouble().isPresent();
    }


    @Override
    public boolean isInstant() {
        return asLiteral().asInstant().isPresent();
    }


    @Override
    public boolean isEmpty() {
        return asLiteral().isEmpty();
    }

    @Override
    public boolean isBlank() {
        return asLiteral().isBlank();
    }

    @Override
    public boolean isNumber() {
        return asLiteral().asNumber().isPresent();
    }

    @Override
    public NOptional<String> asString() {
        return NOptional.ofNamed(image(),"image");
    }


    @Override
    public String toStringLiteral() {
        return asLiteral().toStringLiteral();
    }


    @Override
    public NOptional<Character> asChar() {
        return asLiteral().asChar();
    }


    @Override
    public <ET> NOptional<ET> asType(Class<ET> expectedType) {
        return asLiteral().asType(expectedType);
    }


    @Override
    public <ET> NOptional<ET> asType(Type expectedType) {
        return asLiteral().asType(expectedType);
    }


    @Override
    public NOptional<String> asStringAt(int index) {
        return asLiteral().asStringAt(index);
    }


    @Override
    public NOptional<Long> asLongAt(int index) {
        return asLiteral().asLongAt(index);
    }


    @Override
    public NOptional<Integer> asIntAt(int index) {
        return asLiteral().asIntAt(index);
    }


    @Override
    public NOptional<Double> asDoubleAt(int index) {
        return asLiteral().asDoubleAt(index);
    }


    @Override
    public boolean isNullAt(int index) {
        return asLiteral().isNullAt(index);
    }


    @Override
    public NLiteral asLiteralAt(int index) {
        return asLiteral().asLiteralAt(index);
    }


    @Override
    public NOptional<Object> asObjectAt(int index) {
        return asLiteral().asObjectAt(index);
    }


    @Override
    public boolean isStream() {
        return asLiteral().isStream();
    }


    @Override
    public boolean isOrdinalNumber() {
        return asLiteral().asBigInt().isPresent();
    }


    @Override
    public boolean isFloatingNumber() {
        return asLiteral().asBigDecimal().isPresent();
    }

    @Override
    public NCmdLine getCommandLine() {
        return cmdLine;
    }
}
