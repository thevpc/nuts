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
package net.thevpc.nuts.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Objects;

/**
 * @app.category Format
 */
public class NTextStyle implements NEnum {
    private final NTextStyleType type;
    private final int variant;

    public NTextStyle(NTextStyleType type, int variant) {
        this.type = type;
        this.variant = variant;
    }

    public static NTextStyle of(NTextStyleType style) {
        return of(style, 0);
    }

    public static NTextStyle of(NTextStyleType style, int variant) {
        return new NTextStyle(style, variant);
    }

    public static NTextStyle primary1() {
        return primary(1);
    }

    public static NTextStyle primary2() {
        return primary(2);
    }

    public static NTextStyle primary3() {
        return primary(3);
    }

    public static NTextStyle primary4() {
        return primary(4);
    }

    public static NTextStyle primary5() {
        return primary(5);
    }

    public static NTextStyle primary6() {
        return primary(6);
    }

    public static NTextStyle primary7() {
        return primary(7);
    }

    public static NTextStyle primary8() {
        return primary(8);
    }

    public static NTextStyle primary9() {
        return primary(9);
    }

    public static NTextStyle primary(int variant) {
        return of(NTextStyleType.PRIMARY, variant);
    }

    public static NTextStyle fail(int variant) {
        return of(NTextStyleType.FAIL, variant);
    }

    public static NTextStyle fail() {
        return of(NTextStyleType.FAIL);
    }

    public static NTextStyle danger(int variant) {
        return of(NTextStyleType.DANGER, variant);
    }

    public static NTextStyle danger() {
        return of(NTextStyleType.DANGER);
    }

    public static NTextStyle title(int variant) {
        return of(NTextStyleType.TITLE, variant);
    }

    public static NTextStyle secondary(int variant) {
        return of(NTextStyleType.SECONDARY, variant);
    }

    public static NTextStyle error() {
        return of(NTextStyleType.ERROR);
    }

    public static NTextStyle error(int variant) {
        return of(NTextStyleType.ERROR, variant);
    }

    public static NTextStyle option() {
        return of(NTextStyleType.OPTION);
    }

    public static NTextStyle option(int variant) {
        return of(NTextStyleType.OPTION, variant);
    }

    public static NTextStyle separator() {
        return of(NTextStyleType.SEPARATOR);
    }

    public static NTextStyle separator(int variant) {
        return of(NTextStyleType.SEPARATOR, variant);
    }

    public static NTextStyle version() {
        return of(NTextStyleType.VERSION);
    }

    public static NTextStyle version(int variant) {
        return of(NTextStyleType.VERSION, variant);
    }

    public static NTextStyle keyword() {
        return of(NTextStyleType.KEYWORD);
    }

    public static NTextStyle keyword(int variant) {
        return of(NTextStyleType.KEYWORD, variant);
    }

    public static NTextStyle reversed() {
        return of(NTextStyleType.REVERSED);
    }

    public static NTextStyle reversed(int variant) {
        return of(NTextStyleType.REVERSED, variant);
    }

    public static NTextStyle underlined() {
        return of(NTextStyleType.UNDERLINED);
    }

    public static NTextStyle striked() {
        return of(NTextStyleType.STRIKED);
    }

    public static NTextStyle striked(int variant) {
        return of(NTextStyleType.STRIKED, variant);
    }

    public static NTextStyle italic() {
        return of(NTextStyleType.ITALIC);
    }

    public static NTextStyle italic(int variant) {
        return of(NTextStyleType.ITALIC, variant);
    }

    public static NTextStyle bold() {
        return of(NTextStyleType.BOLD);
    }

    public static NTextStyle bool() {
        return of(NTextStyleType.BOOLEAN);
    }

    public static NTextStyle bool(int variant) {
        return of(NTextStyleType.BOOLEAN, variant);
    }

    public static NTextStyle blink() {
        return of(NTextStyleType.BLINK);
    }

    public static NTextStyle pale() {
        return of(NTextStyleType.PALE);
    }

    public static NTextStyle pale(int variant) {
        return of(NTextStyleType.PALE, variant);
    }

    public static NTextStyle success() {
        return of(NTextStyleType.SUCCESS);
    }

    public static NTextStyle success(int variant) {
        return of(NTextStyleType.SUCCESS, variant);
    }

    public static NTextStyle path() {
        return of(NTextStyleType.PATH);
    }

    public static NTextStyle path(int variant) {
        return of(NTextStyleType.PATH, variant);
    }

    public static NTextStyle warn() {
        return of(NTextStyleType.WARN);
    }

