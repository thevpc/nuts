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
final class PrivateNutsArgumentImpl  {

    public static final Pattern PATTERN_OPTION_EQ = Pattern.compile("^((?<optp>[-]+|[+]+)(?<cmt>//)?(?<flg>[!~])?)?(?<optk>[a-zA-Z][a-zA-Z0-9_-]*)?(?<opts>[=](?<optv>.*))?(?<optr>.*)$");
    public static final Pattern PATTERN_OPTION_COL = Pattern.compile("^((?<optp>[-]+|[+]+)(?<cmt>//)?(?<flg>[!~])?)?(?<optk>[a-zA-Z][a-zA-Z0-9_-]*)?(?<opts>[:](?<optv>.*))?(?<optr>.*)$");
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
                currOptionsPattern = Pattern.compile("^((?<optp>[-]+|[+]+)(?<cmt>//)?(?<flg>[!~])?)?(?<optk>[a-zA-Z][a-zA-Z0-9_-]*)?(?<opts>[" + eq + "](?<optv>.*))?(?<optr>.*)$");
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
                active = !(cmt!=null && cmt.length()>0);
                enabled = !(flg!=null && flg.length()>0);
                optionPrefix = optp;
                if(optr!=null && optr.length()>0){
                    optionName=(optk == null ? "" : optk)+optr;
                    key = optp + optionName;
                    value = null;
                }else {
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
    
    public boolean isOption() {
        return option;
    }

    
    public boolean isNonOption() {
        return !isOption();
    }

    
    public String getString() {
        return expression;
    }

    
    public boolean isNegated() {
        return !enabled;
    }

    
    public boolean isEnabled() {
        return enabled;
    }

    
    public boolean isActive() {
        return active;
    }

    
    public boolean isInactive() {
        return !active;
    }

    
    public PrivateNutsArgumentImpl required() {
        if (expression == null) {
            throw new NoSuchElementException("missing value");
        }
        return this;
    }

    
    public boolean isKeyValue() {
        return value != null;
    }

    
    public NutsVal getOptionPrefix() {
        return new NutsBootStrValImpl(optionPrefix);
    }

    
    public String getSeparator() {
        return String.valueOf(eq);
    }

    
    public NutsVal getOptionName() {
        return new NutsBootStrValImpl(optionName);
    }

    
    public NutsVal getValue() {
        return new NutsBootStrValImpl(value) {

            
            public boolean getBoolean() {
                return getBoolean(true, false);
            }

            
            public Boolean getBoolean(Boolean emptyValue, Boolean errorValue) {
                Boolean b = NutsUtilStrings.parseBoolean(this.getString(), emptyValue, errorValue);
                if (b != null && isNegated()) {
                    return !b;
                }
                return b;
            }
        };
    }

    
    public NutsVal getKey() {
        return new NutsBootStrValImpl((key != null) ? key : expression);
    }

    
    public NutsVal getAll() {
        return new NutsBootStrValImpl(expression);
    }

    public boolean isNull() {
        return expression == null;
    }

    public boolean isBlank() {
        return expression == null || expression.trim().isEmpty();
    }

    
    public String toString() {
        return String.valueOf(expression);
    }
}
