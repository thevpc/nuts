///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nuts.runtime.standalone.text.renderer.ansi;
//
//import net.thevpc.nuts.NutsBlankable;
//import net.thevpc.nuts.NutsColor;
//import net.thevpc.nuts.NutsSession;
//import net.thevpc.nuts.spi.NutsAnsiTermHelper;
//import net.thevpc.nuts.runtime.standalone.text.RenderedRawStream;
//import net.thevpc.nuts.runtime.standalone.text.renderer.StyleRenderer;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
///**
// * @author thevpc
// */
//public class AnsiStyle implements StyleRenderer {
//
//    public static final AnsiStyle PLAIN = new AnsiStyle();
//
//    private NutsColor foreground;
//    private NutsColor background;
//    private int intensity;
//    private boolean underlined;
//    private boolean italic;
//    private boolean striked;
//    private boolean reversed;
//    private boolean blink;
//    private boolean bold;
//    private final List<String> startCommands = new ArrayList<>();
//    private final List<String> endCommands = new ArrayList<>();
//    private final List<String> laterCommands = new ArrayList<>();
//
//    public AnsiStyle() {
//    }
//
//    public AnsiStyle(
//            NutsColor foreground, NutsColor background,
//                     boolean bold, boolean underlined, boolean italic, boolean striked, boolean reversed, boolean blink, int intensity
//            , List<String> startCommands
//            , List<String> endCommands
//            , List<String> laterCommands
//    ) {
//        this.foreground = foreground;
//        this.background = background;
//        this.underlined = underlined;
//        this.bold = bold;
//        this.italic = italic;
//        this.intensity = intensity;
//        this.striked = striked;
//        this.reversed = reversed;
//        this.blink = blink;
//        if (startCommands != null) {
//            this.startCommands.addAll(startCommands);
//        }
//        if (endCommands != null) {
//            this.endCommands.addAll(endCommands);
//        }
//        if (laterCommands != null) {
//            this.laterCommands.addAll(laterCommands);
//        }
//    }
//
//    public boolean isPlain() {
//        if (!bold && !blink && !underlined && !italic && !striked && !reversed
//                && NutsBlankable.isBlank(foreground)
//                && NutsBlankable.isBlank(background)
//        ) {
//            return true;
//        }
//        return false;
//    }
//
//    public String resolveEndEscapeString(NutsSession session) {
//        if(isPlain()){
//            return "";
//        }
//        return NutsAnsiTermHelper.of(session).plain();
//    }
//
//    public String resolveStartEscapeString(NutsSession session) {
//        return NutsAnsiTermHelper.of(session)
//                .styled(
//                        foreground,background,bold, blink, underlined, striked, italic, reversed,intensity, session
//                );
//    }
//
//    public AnsiStyle setForeground(NutsColor foreground) {
//        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
//    }
//
//    public AnsiStyle setIntensity(int intensity) {
//        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
//    }
//
//    public AnsiStyle setBackground(NutsColor background) {
//        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
//    }
//
//    public AnsiStyle setUnderlined(boolean underlined) {
//        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
//    }
//
//    public AnsiStyle setItalic(boolean italic) {
//        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
//    }
//
//    public AnsiStyle setStriked(boolean striked) {
//        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
//    }
//
//    public AnsiStyle setReversed(boolean reversed) {
//        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
//    }
//
//    public AnsiStyle addCommand(String command) {
//        AnsiStyle ansiStyle = new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
//        ansiStyle.startCommands.add(command);
//        return ansiStyle;
//    }
//
//    public AnsiStyle addLaterCommand(String command) {
//        AnsiStyle ansiStyle = new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
//        ansiStyle.laterCommands.add(command);
//        return ansiStyle;
//    }
//
//    public AnsiStyle setBlink(boolean blink) {
//        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
//    }
//
//    public AnsiStyle setBold(boolean bold) {
//        return new AnsiStyle(foreground, background, bold, underlined, italic, striked, reversed, blink, intensity, startCommands,endCommands,laterCommands);
//    }
//
//    public String[] getStartCommands() {
//        return startCommands.toArray(new String[0]);
//    }
//
//    public String[] getEndCommands() {
//        return endCommands.toArray(new String[0]);
//    }
//
//    public String[] getLaterCommands() {
//        return laterCommands.toArray(new String[0]);
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 5;
//        hash = 23 * hash + Objects.hashCode(this.foreground);
//        hash = 23 * hash + Objects.hashCode(this.background);
//        hash = 23 * hash + this.intensity;
//        hash = 23 * hash + (this.underlined ? 1 : 0);
//        hash = 23 * hash + (this.italic ? 1 : 0);
//        hash = 23 * hash + (this.striked ? 1 : 0);
//        hash = 23 * hash + (this.reversed ? 1 : 0);
//        hash = 23 * hash + (this.blink ? 1 : 0);
//        hash = 23 * hash + (this.bold ? 1 : 0);
//        hash = 23 * hash + Objects.hashCode(this.startCommands);
//        hash = 23 * hash + Objects.hashCode(this.endCommands);
//        hash = 23 * hash + Objects.hashCode(this.laterCommands);
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final AnsiStyle other = (AnsiStyle) obj;
//        if (this.intensity != other.intensity) {
//            return false;
//        }
//        if (this.bold != other.bold) {
//            return false;
//        }
//        if (this.underlined != other.underlined) {
//            return false;
//        }
//        if (this.italic != other.italic) {
//            return false;
//        }
//        if (this.striked != other.striked) {
//            return false;
//        }
//        if (this.reversed != other.reversed) {
//            return false;
//        }
//        if (this.blink != other.blink) {
//            return false;
//        }
//        if (!Objects.equals(this.foreground, other.foreground)) {
//            return false;
//        }
//        if (!Objects.equals(this.background, other.background)) {
//            return false;
//        }
//        if (!Objects.equals(this.startCommands, other.startCommands)) {
//            return false;
//        }
//        if (!Objects.equals(this.endCommands, other.endCommands)) {
//            return false;
//        }
//        if (!Objects.equals(this.laterCommands, other.laterCommands)) {
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public void startFormat(RenderedRawStream out, NutsSession session) {
//        for (String command : startCommands) {
//            byte[] bytes = command.getBytes();
//            out.writeRaw(bytes,0,bytes.length);
//        }
//        String s = this.resolveStartEscapeString(session);
//        if (s != null && s.length()>0) {
//            byte[] bytes = s.getBytes();
//            out.writeRaw(bytes,0,bytes.length);
//        }
//    }
//
//    @Override
//    public void endFormat(RenderedRawStream out, NutsSession session)  {
//        String s = this.resolveEndEscapeString(session);
//        if (s != null && s.length()>0) {
//            byte[] bytes = s.getBytes();
//            out.writeRaw(bytes,0,bytes.length);
//        }
//        for (String command : endCommands) {
//            byte[] bytes = command.getBytes();
//            out.writeRaw(bytes,0,bytes.length);
//        }
//        if (!laterCommands.isEmpty()) {
//            StringBuilder sb = new StringBuilder();
//            for (String laterCommand : laterCommands) {
//                sb.append(laterCommand);
//            }
//            out.writeLater(sb.toString().getBytes());
//        }
//    }
//}
