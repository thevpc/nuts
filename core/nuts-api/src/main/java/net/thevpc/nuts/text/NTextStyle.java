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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.text;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.*;

import java.util.Objects;

/**
 * @app.category Format
 */
public class NTextStyle implements NEnum {
    private final NTextStyleType type;
    private final int variant;

    /**
     * N text style.
     *
     * @param type type
     * @param variant variant
     * @return n text style result
     */
    public NTextStyle(NTextStyleType type, int variant) {
        this.type = type;
        this.variant = variant;
    }

    /**
     * Creates a new instance.
     *
     * @param style style
     * @return of result
     */
    public static NTextStyle of(NTextStyleType style) {
        return of(style, 0);
    }

    /**
     * Creates a new instance.
     *
     * @param style style
     * @param variant variant
     * @return of result
     */
    public static NTextStyle of(NTextStyleType style, int variant) {
        return new NTextStyle(style, variant);
    }

    /**
     * Primary1.
     *
     * @return primary1 result
     */
    public static NTextStyle primary1() {
        return primary(1);
    }

    /**
     * Primary2.
     *
     * @return primary2 result
     */
    public static NTextStyle primary2() {
        return primary(2);
    }

    /**
     * Primary3.
     *
     * @return primary3 result
     */
    public static NTextStyle primary3() {
        return primary(3);
    }

    /**
     * Primary4.
     *
     * @return primary4 result
     */
    public static NTextStyle primary4() {
        return primary(4);
    }

    /**
     * Primary5.
     *
     * @return primary5 result
     */
    public static NTextStyle primary5() {
        return primary(5);
    }

    /**
     * Primary6.
     *
     * @return primary6 result
     */
    public static NTextStyle primary6() {
        return primary(6);
    }

    /**
     * Primary7.
     *
     * @return primary7 result
     */
    public static NTextStyle primary7() {
        return primary(7);
    }

    /**
     * Primary8.
     *
     * @return primary8 result
     */
    public static NTextStyle primary8() {
        return primary(8);
    }

    /**
     * Primary9.
     *
     * @return primary9 result
     */
    public static NTextStyle primary9() {
        return primary(9);
    }

    /**
     * Primary.
     *
     * @param variant variant
     * @return primary result
     */
    public static NTextStyle primary(int variant) {
        return of(NTextStyleType.PRIMARY, variant);
    }

    /**
     * Fail.
     *
     * @param variant variant
     * @return fail result
     */
    public static NTextStyle fail(int variant) {
        return of(NTextStyleType.FAIL, variant);
    }

    /**
     * Fail.
     *
     * @return fail result
     */
    public static NTextStyle fail() {
        return of(NTextStyleType.FAIL);
    }

    /**
     * Danger.
     *
     * @param variant variant
     * @return danger result
     */
    public static NTextStyle danger(int variant) {
        return of(NTextStyleType.DANGER, variant);
    }

    /**
     * Danger.
     *
     * @return danger result
     */
    public static NTextStyle danger() {
        return of(NTextStyleType.DANGER);
    }

    /**
     * Title.
     *
     * @param variant variant
     * @return title result
     */
    public static NTextStyle title(int variant) {
        return of(NTextStyleType.TITLE, variant);
    }

    /**
     * Title1.
     *
     * @return title1 result
     */
    public static NTextStyle title1() {
        return title(1);
    }

    /**
     * Title2.
     *
     * @return title2 result
     */
    public static NTextStyle title2() {
        return title(2);
    }

    /**
     * Title3.
     *
     * @return title3 result
     */
    public static NTextStyle title3() {
        return title(3);
    }

    /**
     * Title4.
     *
     * @return title4 result
     */
    public static NTextStyle title4() {
        return title(4);
    }

    /**
     * Title5.
     *
     * @return title5 result
     */
    public static NTextStyle title5() {
        return title(5);
    }

    /**
     * Title6.
     *
     * @return title6 result
     */
    public static NTextStyle title6() {
        return title(6);
    }

    /**
     * Title7.
     *
     * @return title7 result
     */
    public static NTextStyle title7() {
        return title(7);
    }

    /**
     * Title8.
     *
     * @return title8 result
     */
    public static NTextStyle title8() {
        return title(8);
    }

    /**
     * Title9.
     *
     * @return title9 result
     */
    public static NTextStyle title9() {
        return title(9);
    }

