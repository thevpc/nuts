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
 *
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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLines;
import net.thevpc.nuts.NutsUtilStrings;
import net.thevpc.nuts.NutsVal;

import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a minimal implementation of NutsArgument and hence should not be
 * used. Instead an instance of NutsArgument can be retrieved using
 * {@link NutsCommandLines#createArgument(String)}
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
final class PrivateNutsArgumentImpl implements NutsArgument {

    public static final Pattern PATTERN_OPTION_EQ = Pattern.compile("^((?<optp>[-]+|[+]+)(?<flg>//|!|~)?)?(?<optk>[a-zA-Z-9][a-zA-Z-9_-]*)?(?<opts>=(?<optv>.*))?(?<optr>.*)$");
    public static final Pattern PATTERN_OPTION_COL = Pattern.compile("^((?<optp>[-]+|[+]+)(?<flg>//|!|~)?)?(?<optk>[a-zA-Z-9][a-zA-Z-9_-]*)?(?<opts>:(?<optv>.*))?(?<optr>.*)$");
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
    protected String expression;

    /**
     * Constructor
     *
     * @param expression expression
     * @param eq         equals
     */
    public PrivateNutsArgumentImpl(String expression, char eq) {
        this.eq = (eq == '\0' ? '=' : eq);
        this.expression = expression;
        Pattern currOptionsPattern;
        switch (eq) {
            case '=': {
                currOptionsPattern = PATTERN_OPTION_EQ;
                break;
            }
            case ':': {
                currOptionsPattern = PATTERN_OPTION_COL;
                break;
            }
            default: {
                currOptionsPattern = Pattern.compile("^((?<optp>[-]+|[+]+)(?<flg>//|!|~)?)?(?<optk>[a-zA-Z-9][a-zA-Z-9_-]*)?(?<opts>[" + eq + "](?<optv>.*))?(?<optr>.*)$");
            }
        }
        Matcher matcher = currOptionsPattern.matcher(expression == null ? "" : expression);
        if (matcher.find()) {
            String optp = matcher.group("optp");
            String flg = matcher.group("flg");
            String optk = matcher.group("optk");
            String opts = matcher.group("opts");
            String optv = matcher.group("optv");
            String optr = matcher.group("optr");
            if (optp != null && optp.length() > 0) {
                option = true;
                switch (flg == null ? "" : flg) {
                    case "//": {
                        active = false;
                        enabled = true;
                        break;
                    }
                    case "!":
                    case "~": {
                        active = true;
                        enabled = false;
                        break;
                    }
                    default: {
                        active = true;
                        enabled = true;
                    }
                }
                optionPrefix = optp;
                optionName = (optk == null ? "" : optk);
                if (opts != null && opts.length() > 0) {
                    key = optp + optionName;
                    value = optv + optr;
                } else {
                    key = optp + optionName + optr;
                    value = null;
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
                    key = (optk == null ? "" : optk) + optr;
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
    public String getString() {
        return expression;
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
        return expression != null && expression.indexOf(eq) >= 0;
    }

    @Override
    public NutsVal getOptionPrefix() {
        return new NutsBootStrValImpl(optionPrefix);
    }

    @Override
    public String getSeparator() {
        return String.valueOf(eq);
    }

    @Override
    public NutsVal getOptionName() {
        return new NutsBootStrValImpl(optionName);
    }

    @Override
    public NutsVal getValue() {
        return new NutsBootStrValImpl(value) {

            @Override
            public boolean getBoolean() {
                return getBoolean(true, false);
            }

            @Override
            public Boolean getBoolean(Boolean emptyValue, Boolean errorValue) {
                Boolean b = NutsUtilStrings.parseBoolean(this.getString(), emptyValue, errorValue);
                if (b != null && isNegated()) {
                    return !b;
                }
                return b;
            }
        };
    }

    @Override
    public NutsVal getKey() {
        return new NutsBootStrValImpl((key != null) ? key : expression);
    }

    @Override
    public NutsVal getAll() {
        return new NutsBootStrValImpl(expression);
    }

    public boolean isNull() {
        return expression == null;
    }

    public boolean isBlank() {
        return expression == null || expression.trim().isEmpty();
    }

    @Override
    public String toString() {
        return String.valueOf(expression);
    }
}
