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
package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

import java.util.Objects;

/**
 * @app.category Format
 */
public class NutsTextStyle implements NutsEnum {
    private final NutsTextStyleType type;
    private final int variant;

    public NutsTextStyle(NutsTextStyleType type, int variant) {
        this.type = type;
        this.variant = variant;
    }

    public static NutsTextStyle of(NutsTextStyleType style) {
        return of(style, 0);
    }

    public static NutsTextStyle of(NutsTextStyleType style, int variant) {
        return new NutsTextStyle(style, variant);
    }

    public static NutsTextStyle primary1() {
        return primary(1);
    }

    public static NutsTextStyle primary2() {
        return primary(2);
    }

    public static NutsTextStyle primary3() {
        return primary(3);
    }

    public static NutsTextStyle primary4() {
        return primary(4);
    }

    public static NutsTextStyle primary5() {
        return primary(5);
    }

    public static NutsTextStyle primary6() {
        return primary(6);
    }

    public static NutsTextStyle primary7() {
        return primary(7);
    }

    public static NutsTextStyle primary8() {
        return primary(8);
    }

    public static NutsTextStyle primary9() {
        return primary(9);
    }

    public static NutsTextStyle primary(int variant) {
        return of(NutsTextStyleType.PRIMARY, variant);
    }

    public static NutsTextStyle fail(int variant) {
        return of(NutsTextStyleType.FAIL, variant);
    }

    public static NutsTextStyle fail() {
        return of(NutsTextStyleType.FAIL);
    }

    public static NutsTextStyle danger(int variant) {
        return of(NutsTextStyleType.DANGER, variant);
    }

    public static NutsTextStyle danger() {
        return of(NutsTextStyleType.DANGER);
    }

    public static NutsTextStyle title(int variant) {
        return of(NutsTextStyleType.TITLE, variant);
    }

    public static NutsTextStyle secondary(int variant) {
        return of(NutsTextStyleType.SECONDARY, variant);
    }

    public static NutsTextStyle error() {
        return of(NutsTextStyleType.ERROR);
    }

    public static NutsTextStyle error(int variant) {
        return of(NutsTextStyleType.ERROR, variant);
    }

    public static NutsTextStyle option() {
        return of(NutsTextStyleType.OPTION);
    }

    public static NutsTextStyle option(int variant) {
        return of(NutsTextStyleType.OPTION, variant);
    }

    public static NutsTextStyle separator() {
        return of(NutsTextStyleType.SEPARATOR);
    }

    public static NutsTextStyle separator(int variant) {
        return of(NutsTextStyleType.SEPARATOR, variant);
    }

    public static NutsTextStyle version() {
        return of(NutsTextStyleType.VERSION);
    }

    public static NutsTextStyle version(int variant) {
        return of(NutsTextStyleType.VERSION, variant);
    }

    public static NutsTextStyle keyword() {
        return of(NutsTextStyleType.KEYWORD);
    }

    public static NutsTextStyle keyword(int variant) {
        return of(NutsTextStyleType.KEYWORD, variant);
    }

    public static NutsTextStyle reversed() {
        return of(NutsTextStyleType.REVERSED);
    }

    public static NutsTextStyle reversed(int variant) {
        return of(NutsTextStyleType.REVERSED, variant);
    }

    public static NutsTextStyle underlined() {
        return of(NutsTextStyleType.UNDERLINED);
    }

    public static NutsTextStyle striked() {
        return of(NutsTextStyleType.STRIKED);
    }

    public static NutsTextStyle striked(int variant) {
        return of(NutsTextStyleType.STRIKED, variant);
    }

    public static NutsTextStyle italic() {
        return of(NutsTextStyleType.ITALIC);
    }

    public static NutsTextStyle italic(int variant) {
        return of(NutsTextStyleType.ITALIC, variant);
    }

    public static NutsTextStyle bold() {
        return of(NutsTextStyleType.BOLD);
    }

    public static NutsTextStyle bool() {
        return of(NutsTextStyleType.BOOLEAN);
    }

    public static NutsTextStyle bool(int variant) {
        return of(NutsTextStyleType.BOOLEAN, variant);
    }

    public static NutsTextStyle blink() {
        return of(NutsTextStyleType.BLINK);
    }

    public static NutsTextStyle pale() {
        return of(NutsTextStyleType.PALE);
    }

    public static NutsTextStyle pale(int variant) {
        return of(NutsTextStyleType.PALE, variant);
    }

