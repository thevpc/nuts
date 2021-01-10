package net.thevpc.nuts.runtime.core.format.text.stylethemes;

import net.thevpc.nuts.*;

public class DefaultNutsTextStyleTheme implements NutsTextStyleTheme {
    public static final NutsTextStyleTheme DEFAULT=new DefaultNutsTextStyleTheme();

    public static final int BLACK=1;
    public static final int DARK_RED=1;
    public static final int DARK_GREEN=2;
    public static final int DARK_YELLOW=3;
    public static final int DARK_BLUE=4;
    public static final int DARK_VIOLET=5;
    public static final int DARK_SKY=6;
    public static final int LIGHT_GRAY=7;
    public static final int DARK_GRAY=8;
    public static final int BRIGHT_RED=9;
    public static final int BRIGHT_GREEN=10;
    public static final int BRIGHT_YELLOW=11;
    public static final int BRIGHT_BLUE=12;
    public static final int BRIGHT_VIOLET=13;
    public static final int BRIGHT_SKY=14;
    public static final int WHITE=15;

    private static final int[] FG={BLACK,DARK_BLUE,BRIGHT_BLUE,DARK_SKY,BRIGHT_SKY,DARK_GREEN,BRIGHT_GREEN,DARK_VIOLET,BRIGHT_VIOLET,DARK_YELLOW,BRIGHT_YELLOW,DARK_RED,BRIGHT_RED,DARK_GRAY,LIGHT_GRAY,WHITE};
    private static final int[] BG={BLACK,DARK_BLUE,BRIGHT_BLUE,DARK_SKY,BRIGHT_SKY,DARK_GREEN,BRIGHT_GREEN,DARK_VIOLET,BRIGHT_VIOLET,DARK_YELLOW,BRIGHT_YELLOW,DARK_RED,BRIGHT_RED,DARK_GRAY,LIGHT_GRAY,WHITE};
    private static int mod16(int x){
        return x>=0 ? x%16:-x%16;
    }
    private static int mod4(int x){
        return x>=0 ? x%4:-x%4;
    }
    private static int mod2(int x){
        return x>=0 ? x%2:-x%2;
    }
    /**
     * this is the default theme!
     * @param textNodeStyle textNodeStyle
     * @return NutsTextNode
     */
    @Override
    public NutsTextNodeStyle[] toBasicStyles(NutsTextNodeStyle textNodeStyle, NutsWorkspace workspace) {
        if (textNodeStyle == null) {
            return new NutsTextNodeStyle[0];
        }
        switch (textNodeStyle.getType()) {
            case FORE_COLOR: //will be called by recursion
            case BACK_COLOR:
            case FORE_TRUE_COLOR:
            case BACK_TRUE_COLOR:
            case UNDERLINED:
            case ITALIC:
            case STRIKED:
            case REVERSED:
            case BOLD:
            case BLINK:{
                return new NutsTextNodeStyle[]{textNodeStyle};
            }
            case PRIMARY:{

                return toBasicStyles(NutsTextNodeStyle.foregroundColor(mapColor(textNodeStyle.getVariant())), workspace);
            }
            case SECONDARY:{
                return toBasicStyles(NutsTextNodeStyle.backgroundColor(mapColor(textNodeStyle.getVariant())), workspace);
            }
            case TITLE:
            {
                return new NutsTextNodeStyle[]{
                        toBasicStyles(NutsTextNodeStyle.primary(textNodeStyle.getVariant()),workspace)[0],
                        NutsTextNodeStyle.underlined()
                };
            }
            case KEYWORD:{
                int x = mod4(textNodeStyle.getVariant());
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(
                        x==0?DARK_BLUE
                        :x==1?DARK_SKY
                        :x==2?DARK_VIOLET
                        :BRIGHT_VIOLET
                        ), workspace);
            }

            case OPTION:{
                int x = mod4(textNodeStyle.getVariant());
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(
                        x==0?DARK_SKY
                                :x==1?66
                                :x==2?102
                                :138
                ), workspace);
            }

            case ERROR:{
                return new NutsTextNodeStyle[]{NutsTextNodeStyle.foregroundColor(DARK_RED)};
            }
            case SUCCESS:{
                return new NutsTextNodeStyle[]{NutsTextNodeStyle.foregroundColor(DARK_GREEN)};
            }
            case WARN:{
                return new NutsTextNodeStyle[]{NutsTextNodeStyle.foregroundColor(DARK_YELLOW)};
            }
            case INFO:{
                return new NutsTextNodeStyle[]{NutsTextNodeStyle.foregroundColor(DARK_SKY)};
            }

            case CONFIG:{
                return new NutsTextNodeStyle[]{NutsTextNodeStyle.foregroundColor(DARK_VIOLET)};
            }
            case NUMBER:
            case BOOLEAN:{
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(DARK_VIOLET), workspace);
            }

            case STRING: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(DARK_GREEN), workspace);
            }

            case COMMENTS:{
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(DARK_GRAY), workspace);
            }

            case SEPARATOR:{
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(208), workspace);
            }

            case OPERATOR: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(208), workspace);
            }

            case USER_INPUT: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(BRIGHT_YELLOW), workspace);
            }

            case FAIL: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(124), workspace);
            }

            case DANGER: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(124), workspace);
            }

            case VAR: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(190), workspace);
            }

            case PALE: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(250), workspace);
            }

            case VERSION: {
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(220), workspace);
            }

            case PATH:{
                return toBasicStyles(NutsTextNodeStyle.foregroundColor(114), workspace);
            }
        }
        throw new IllegalArgumentException("invalid text node style " + textNodeStyle);
    }

    private int mapColor(int v) {
        if(v==0){
            v=1; //ignore 0
        }
        if(v<0){
            v=-v;
        }
        if(v<16){
            return FG[v];
        }
//        int z1=(v-16)/32;
//        int z2=(v-16)%32;
//        int z3=z2*32+z1+16;
//        return z3;
        return v;
    }

}
