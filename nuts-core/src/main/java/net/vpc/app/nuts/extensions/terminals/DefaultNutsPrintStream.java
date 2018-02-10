/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.terminals;

import net.vpc.app.nuts.NutsPrintColors;
import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.NutsTextChunck;

import java.awt.*;
import java.io.*;
import java.util.Locale;

/**
 * Created by vpc on 2/20/17.
 */
public class DefaultNutsPrintStream extends NutsPrintStream {

    private boolean enableColoring = true;
    private Color color;

    public DefaultNutsPrintStream() {
        super();
    }

    public DefaultNutsPrintStream(OutputStream out) {
        super(out);
    }

    public DefaultNutsPrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public DefaultNutsPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
    }

    public DefaultNutsPrintStream(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public DefaultNutsPrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public DefaultNutsPrintStream(File file) throws FileNotFoundException {
        super(file);
    }

    public DefaultNutsPrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    public Color getColor() {
        return color;
    }

    public DefaultNutsPrintStream setColor(Color color) {
        this.color = color;
        if (enableColoring) {
            applyColor(color);
        }
        return this;
    }

    public DefaultNutsPrintStream setEnableColoring(boolean enableColoring) {
        this.enableColoring = enableColoring;
        if (enableColoring) {
            applyColor(color);
        }
        return this;
    }

    protected void applyColor(Color color) {
    }

    private void doWithColor(Color col, Runnable runnable) {
        if (enableColoring && col != null) {
            Color c = color;
            try {
                setColor(col);
                runnable.run();
            } finally {
                setColor(c);
            }
        } else {
            runnable.run();
        }
    }

    public DefaultNutsPrintStream write(Color c, final int b) {
        doWithColor(c, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.write(b);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream write(Color c, final byte[] buf, final int off, final int len) {
        doWithColor(c, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.write(buf, off, len);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(Color c, final boolean b) {
        doWithColor(c, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.print(b);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(Color color, final char c) {
        doWithColor(color, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.print(c);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(Color c, final int i) {
        doWithColor(c, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.print(i);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(Color c, final long l) {
        doWithColor(c, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.print(l);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(Color c, final float f) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.print(f);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(Color c, final double d) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.print(d);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(Color c, final char[] s) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.print(s);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(Color c, final String s) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.print(s);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream print(Color c, final Object obj) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.print(obj);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream println(Color c, final boolean x) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream println(Color c, final char x) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream println(Color c, final int x) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream println(Color c, final long x) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public void println(Color c, final float x) {
        doWithColor(c, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
    }

    public DefaultNutsPrintStream println(Color c, final double x) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream println(Color c, final char[] x) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream println(Color c, final String x) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream println(Color c, final Object x) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public PrintStream printf(Color c, final String format, final Object... args) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.printf(format, args);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream printf(Color c, final Locale l, final String format, final Object... args) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.printf(l, format, args);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream format(Color c, final String format, final Object... args) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.format(format, args);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream format(Color c, final Locale l, final String format, final Object... args) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.format(l, format, args);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream append(Color c, final CharSequence csq) {
        doWithColor(c, new Runnable() {

            public void run() {
                DefaultNutsPrintStream.super.append(csq);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream append(Color c, final CharSequence csq, final int start, final int end) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.append(csq, start, end);
            }
        });
        return this;
    }

    public DefaultNutsPrintStream append(Color color, final char c) {
        doWithColor(color, new Runnable() {
            @Override
            public void run() {
                DefaultNutsPrintStream.super.append(c);
            }
        });
        return this;
    }
//
//    public void write(int b) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.write(b);
//            }
//        });
//    }
//
//    public void write(byte[] buf, int off, int len) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.write(buf, off, len);
//            }
//        });
//    }
//
//    public void print(boolean b) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.print(b);
//            }
//        });
//    }
//
//    public void print(char c) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.print(c);
//            }
//        });
//    }
//
//    public void print(int i) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.print(i);
//            }
//        });
//    }
//
//    public void print(long l) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.print(l);
//            }
//        });
//    }
//
//    public void print(float f) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.print(f);
//            }
//        });
//    }
//
//    public void print(double d) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.print(d);
//            }
//        });
//    }
//
//    public void print(char[] s) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.print(s);
//            }
//        });
//    }
//
//    public void print(String s) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.print(s);
//            }
//        });
//    }
//
//    public void print(Object obj) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.print(obj);
//            }
//        });
//    }
//
//    public void println() {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.println();
//            }
//        });
//    }
//
//    public void println(boolean x) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.println(x);
//            }
//        });
//    }
//
//    public void println(char x) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.println(x);
//            }
//        });
//    }
//
//    public void println(int x) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.println(x);
//            }
//        });
//    }
//
//    public void println(long x) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.println(x);
//            }
//        });
//    }
//
//    public void println(float x) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.println(x);
//            }
//        });
//    }
//
//    public void println(double x) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.println(x);
//            }
//        });
//    }
//
//    public void println(char[] x) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.println(x);
//            }
//        });
//    }
//
//    public void println(String x) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.println(x);
//            }
//        });
//    }
//
//    public void println(Object x) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.println(x);
//            }
//        });
//    }
//
//    public PrintStream printf(String format, Object... args) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.printf(format, args);
//            }
//        });
//        return this;
//    }
//
//    public NutsPrintStream printf(Locale l, String format, Object... args) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.printf(l, format, args);
//            }
//        });
//        return this;
//    }
//
//    public NutsPrintStream format(String format, Object... args) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.format(format, args);
//            }
//        });
//        return this;
//    }
//
//    public NutsPrintStream format(Locale l, String format, Object... args) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.format(l, format, args);
//            }
//        });
//        return this;
//    }
//
//    public NutsPrintStream append(CharSequence csq) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.append(csq);
//            }
//        });
//        return this;
//    }
//
//    public NutsPrintStream append(CharSequence csq, int start, int end) {
//        doWithColor(getColor(), new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.append(csq, start, end);
//            }
//        });
//        return this;
//    }
//
//    public NutsPrintStream append(char c) {
//        doWithColor(color, new Runnable() {
//
//            public void run() {
//                NutsPrintStream.super.append(c);
//            }
//        });
//        return this;
//    }

    @Override
    public DefaultNutsPrintStream printf(String format, Object... args) {
        DefaultNutsPrintStream.super.printf(format, args);
        return this;
    }

    @Override
    public DefaultNutsPrintStream printf(Locale l, String format, Object... args) {
        DefaultNutsPrintStream.super.printf(l, format, args);
        return this;
    }

    @Override
    public DefaultNutsPrintStream format(String format, Object... args) {
        DefaultNutsPrintStream.super.format(format, args);
        return this;
    }

    @Override
    public DefaultNutsPrintStream format(Locale l, String format, Object... args) {
        DefaultNutsPrintStream.super.format(l, format, args);
        return this;
    }

    @Override
    public DefaultNutsPrintStream append(CharSequence csq) {
        DefaultNutsPrintStream.super.append(csq);
        return this;
    }

    @Override
    public DefaultNutsPrintStream append(CharSequence csq, int start, int end) {
        DefaultNutsPrintStream.super.append(csq, start, end);
        return this;
    }

    @Override
    public DefaultNutsPrintStream append(char c) {
        DefaultNutsPrintStream.super.append(c);
        return this;
    }

    public void writeRaw(String s) {
        boolean c = enableColoring;
        enableColoring = false;
        super.print(s);
        enableColoring = c;
    }

    public void drawln(String text) {
        draw(text);
        println();
    }

    public void draw(NutsTextChunck chunk) {
        if (chunk == null) {
            chunk = NutsTextChunck.NULL;
        }
        String text = chunk.getValue();
        if (text == null || text.length() == 0) {
            this.append("");
        } else {
            if (chunk.getPattern() == null) {
                this.print(text);
            } else {
                switch (chunk.getPattern()) {
                    case "---": {
                        this.print(NutsPrintColors.BLUE, text);
                        break;
                    }
                    case "[[[": {
                        this.print(NutsPrintColors.PURPLE, "[" + text + "]");
                        break;
                    }
                    case "[[": {
                        this.print(NutsPrintColors.PURPLE, text);
                        break;
                    }
                    case "<<<": {
                        this.print(NutsPrintColors.RED, "<" + text + ">");
                        break;
                    }
                    case "<<": {
                        this.print(NutsPrintColors.RED, text);
                        break;
                    }
                    case "(((": {
                        this.print(NutsPrintColors.LIGHT_CYAN, "(" + text + ")");
                        break;
                    }
                    case "((": {
                        this.print(NutsPrintColors.LIGHT_CYAN, text);
                        break;
                    }
                    case "'": {
                        this.print(NutsPrintColors.LIGHT_GREEN, text);
                        break;
                    }
                    case "\"": {
                        this.print(NutsPrintColors.LIGHT_GREEN, text);
                        break;
                    }
                    case "\"\"": {
                        this.print(NutsPrintColors.GREEN, text);
                        break;
                    }
                    case "\"\"\"": {
                        this.print(NutsPrintColors.GREEN, "\"" + text + "\"");
                        break;
                    }
                    case "===": {
                        this.print(NutsPrintColors.DARK_GRAY, text);
                        break;
                    }
                    case "==": {
                        this.print(NutsPrintColors.LIGHT_GRAY, text);
                        break;
                    }
                    case "{{{": {
                        this.print(NutsPrintColors.YELLOW, "{" + text + "}");
                        break;
                    }
                    case "{{": {
                        this.print(NutsPrintColors.YELLOW, text);
                        break;
                    }
                    case "***": {
                        this.print(NutsPrintColors.BLUE, "*" + text + "*");
                        break;
                    }
                    case "**": {
                        this.print(NutsPrintColors.BLUE, text);
                        break;
                    }
                    case "@@@": {
                        this.print(NutsPrintColors.CYAN, "@" + text + "@");
                        break;
                    }
                    case "@@": {
                        this.print(NutsPrintColors.CYAN, text);
                        break;
                    }
                    default: {
                        this.print(text);
                        break;
                    }
                }
            }
        }
    }

//    public void draw(String text) {
//        if (text == null) {
//            text = "<<<null>>>";
//        }
//        String delim = " \n,;()";
//        StringTokenizer st = new StringTokenizer(text, delim, true);
//        this.print(DefaultNutsPrintStream.BLUE, "");
//        while (st.hasMoreElements()) {
//            String o = st.nextToken();
//            if (delim.indexOf(o.charAt(0)) >= 0) {
//                this.append(o);
//
//            } else if (isSeparatedBy(o, "[[[", "]]]")) {
//                this.print(DefaultNutsPrintStream.PURPLE, "[" + removeSeparators(o, "[[[", "]]]") + "]");
//            } else if (isSeparatedBy(o, "[[", "]]")) {
//                this.print(DefaultNutsPrintStream.PURPLE, removeSeparators(o, "[[", "]]"));
//            } else if (isSeparatedBy(o, "[", "]")) {
//                this.print(DefaultNutsPrintStream.LIGHT_PURPLE, removeSeparators(o, "[", "]"));
//
//            } else if (isSeparatedBy(o, "<<<", ">>>")) {
//                this.print(DefaultNutsPrintStream.RED, "<" + removeSeparators(o, "<<<", ">>>") + ">");
//            } else if (isSeparatedBy(o, "<<", ">>")) {
//                this.print(DefaultNutsPrintStream.RED, removeSeparators(o, "<<", ">>"));
//            } else if (isSeparatedBy(o, "<", ">")) {
//                this.print(DefaultNutsPrintStream.LIGHT_RED, removeSeparators(o, "<", ">"));
//
//            } else if (isSeparatedBy(o, "'", "'")) {
//                this.print(DefaultNutsPrintStream.LIGHT_GREEN, removeSeparators(o, "/", "/"));
//
//            } else if (isSeparatedBy(o, "\"\"", "\"\"")) {
//                this.print(DefaultNutsPrintStream.GREEN, removeSeparators(o, "\"\"", "\"\""));
//
//            } else if (isSeparatedBy(o, "\"", "\"")) {
//                this.print(DefaultNutsPrintStream.LIGHT_GREEN, removeSeparators(o, "/", "/"));
//
//            } else if (isSeparatedBy(o, "===", "===")) {
//                this.print(DefaultNutsPrintStream.DARK_GRAY, removeSeparators(o, "===", "==="));
//            } else if (isSeparatedBy(o, "==", "==")) {
//                this.print(DefaultNutsPrintStream.DARK_GRAY, removeSeparators(o, "==", "=="));
//            } else if (isSeparatedBy(o, "=", "=")) {
//                this.print(DefaultNutsPrintStream.BROWN, removeSeparators(o, "=", "="));
//            } else {
//                this.print(o);
//            }
//        }
//    }
    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT + 1;
    }
}