    public static NutsTextStyle success() {
        return of(NutsTextStyleType.SUCCESS);
    }

    public static NutsTextStyle success(int variant) {
        return of(NutsTextStyleType.SUCCESS, variant);
    }

    public static NutsTextStyle path() {
        return of(NutsTextStyleType.PATH);
    }

    public static NutsTextStyle path(int variant) {
        return of(NutsTextStyleType.PATH, variant);
    }

    public static NutsTextStyle warn() {
        return of(NutsTextStyleType.WARN);
    }

    public static NutsTextStyle warn(int variant) {
        return of(NutsTextStyleType.WARN, variant);
    }

    public static NutsTextStyle config() {
        return of(NutsTextStyleType.CONFIG);
    }

    public static NutsTextStyle config(int variant) {
        return of(NutsTextStyleType.CONFIG, variant);
    }

    public static NutsTextStyle info() {
        return of(NutsTextStyleType.INFO);
    }

    public static NutsTextStyle info(int variant) {
        return of(NutsTextStyleType.INFO, variant);
    }

    public static NutsTextStyle string() {
        return of(NutsTextStyleType.STRING);
    }

    public static NutsTextStyle string(int variant) {
        return of(NutsTextStyleType.STRING, variant);
    }

    public static NutsTextStyle operator() {
        return of(NutsTextStyleType.OPERATOR);
    }

    public static NutsTextStyle operator(int variant) {
        return of(NutsTextStyleType.OPERATOR, variant);
    }

    public static NutsTextStyle input() {
        return of(NutsTextStyleType.INPUT);
    }

    public static NutsTextStyle input(int variant) {
        return of(NutsTextStyleType.INPUT, variant);
    }

    public static NutsTextStyle comments() {
        return of(NutsTextStyleType.COMMENTS);
    }

    public static NutsTextStyle comments(int variant) {
        return of(NutsTextStyleType.COMMENTS, variant);
    }

    public static NutsTextStyle variable() {
        return of(NutsTextStyleType.VAR);
    }

    public static NutsTextStyle variable(int variant) {
        return of(NutsTextStyleType.VAR, variant);
    }

    public static NutsTextStyle number() {
        return of(NutsTextStyleType.NUMBER);
    }

    public static NutsTextStyle date() {
        return of(NutsTextStyleType.DATE);
    }

    public static NutsTextStyle date(int variant) {
        return of(NutsTextStyleType.DATE, variant);
    }

    public static NutsTextStyle number(int variant) {
        return of(NutsTextStyleType.VAR, variant);
    }

    public static NutsTextStyle foregroundColor(int variant) {
        return of(NutsTextStyleType.FORE_COLOR, variant);
    }

    public static NutsTextStyle foregroundTrueColor(int variant) {
        return of(NutsTextStyleType.FORE_TRUE_COLOR, variant);
    }

    public static NutsTextStyle backgroundColor(int variant) {
        return of(NutsTextStyleType.BACK_COLOR, variant);
    }

    public static NutsTextStyle backgroundTrueColor(int variant) {
        return of(NutsTextStyleType.BACK_TRUE_COLOR, variant);
    }

