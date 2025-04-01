package net.thevpc.nuts.runtime.standalone.text.theme;

import net.thevpc.nuts.text.NTextFormatTheme;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.util.NColors;

import java.awt.*;

public class DefaultNTextFormatTheme implements NTextFormatTheme {

    public static final int BLACK = 1;
    public static final int DARK_RED = 1;
    public static final int DARK_GREEN = 2;
    public static final int DARK_YELLOW = 3;
    public static final int DARK_BLUE = 4;
    public static final int DARK_VIOLET = 5;
    public static final int DARK_SKY = 6;
    public static final int LIGHT_GRAY = 7;
    public static final int DARK_GRAY = 8;
    public static final int BRIGHT_RED = 9;
    public static final int BRIGHT_GREEN = 10;
    public static final int BRIGHT_YELLOW = 11;
    public static final int BRIGHT_BLUE = 12;
    public static final int BRIGHT_VIOLET = 13;
    public static final int BRIGHT_SKY = 14;
    public static final int WHITE = 15;

    private static final int[] FG = {BLACK, DARK_BLUE, BRIGHT_BLUE, DARK_SKY, BRIGHT_SKY, DARK_GREEN, BRIGHT_GREEN, DARK_VIOLET, BRIGHT_VIOLET, DARK_YELLOW, BRIGHT_YELLOW, DARK_RED, BRIGHT_RED, DARK_GRAY, LIGHT_GRAY, WHITE};
    private static final int[] BG = {BLACK, DARK_BLUE, BRIGHT_BLUE, DARK_SKY, BRIGHT_SKY, DARK_GREEN, BRIGHT_GREEN, DARK_VIOLET, BRIGHT_VIOLET, DARK_YELLOW, BRIGHT_YELLOW, DARK_RED, BRIGHT_RED, DARK_GRAY, LIGHT_GRAY, WHITE};

    private static int mod16(int x) {
        return x >= 0 ? x % 16 : -x % 16;
    }

    private static int mod4(int x) {
        return x >= 0 ? x % 4 : -x % 4;
    }

    private static int mod2(int x) {
        return x >= 0 ? x % 2 : -x % 2;
    }

    @Override
    public String getName() {
        return "default";
    }

    public static int foregroundSimpleToTrueColor(int color) {
        if (color >= 0 && color < FG.length) {
            color = FG[color];
        }
        if (color < 0) {
            color = -color;
        }
        color = color % 255;
        Color c = NColors.ansiToColor(color);
        int rgb = c.getRGB();
        return rgb;
    }

    public static int backgroundSimpleToTrueColor(int color) {
        if (color >= 0 && color < BG.length) {
            color = BG[color];
        }
        if (color < 0) {
            color = -color;
        }
        color = color % 255;
        Color c = NColors.ansiToColor(color);
        int rgb = c.getRGB();
        return rgb;
    }

    public DefaultNTextFormatTheme() {
    }

    @Override
    public NTextStyles toBasicStyles(NTextStyles styles, boolean basicTrueStyles) {
        NTextStyles ret = NTextStyles.PLAIN;
        if (styles != null) {
            for (NTextStyle style : styles) {
                ret = ret.append(toBasicStyles(style, basicTrueStyles));
            }
        }
        return ret;
    }

    public NTextStyles toBasicStyles(NTextStyle style, boolean basicTrueStyles) {
        if (style == null) {
            return NTextStyles.PLAIN;
        }
        if(style.getType().isBasic(basicTrueStyles)) {
            return NTextStyles.of(style);
        }
        NTextStyles y = toBasicStyles0(style, basicTrueStyles);
        if(basicTrueStyles) {
            for (NTextStyle yy : y) {
                switch (yy.getType()) {
                    case FORE_COLOR:
                    case BACK_COLOR: {
                        System.out.println("why");
                    }
                }
            }
        }
        return y;
    }

