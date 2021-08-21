/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format.text.renderer.ansi;

import net.thevpc.nuts.NutsUtilStrings;
import net.thevpc.nuts.runtime.core.format.text.RenderedRawStream;
import net.thevpc.nuts.runtime.core.format.text.renderer.StyleRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author thevpc
 */
public class AnsiStyle implements StyleRenderer {

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
    private final List<String> startCommands = new ArrayList<>();
    private final List<String> endCommands = new ArrayList<>();
    private final List<String> laterCommands = new ArrayList<>();

    public AnsiStyle() {
    }

    public AnsiStyle(
            String foreground, String background,
                     boolean bold, boolean underlined, boolean italic, boolean striked, boolean reversed, boolean blink, int intensity
            , List<String> startCommands
            , List<String> endCommands
            , List<String> laterCommands
    ) {
        this.foreground = foreground;
        this.background = background;
        this.underlined = underlined;
        this.bold = bold;
        this.italic = italic;
        this.intensity = intensity;
        this.striked = striked;
        this.reversed = reversed;
        this.blink = blink;
        if (startCommands != null) {
            this.startCommands.addAll(startCommands);
        }
        if (endCommands != null) {
            this.endCommands.addAll(endCommands);
        }
        if (laterCommands != null) {
            this.laterCommands.addAll(laterCommands);
        }
    }

    public boolean isPlain() {
        if (!bold && !blink && !underlined && !italic && !striked && !reversed
                && NutsUtilStrings.isBlank(foreground)
                && NutsUtilStrings.isBlank(background)
        ) {
            return true;
        }
        return false;
    }

    public String resolveEndEscapeString() {
        if(isPlain()){
            return "";
        }
        return "\u001B[0m";
    }

    public String resolveStartEscapeString() {
        if (isPlain()) {
            return "\u001B[0m";
        }
        StringBuilder sb = new StringBuilder("\u001B[");
        boolean first = true;
        if (foreground != null && foreground.length()>0) {
            first=false;

            //sb.append(foreground);

            sb.append(foreground);
        }
        if (!NutsUtilStrings.isBlank(background)) {
            if (first) {
                first = false;
            } else {
                sb.append(';');
            }
//            sb.append(background);
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

    private static final int[] FG8={30,31,32,33,34,35,36,37,90,91,92,93,94,95,96,97};
    private static final int[] BG8={40,41,42,43,44,45,46,47,100,101,102,103,104,105,106,107};

    public AnsiStyle setForeground4(int intColor) {
        if(intColor<=0){
            intColor=0;
        }
        if(intColor>=15){
            intColor=15;
        }
        return setForeground(""+FG8[intColor]);
    }

    public AnsiStyle setBackground4(int intColor) {
        if(intColor<=0){
            intColor=0;
        }
        if(intColor>=15){
            intColor=15;
        }
        return setBackground(""+BG8[intColor]);
    }

    public AnsiStyle setForeground8(int intColor) {
        if(intColor<=0){
            intColor=0;
        }
        if(intColor>=255){
            intColor=255;
        }
        return setForeground("38;5;"+intColor);
    }

    public AnsiStyle setForeground24(int intColor) {
        Color color = new Color(intColor);
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        return setForeground("38;2;"+red+";"+green+";"+blue);
    }

    public AnsiStyle setBackground8(int intColor) {
        if(intColor<=0){
            intColor=0;
        }
        if(intColor>=255){
            intColor=255;
        }
        return setForeground("48;5;"+intColor);
    }

    public AnsiStyle setBackground24(int intColor) {
        Color color = new Color(intColor);
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        return setForeground("48;2;"+red+";"+green+";"+blue);
    }

    public AnsiStyle setForeground(String foreground) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
    }

    public AnsiStyle setIntensity(int intensity) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
    }

    public AnsiStyle setBackground(String background) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
    }

    public AnsiStyle setUnderlined(boolean underlined) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
    }

    public AnsiStyle setItalic(boolean italic) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
    }

    public AnsiStyle setStriked(boolean striked) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
    }

    public AnsiStyle setReversed(boolean reversed) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
    }

    public AnsiStyle addCommand(String command) {
        AnsiStyle ansiStyle = new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
        ansiStyle.startCommands.add(command);
        return ansiStyle;
    }

    public AnsiStyle addLaterCommand(String command) {
        AnsiStyle ansiStyle = new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
        ansiStyle.laterCommands.add(command);
        return ansiStyle;
    }

    public AnsiStyle setBlink(boolean blink) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
    }

    public AnsiStyle setBold(boolean bold) {
        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
    }

    public String[] getStartCommands() {
        return startCommands.toArray(new String[0]);
    }

    public String[] getEndCommands() {
        return endCommands.toArray(new String[0]);
    }

    public String[] getLaterCommands() {
        return laterCommands.toArray(new String[0]);
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
        hash = 23 * hash + Objects.hashCode(this.startCommands);
        hash = 23 * hash + Objects.hashCode(this.endCommands);
        hash = 23 * hash + Objects.hashCode(this.laterCommands);
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
        if (!Objects.equals(this.startCommands, other.startCommands)) {
            return false;
        }
        if (!Objects.equals(this.endCommands, other.endCommands)) {
            return false;
        }
        if (!Objects.equals(this.laterCommands, other.laterCommands)) {
            return false;
        }
        return true;
    }

    @Override
    public void startFormat(RenderedRawStream out) {
        for (String command : startCommands) {
            byte[] bytes = command.getBytes();
            out.writeRaw(bytes,0,bytes.length);
        }
        String s = this.resolveStartEscapeString();
        if (s != null && s.length()>0) {
            byte[] bytes = s.getBytes();
            out.writeRaw(bytes,0,bytes.length);
        }
    }

    @Override
    public void endFormat(RenderedRawStream out)  {
        String s = this.resolveEndEscapeString();
        if (s != null && s.length()>0) {
            byte[] bytes = s.getBytes();
            out.writeRaw(bytes,0,bytes.length);
        }
        for (String command : endCommands) {
            byte[] bytes = command.getBytes();
            out.writeRaw(bytes,0,bytes.length);
        }
        if (!laterCommands.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String laterCommand : laterCommands) {
                sb.append(laterCommand);
            }
            out.writeLater(sb.toString().getBytes());
        }
    }
}
