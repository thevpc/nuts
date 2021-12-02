package net.thevpc.nuts.runtime.standalone.text.theme;

import net.thevpc.nuts.*;

public class DefaultNutsTextFormatTheme implements NutsTextFormatTheme {

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
    private NutsWorkspace ws;

    public DefaultNutsTextFormatTheme(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsTextStyles toBasicStyles(NutsTextStyles styles, NutsSession session) {
        NutsTextStyles ret = NutsTextStyles.PLAIN;
        if (styles != null) {
            for (NutsTextStyle style : styles) {
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
    public NutsTextStyles toBasicStyles(NutsTextStyle style) {
        if (style == null) {
            return NutsTextStyles.PLAIN;
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
                return NutsTextStyles.of(style);
            }
            case PRIMARY: {

                return toBasicStyles(NutsTextStyle.foregroundColor(mapColor(style.getVariant())));
            }
            case SECONDARY: {
                return toBasicStyles(NutsTextStyle.backgroundColor(mapColor(style.getVariant())));
            }
            case TITLE: {
                return toBasicStyles(NutsTextStyle.primary(style.getVariant()))
                        .append(NutsTextStyle.underlined());
            }
            case KEYWORD: {
                int x = mod4(style.getVariant());
                return toBasicStyles(NutsTextStyle.foregroundColor(
                        x == 0 ? DARK_BLUE
                                : x == 1 ? DARK_SKY
                                        : x == 2 ? DARK_VIOLET
                                                : BRIGHT_VIOLET
                ));
            }

            case OPTION: {
                int x = mod4(style.getVariant());
                return toBasicStyles(NutsTextStyle.foregroundColor(
                        x == 0 ? DARK_SKY
                                : x == 1 ? 66
                                        : x == 2 ? 102
                                                : 138
                ));
            }

            case ERROR: {
                return NutsTextStyles.of(NutsTextStyle.foregroundColor(DARK_RED));
            }
            case SUCCESS: {
                return NutsTextStyles.of(NutsTextStyle.foregroundColor(DARK_GREEN));
            }
            case WARN: {
                return NutsTextStyles.of(NutsTextStyle.foregroundColor(DARK_YELLOW));
            }
            case INFO: {
                return NutsTextStyles.of(NutsTextStyle.foregroundColor(DARK_SKY));
            }

            case CONFIG: {
                return NutsTextStyles.of(NutsTextStyle.foregroundColor(DARK_VIOLET));
            }
            case DATE:
            case NUMBER:
            case BOOLEAN: {
                return toBasicStyles(NutsTextStyle.foregroundColor(DARK_VIOLET));
            }

            case STRING: {
                return toBasicStyles(NutsTextStyle.foregroundColor(DARK_GREEN));
            }

            case COMMENTS: {
                return toBasicStyles(NutsTextStyle.foregroundColor(DARK_GRAY));
            }

            case SEPARATOR: {
                return toBasicStyles(NutsTextStyle.foregroundColor(208));
            }

            case OPERATOR: {
                return toBasicStyles(NutsTextStyle.foregroundColor(208));
            }

            case INPUT: {
                return toBasicStyles(NutsTextStyle.foregroundColor(BRIGHT_YELLOW));
            }

            case FAIL: {
                return toBasicStyles(NutsTextStyle.foregroundColor(124));
            }

            case DANGER: {
                return toBasicStyles(NutsTextStyle.foregroundColor(124));
            }

            case VAR: {
                return toBasicStyles(NutsTextStyle.foregroundColor(190));
            }

            case PALE: {
                return toBasicStyles(NutsTextStyle.foregroundColor(250));
            }

            case VERSION: {
                return toBasicStyles(NutsTextStyle.foregroundColor(220));
            }

            case PATH: {
                return toBasicStyles(NutsTextStyle.foregroundColor(114));
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
