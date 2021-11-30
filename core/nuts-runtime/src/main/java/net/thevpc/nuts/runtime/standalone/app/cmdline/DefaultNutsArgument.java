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

import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author thevpc
 */
public class DefaultNutsArgument implements NutsArgument {
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
    private final String expression;
    private transient final NutsElements elems;

    public DefaultNutsArgument(String expression,NutsElements elems) {
        this(expression, '=',elems);
    }


    /**
     * Constructor
     *
     * @param expression expression
     * @param eq         equals
     */
    public DefaultNutsArgument(String expression, char eq,NutsElements elems) {
        this.elems = elems;
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
        return value != null;
    }

    public NutsPrimitiveElement getOptionPrefix() {
        return elems.ofString(optionPrefix);
    }

    @Override
    public String getSeparator() {
        return String.valueOf(eq);
    }

    @Override
    public NutsPrimitiveElement getOptionName() {
        return elems.ofString(optionName);
    }

    @Override
    public boolean getBooleanValue() {
        return getBooleanValue(true,false);
    }

    @Override
    public Boolean getBooleanValue(Boolean emptyOrValue) {
        return getBooleanValue(emptyOrValue,emptyOrValue);
    }

    @Override
    public Boolean getBooleanValue(Boolean emptyValue, Boolean errValue) {
        boolean a = NutsUtilStrings.parseBoolean(value, emptyValue, errValue);
        return isNegated() != a;
    }

    @Override
    public NutsPrimitiveElement getValue() {
        return elems.ofString(value);
    }

    @Override
    public NutsPrimitiveElement getKey() {
        return elems.ofString(key == null ? expression : key);
    }

    @Override
    public NutsPrimitiveElement toElement() {
        return elems.ofString(expression);
    }

    @Override
    public String toString() {
        return String.valueOf(expression);
    }
}