    /**
     * Secondary.
     *
     * @param variant variant
     * @return secondary result
     */
    public static NTextStyle secondary(int variant) {
        return of(NTextStyleType.SECONDARY, variant);
    }

    /**
     * Secondary1.
     *
     * @return secondary1 result
     */
    public static NTextStyle secondary1() {
        return secondary(1);
    }

    /**
     * Secondary2.
     *
     * @return secondary2 result
     */
    public static NTextStyle secondary2() {
        return secondary(2);
    }

    /**
     * Secondary3.
     *
     * @return secondary3 result
     */
    public static NTextStyle secondary3() {
        return secondary(3);
    }

    /**
     * Secondary4.
     *
     * @return secondary4 result
     */
    public static NTextStyle secondary4() {
        return secondary(4);
    }

    /**
     * Secondary5.
     *
     * @return secondary5 result
     */
    public static NTextStyle secondary5() {
        return secondary(5);
    }

    /**
     * Secondary6.
     *
     * @return secondary6 result
     */
    public static NTextStyle secondary6() {
        return secondary(6);
    }

    /**
     * Secondary7.
     *
     * @return secondary7 result
     */
    public static NTextStyle secondary7() {
        return secondary(7);
    }

    /**
     * Secondary8.
     *
     * @return secondary8 result
     */
    public static NTextStyle secondary8() {
        return secondary(8);
    }

    /**
     * Secondary9.
     *
     * @return secondary9 result
     */
    public static NTextStyle secondary9() {
        return secondary(9);
    }

    /**
     * Error.
     *
     * @return error result
     */
    public static NTextStyle error() {
        return of(NTextStyleType.ERROR);
    }

    /**
     * Error.
     *
     * @param variant variant
     * @return error result
     */
    public static NTextStyle error(int variant) {
        return of(NTextStyleType.ERROR, variant);
    }

    /**
     * Option.
     *
     * @return option result
     */
    public static NTextStyle option() {
        return of(NTextStyleType.OPTION);
    }

    /**
     * Option.
     *
     * @param variant variant
     * @return option result
     */
    public static NTextStyle option(int variant) {
        return of(NTextStyleType.OPTION, variant);
    }

    /**
     * Separator.
     *
     * @return separator result
     */
    public static NTextStyle separator() {
        return of(NTextStyleType.SEPARATOR);
    }

    /**
     * Separator.
     *
     * @param variant variant
     * @return separator result
     */
    public static NTextStyle separator(int variant) {
        return of(NTextStyleType.SEPARATOR, variant);
    }

    /**
     * Version.
     *
     * @return version result
     */
    public static NTextStyle version() {
        return of(NTextStyleType.VERSION);
    }

    /**
     * Version.
     *
     * @param variant variant
     * @return version result
     */
    public static NTextStyle version(int variant) {
        return of(NTextStyleType.VERSION, variant);
    }

    /**
     * Keyword.
     *
     * @return keyword result
     */
    public static NTextStyle keyword() {
        return of(NTextStyleType.KEYWORD);
    }

    /**
     * Keyword.
     *
     * @param variant variant
     * @return keyword result
     */
    public static NTextStyle keyword(int variant) {
        return of(NTextStyleType.KEYWORD, variant);
    }

    /**
     * entity style
     * @since 1.0.0
     * @return entity style
     */
    public static NTextStyle entity() {
        return of(NTextStyleType.ENTITY);
    }

    /**
     * action style
     * @since 1.0.0
     * @return action style
     */
    public static NTextStyle action() {
        return of(NTextStyleType.ACTION);
    }

    /**
     * annotation style
     * @since 1.0.0
     * @return annotation style
     */
    public static NTextStyle annotation() {
        return of(NTextStyleType.ANNOTATION);
    }

    /**
     * entity style
     * @since 1.0.0
     * @return entity style
     */
    public static NTextStyle entity(int variant) {
        return of(NTextStyleType.ENTITY, variant);
    }

    /**
     * action style
     * @since 1.0.0
     * @return action style
     */
    public static NTextStyle action(int variant) {
        return of(NTextStyleType.ACTION, variant);
    }

    /**
     * annotation style
     * @since 1.0.0
     * @return annotation style
     */
    public static NTextStyle annotation(int variant) {
        return of(NTextStyleType.ANNOTATION, variant);
    }

    /**
     * Reversed.
     *
     * @return reversed result
     */
    public static NTextStyle reversed() {
        return of(NTextStyleType.REVERSED);
    }

