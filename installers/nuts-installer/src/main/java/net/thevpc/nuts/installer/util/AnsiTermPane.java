package net.thevpc.nuts.installer.util;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * thanks to https://stackoverflow.com/questions/6913983/jtextpane-removing-first-line
 */
public class AnsiTermPane extends JTextPane {
    static final Color D_Black   = Color.getHSBColor( 0.000f, 0.000f, 0.000f );
    static final Color D_Red     = Color.getHSBColor( 0.000f, 1.000f, 0.502f );
    static final Color D_Blue    = Color.getHSBColor( 0.667f, 1.000f, 0.502f );
    static final Color D_Magenta = Color.getHSBColor( 0.833f, 1.000f, 0.502f );
    static final Color D_Green   = Color.getHSBColor( 0.333f, 1.000f, 0.502f );
    static final Color D_Yellow  = Color.getHSBColor( 0.167f, 1.000f, 0.502f );
    static final Color D_Cyan    = Color.getHSBColor( 0.500f, 1.000f, 0.502f );
    static final Color D_White   = Color.getHSBColor( 0.000f, 0.000f, 0.753f );
    static final Color B_Black   = Color.getHSBColor( 0.000f, 0.000f, 0.502f );
    static final Color B_Red     = Color.getHSBColor( 0.000f, 1.000f, 1.000f );
    static final Color B_Blue    = Color.getHSBColor( 0.667f, 1.000f, 1.000f );
    static final Color B_Magenta = Color.getHSBColor( 0.833f, 1.000f, 1.000f );
    static final Color B_Green   = Color.getHSBColor( 0.333f, 1.000f, 1.000f );
    static final Color B_Yellow  = Color.getHSBColor( 0.167f, 1.000f, 1.000f );
    static final Color B_Cyan    = Color.getHSBColor( 0.500f, 1.000f, 1.000f );
    static final Color B_White   = Color.getHSBColor( 0.000f, 0.000f, 1.000f );
    static final Color cReset    = Color.getHSBColor( 0.000f, 0.000f, 1.000f );
    static Color colorCurrent    = Color.BLACK;//cReset;
    String remaining = "";

    public AnsiTermPane() {
        setBackground(Color.BLACK);
        setFont(new Font("Courier New",Font.PLAIN,14));
    }

    public void clearLastLine() {
        Element root = getDocument().getDefaultRootElement();
        if(root.getElementCount()>0) {
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
                append(colorCurrent,addString);
                return;
            }
// otherwise There is an escape character in the string, so we must process it

            if (aIndex > 0) { // Escape is not first char, so send text up to first escape
                tmpString = addString.substring(0,aIndex);
                append(colorCurrent, tmpString);
                aPos = aIndex;
            }
// aPos is now at the beginning of the first escape sequence

            stillSearching = true;
            while (stillSearching) {
                mIndex = addString.indexOf("m",aPos); // find the end of the escape sequence
                if (mIndex < 0) { // the buffer ends halfway through the ansi string!
                    remaining = addString.substring(aPos,addString.length());
                    stillSearching = false;
                    continue;
                }
                else {
                    tmpString = addString.substring(aPos,mIndex+1);
                    colorCurrent = getANSIColor(tmpString);
                }
                aPos = mIndex + 1;
// now we have the color, send text that is in that color (up to next escape)

                aIndex = addString.indexOf("\u001B", aPos);

                if (aIndex == -1) { // if that was the last sequence of the input, send remaining text
                    tmpString = addString.substring(aPos,addString.length());
                    append(colorCurrent, tmpString);
                    stillSearching = false;
                    continue; // jump out of loop early, as the whole string has been sent now
                }

                // there is another escape sequence, so send part of the string and prepare for the next
                tmpString = addString.substring(aPos,aIndex);
                aPos = aIndex;
                append(colorCurrent, tmpString);

            } // while there's text in the input buffer
        }
    }

    public Color getANSIColor(String ANSIColor) {
        switch (ANSIColor) {
            case "\u001B[30m":
                return D_Black;
            case "\u001B[31m":
                return D_Red;
            case "\u001B[32m":
                return D_Green;
            case "\u001B[33m":
                return D_Yellow;
            case "\u001B[34m":
                return D_Blue;
            case "\u001B[35m":
                return D_Magenta;
            case "\u001B[36m":
                return D_Cyan;
            case "\u001B[37m":
                return D_White;
            case "\u001B[0;30m":
                return D_Black;
            case "\u001B[0;31m":
                return D_Red;
            case "\u001B[0;32m":
                return D_Green;
            case "\u001B[0;33m":
                return D_Yellow;
            case "\u001B[0;34m":
                return D_Blue;
            case "\u001B[0;35m":
                return D_Magenta;
            case "\u001B[0;36m":
                return D_Cyan;
            case "\u001B[0;37m":
                return D_White;
            case "\u001B[1;30m":
                return B_Black;
            case "\u001B[1;31m":
                return B_Red;
            case "\u001B[1;32m":
                return B_Green;
            case "\u001B[1;33m":
                return B_Yellow;
            case "\u001B[1;34m":
                return B_Blue;
            case "\u001B[1;35m":
                return B_Magenta;
            case "\u001B[1;36m":
                return B_Cyan;
            case "\u001B[1;37m":
                return B_White;
            case "\u001B[0m":
                return cReset;
            default:
                return B_White;
        }
    }

    public void clearScreen() {
        setText("");
    }
}
