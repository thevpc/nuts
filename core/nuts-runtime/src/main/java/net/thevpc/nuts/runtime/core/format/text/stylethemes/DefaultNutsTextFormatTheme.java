package net.thevpc.nuts.runtime.core.format.text.stylethemes;

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
    public NutsTextNodeStyles toBasicStyles(NutsTextNodeStyles styles) {
        NutsTextNodeStyles ret = NutsTextNodeStyles.NONE;
        if (styles != null) {
            for (NutsTextNodeStyle style : styles) {
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
    public NutsTextNodeStyles toBasicStyles(NutsTextNodeStyle style) {
        if (style == null) {
            return NutsTextNodeStyles.NONE;
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
                return NutsTextNodeStyles.of(style);
            }
            case PRIMARY: {

                return toBasicStyles(NutsTextNodeStyle.foregroundColor(mapColor(style.getVariant())));
            }
            case SECONDARY: {
                return toBasicStyles(NutsTextNodeStyle.backgroundColor(mapColor(style.getVariant())));
            }
            case TITLE: {
                return toBasicStyles(NutsTextNodeStyle.primary(style.getVariant()))
                        .append(NutsTextNodeStyle.underlined());
            }
            case KEYWORD: {
                int x = mod4(style.getVariant());
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(
                        x == 0 ? DARK_BLUE
                                : x == 1 ? DARK_SKY
                                        : x == 2 ? DARK_VIOLET
                                                : BRIGHT_VIOLET
                ));
            }

            case OPTION: {
                int x = mod4(style.getVariant());
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(
                        x == 0 ? DARK_SKY
                                : x == 1 ? 66
                                        : x == 2 ? 102
                                                : 138
                ));
            }

            case ERROR: {
                return NutsTextNodeStyles.of(NutsTextNodeStyle.foregroundColor(DARK_RED));
            }
            case SUCCESS: {
                return NutsTextNodeStyles.of(NutsTextNodeStyle.foregroundColor(DARK_GREEN));
            }
            case WARN: {
                return NutsTextNodeStyles.of(NutsTextNodeStyle.foregroundColor(DARK_YELLOW));
            }
            case INFO: {
                return NutsTextNodeStyles.of(NutsTextNodeStyle.foregroundColor(DARK_SKY));
            }

            case CONFIG: {
                return NutsTextNodeStyles.of(NutsTextNodeStyle.foregroundColor(DARK_VIOLET));
            }
            case DATE:
            case NUMBER:
            case BOOLEAN: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(DARK_VIOLET));
            }

            case STRING: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(DARK_GREEN));
            }

            case COMMENTS: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(DARK_GRAY));
            }

            case SEPARATOR: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(208));
            }

            case OPERATOR: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(208));
            }

            case INPUT: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(BRIGHT_YELLOW));
            }

            case FAIL: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(124));
            }

            case DANGER: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(124));
            }

            case VAR: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(190));
            }

            case PALE: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(250));
            }

            case VERSION: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(220));
            }

            case PATH: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(114));
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
