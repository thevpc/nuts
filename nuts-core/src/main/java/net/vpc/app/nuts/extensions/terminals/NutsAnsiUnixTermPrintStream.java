/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.terminals;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vpc on 2/20/17.
 */
public class NutsAnsiUnixTermPrintStream extends NutsDefaultFormattedPrintStream {

//    public static final NutsTextFormat[] ALL_SUPPORTED_COLORS = new NutsTextFormat[]{
//        NutsTextFormats.FG_BLACK,
//        NutsTextFormats.FG_RED,
//        NutsTextFormats.FG_BLUE,
//        NutsTextFormats.FG_GREEN,
//        NutsTextFormats.FG_CYAN,
//        NutsTextFormats.FG_LIGHT_GRAY,
//        NutsTextFormats.FG_LIGHT_BLUE,
//        NutsTextFormats.FG_LIGHT_GREEN,
//        NutsTextFormats.FG_LIGHT_CYAN,
//        NutsTextFormats.FG_LIGHT_RED,
//        NutsTextFormats.FG_LIGHT_PURPLE,
//        NutsTextFormats.FG_DARK_GRAY,
//        NutsTextFormats.FG_MAGENTA,
//        NutsTextFormats.FG_BROWN,
//        NutsTextFormats.FG_YELLOW,
//        NutsTextFormats.FG_WHITE
//    };
    private final Map<NutsTextFormat, AnsiStyleStyleApplier> stylesAppliers = new HashMap<>();

