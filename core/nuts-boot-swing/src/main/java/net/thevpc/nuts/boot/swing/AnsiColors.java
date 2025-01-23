package net.thevpc.nuts.boot.swing;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnsiColors {
    public Color D_Black = Color.getHSBColor(0.000f, 0.000f, 0.000f);
    public Color D_Red = Color.getHSBColor(0.000f, 1.000f, 0.502f);
    public Color D_Blue = Color.getHSBColor(0.667f, 1.000f, 0.502f);
    public Color D_Magenta = Color.getHSBColor(0.833f, 1.000f, 0.502f);
    public Color D_Green = Color.getHSBColor(0.333f, 1.000f, 0.502f);
    public Color D_Yellow = Color.getHSBColor(0.167f, 1.000f, 0.502f);
    public Color D_Cyan = Color.getHSBColor(0.500f, 1.000f, 0.502f);
    public Color D_White = Color.getHSBColor(0.000f, 0.000f, 0.753f);
    public Color B_Black = Color.getHSBColor(0.000f, 0.000f, 0.502f);
    public Color B_Red = Color.getHSBColor(0.000f, 1.000f, 1.000f);
    public Color B_Blue = Color.getHSBColor(0.667f, 1.000f, 1.000f);
    public Color B_Magenta = Color.getHSBColor(0.833f, 1.000f, 1.000f);
    public Color B_Green = Color.getHSBColor(0.333f, 1.000f, 1.000f);
    public Color B_Yellow = Color.getHSBColor(0.167f, 1.000f, 1.000f).darker();
    public Color B_Cyan = Color.getHSBColor(0.500f, 1.000f, 1.000f);
    public Color B_White = Color.getHSBColor(0.000f, 0.000f, 1.000f);
    public Color cResetForeground = Color.BLACK;//Color.getHSBColor(0.000f, 0.000f, 1.000f);
    public Color cResetBackground = Color.WHITE;//Color.getHSBColor(0.000f, 0.000f, 1.000f);
    public Color[] COLS = new Color[]{
            D_Black, D_Red, D_Green, D_Yellow, D_Blue, D_Magenta, D_Cyan, D_White,
            B_Black, B_Red, B_Green, B_Yellow, B_Blue, B_Magenta, B_Cyan, B_White,
    };
    public Color preferredBackground = Color.WHITE;//Color.getHSBColor(0.000f, 0.000f, 1.000f);
    public boolean darkMode=false;//Color.getHSBColor(0.000f, 0.000f, 1.000f);

    public String colorName(Color c) {
        if (c.equals(cResetForeground)) {
            return "reset";
        }
        if (c.equals(D_Black)) {
            return "D_Black";
        }
        if (c.equals(D_Red)) {
            return "D_Red";
        }
        if (c.equals(D_Green)) {
            return "D_Green";
        }
        if (c.equals(D_Yellow)) {
            return "D_Yellow";
        }
        if (c.equals(D_Blue)) {
            return "D_Blue";
        }
        if (c.equals(D_Magenta)) {
            return "D_Magenta";
        }
        if (c.equals(D_Cyan)) {
            return "D_Cyan";
        }
        if (c.equals(D_White)) {
            return "D_White";
        }
        if (c.equals(B_Black)) {
            return "B_Black";
        }
        if (c.equals(B_Red)) {
            return "B_Red";
        }
        if (c.equals(B_Green)) {
            return "B_Green";
        }
        if (c.equals(B_Yellow)) {
            return "B_Yellow";
        }
        if (c.equals(B_Blue)) {
            return "B_Blue";
        }
        if (c.equals(B_Magenta)) {
            return "B_Magenta";
        }
        if (c.equals(B_Cyan)) {
            return "B_Cyan";
        }
        if (c.equals(B_White)) {
            return "B_White";
        }
        return "?";
    }

    public void setDarkMode(boolean darkMode) {
        cResetForeground = darkMode ? Color.WHITE : Color.BLACK;
        if (darkMode) {
            D_Blue = new Color(124, 124, 220);
            B_Blue = new Color(162, 162, 225);
            B_White = new Color(255, 255, 255);
            D_Red = new Color(200, 0, 0);
            D_Magenta = new Color(142, 57, 137);
            preferredBackground=(new Color(22, 22, 22));
        } else {
            D_Blue = Color.getHSBColor(0.667f, 1.000f, 0.502f);
            B_Blue = Color.getHSBColor(0.667f, 1.000f, 1.000f);
            B_White = new Color(0, 0, 0);
            D_Red = Color.getHSBColor(0.000f, 1.000f, 0.502f);
            D_Magenta = Color.getHSBColor(0.833f, 1.000f, 0.502f);
            preferredBackground=(new Color(250, 250, 250));
        }
        COLS = new Color[]{
                D_Black, D_Red, D_Green, D_Yellow, D_Blue, D_Magenta, D_Cyan, D_White,
                B_Black, B_Red, B_Green, B_Yellow, B_Blue, B_Magenta, B_Cyan, B_White,
        };
    }

    public Color rgb(int r, int g, int b) {
        int rr = valid255(r);
        int gg = valid255(g);
        int bb = valid255(b);
        return new Color(rr,gg,bb);
    }
    public TextStyle restStyle() {
        return new TextStyle().setForeColor(cResetForeground).setBackColor(cResetBackground);
    }
    public TextStyle applyANSIColor(String ANSIColor,TextStyle currentStyle) {
        Pattern p = Pattern.compile("\u001B\\[(?<a>\\d+)(;(?<b>\\d+)(;(?<c>\\d+)(;(?<d>\\d+)(;(?<e>\\d+))?)?)?)?m");
        Matcher m = p.matcher(ANSIColor);
        if (m.find()) {
            int a = Integer.parseInt(m.group("a"));
            int b = m.group("b") == null ? -1 : Integer.parseInt(m.group("b"));
            int c = m.group("c") == null ? -1 : Integer.parseInt(m.group("c"));
            int d = m.group("d") == null ? -1 : Integer.parseInt(m.group("d"));
            int e = m.group("e") == null ? -1 : Integer.parseInt(m.group("e"));
            switch (a) {
                case 0: {
//                    currentStyle=currentStyle.copy().setForeColor(color256(0));
                    currentStyle = restStyle();// new Style().setForeColor(color256(0));
                    break;
                }
                case 1: {
                    currentStyle = currentStyle.copy().setBold(true);
                    break;
                }
                case 3: {
                    currentStyle = currentStyle.copy().setItalic(true);
                    break;
                }
                case 4: {
                    currentStyle = currentStyle.copy().setUnderline(true);
                    break;
                }
                case 9: {
                    currentStyle = currentStyle.copy().setStrikeThrough(true);
                    break;
                }
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37: {
                    currentStyle = currentStyle.copy().setForeColor(color256(a - 30));
                    break;
                }
                case 38: {
                    switch (b) {
                        case 5: {
                            currentStyle = currentStyle.copy().setForeColor(color256(c));
                            break;
                        }
                        case 2: {
                            currentStyle = currentStyle.copy().setForeColor(rgb(c, d, e));
                            break;
                        }
                    }
                    break;
                }
                case 48: {
                    switch (b) {
                        case 5: {
                            if (c == 0) {
                                currentStyle = currentStyle.copy().setBackColor(null);
                            } else {
                                currentStyle = currentStyle.copy().setBackColor(color256(c));
                            }
                            break;
                        }
                        case 2: {
                            currentStyle = currentStyle.copy().setBackColor(rgb(c,d,e));
                            break;
                        }
                    }
                }
            }
            return currentStyle;
        }

        switch (ANSIColor) {
            default: {
                //colorCurrent = cReset;
                break;
            }
        }
        return currentStyle;
    }
    private int valid255(int c) {
        if (c < 0) {
            return 0;
        }
        if (c > 255) {
            return 255;
        }
        return c;
    }

    public Color color256(int c) {
        if (c < 0) {
            c = 0;
        }
        if (c < 16) {
            c = Math.abs(c) % COLS.length;
            if (c == 0) {
                return cResetForeground;
            } else {
                return COLS[c];
            }
        }
        if (c <= 231) {
            c = c - 16;
            int r = (c / 36);
            int g = ((c % 36) / 6);
            int b = (c % 6);
            // 36 * r + 6 * g + b (0 ≤ r, g, b ≤ 5)
            r = r * 255 / 5;
            g = g * 255 / 5;
            b = b * 255 / 5;
            return new Color(r, g, b);
        } else {
            int i = c - 232;
            int r = 255 * i / 24;
            return new Color(r, r, r);
        }
    }
}