    public static NutsOptional<NutsTextStyle> parse(String value) {
        value = value == null ? "" : value.trim();
        if (value.isEmpty()) {
            return NutsOptional.ofEmpty(s -> NutsMessage.cstyle(NutsTextStyle.class.getSimpleName() + " is empty"));
        }
        switch (value) {
            case "/":
                return NutsOptional.of(italic());
            case "_":
                return NutsOptional.of(underlined());
            case "%":
                return NutsOptional.of(blink());
            case "!":
                return NutsOptional.of(reversed());
            case "+":
                return NutsOptional.of(bold());
            case "-":
                return NutsOptional.of(striked());
        }
        String finalValue = value;
        int par = value.indexOf('(');
        String nbr = "";
        String key = value;
        if (par > 0) {
            int b = value.indexOf(')', par);
            if (b > 0) {
                nbr = value.substring(par + 1, b);
                key = value.substring(0, par);
            }
        } else {
            if (value.trim().startsWith("fx") || value.trim().startsWith("bx")) {
                key = value.trim().substring(0, 2);
                nbr = value.trim().substring(2);
            } else if (value.trim().startsWith("foregroundx")) {
                int len = "foregroundx".length();
                key = value.trim().substring(0, len);
                nbr = value.trim().substring(len);
            } else if (value.trim().startsWith("backgroundx")) {
                int len = "backgroundx".length();
                key = value.trim().substring(0, len);
                nbr = value.trim().substring(len);
            } else {
                int len = value.length();
                int x = len;
                while (x - 1 >= 0 && Character.isDigit(value.charAt(x - 1))) {
                    x--;
                }
                if (x < len) {
                    nbr = value.substring(x, len);
                    key = value.substring(0, x);
                }
            }
        }
        nbr = nbr.trim();
        key = key.trim();
        if (nbr.isEmpty()) {
            nbr = "0";
        }
        if (key.isEmpty()) {
            key = "p";
        }
        NutsTextStyleType t = NutsTextStyleType.parse(key).orElse(null);
        if (t == null) {
            if (NutsBlankable.isBlank(key)) {
                return NutsOptional.ofEmpty(s -> NutsMessage.cstyle(NutsTextStyle.class.getSimpleName() + " is empty"));
            }
            return NutsOptional.ofError(s -> NutsMessage.cstyle(NutsTextStyle.class.getSimpleName() + " invalid value : %s", finalValue));
        }
        switch (t) {
            case FORE_TRUE_COLOR:
            case BACK_TRUE_COLOR: {
                Integer ii = NutsApiUtils.parseInt16(nbr, null, null);
                if (ii == null) {
                    if (NutsBlankable.isBlank(key)) {
                        ii = 0;
                    } else {
                        return NutsOptional.ofError(s -> NutsMessage.cstyle(NutsTextStyle.class.getSimpleName() + " invalid value : %s", finalValue));
                    }
                }
                return NutsOptional.of(NutsTextStyle.of(t, ii));
            }
            default: {
                Integer ii = NutsApiUtils.parseInt(nbr, null, null);
                if (ii == null) {
                    if (NutsBlankable.isBlank(key)) {
                        ii = 0;
                    } else {
                        return NutsOptional.ofError(s -> NutsMessage.cstyle(NutsTextStyle.class.getSimpleName() + " invalid value : %s", finalValue));
                    }
                }
                return NutsOptional.of(NutsTextStyle.of(t, ii));
            }
        }
    }

    public NutsTextStyles append(NutsTextStyle other) {
        return NutsTextStyles.of(this, other);
    }

    public NutsTextStyles append(NutsTextStyles other) {
        return NutsTextStyles.of(this).append(other);
    }

    public NutsTextStyleType getType() {
        return type;
    }

    public int getVariant() {
        return variant;
    }

    @Override
    public String id() {
        switch (type) {
            case PLAIN:
                return "";
            case PRIMARY:
                return "p" + (variant <= 0 ? "" : String.valueOf(variant));
            case SECONDARY:
                return "s" + (variant <= 0 ? "" : String.valueOf(variant));
            case UNDERLINED:
                return "_" + (variant <= 0 ? "" : String.valueOf(variant));
            case STRIKED:
                return "-" + (variant <= 0 ? "" : String.valueOf(variant));
            case BLINK:
                return "%" + (variant <= 0 ? "" : String.valueOf(variant));
            case ITALIC:
                return "/" + (variant <= 0 ? "" : String.valueOf(variant));
            case BOLD:
                return "+" + (variant <= 0 ? "" : String.valueOf(variant));
            case REVERSED:
                return "!" + (variant <= 0 ? "" : String.valueOf(variant));
            case FORE_COLOR: {
                return "f" + (variant <= 0 ? "0" : String.valueOf(variant));
            }
            case BACK_COLOR: {
                return "b" + (variant <= 0 ? "0" : String.valueOf(variant));
            }
            case FORE_TRUE_COLOR: {
                StringBuilder s = new StringBuilder(Integer.toString(variant, 16));
                while (s.length() < 8) {
                    s.insert(0, '0');
                }
                return "fx" + s;
            }
            case BACK_TRUE_COLOR: {
                StringBuilder s = new StringBuilder(Integer.toString(variant, 16));
                while (s.length() < 8) {
                    s.insert(0, '0');
                }
                return "bx" + s;
            }
            default: {
                return type.id() + (variant <= 0 ? "" : String.valueOf(variant));
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, variant);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsTextStyle that = (NutsTextStyle) o;
        return variant == that.variant && type == that.type;
    }

    @Override
    public String toString() {
        if (variant == 0) {
            return String.valueOf(type);
        }
        return type + "(" + variant + ")";
    }
}
