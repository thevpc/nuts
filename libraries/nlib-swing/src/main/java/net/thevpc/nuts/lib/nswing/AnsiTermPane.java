package net.thevpc.nuts.lib.nswing;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * thanks to https://stackoverflow.com/questions/6913983/jtextpane-removing-first-line
 */
public class AnsiTermPane extends JTextPane {
    //    public static Color colorForeground = Color.BLACK;//cReset;
//    public static Color colorBackground = null;//cReset;
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
    //    public static Color colorCurrent = Color.WHITE;//cReset;
    String remaining = "";
    PrintStream ps;
    Style currentStyle = new Style()
            .setForeColor(Color.BLACK);

    public AnsiTermPane(boolean darkMode) {
        setDarkMode(darkMode);
    }

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

    public Style restStyle() {
        return new Style().setForeColor(cResetForeground).setBackColor(cResetBackground);
    }

    public void resetCurr() {
        currentStyle = restStyle();
    }

    public void setDarkMode(boolean darkMode) {
        cResetForeground = darkMode ? Color.WHITE : Color.BLACK;
        cResetBackground = getBackground();
//        setForeground(Color.WHITE);
        setFont(new Font("Courier New", Font.PLAIN, 14));
        setForeground(cResetForeground);
        resetCurr();
        if (darkMode) {
            D_Blue = new Color(124, 124, 220);
            B_Blue = new Color(162, 162, 225);
            B_White = new Color(255, 255, 255);
            D_Red = new Color(200, 0, 0);
            D_Magenta = new Color(142, 57, 137);
            setBackground(new Color(22, 22, 22));
        } else {
            D_Blue = Color.getHSBColor(0.667f, 1.000f, 0.502f);
            B_Blue = Color.getHSBColor(0.667f, 1.000f, 1.000f);
            B_White = new Color(0, 0, 0);
            D_Red = Color.getHSBColor(0.000f, 1.000f, 0.502f);
            D_Magenta = Color.getHSBColor(0.833f, 1.000f, 0.502f);
            setBackground(new Color(250, 250, 250));
        }
        COLS = new Color[]{
                D_Black, D_Red, D_Green, D_Yellow, D_Blue, D_Magenta, D_Cyan, D_White,
                B_Black, B_Red, B_Green, B_Yellow, B_Blue, B_Magenta, B_Cyan, B_White,
        };
    }

