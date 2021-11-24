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
package net.thevpc.nuts.runtime.standalone.app.cmdline;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NutsBootStrValImpl;
import net.thevpc.nuts.runtime.standalone.xtra.vals.DefaultNutsVal;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author thevpc
 */
public class DefaultNutsArgument implements NutsArgument {
    public static final Pattern PATTERN_OPTION_EQ = Pattern.compile("^((?<optp>[-]+|[+]+)(?<flg>//|!|~)?)?(?<optk>[a-zA-Z-9][a-zA-Z-9_-]*)?(?<opts>=(?<optv>.*))?(?<optr>.*)$");
    public static final Pattern PATTERN_OPTION_COL = Pattern.compile("^((?<optp>[-]+|[+]+)(?<flg>//|!|~)?)?(?<optk>[a-zA-Z-9][a-zA-Z-9_-]*)?(?<opts>:(?<optv>.*))?(?<optr>.*)$");
    /**
     * equal character
     */
    private final char eq;
    private final boolean option;
    private final boolean enabled;
    private final boolean negated;
    private final String optionPrefix;
    private final String optionName;
    private final String key;
    private final String value;
    private final String expression;

    public DefaultNutsArgument(String expression) {
        this(expression, '\0');
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
                        enabled = false;
                        negated = false;
                        break;
                    }
                    case "!":
                    case "~": {
                        enabled = true;
                        negated = true;
                        break;
                    }
                    default: {
                        enabled = true;
                        negated = false;
                    }
                }
                optionPrefix = optp;
                optionName = (optk==null?"":optk);
                if (opts!=null && opts.length() > 0) {
                    key = optp + optionName;
                    value = optv + optr;
                } else {
                    key = optp + optionName + optr;
                    value = null;
                }
            } else {
                option = false;
                enabled = true;
                negated = false;
                optionPrefix = null;
                optionName = null;
                if (opts!=null && opts.length() > 0) {
                    key = (optk==null?"":optk);
                    value = optv;
                } else {
                    key = (optk==null?"":optk) + optr;
                    value = null;
                }
            }
        } else {
            enabled = true;
            negated = false;
            option = false;
            optionName = null;
            key = null;
            value = null;
            optionPrefix = null;
        }
    }

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
        return negated;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
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

    @Override
    public NutsVal getValue() {
        return new DefaultNutsVal(value) {
            @Override
            public boolean getBoolean() {
                return getBoolean(true,false);
            }
            @Override
            public Boolean getBoolean(Boolean emptyValue, Boolean errorValue) {
                Boolean b = NutsUtilStrings.parseBoolean(this.getString(), emptyValue, errorValue);
                if (b!=null && isNegated()) {
                    return !b;
                }
                return b;
            }
        };
    }

    @Override
    public NutsVal getKey() {
        return new DefaultNutsVal(key==null?expression:key);
    }

    @Override
    public NutsVal getAll() {
        return new DefaultNutsVal(expression);
    }

    public NutsVal getOptionPrefix() {
        return new DefaultNutsVal(optionPrefix);
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
    public String toString() {
        return String.valueOf(expression);
    }
}