    /**
     * Reversed.
     *
     * @param variant variant
     * @return reversed result
     */
    public static NTextStyle reversed(int variant) {
        return of(NTextStyleType.REVERSED, variant);
    }

    /**
     * Underlined.
     *
     * @return underlined result
     */
    public static NTextStyle underlined() {
        return of(NTextStyleType.UNDERLINED);
    }

    /**
     * Striked.
     *
     * @return striked result
     */
    public static NTextStyle striked() {
        return striked(0);
    }

    /**
     * Striked.
     *
     * @param variant variant
     * @return striked result
     */
    public static NTextStyle striked(int variant) {
        return of(NTextStyleType.STRIKED, variant);
    }

    /**
     * Italic.
     *
     * @return italic result
     */
    public static NTextStyle italic() {
        return of(NTextStyleType.ITALIC);
    }

    /**
     * Italic.
     *
     * @param variant variant
     * @return italic result
     */
    public static NTextStyle italic(int variant) {
        return of(NTextStyleType.ITALIC, variant);
    }

    /**
     * Bold.
     *
     * @return bold result
     */
    public static NTextStyle bold() {
        return of(NTextStyleType.BOLD);
    }

    /**
     * Bool.
     *
     * @return bool result
     */
    public static NTextStyle bool() {
        return of(NTextStyleType.BOOLEAN);
    }

    /**
     * Bool.
     *
     * @param variant variant
     * @return bool result
     */
    public static NTextStyle bool(int variant) {
        return of(NTextStyleType.BOOLEAN, variant);
    }

    /**
     * Blink.
     *
     * @return blink result
     */
    public static NTextStyle blink() {
        return of(NTextStyleType.BLINK);
    }

    /**
     * Pale.
     *
     * @return pale result
     */
    public static NTextStyle pale() {
        return of(NTextStyleType.PALE);
    }

    /**
     * Pale.
     *
     * @param variant variant
     * @return pale result
     */
    public static NTextStyle pale(int variant) {
        return of(NTextStyleType.PALE, variant);
    }

    /**
     * Success.
     *
     * @return success result
     */
    public static NTextStyle success() {
        return of(NTextStyleType.SUCCESS);
    }

    /**
     * Success.
     *
     * @param variant variant
     * @return success result
     */
    public static NTextStyle success(int variant) {
        return of(NTextStyleType.SUCCESS, variant);
    }

    /**
     * Path.
     *
     * @return path result
     */
    public static NTextStyle path() {
        return of(NTextStyleType.PATH);
    }

    /**
     * Path.
     *
     * @param variant variant
     * @return path result
     */
    public static NTextStyle path(int variant) {
        return of(NTextStyleType.PATH, variant);
    }

    /**
     * Warn.
     *
     * @return warn result
     */
    public static NTextStyle warn() {
        return of(NTextStyleType.WARN);
    }

    /**
     * Warn.
     *
     * @param variant variant
     * @return warn result
     */
    public static NTextStyle warn(int variant) {
        return of(NTextStyleType.WARN, variant);
    }

    /**
     * Config.
     *
     * @return config result
     */
    public static NTextStyle config() {
        return of(NTextStyleType.CONFIG);
    }

    /**
     * Config.
     *
     * @param variant variant
     * @return config result
     */
    public static NTextStyle config(int variant) {
        return of(NTextStyleType.CONFIG, variant);
    }

    /**
     * Info.
     *
     * @return info result
     */
    public static NTextStyle info() {
        return of(NTextStyleType.INFO);
    }

    /**
     * Info.
     *
     * @param variant variant
     * @return info result
     */
    public static NTextStyle info(int variant) {
        return of(NTextStyleType.INFO, variant);
    }

    /**
     * String.
     *
     * @return string result
     */
    public static NTextStyle string() {
        return of(NTextStyleType.STRING);
    }

    /**
     * String.
     *
     * @param variant variant
     * @return string result
     */
    public static NTextStyle string(int variant) {
        return of(NTextStyleType.STRING, variant);
    }

    /**
     * Operator.
     *
     * @return operator result
     */
    public static NTextStyle operator() {
        return of(NTextStyleType.OPERATOR);
    }

    /**
     * Operator.
     *
     * @param variant variant
     * @return operator result
     */
    public static NTextStyle operator(int variant) {
        return of(NTextStyleType.OPERATOR, variant);
    }

    /**
     * Input.
     *
     * @return input result
     */
    public static NTextStyle input() {
        return of(NTextStyleType.INPUT);
    }