    public static NTextStyle warn(int variant) {
        return of(NTextStyleType.WARN, variant);
    }

    public static NTextStyle config() {
        return of(NTextStyleType.CONFIG);
    }

    public static NTextStyle config(int variant) {
        return of(NTextStyleType.CONFIG, variant);
    }

    public static NTextStyle info() {
        return of(NTextStyleType.INFO);
    }

    public static NTextStyle info(int variant) {
        return of(NTextStyleType.INFO, variant);
    }

    public static NTextStyle string() {
        return of(NTextStyleType.STRING);
    }

    public static NTextStyle string(int variant) {
        return of(NTextStyleType.STRING, variant);
    }

    public static NTextStyle operator() {
        return of(NTextStyleType.OPERATOR);
    }

    public static NTextStyle operator(int variant) {
        return of(NTextStyleType.OPERATOR, variant);
    }

    public static NTextStyle input() {
        return of(NTextStyleType.INPUT);
    }

    public static NTextStyle input(int variant) {
        return of(NTextStyleType.INPUT, variant);
    }

    public static NTextStyle comments() {
        return of(NTextStyleType.COMMENTS);
    }

    public static NTextStyle comments(int variant) {
        return of(NTextStyleType.COMMENTS, variant);
    }

    public static NTextStyle variable() {
        return of(NTextStyleType.VAR);
    }

    public static NTextStyle variable(int variant) {
        return of(NTextStyleType.VAR, variant);
    }

    public static NTextStyle number() {
        return of(NTextStyleType.NUMBER);
    }

    public static NTextStyle date() {
        return of(NTextStyleType.DATE);
    }

    public static NTextStyle date(int variant) {
        return of(NTextStyleType.DATE, variant);
    }

    public static NTextStyle number(int variant) {
        return of(NTextStyleType.VAR, variant);
    }

    public static NTextStyle foregroundColor(int variant) {
        return of(NTextStyleType.FORE_COLOR, variant);
    }

    public static NTextStyle foregroundTrueColor(int variant) {
        return of(NTextStyleType.FORE_TRUE_COLOR, variant);
    }

    public static NTextStyle backgroundColor(int variant) {
        return of(NTextStyleType.BACK_COLOR, variant);
    }

    public static NTextStyle backgroundTrueColor(int variant) {
        return of(NTextStyleType.BACK_TRUE_COLOR, variant);
    }

    public static NOptional<NTextStyle> parse(String value) {
        value = value == null ? "" : value.trim();
        if (value.isEmpty()) {
            return NOptional.ofEmpty(s -> NMsg.ofC("%s is empty", NTextStyle.class.getSimpleName()));
        }
        switch (value) {
            case "/":
                return NOptional.of(italic());
            case "_":
                return NOptional.of(underlined());
            case "%":
                return NOptional.of(blink());
            case "!":
                return NOptional.of(reversed());
            case "+":
                return NOptional.of(bold());
            case "-":
                return NOptional.of(striked());
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
        NTextStyleType t = NTextStyleType.parse(key).orNull();
        if (t == null) {
            if (NBlankable.isBlank(key)) {
                return NOptional.ofEmpty(s -> NMsg.ofC("%s is empty", NTextStyle.class.getSimpleName()));
            }
            return NOptional.ofError(s -> NMsg.ofC("%s invalid value : %s", NTextStyle.class.getSimpleName(), finalValue));
        }
        switch (t) {
            case FORE_TRUE_COLOR:
            case BACK_TRUE_COLOR: {
                Integer ii = NLiteral.of("0x" + nbr).asInt().orNull();
                if (ii == null) {
                    if (NBlankable.isBlank(key)) {
                        ii = 0;
                    } else {
                        return NOptional.ofError(s -> NMsg.ofC(NTextStyle.class.getSimpleName() + " invalid value : %s", finalValue));
                    }
                }
                return NOptional.of(NTextStyle.of(t, ii));
            }
            default: {
                Integer ii = NLiteral.of(nbr).asInt().orNull();
                if (ii == null) {
                    if (NBlankable.isBlank(key)) {
                        ii = 0;
                    } else {
                        return NOptional.ofError(s -> NMsg.ofC(NTextStyle.class.getSimpleName() + " invalid value : %s", finalValue));
                    }
                }
                return NOptional.of(NTextStyle.of(t, ii));
            }
        }
    }

    public NTextStyles append(NTextStyle other) {
        return NTextStyles.of(this, other);
    }

    public NTextStyles append(NTextStyles other) {
        return NTextStyles.of(this).append(other);
    }

    public NTextStyleType getType() {
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
        NTextStyle that = (NTextStyle) o;
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
