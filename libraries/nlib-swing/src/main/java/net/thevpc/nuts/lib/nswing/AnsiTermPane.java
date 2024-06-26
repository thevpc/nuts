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

    //    public static Color colorCurrent = Color.WHITE;//cReset;
    String remaining = "";
    PrintStream ps;
    AnsiColors ansiColors=new AnsiColors();
    TextStyle currentStyle = new TextStyle()
            .setForeColor(Color.BLACK);

    public AnsiTermPane(boolean darkMode) {
        setDarkMode(darkMode);
    }



    public void resetCurr() {
        currentStyle = ansiColors.restStyle();
    }

    public void setDarkMode(boolean darkMode) {
        ansiColors.cResetBackground = getBackground();
        ansiColors.setDarkMode(darkMode);;
        setFont(new Font("Courier New", Font.PLAIN, 14));
        setForeground(ansiColors.cResetForeground);
        setBackground(ansiColors.preferredBackground);
        resetCurr();
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




    public void append(int color256, String s) {
        append(currentStyle.copy().setForeColor(ansiColors.color256(color256)), s);
    }

    public void append(TextStyle c, String s) {
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
        // there is no selection, so inserts at caret
        setEditable(editable);
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

        if (!addString.isEmpty()) {
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
        currentStyle=ansiColors.applyANSIColor(ANSIColor, currentStyle);
    }

    public void clearScreen() {
        setText("");
    }
}