    {
//        defineEscape(NutsTextFormats.FG_BLACK, "\u001B[0;30m", "\u001B[0m");
//        defineEscape(NutsTextFormats.FG_RED, "\u001B[0;31m", "\u001B[0m");
//        defineEscape(NutsTextFormats.FG_GREEN, "\u001B[0;32m", "\u001B[0m");
//        defineEscape(NutsTextFormats.FG_BLUE, "\u001B[0;34m", "\u001B[0m");
//        defineEscape(NutsTextFormats.FG_BROWN,  "\u001B[0;35m", "\u001B[0m");
//        defineEscape(NutsTextFormats.FG_CYAN, "\u001B[0;36m", "\u001B[0m");
//        defineEscape(NutsTextFormats.FG_LIGHT_GRAY, "\u001B[0;37m", "\u001B[0m");

//        defineEscape(NutsTextFormats.FG_DARK_GRAY, "\u001B[1;30m", "\u001B[0m");
//        defineEscape(NutsTextFormats.FG_LIGHT_BLUE, "\u001B[1;34m", "\u001B[0m");
//        defineEscape(NutsTextFormats.FG_LIGHT_GREEN, "\u001B[1;32m", "\u001B[0m");
//        defineEscape(NutsTextFormats.FG_LIGHT_RED, "\u001B[1;31m", "\u001B[0m");
//        defineEscape(NutsTextFormats.FG_LIGHT_PURPLE, "\u001B[1;35m", "\u001B[0m");
//        defineEscape(NutsTextFormats.FG_YELLOW, "\u001B[1;35m", "\u001B[0m");
//        defineEscape(NutsTextFormats.FG_WHITE, "\u001B[1;35m", "\u001B[0m");
//        
//        defineEscape(NutsTextFormats.BG_BLACK, "\u001B[0;30;1m", "\u001B[0m");
//        defineEscape(NutsTextFormats.BG_BLUE, "\u001B[0;34;1m", "\u001B[0m");
//        defineEscape(NutsTextFormats.BG_GREEN, "\u001B[0;32;1m", "\u001B[0m");
//        defineEscape(NutsTextFormats.BG_CYAN, "\u001B[0;36;1m", "\u001B[0m");
//        defineEscape(NutsTextFormats.BG_RED, "\u001B[0;31;1m", "\u001B[0m");
//        defineEscape(NutsTextFormats.BG_MAGENTA, "\u001B[0;35;1m", "\u001B[0m");
//        defineEscape(NutsTextFormats.BG_BROWN, "\u001B[0;35;1m", "\u001B[0m");
//        defineEscape(NutsTextFormats.BG_LIGHT_GRAY, "\u001B[0;37;1m", "\u001B[0m");
//        defineEscape(NutsTextFormats.BG_DARK_GRAY, "\u001B[1;30;1m", "\u001B[0m");
//        defineEscape(NutsTextFormats.BG_LIGHT_BLUE, "\u001B[1;34;1m", "\u001B[0m");
//        defineEscape(NutsTextFormats.BG_LIGHT_GREEN, "\u001B[1;32;1m", "\u001B[0m");
//        defineEscape(NutsTextFormats.BG_LIGHT_CYAN, "\u001B[1;36;1m", "\u001B[0m");
//        defineEscape(NutsTextFormats.BG_LIGHT_RED, "\u001B[1;31;1m", "\u001B[0m");
//        defineEscape(NutsTextFormats.BG_LIGHT_PURPLE, "\u001B[1;35;1m", "\u001B[0m");
        defineEscape(NutsTextFormats.FG_BLACK, new ForegroundStyleApplier("30", 0));
        defineEscape(NutsTextFormats.FG_RED, new ForegroundStyleApplier("31", 0));
        defineEscape(NutsTextFormats.FG_GREEN, new ForegroundStyleApplier("32", 0));
        defineEscape(NutsTextFormats.FG_YELLOW, new ForegroundStyleApplier("33", 0));
        defineEscape(NutsTextFormats.FG_BLUE, new ForegroundStyleApplier("34", 0));
        defineEscape(NutsTextFormats.FG_MAGENTA, new ForegroundStyleApplier("35", 0));
        defineEscape(NutsTextFormats.FG_CYAN, new ForegroundStyleApplier("36", 0));
        defineEscape(NutsTextFormats.FG_WHITE, new ForegroundStyleApplier("37", 0));

        defineEscape(NutsTextFormats.BG_BLACK, new BackgroundStyleApplier("40"));
        defineEscape(NutsTextFormats.BG_RED, new BackgroundStyleApplier("41"));
        defineEscape(NutsTextFormats.BG_GREEN, new BackgroundStyleApplier("42"));
        defineEscape(NutsTextFormats.BG_YELLOW, new BackgroundStyleApplier("43"));
        defineEscape(NutsTextFormats.BG_BLUE, new BackgroundStyleApplier("44"));
        defineEscape(NutsTextFormats.BG_MAGENTA, new BackgroundStyleApplier("35"));
        defineEscape(NutsTextFormats.BG_CYAN, new BackgroundStyleApplier("36"));
        defineEscape(NutsTextFormats.BG_WHITE, new BackgroundStyleApplier("37"));
        defineEscape(NutsTextFormats.UNDERLINED, new AnsiStyleStyleApplier() {
            @Override
            public AnsiStyle apply(AnsiStyle old) {
                return old.setUnderlined(true);
            }
        });
        defineEscape(NutsTextFormats.REVERSED, new AnsiStyleStyleApplier() {
            @Override
            public AnsiStyle apply(AnsiStyle old) {
                return old.setReversed(true);
            }
        });
        defineEscape(NutsTextFormats.MOVE_LINE_START, new AnsiStyleStyleApplier() {
            @Override
            public AnsiStyle apply(AnsiStyle old) {
                return old.addCommand("\u001b[1000D");
            }
        });
        defineEscape(NutsTextFormats.MOVE_UP, new AnsiStyleStyleApplier() {
            @Override
            public AnsiStyle apply(AnsiStyle old) {
                return old.addCommand("\u001b[1A");
            }
        });

//        defineEscape(NutsTextFormats.MOVE_LINE_START, "\u001B[1000D", "");
//        defineEscape(NutsTextFormats.BOLD, "\u001b[1m", "\u001B[0m");
//        defineEscape(NutsTextFormats.UNDERLINED, "\u001b[4m", "\u001B[0m");
//        defineEscape(NutsTextFormats.REVERSED, "\u001b[7m", "\u001B[0m");
    }

//    public static void main(String[] args) {
//        System.out.println("\u001b[38;2;255;100;0mTRUECOLOR\u001b[0m\n");
//        NutsAnsiUnixTermPrintStream out = new NutsAnsiUnixTermPrintStream(System.out);
//        for (int i = 0; i < 200; i++) {
//
//            System.out.print("\u001b[" + i + ";0m");
//            System.out.print("      " + i + "      ");
//            System.out.print("\u001B[0m");
//            System.out.print("  ");
//
//            System.out.print("\u001b[" + i + ";1m");
//            System.out.print("      " + i + "      ");
//            System.out.print("\u001B[0m");
//            System.out.print("  ");
//
//            System.out.print("\u001b[" + i + "m");
//            System.out.print("      " + i + "      ");
//            System.out.print("\u001B[0m");
//            System.out.print("  ");
//            System.out.print("\u001b[" + i + ";2m");
//            System.out.print("      " + i + "      ");
//            System.out.print("\u001B[0m");
//            System.out.print("\u001b[4;" + i + "m");
//            System.out.print("      " + i + "      ");
//            System.out.print("\u001B[0m");
//            System.out.println();
//        }
//        Scanner s = new Scanner(System.in);
//        s.nextLine();
////        out.print(NutsTextFormats.BG_YELLOW,"Hello");
////        System.out.println("\u001b[44mAA\u001B[0m");
////        for (NutsTextFormat k : out.escapesStart.keySet()) {
//////            System.out.println(k);
////            System.out.print(out.escapesStart.get(k));
////            System.out.println("  "+k+"  ");
////            System.out.print(out.escapesStop.get(k));
////            System.out.println();
////        }
//    }

