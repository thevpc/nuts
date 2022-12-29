package net.thevpc.nuts.runtime.standalone.text.theme;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NTextFormatTheme;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyles;

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
    private NWorkspace ws;

    public DefaultNTextFormatTheme(NWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NTextStyles toBasicStyles(NTextStyles styles, NSession session) {
        NTextStyles ret = NTextStyles.PLAIN;
        if (styles != null) {
            for (NTextStyle style : styles) {
                ret = ret.append(toBasicStyles(style));
            }
        }
        return ret;
    }

    /**
     * this is the default theme!
     *
     * @param style textNodeStyle
     * @return NutsTextNode
     */
    public NTextStyles toBasicStyles(NTextStyle style) {
        if (style == null) {
            return NTextStyles.PLAIN;
        }
        switch (style.getType()) {
            case FORE_COLOR: //will be called by recursion
            case BACK_COLOR:
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

                return toBasicStyles(NTextStyle.foregroundColor(mapColor(style.getVariant())));
            }
            case SECONDARY: {
                return toBasicStyles(NTextStyle.backgroundColor(mapColor(style.getVariant())));
            }
            case TITLE: {
                return toBasicStyles(NTextStyle.primary(style.getVariant()))
                        .append(NTextStyle.underlined());
            }
            case KEYWORD: {
                int x = mod4(style.getVariant());
                return toBasicStyles(NTextStyle.foregroundColor(
                        x == 0 ? DARK_BLUE
                                : x == 1 ? DARK_SKY
                                        : x == 2 ? DARK_VIOLET
                                                : BRIGHT_VIOLET
                ));
            }

            case OPTION: {
                int x = mod4(style.getVariant());
                return toBasicStyles(NTextStyle.foregroundColor(
                        x == 0 ? DARK_SKY
                                : x == 1 ? 66
                                        : x == 2 ? 102
                                                : 138
                ));
            }

            case ERROR: {
                return NTextStyles.of(NTextStyle.foregroundColor(DARK_RED));
            }
            case SUCCESS: {
                return NTextStyles.of(NTextStyle.foregroundColor(DARK_GREEN));
            }
            case WARN: {
                return NTextStyles.of(NTextStyle.foregroundColor(DARK_YELLOW));
            }
            case INFO: {
                return NTextStyles.of(NTextStyle.foregroundColor(DARK_SKY));
            }

            case CONFIG: {
                return NTextStyles.of(NTextStyle.foregroundColor(DARK_VIOLET));
            }
            case DATE:
            case NUMBER:
            case BOOLEAN: {
                return toBasicStyles(NTextStyle.foregroundColor(DARK_VIOLET));
            }

            case STRING: {
                return toBasicStyles(NTextStyle.foregroundColor(DARK_GREEN));
            }

            case COMMENTS: {
                return toBasicStyles(NTextStyle.foregroundColor(DARK_GRAY));
            }

            case SEPARATOR: {
                return toBasicStyles(NTextStyle.foregroundColor(208));
            }

            case OPERATOR: {
                return toBasicStyles(NTextStyle.foregroundColor(208));
            }

            case INPUT: {
                return toBasicStyles(NTextStyle.foregroundColor(BRIGHT_YELLOW));
            }

            case FAIL: {
                return toBasicStyles(NTextStyle.foregroundColor(124));
            }

            case DANGER: {
                return toBasicStyles(NTextStyle.foregroundColor(124));
            }

            case VAR: {
                return toBasicStyles(NTextStyle.foregroundColor(190));
            }

            case PALE: {
                return toBasicStyles(NTextStyle.foregroundColor(250));
            }

            case VERSION: {
                return toBasicStyles(NTextStyle.foregroundColor(220));
            }

            case PATH: {
                return toBasicStyles(NTextStyle.foregroundColor(114));
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
