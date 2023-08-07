/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NLiteral;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.NoSuchElementException;
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
                || c == '%'
                ;
    }

    public static boolean isKeyPart(char c) {
        return isKeyStart(c)
                || c == '-'
                || c == '+'
                || c == '!'
                ;
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
    private final String expression;

    public DefaultNArg(String expression) {
        this(expression, '=');
    }


    /**
     * Constructor
     *
     * @param expression expression
     * @param eq         equals
     */
    public DefaultNArg(String expression, char eq) {
        this.eq = (eq == '\0' ? '=' : eq);
        this.expression = expression;
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
        Matcher matcher = currOptionsPattern.matcher(expression == null ? "" : expression);
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
                    key = expression == null ? null : (optk == null ? "" : optk);
                    value = optv;
                } else {
                    key = expression == null ? null : ((optk == null ? "" : optk) + optr);
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
                        () -> NOptional.ofEmpty(s -> NMsg.ofC("missing value for : %s", getKey().asString().orElse("")))
                )
                .ifErrorUse(
                        () -> NOptional.ofEmpty(s -> NMsg.ofC("erroneous value for : %s", getKey().asString().orElse("")))
                );
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
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isInactive() {
        return !active;
    }

    @Override
    public NArg required() {
        if (expression == null) {
            throw new NoSuchElementException("missing value");
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
                            () -> NOptional.ofEmpty(s -> NMsg.ofC("missing value for : %s", getKey().asString().orElse("")))
                    )
                    .ifErrorUse(
                            () -> NOptional.ofEmpty(s -> NMsg.ofC("erroneous value for : %s", getKey().asString().orElse("")))
                    );
        }
        return getValue().asBoolean()
                .ifEmptyUse(
                        () -> NOptional.ofEmpty(s -> NMsg.ofC("missing value for : %s", getKey().asString().orElse("")))
                )
                .ifErrorUse(
                        () -> NOptional.ofEmpty(s -> NMsg.ofC("erroneous value for : %s", getKey().asString().orElse("")))
                );
    }

    @Override
    public NLiteral getKey() {
        return NLiteral.of(key == null ? expression : key);
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

    private NLiteral toValue() {
        return NLiteral.of(expression);
    }

    @Override
    public String toString() {
        return String.valueOf(expression);
    }

    @Override
    public Object getRaw() {
        return expression;
    }

    @Override
    public NOptional<Instant> asInstant() {
        return toValue().asInstant();
    }

    @Override
    public NOptional<Number> asNumber() {
        return toValue().asNumber();
    }

    @Override
    public NOptional<Boolean> asBoolean() {
        return toValue().asBoolean();
    }

    @Override
    public NOptional<Long> asLong() {
        return toValue().asLong();
    }

    @Override
    public NOptional<Double> asDouble() {
        return toValue().asDouble();
    }

    @Override
    public NOptional<Float> asFloat() {
        return toValue().asFloat();
    }

    @Override
    public NOptional<Byte> asByte() {
        return toValue().asByte();
    }

    @Override
    public NOptional<Short> asShort() {
        return toValue().asShort();
    }

    @Override
    public NOptional<Integer> asInt() {
        return toValue().asInt();
    }

    @Override
    public NOptional<BigInteger> asBigInt() {
        return toValue().asBigInt();
    }

    @Override
    public NOptional<BigDecimal> asBigDecimal() {
        return toValue().asBigDecimal();
    }

    @Override
    public boolean isBoolean() {
        return toValue().isBoolean();
    }

    @Override
    public boolean isString() {
        return toValue().isString();
    }

    @Override
    public boolean isNull() {
        return toValue().isNull();
    }

    @Override
    public boolean isByte() {
        return toValue().isByte();
    }

    @Override
    public boolean isInt() {
        return toValue().isInt();
    }

    @Override
    public boolean isLong() {
        return toValue().isLong();
    }

    @Override
    public boolean isShort() {
        return toValue().isShort();
    }

    @Override
    public boolean isFloat() {
        return toValue().isFloat();
    }

    @Override
    public boolean isDouble() {
        return toValue().isDouble();
    }

    @Override
    public boolean isInstant() {
        return toValue().isInstant();
    }

    @Override
    public boolean isEmpty() {
        return toValue().isEmpty();
    }

    @Override
    public boolean isBlank() {
        return toValue().isBlank();
    }

    @Override
    public boolean isNumber() {
        return toValue().isNumber();
    }

    @Override
    public NOptional<String> asString() {
        return toValue().asString();
    }

    @Override
    public String toStringLiteral() {
        return toValue().toStringLiteral();
    }

    @Override
    public NOptional<Character> asChar() {
        return toValue().asChar();
    }

    @Override
    public boolean isSupportedType(Class<?> type) {
        return toValue().isSupportedType(type);
    }

    @Override
    public <ET> NOptional<ET> asType(Class<ET> expectedType) {
        return toValue().asType(expectedType);
    }

    @Override
    public <ET> NOptional<ET> asType(Type expectedType) {
        return toValue().asType(expectedType);
    }
}
