/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.util.fprint.renderer.ansi;

import net.vpc.app.nuts.runtime.util.fprint.util.FormattedPrintStreamUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author vpc
 */
public class AnsiStyle {

    public static final AnsiStyle PLAIN = new AnsiStyle();

    private String foreground = "";
    private String background = "";
    private int intensity;
    private boolean underlined;
    private boolean italic;
    private boolean striked;
    private boolean reversed;
    private boolean blink;
    private boolean bold;
    private final List<String> commands = new ArrayList<>();

    public AnsiStyle() {
    }

    public AnsiStyle(String foreground, String background, boolean bold, boolean underlined, boolean italic, boolean striked, boolean reversed, boolean blink, int intensity, List<String> commands) {
        this.foreground = foreground;
        this.background = background;
        this.underlined = underlined;
        this.bold = bold;
        this.italic = italic;
        this.intensity = intensity;
        this.striked = striked;
        this.reversed = reversed;
        this.blink = blink;
        if (commands != null) {
            this.commands.addAll(commands);
        }
    }

    public String resolveEscapeString() {
        if (!bold && !blink && !underlined && !italic && !striked && !reversed && !blink && FormattedPrintStreamUtils.isEmpty(foreground) && FormattedPrintStreamUtils.isEmpty(background)) {
            return "\u001B[0m";
        }
        StringBuilder sb = new StringBuilder("\u001B[");
        boolean first = true;
        if (foreground != null) {
            sb.append(foreground);
        }
        if (!FormattedPrintStreamUtils.isEmpty(background)) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append(background);
        }
        if (bold) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append("1");
        }
        if (blink) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append("5");
        }
        if (underlined) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append("4");
        }
        if (striked) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append("9");
        }
        if (italic) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append("3");
        }
        if (reversed) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
            sb.append("7");
        }
        sb.append("m");
        return sb.toString();
    }

    public AnsiStyle setForeground(String foreground) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, commands);
    }

    public AnsiStyle setIntensity(int intensity) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, commands);
    }

    public AnsiStyle setBackground(String background) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, commands);
    }

    public AnsiStyle setUnderlined(boolean underlined) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, commands);
    }

    public AnsiStyle setItalic(boolean italic) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, commands);
    }

    public AnsiStyle setStriked(boolean striked) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, commands);
    }

    public AnsiStyle setReversed(boolean reversed) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, commands);
    }

    public AnsiStyle addCommand(String command) {
        AnsiStyle ansiStyle = new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, commands);
        ansiStyle.commands.add(command);
        return ansiStyle;
    }

    public AnsiStyle setBlink(boolean blink) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, commands);
    }

    public AnsiStyle setBold(boolean bold) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, commands);
    }

    public String[] getCommands() {
        return commands.toArray(new String[0]);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.foreground);
        hash = 23 * hash + Objects.hashCode(this.background);
        hash = 23 * hash + this.intensity;
        hash = 23 * hash + (this.underlined ? 1 : 0);
        hash = 23 * hash + (this.italic ? 1 : 0);
        hash = 23 * hash + (this.striked ? 1 : 0);
        hash = 23 * hash + (this.reversed ? 1 : 0);
        hash = 23 * hash + (this.blink ? 1 : 0);
        hash = 23 * hash + (this.bold ? 1 : 0);
        hash = 23 * hash + Objects.hashCode(this.commands);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnsiStyle other = (AnsiStyle) obj;
        if (this.intensity != other.intensity) {
            return false;
        }
        if (this.bold != other.bold) {
            return false;
        }
        if (this.underlined != other.underlined) {
            return false;
        }
        if (this.italic != other.italic) {
            return false;
        }
        if (this.striked != other.striked) {
            return false;
        }
        if (this.reversed != other.reversed) {
            return false;
        }
        if (this.blink != other.blink) {
            return false;
        }
        if (!Objects.equals(this.foreground, other.foreground)) {
            return false;
        }
        if (!Objects.equals(this.background, other.background)) {
            return false;
        }
        if (!Objects.equals(this.commands, other.commands)) {
            return false;
        }
        return true;
    }

}
