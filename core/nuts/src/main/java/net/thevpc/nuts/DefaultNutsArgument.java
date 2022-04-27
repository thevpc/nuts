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
package net.thevpc.nuts;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author thevpc
 */
public class DefaultNutsArgument implements NutsArgument {
    public static final Pattern PATTERN_OPTION_EQ = Pattern.compile("^((?<optp>[-]+|[+]+)(?<cmt>//)?(?<flg>[!~])?)?(?<optk>[a-zA-Z][a-zA-Z0-9_.-]*)?(?<opts>[=](?<optv>.*))?(?<optr>.*)$");
    public static final Pattern PATTERN_OPTION_COL = Pattern.compile("^((?<optp>[-]+|[+]+)(?<cmt>//)?(?<flg>[!~])?)?(?<optk>[a-zA-Z][a-zA-Z0-9_.-]*)?(?<opts>[:](?<optv>.*))?(?<optr>.*)$");
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

    public DefaultNutsArgument(String expression) {
        this(expression, '=');
    }


    /**
     * Constructor
     *
     * @param expression expression
     * @param eq         equals
     */
    public DefaultNutsArgument(String expression, char eq) {
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
                currOptionsPattern = Pattern.compile("^((?<optp>[-]+|[+]+)(?<cmt>//)?(?<flg>[!~])?)?(?<optk>[a-zA-Z][a-zA-Z0-9_.-]*)?(?<opts>[" + eq + "](?<optv>.*))?(?<optr>.*)$");
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
    public NutsOptional<String> getStringKey() {
        return getKey().asString();
    }

    @Override
    public String key() {
        return getStringKey().orElse("");
    }

    @Override
    public NutsOptional<String> getStringValue() {
        return getValue().asString();
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
    public NutsArgument required() {
        if (expression == null) {
            throw new NoSuchElementException("missing value");
        }
        return this;
    }

    @Override
    public boolean isKeyValue() {
        return value != null;
    }

    public NutsValue getOptionPrefix() {
        return NutsValue.of(optionPrefix);
    }

    @Override
    public String getSeparator() {
        return String.valueOf(eq);
    }

    @Override
    public NutsValue getOptionName() {
        return NutsValue.of(optionName);
    }

    @Override
    public NutsValue getValue() {
        return NutsValue.of(value);
    }

    @Override
    public NutsOptional<Boolean> getBooleanValue() {
        if (isNegated()) {
            return getValue().asBoolean().ifEmpty(true).map(x -> isNegated() != x);
        }
        return getValue().asBoolean();
    }

    @Override
    public NutsValue getKey() {
        return NutsValue.of(key == null ? expression : key);
    }

    private NutsValue toValue() {
        return NutsValue.of(expression);
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
    public NutsOptional<Instant> asInstant() {
        return toValue().asInstant();
    }

    @Override
    public NutsOptional<Number> asNumber() {
        return toValue().asNumber();
    }

    @Override
    public NutsOptional<Boolean> asBoolean() {
        return toValue().asBoolean();
    }

    @Override
    public NutsOptional<Long> asLong() {
        return toValue().asLong();
    }

    @Override
    public NutsOptional<Double> asDouble() {
        return toValue().asDouble();
    }

    @Override
    public NutsOptional<Float> asFloat() {
        return toValue().asFloat();
    }

    @Override
    public NutsOptional<Byte> asByte() {
        return toValue().asByte();
    }

    @Override
    public NutsOptional<Short> asShort() {
        return toValue().asShort();
    }

    @Override
    public NutsOptional<Integer> asInt() {
        return toValue().asInt();
    }

    @Override
    public NutsOptional<BigInteger> asBigInt() {
        return toValue().asBigInt();
    }

    @Override
    public NutsOptional<BigDecimal> asBigDecimal() {
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
    public NutsOptional<String> asString() {
        return toValue().asString();
    }
}
