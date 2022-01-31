package net.thevpc.nuts.installer.util;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * thanks to https://stackoverflow.com/questions/6913983/jtextpane-removing-first-line
 */
public class AnsiTermPane extends JTextPane {
    public static final Color D_Black = Color.getHSBColor(0.000f, 0.000f, 0.000f);
    public static final Color D_Red = Color.getHSBColor(0.000f, 1.000f, 0.502f);
    public static final Color D_Blue = Color.getHSBColor(0.667f, 1.000f, 0.502f);
    public static final Color D_Magenta = Color.getHSBColor(0.833f, 1.000f, 0.502f);
    public static final Color D_Green = Color.getHSBColor(0.333f, 1.000f, 0.502f);
    public static final Color D_Yellow = Color.getHSBColor(0.167f, 1.000f, 0.502f);
    public static final Color D_Cyan = Color.getHSBColor(0.500f, 1.000f, 0.502f);
    public static final Color D_White = Color.getHSBColor(0.000f, 0.000f, 0.753f);
    public static final Color B_Black = Color.getHSBColor(0.000f, 0.000f, 0.502f);
    public static final Color B_Red = Color.getHSBColor(0.000f, 1.000f, 1.000f);
    public static final Color B_Blue = Color.getHSBColor(0.667f, 1.000f, 1.000f);
    public static final Color B_Magenta = Color.getHSBColor(0.833f, 1.000f, 1.000f);
    public static final Color B_Green = Color.getHSBColor(0.333f, 1.000f, 1.000f);
    public static final Color B_Yellow = Color.getHSBColor(0.167f, 1.000f, 1.000f);
    public static final Color B_Cyan = Color.getHSBColor(0.500f, 1.000f, 1.000f);
    public static final Color B_White = Color.getHSBColor(0.000f, 0.000f, 1.000f);
    public static final Color cReset = Color.BLACK;//Color.getHSBColor(0.000f, 0.000f, 1.000f);
    public static Color[] COLS = new Color[]{
            D_Black,D_Red,D_Green,D_Yellow,D_Blue,D_Magenta,D_Cyan,D_White,
            B_Black,B_Red,B_Green,B_Yellow,B_Blue,B_Magenta,B_Cyan,B_White,
    };
    public static Color colorCurrent = Color.BLACK;//cReset;
//    public static Color colorCurrent = Color.WHITE;//cReset;
    String remaining = "";

    public AnsiTermPane() {
//        setBackground(Color.BLACK);
//        setForeground(Color.WHITE);
        setFont(new Font("Courier New", Font.PLAIN, 14));
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

    public void append(Color c, String s) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        int len = getDocument().getLength(); // same value as getText().length();
        setCaretPosition(len);  // place caret at the end (with no selection)
        setCharacterAttributes(aset, false);
        replaceSelection(s); // there is no selection, so inserts at caret
    }

    public void printlnAnsi(String s) {
        appendANSI(s);
        appendANSI("\n");
    }

    public int endOfEscape(String s,int pos) {
        for (int i = pos; i < s.length(); i++) {
            char c=s.charAt(i);
            if( c=='\u001B' || (c>='0' && c<='9') || c==';' || c=='['){
                continue;
            }else{
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
                append(colorCurrent, addString);
                return;
            }
// otherwise There is an escape character in the string, so we must process it

            if (aIndex > 0) { // Escape is not first char, so send text up to first escape
                tmpString = addString.substring(0, aIndex);
                append(colorCurrent, tmpString);
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
                    getANSIColor(tmpString);
                }
                aPos = mIndex + 1;
// now we have the color, send text that is in that color (up to next escape)

                aIndex = addString.indexOf("\u001B", aPos);

                if (aIndex == -1) { // if that was the last sequence of the input, send remaining text
                    tmpString = addString.substring(aPos);
                    append(colorCurrent, tmpString);
                    stillSearching = false;
                    continue; // jump out of loop early, as the whole string has been sent now
                }

                // there is another escape sequence, so send part of the string and prepare for the next
                tmpString = addString.substring(aPos, aIndex);
                aPos = aIndex;
                append(colorCurrent, tmpString);

            } // while there's text in the input buffer
        }
    }

    public void getANSIColor(String ANSIColor) {
        Pattern p = Pattern.compile("\u001B\\[(?<a>\\d+)(;(?<b>\\d+)(;(?<c>\\d+))?)?m");
        Matcher m = p.matcher(ANSIColor);
        if(m.find()){
            int a=Integer.parseInt(m.group("a"));
            int b=m.group("b")==null?-1:Integer.parseInt(m.group("b"));
            int c=m.group("c")==null?-1:Integer.parseInt(m.group("c"));
            switch (a){
                case 0:{
                    colorCurrent = cReset;
                    break;
                }
                case 30:
                case 31:
                case 32:
                case 33:
                case 34:
                case 35:
                case 36:
                case 37:
                {
                    colorCurrent = COLS[a-30];
                    break;
                }
                case 38:{
                    colorCurrent = COLS[c%15];
                    break;
                }
            }
            return;
        }
        switch (ANSIColor) {
            case "\u001B[30m": {
                colorCurrent = D_Black;
                break;
            }
            case "\u001B[31m": {
                colorCurrent = D_Red;
                break;
            }
            case "\u001B[32m": {
                colorCurrent = D_Green;
                break;
            }
            case "\u001B[33m": {
                colorCurrent = D_Yellow;
                break;
            }
            case "\u001B[34m": {
                colorCurrent = D_Blue;
                break;
            }
            case "\u001B[35m": {
                colorCurrent = D_Magenta;
                break;
            }
            case "\u001B[36m": {
                colorCurrent = D_Cyan;
                break;
            }
            case "\u001B[37m": {
                colorCurrent = D_White;
                break;
            }
            case "\u001B[0;30m": {
                colorCurrent = D_Black;
                break;
            }
            case "\u001B[0;31m": {
                colorCurrent = D_Red;
                break;
            }
            case "\u001B[0;32m": {
                colorCurrent = D_Green;
                break;
            }
            case "\u001B[0;33m": {
                colorCurrent = D_Yellow;
                break;
            }
            case "\u001B[0;34m": {
                colorCurrent = D_Blue;
                break;
            }
            case "\u001B[0;35m": {
                colorCurrent = D_Magenta;
                break;
            }
            case "\u001B[0;36m": {
                colorCurrent = D_Cyan;
                break;
            }
            case "\u001B[0;37m": {
                colorCurrent = D_White;
                break;
            }
            case "\u001B[1;30m": {
                colorCurrent = B_Black;
                break;
            }
            case "\u001B[1;31m": {
                colorCurrent = B_Red;
                break;
            }
            case "\u001B[1;32m": {
                colorCurrent = B_Green;
                break;
            }
            case "\u001B[1;33m": {
                colorCurrent = B_Yellow;
                break;
            }
            case "\u001B[1;34m": {
                colorCurrent = B_Blue;
                break;
            }
            case "\u001B[1;35m": {
                colorCurrent = B_Magenta;
                break;
            }
            case "\u001B[1;36m": {
                colorCurrent = B_Cyan;
                break;
            }
            case "\u001B[1;37m": {
                colorCurrent = B_White;
                break;
            }
            case "\u001B[0m": {
                colorCurrent = cReset;
                break;
            }
            default: {
                colorCurrent = cReset;
                break;
            }
        }
    }

    public void clearScreen() {
        setText("");
    }
}