    public void clearLastLine() {
        Element root = getDocument().getDefaultRootElement();
        if (root.getElementCount() > 0) {
            Element first = root.getElement(root.getElementCount() - 1);
            try {
                getDocument().remove(first.getStartOffset(), first.getEndOffset());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
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


    public void append(int c, String s) {
        append(currentStyle.copy().setForeColor(color256(c)), s);
    }

    public void append(Style c, String s) {
//        System.out.println(">>"+colorName(c)+" : "+s);
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = SimpleAttributeSet.EMPTY;
        aset = sc.addAttribute(aset, StyleConstants.Foreground, c.foreColor);
        if (c.backColor != null) {
            aset = sc.addAttribute(aset, StyleConstants.Background, c.backColor);
        }
        aset = sc.addAttribute(aset, StyleConstants.Underline, c.underline);
        aset = sc.addAttribute(aset, StyleConstants.Bold, c.bold);
        aset = sc.addAttribute(aset, StyleConstants.Italic, c.italic);
        aset = sc.addAttribute(aset, StyleConstants.StrikeThrough, c.strikeThrough);
        int len = getDocument().getLength(); // same value as getText().length();
        boolean editable = isEditable();
        setEditable(true);
        setCaretPosition(len);  // place caret at the end (with no selection)
        setCharacterAttributes(aset, false);
        replaceSelection(s);
        System.out.print(s);// there is no selection, so inserts at caret
        setEditable(editable);
    }

    private static class Style implements Cloneable {
        Color foreColor;
        Color backColor;
        boolean underline;
        boolean bold;
        boolean strikeThrough;
        boolean italic;

        public Color getForeColor() {
            return foreColor;
        }

        public Style setForeColor(Color foreColor) {
            this.foreColor = foreColor;
            return this;
        }

        public Color getBackColor() {
            return backColor;
        }

        public Style setBackColor(Color backColor) {
            this.backColor = backColor;
            return this;
        }

        public boolean isUnderline() {
            return underline;
        }

        public Style setUnderline(boolean underline) {
            this.underline = underline;
            return this;
        }

        public boolean isBold() {
            return bold;
        }

        public Style setBold(boolean bold) {
            this.bold = bold;
            return this;
        }

        public boolean isStrikeThrough() {
            return strikeThrough;
        }

        public Style setStrikeThrough(boolean strikeThrough) {
            this.strikeThrough = strikeThrough;
            return this;
        }

        public boolean isItalic() {
            return italic;
        }

        public Style setItalic(boolean italic) {
            this.italic = italic;
            return this;
        }

        public Style copy() {
            try {
                return (Style) clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public PrintStream asPrintStream() {
        if (ps == null) {
            ps = new PrintStream(
                    new OutputStream() {
                        @Override
                        public void write(int b) throws IOException {
                            UI.withinGUI(() -> {
                                appendANSI(String.valueOf((char) b));
                            });
                        }

                        @Override
                        public void write(byte[] b, int off, int len) throws IOException {
                            UI.withinGUI(() -> {
                                appendANSI(new String(b, off, len));
                            });
                        }
                    }
            );
        }
        return ps;
    }

    public void printlnAnsi(String s) {
        appendANSI(s);
        appendANSI("\n");
    }

    public int endOfEscape(String s, int pos) {
        for (int i = pos; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\u001B' || (c >= '0' && c <= '9') || c == ';' || c == '[') {
                continue;
            } else {
                return i;
            }
        }
        return -1;
    }

    public void appendANSI(String s) { // convert ANSI color codes first
        int aPos = 0;   // current char position in addString
        int aIndex = 0; // index of next Escape sequence
        int mIndex = 0; // index of "m" terminating Escape sequence
        String tmpString = "";
        boolean stillSearching = true; // true until no more Escape sequences
        String addString = remaining + s;
        remaining = "";

        if (addString.length() > 0) {
            aIndex = addString.indexOf("\u001B"); // find first escape
            if (aIndex == -1) { // no escape/color change in this string, so just send it with current color
                append(currentStyle, addString);
                return;
            }
// otherwise There is an escape character in the string, so we must process it

            if (aIndex > 0) { // Escape is not first char, so send text up to first escape
                tmpString = addString.substring(0, aIndex);
                append(currentStyle, tmpString);
                aPos = aIndex;
            }
// aPos is now at the beginning of the first escape sequence

            stillSearching = true;
            while (stillSearching) {
                mIndex = endOfEscape(addString, aPos); // find the end of the escape sequence
                if (mIndex < 0) { // the buffer ends halfway through the ansi string!
                    remaining = addString.substring(aPos);
                    stillSearching = false;
                    continue;
                } else {
                    tmpString = addString.substring(aPos, mIndex + 1);
                    applyANSIColor(tmpString);
                }
                aPos = mIndex + 1;
// now we have the color, send text that is in that color (up to next escape)

                aIndex = addString.indexOf("\u001B", aPos);

                if (aIndex == -1) { // if that was the last sequence of the input, send remaining text
                    tmpString = addString.substring(aPos);
                    append(currentStyle, tmpString);
                    stillSearching = false;
                    continue; // jump out of loop early, as the whole string has been sent now
                }

                // there is another escape sequence, so send part of the string and prepare for the next
                tmpString = addString.substring(aPos, aIndex);
                aPos = aIndex;
                append(currentStyle, tmpString);

            } // while there's text in the input buffer
        }
    }

    public void applyANSIColor(String ANSIColor) {
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
                            int rr = valid255(c);
                            int gg = valid255(d);
                            int bb = valid255(e);
                            currentStyle = currentStyle.copy().setForeColor(new Color(rr, gg, bb));
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
                            int rr = valid255(c);
                            int gg = valid255(d);
                            int bb = valid255(e);
                            currentStyle = currentStyle.copy().setBackColor(new Color(rr, gg, bb));
                            break;
                        }
                    }
                }
            }
            return;
        }
        switch (ANSIColor) {
            default: {
                //colorCurrent = cReset;
                break;
            }
        }
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

    public void clearScreen() {
        setText("");
    }
}