    /**
     * Input.
     *
     * @param variant variant
     * @return input result
     */
    public static NTextStyle input(int variant) {
        return of(NTextStyleType.INPUT, variant);
    }

    /**
     * Comments.
     *
     * @return comments result
     */
    public static NTextStyle comments() {
        return of(NTextStyleType.COMMENTS);
    }

    /**
     * Comments.
     *
     * @param variant variant
     * @return comments result
     */
    public static NTextStyle comments(int variant) {
        return of(NTextStyleType.COMMENTS, variant);
    }

    /**
     * Variable.
     *
     * @return variable result
     */
    public static NTextStyle variable() {
        return of(NTextStyleType.VAR);
    }

    /**
     * Variable.
     *
     * @param variant variant
     * @return variable result
     */
    public static NTextStyle variable(int variant) {
        return of(NTextStyleType.VAR, variant);
    }

    /**
     * Number.
     *
     * @return number result
     */
    public static NTextStyle number() {
        return of(NTextStyleType.NUMBER);
    }

    /**
     * Date.
     *
     * @return date result
     */
    public static NTextStyle date() {
        return of(NTextStyleType.DATE);
    }

    /**
     * Date.
     *
     * @param variant variant
     * @return date result
     */
    public static NTextStyle date(int variant) {
        return of(NTextStyleType.DATE, variant);
    }

    /**
     * Number.
     *
     * @param variant variant
     * @return number result
     */
    public static NTextStyle number(int variant) {
        return of(NTextStyleType.VAR, variant);
    }

    /**
     * Foreground color.
     *
     * @param variant variant
     * @return foreground color result
     */
    public static NTextStyle foregroundColor(int variant) {
        return of(NTextStyleType.FORE_COLOR, variant);
    }

    /**
     * Foreground true color.
     *
     * @param variant variant
     * @return foreground true color result
     */
    public static NTextStyle foregroundTrueColor(int variant) {
        return of(NTextStyleType.FORE_TRUE_COLOR, variant);
    }

    /**
     * Foreground true color.
     *
     * @param variant variant
     * @return foreground true color result
     */
    public static NTextStyle foregroundTrueColor(NColor variant) {
        return of(NTextStyleType.FORE_TRUE_COLOR, variant == null ? 0 : variant.rgb());
    }

    /**
     * Background color.
     *
     * @param variant variant
     * @return background color result
     */
    public static NTextStyle backgroundColor(int variant) {
        return of(NTextStyleType.BACK_COLOR, variant);
    }

    /**
     * Background true color.
     *
     * @param variant variant
     * @return background true color result
     */
    public static NTextStyle backgroundTrueColor(int variant) {
        return of(NTextStyleType.BACK_TRUE_COLOR, variant);
    }

    /**
     * Background true color.
     *
     * @param variant variant
     * @return background true color result
     */
    public static NTextStyle backgroundTrueColor(NColor variant) {
        return of(NTextStyleType.BACK_TRUE_COLOR, variant == null ? 0 : variant.rgb());
    }

    /**
     * Placeholder.
     *
     * @return placeholder result
     */
    public static NTextStyle placeholder() {
        return of(NTextStyleType.PLACEHOLDER);
    }

    /**
     * Placeholder.
     *
     * @param variant variant
     * @return placeholder result
     */
    public static NTextStyle placeholder(int variant) {
        return of(NTextStyleType.PLACEHOLDER, variant);
    }

    /**
     * Background color.
     *
     * @param variant variant
     * @return background color result
     */
    public static NTextStyle backgroundColor(NColor variant) {
        if (variant == null) {
            return backgroundColor(0);
        }
        switch (variant.bits()) {
            case BITS_4:
            case BITS_8: {
                return secondary(variant.intColor());
            }
            case BITS_16:
            case BITS_24: {
                return backgroundColor(variant.intColor());
            }
            case BITS_32:
            case BITS_64: {
                return backgroundTrueColor(variant.intColor());
            }
        }
        return of(NTextStyleType.BACK_TRUE_COLOR, variant.rgb());
    }

    /**
     * Foreground color.
     *
     * @param variant variant
     * @return foreground color result
     */
    public static NTextStyle foregroundColor(NColor variant) {
        if (variant == null) {
            return foregroundColor(0);
        }
        switch (variant.bits()) {
            case BITS_4:
            case BITS_8: {
                return primary(variant.intColor());
            }
            case BITS_16:
            case BITS_24: {
                return foregroundColor(variant.intColor());
            }
            case BITS_32:
            case BITS_64: {
                return foregroundTrueColor(variant.intColor());
            }
        }
        return of(NTextStyleType.FORE_TRUE_COLOR, variant.rgb());
    }