    public NutsAnsiUnixTermPrintStream() {
    }

    public NutsAnsiUnixTermPrintStream(OutputStream out) {
        super(out);
    }

    public NutsAnsiUnixTermPrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public NutsAnsiUnixTermPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
    }

    public NutsAnsiUnixTermPrintStream(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public NutsAnsiUnixTermPrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public NutsAnsiUnixTermPrintStream(File file) throws FileNotFoundException {
        super(file);
    }

    public NutsAnsiUnixTermPrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    @Override
    protected void startFormat(NutsTextFormat format) {
        AnsiStyleStyleApplier applier = resolveStyleApplyer(format);
        AnsiStyle style = applier.apply(new AnsiStyle());
        for (String command : style.getCommands()) {
            writeRaw(command);
        }
        String escaped = style.resolveEscapeString();
//        super.print("\u001B[0m");
        writeRaw(escaped);
    }

    @Override
    protected void endFormat(NutsTextFormat color) {
        writeRaw("\u001B[0m");
    }

    @Override
    public int getSupportLevel(Object criteria) {
//        Console console = System.console();
////        if(console ==null){
////            return -1;
////        }
////        if(criteria==console.writer()){
////            return DEFAULT_SUPPORT + 2;
////        }
//        if(criteria ==System.out){
//            return DEFAULT_SUPPORT + 2;
//        }
//        if(criteria ==System.err){
//            return DEFAULT_SUPPORT + 2;
//        }
//        System.out.println(criteria+" :: "+System.out+" :: "+System.err);
//        return -1;
        return DEFAULT_SUPPORT + 2;
    }

    private AnsiStyleStyleApplier createAnsiStyleStyleApplier(NutsTextFormatList list) {
        List<AnsiStyleStyleApplier> suppliers = new ArrayList<AnsiStyleStyleApplier>();
        for (NutsTextFormat item : list) {
            suppliers.add(resolveStyleApplyer(item));
        }
        return new ListAnsiStyleStyleApplier(suppliers);
    }

    private AnsiStyleStyleApplier resolveStyleApplyer(NutsTextFormat format) {
        if (format instanceof NutsTextFormatList) {
            return createAnsiStyleStyleApplier((NutsTextFormatList) format);
        }
        AnsiStyleStyleApplier s = stylesAppliers.get(format);
        if (s != null) {
            return s;
        }
        return DoNothingAnsiStyleStyleApplier.INSTANCE;
    }

//    private AnsiStyle resolveStyle(NutsTextFormat format, AnsiStyle old) {
//        AnsiStyleStyleApplier s = resolveStyleApplyer(format);
//        return s.apply(old);
//    }
    private void defineEscape(NutsTextFormat a, AnsiStyleStyleApplier style) {
        stylesAppliers.put(a, style);
    }

}
