package net.thevpc.nuts.installer.util.swing;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * thanks to
 * https://stackoverflow.com/questions/6913983/jtextpane-removing-first-line
 */
/**
 * thanks to
 * https://stackoverflow.com/questions/6913983/jtextpane-removing-first-line
 */
public class AnsiTermPane extends JTextPane {

    String remaining = "";

    private volatile PrintStream ps;

    AnsiColors ansiColors = new AnsiColors();
    TextStyle currentStyle = new TextStyle()
            .setForeColor(Color.BLACK);
    private int maxRows = -1;

    public AnsiTermPane(boolean darkMode) {
        setDarkMode(darkMode);
    }

    public int getMaxRows() {
        return maxRows;
    }

    public AnsiTermPane setMaxRows(int maxRows) {
        this.maxRows = maxRows;
        return this;
    }

    public void resetCurr() {
        currentStyle = ansiColors.resetStyle();
    }

    public void setDarkMode(boolean darkMode) {
        ansiColors.setDarkMode(darkMode);
        ansiColors.cResetBackground = getBackground();
        applyFont();
        setForeground(ansiColors.cResetForeground);
        setBackground(ansiColors.preferredBackground);
        resetCurr();
    }

    private void applyFont() {
        final Font font = getFont();
        float mulFactor = 1;
        try {
            String s = System.getProperty("flatlaf.uiScale");
            if (s != null) {
                float f = Float.parseFloat(s);
                if (f > 0) {
                    mulFactor = f;
                }
            }
        } catch (Exception e) {
            //
        }
        float expectedSize = 14 * mulFactor;
        if (font == null || font.getName().equals("Courrier New") || font.getSize2D() != expectedSize) {
            SwingUtilities.invokeLater(() -> {
                setFont(new Font("Courier New", Font.PLAIN, (int) expectedSize).deriveFont(expectedSize));
            });
        }
    }

    private long lastCheckedTime = 0;

    @Override
    protected void paintComponent(Graphics g) {
        final long now = System.currentTimeMillis();
        if (lastCheckedTime == 0 || now - lastCheckedTime > 1000) {
            lastCheckedTime = now;
            applyFont();
        }
        super.paintComponent(g);
    }

    public void clearLastLine() {
        Element root = getDocument().getDefaultRootElement();
        if (root.getElementCount() > 0) {
            Element first = root.getElement(root.getElementCount() - 1);
            try {
                int offs = first.getStartOffset();
                int len = first.getEndOffset() - offs - 1;
                int length = getDocument().getLength();
                if (len > 0 && offs >= 0 && (offs + len) <= length) {
                    getDocument().remove(offs, len);
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        applyFont();
    }

    public void append(int color256, String s) {
        append(currentStyle.copy().setForeColor(ansiColors.color256(color256)), s);
    }

    public void append(TextStyle c, String s) {
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
        int len = getDocument().getLength();
        boolean editable = isEditable();
        setEditable(true);
        setCaretPosition(len);
        setCharacterAttributes(aset, false);
        replaceSelection(s);
        ensureMaxRows();
        setEditable(editable);
    }

    private void ensureMaxRows() {
        int currentMaxRows = getMaxRows();
        if (currentMaxRows > 0) {
            Element root = this.getDocument().getDefaultRootElement();
            while (root.getElementCount() > currentMaxRows) {
                Element first = root.getElement(0);
                try {
                    this.getDocument().remove(first.getStartOffset(), first.getEndOffset());
                } catch (BadLocationException e) {
                    break;
                }
            }
        }
    }

    public PrintStream asPrintStream() {
        if (ps == null) {
            synchronized (this) {
                if (ps == null) {
                    ps = new TempPrintStream(this);
                }
            }
        }
        return ps;
    }

    private static class TempPrintStream extends PrintStream {
        private AnsiTermPane termPane;

        public TempPrintStream(AnsiTermPane termPane) {
            super(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    UIHelper.withinGUI(() -> {
                        termPane.appendANSI(String.valueOf((char) b));
                    });
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    String decoded = new String(b, off, len);
                    UIHelper.withinGUI(() -> {
                        termPane.appendANSI(decoded);
                    });
                }
            });
            this.termPane = termPane;
        }

        public AnsiTermPane getTermPane() {
            return termPane;
        }
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

    public void appendANSI(String s) {
        int aPos = 0;
        int aIndex = 0;
        int mIndex = 0;
        String tmpString = "";
        boolean stillSearching = true;
        String addString = remaining + s;
        remaining = "";

        if (!addString.isEmpty()) {
            aIndex = addString.indexOf("\u001B");
            if (aIndex == -1) {
                append(currentStyle, addString);
                return;
            }

            if (aIndex > 0) {
                tmpString = addString.substring(0, aIndex);
                append(currentStyle, tmpString);
                aPos = aIndex;
            }

            stillSearching = true;
            while (stillSearching) {
                mIndex = endOfEscape(addString, aPos);
                if (mIndex < 0) {
                    remaining = addString.substring(aPos);
                    stillSearching = false;
                    continue;
                } else {
                    tmpString = addString.substring(aPos, mIndex + 1);
                    applyANSIColor(tmpString);
                }
                aPos = mIndex + 1;

                aIndex = addString.indexOf("\u001B", aPos);

                if (aIndex == -1) {
                    tmpString = addString.substring(aPos);
                    append(currentStyle, tmpString);
                    stillSearching = false;
                    continue;
                }

                tmpString = addString.substring(aPos, aIndex);
                aPos = aIndex;
                append(currentStyle, tmpString);
            }
        }
        applyFont();
    }

    public void applyANSIColor(String ANSIColor) {
        switch (ANSIColor) {
            case "\u001B[2K": {
                clearLastLine();
                return;
            }
        }
        currentStyle = ansiColors.applyANSIColor(ANSIColor, currentStyle);
    }

    public void clearScreen() {
        setText("");
        applyFont();
    }
}