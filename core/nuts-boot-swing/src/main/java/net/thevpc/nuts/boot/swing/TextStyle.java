package net.thevpc.nuts.boot.swing;

import java.awt.*;

public class TextStyle implements Cloneable {
    Color foreColor;
    Color backColor;
    boolean underline;
    boolean bold;
    boolean strikeThrough;
    boolean italic;

    public Color getForeColor() {
        return foreColor;
    }

    public TextStyle setForeColor(Color foreColor) {
        this.foreColor = foreColor;
        return this;
    }

    public Color getBackColor() {
        return backColor;
    }

    public TextStyle setBackColor(Color backColor) {
        this.backColor = backColor;
        return this;
    }

    public boolean isUnderline() {
        return underline;
    }

    public TextStyle setUnderline(boolean underline) {
        this.underline = underline;
        return this;
    }

    public boolean isBold() {
        return bold;
    }

    public TextStyle setBold(boolean bold) {
        this.bold = bold;
        return this;
    }

    public boolean isStrikeThrough() {
        return strikeThrough;
    }

    public TextStyle setStrikeThrough(boolean strikeThrough) {
        this.strikeThrough = strikeThrough;
        return this;
    }

    public boolean isItalic() {
        return italic;
    }

    public TextStyle setItalic(boolean italic) {
        this.italic = italic;
        return this;
    }

    public TextStyle copy() {
        try {
            return (TextStyle) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
