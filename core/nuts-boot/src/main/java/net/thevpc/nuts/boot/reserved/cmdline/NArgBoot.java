/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
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
package net.thevpc.nuts.boot.reserved.cmdline;


import net.thevpc.nuts.boot.reserved.util.NUtilsBoot;


import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author thevpc
 */
public class NArgBoot  {

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

    public NArgBoot(String expression) {
        this(expression, '=');
    }

    /**
     * Constructor
     *
     * @param image image string
     * @param eq equals
     */
    public NArgBoot(String image, char eq) {
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

    public boolean isOption() {
        return option;
    }


    public boolean isNonOption() {
        return !isOption();
    }


    public String getStringKey() {
        return getKey();
    }


    public String key() {
        String k = getStringKey();
        return k==null?"":k;
    }


    public String value() {
        return getStringValue();
    }


    public String getStringValue() {
        return getValue();
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


    public NArgBoot required() {
        if (image == null) {
            throw new NoSuchElementException("missing value");
        }
        return this;
    }


    public boolean isKeyValue() {
        return value != null;
    }

    public String getOptionPrefix() {
        return optionPrefix;
    }


    public String getSeparator() {
        return String.valueOf(eq);
    }


    public String getOptionName() {
        return optionName;
    }


    public String getValue() {
        return value;
    }


    public Boolean getBooleanValue() {
        boolean v = NUtilsBoot.parseBooleanOr(getValue(), true);
        if (isNegated()) {
            return isNegated() != v;
        }
        return v;
    }


    public String getKey() {
        return (key == null ? image : key);
    }


    public boolean isFlagOption() {
        if (isOption()) {
            if (getValue()==null) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return String.valueOf(image);
    }

    public String getImage() {
        return image;
    }

    public static NArgBoot of(String s) {
        return new NArgBoot(s);
    }
}