    /**
     * Parse.
     *
     * @param value value
     * @return parse result
     */
    public static NOptional<NTextStyle> parse(String value) {
        value = NStringUtils.strip(value);
        if (value.isEmpty()) {
            return NOptional.ofEmpty(() -> NMsg.ofC("%s is empty", NTextStyle.class.getSimpleName()));
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
            String svalue = NStringUtils.strip(value);
            if (svalue.startsWith("fx") || svalue.startsWith("bx")) {
                key = svalue.substring(0, 2);
                nbr = svalue.substring(2);
            } else if (svalue.startsWith("foregroundx")) {
                int len = "foregroundx".length();
                key = svalue.substring(0, len);
                nbr = svalue.substring(len);
            } else if (svalue.startsWith("backgroundx")) {
                int len = "backgroundx".length();
                key = svalue.substring(0, len);
                nbr = svalue.substring(len);
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
        nbr = NStringUtils.strip(nbr);
        key = NStringUtils.strip(key);
        if (nbr.isEmpty()) {
            nbr = "0";
        }
        if (key.isEmpty()) {
            key = "p";
        }
        NTextStyleType t = NTextStyleType.parse(key).orNull();
        if (t == null) {
            if (NBlankable.isBlank(key)) {
                return NOptional.ofEmpty(() -> NMsg.ofC("%s is empty", NTextStyle.class.getSimpleName()));
            }
            NOptional<NColor> u = NColor.ofName(key);
            if (u.isPresent()) {
                return NOptional.of(NTextStyle.of(NTextStyleType.FORE_TRUE_COLOR, u.get().intColor()));
            }
            if (NBlankable.isBlank(key)) {
                u = NColor.ofName("fg_" + NStringUtils.strip(key));
                if (u.isPresent()) {
                    return NOptional.of(NTextStyle.of(NTextStyleType.FORE_TRUE_COLOR, u.get().intColor()));
                }
            }
            if (NBlankable.isBlank(key)) {
                u = NColor.ofName("fore_" + NStringUtils.strip(key));
                if (u.isPresent()) {
                    return NOptional.of(NTextStyle.of(NTextStyleType.FORE_TRUE_COLOR, u.get().intColor()));
                }
            }
            if (NBlankable.isBlank(key)) {
                u = NColor.ofName("bg_" + NStringUtils.strip(key));
                if (u.isPresent()) {
                    return NOptional.of(NTextStyle.of(NTextStyleType.BACK_TRUE_COLOR, u.get().intColor()));
                }
            }
            if (NBlankable.isBlank(key)) {
                u = NColor.ofName("back_" + NStringUtils.strip(key));
                if (u.isPresent()) {
                    return NOptional.of(NTextStyle.of(NTextStyleType.BACK_TRUE_COLOR, u.get().intColor()));
                }
            }
            return NOptional.ofError(() -> NMsg.ofC("%s invalid value : %s", NTextStyle.class.getSimpleName(), finalValue));
        }
        switch (t) {
            case FORE_TRUE_COLOR:
            case BACK_TRUE_COLOR: {
                Integer ii = NLiteral.of("0x" + nbr).asInt().orNull();
                if (ii == null) {
                    if (NBlankable.isBlank(key)) {
                        ii = 0;
                    } else {
                        return NOptional.ofError(() -> NMsg.ofC(NTextStyle.class.getSimpleName() + " invalid value : %s", finalValue));
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
                        return NOptional.ofError(() -> NMsg.ofC(NTextStyle.class.getSimpleName() + " invalid value : %s", finalValue));
                    }
                }
                return NOptional.of(NTextStyle.of(t, ii));
            }
        }
    }

    /**
     * Append.
     *
     * @param other other
     * @return append result
     */
    public NTextStyles append(NTextStyle other) {
        return NTextStyles.of(this, other);
    }

    /**
     * Append.
     *
     * @param other other
     * @return append result
     */
    public NTextStyles append(NTextStyles other) {
        return NTextStyles.of(this).append(other);
    }

    /**
     * Type.
     *
     * @return type result
     */
    public NTextStyleType type() {
        return type;
    }

    /**
     * Variant.
     *
     * @return variant result
     */
    public int variant() {
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