    /**
     * this is the default theme!
     *
     * @param style           textNodeStyle
     * @param basicTrueStyles
     * @return NutsTextNode
     */
    public NTextStyles toBasicStyles0(NTextStyle style, boolean basicTrueStyles) {
        if (style == null) {
            return NTextStyles.PLAIN;
        }
        switch (style.getType()) {
            case FORE_COLOR:{
                if(basicTrueStyles){
                    return NTextStyles.of(NTextStyle.foregroundTrueColor(foregroundSimpleToTrueColor(style.getVariant())));
                }
                return NTextStyles.of(style);
            }
            case BACK_COLOR:{
                if(basicTrueStyles){
                    return NTextStyles.of(NTextStyle.backgroundTrueColor(backgroundSimpleToTrueColor(style.getVariant())));
                }
                return NTextStyles.of(style);
            }
            case FORE_TRUE_COLOR:
            case BACK_TRUE_COLOR:
            case UNDERLINED:
            case ITALIC:
            case STRIKED:
            case REVERSED:
            case BOLD:
            case BLINK: {
                return NTextStyles.of(style);
            }
            case PRIMARY: {
                return toBasicStyles(NTextStyle.foregroundColor(mapColor(style.getVariant())), basicTrueStyles);
            }
            case SECONDARY: {
                return toBasicStyles(NTextStyle.backgroundColor(mapColor(style.getVariant())), basicTrueStyles);
            }
            case TITLE: {
                return toBasicStyles(NTextStyle.primary(style.getVariant()), basicTrueStyles)
                        .append(NTextStyle.underlined());
            }
            case KEYWORD: {
                int x = mod4(style.getVariant());
                return toBasicStyles(NTextStyle.foregroundColor(
                        x == 0 ? DARK_BLUE
                                : x == 1 ? DARK_SKY
                                : x == 2 ? DARK_VIOLET
                                : BRIGHT_VIOLET
                ), basicTrueStyles);
            }

            case OPTION: {
                int x = mod4(style.getVariant());
                return toBasicStyles(NTextStyle.foregroundColor(
                        x == 0 ? DARK_SKY
                                : x == 1 ? 66
                                : x == 2 ? 102
                                : 138
                ), basicTrueStyles);
            }

            case ERROR: {
                return toBasicStyles(NTextStyle.foregroundColor(DARK_RED),basicTrueStyles);
            }
            case SUCCESS: {
                return toBasicStyles(NTextStyle.foregroundColor(DARK_GREEN),basicTrueStyles);
            }
            case WARN: {
                return toBasicStyles(NTextStyle.foregroundColor(DARK_YELLOW),basicTrueStyles);
            }
            case INFO: {
                return toBasicStyles(NTextStyle.foregroundColor(DARK_SKY),basicTrueStyles);
            }

            case CONFIG: {
                return toBasicStyles(NTextStyle.foregroundColor(DARK_VIOLET),basicTrueStyles);
            }
            case DATE:
            case NUMBER:
            case BOOLEAN: {
                return toBasicStyles(NTextStyle.foregroundColor(DARK_VIOLET), basicTrueStyles);
            }

            case STRING: {
                return toBasicStyles(NTextStyle.foregroundColor(DARK_GREEN), basicTrueStyles);
            }

            case COMMENTS: {
                return toBasicStyles(NTextStyle.foregroundColor(DARK_GRAY), basicTrueStyles);
            }

            case SEPARATOR: {
                return toBasicStyles(NTextStyle.foregroundColor(208), basicTrueStyles);
            }

            case OPERATOR: {
                return toBasicStyles(NTextStyle.foregroundColor(208), basicTrueStyles);
            }

            case INPUT: {
                return toBasicStyles(NTextStyle.foregroundColor(BRIGHT_YELLOW), basicTrueStyles);
            }

            case FAIL: {
                return toBasicStyles(NTextStyle.foregroundColor(124), basicTrueStyles);
            }

            case DANGER: {
                return toBasicStyles(NTextStyle.foregroundColor(124), basicTrueStyles);
            }

            case VAR: {
                return toBasicStyles(NTextStyle.foregroundColor(190), basicTrueStyles);
            }

            case PALE: {
                return toBasicStyles(NTextStyle.foregroundColor(250), basicTrueStyles);
            }

            case VERSION: {
                return toBasicStyles(NTextStyle.foregroundColor(220), basicTrueStyles);
            }

            case PATH: {
                return toBasicStyles(NTextStyle.foregroundColor(114), basicTrueStyles);
            }
        }
        throw new IllegalArgumentException("invalid text node style " + style);
    }

    private int mapColor(int v) {
        if (v == 0) {
            v = 1; //ignore 0
        }
        if (v < 0) {
            v = -v;
        }
        if (v < 16) {
            return FG[v];
        }
//        int z1=(v-16)/32;
//        int z2=(v-16)%32;
//        int z3=z2*32+z1+16;
//        return z3;
        return v;
    }

}
