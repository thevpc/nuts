/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import net.vpc.app.nuts.util.EnhancedTextParser;
import net.vpc.app.nuts.util.IOUtils;

import java.awt.*;
import java.io.*;
import java.util.Locale;

/**
 * Created by vpc on 2/20/17.
 */
public class NutsPrintStream extends PrintStream implements NutsComponent<Object> {

    public NutsPrintStream() {
        super(IOUtils.NULL_OUTPUT_STREAM);
    }

    public NutsPrintStream(OutputStream out) {
        super(out);
    }

    public NutsPrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public NutsPrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
    }

    public NutsPrintStream(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public NutsPrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public NutsPrintStream(File file) throws FileNotFoundException {
        super(file);
    }

    public NutsPrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    public Color getColor() {
        return null;
    }

    public NutsPrintStream setColor(Color color) {
        return this;
    }

    public NutsPrintStream setEnableColoring(boolean enableColoring) {
        return this;
    }

    protected void applyColor(Color color) {
    }

    private void doWithColor(Color col, Runnable runnable) {
        runnable.run();
    }

    public NutsPrintStream write(Color c, int b) {
        super.write(b);
        return this;
    }

    public NutsPrintStream write(Color c, byte[] buf, int off, int len) {
        super.write(buf, off, len);
        return this;
    }

    public NutsPrintStream print(Color c, boolean b) {
        super.print(b);
        return this;
    }

    public NutsPrintStream print(Color color, char c) {
        super.print(c);
        return this;
    }

    public NutsPrintStream print(Color c, int i) {
        super.print(i);
        return this;
    }

    public NutsPrintStream print(Color c, long l) {
        super.print(l);
        return this;
    }

    public NutsPrintStream print(Color c, float f) {
        super.print(f);
        return this;
    }

    public NutsPrintStream print(Color c, double d) {
        super.print(d);
        return this;
    }

    public NutsPrintStream print(Color c, char[] s) {
        super.print(s);
        return this;
    }

    public NutsPrintStream print(Color c, final String s) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                NutsPrintStream.super.print(s);
            }
        });
        return this;
    }

    public NutsPrintStream print(Color c, Object obj) {
        super.print(obj);
        return this;
    }

    public NutsPrintStream println(Color c, boolean x) {
        super.println(x);
        return this;
    }

    public NutsPrintStream println(Color c, char x) {
        super.println(x);
        return this;
    }

    public NutsPrintStream println(Color c, final int x) {
        doWithColor(c, new Runnable() {
            @Override
            public void run() {
                NutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public NutsPrintStream println(Color c, long x) {
        super.println(x);
        return this;
    }

    public void println(Color c, float x) {
        super.println(x);
    }

    public NutsPrintStream println(Color c, double x) {
        super.println(x);
        return this;
    }

    public NutsPrintStream println(Color c, char[] x) {
        super.println(x);
        return this;
    }

    public NutsPrintStream println(Color c, String x) {
        super.println(x);
        return this;
    }

    public NutsPrintStream println(Color c, Object x) {
        super.println(x);
        return this;
    }

    public PrintStream printf(Color c, String format, Object... args) {
        super.printf(format, args);
        return this;
    }

    public NutsPrintStream printf(Color c, Locale l, String format, Object... args) {
        super.printf(l, format, args);
        return this;
    }

    public NutsPrintStream format(Color c, String format, Object... args) {
        super.format(format, args);
        return this;
    }

    public NutsPrintStream format(Color c, Locale l, String format, Object... args) {
        super.format(l, format, args);
        return this;
    }

    public NutsPrintStream append(Color c, CharSequence csq) {
        super.append(csq);
        return this;
    }

    public NutsPrintStream append(Color c, CharSequence csq, int start, int end) {
        super.append(csq, start, end);
        return this;
    }

    public NutsPrintStream append(Color color, char c) {
        super.append(c);
        return this;
    }

    @Override
    public NutsPrintStream printf(String format, Object... args) {
        NutsPrintStream.super.printf(format, args);
        return this;
    }

    @Override
    public NutsPrintStream printf(Locale l, String format, Object... args) {
        NutsPrintStream.super.printf(l, format, args);
        return this;
    }

    @Override
    public NutsPrintStream format(String format, Object... args) {
        NutsPrintStream.super.format(format, args);
        return this;
    }

    @Override
    public NutsPrintStream format(Locale l, String format, Object... args) {
        NutsPrintStream.super.format(l, format, args);
        return this;
    }

    @Override
    public NutsPrintStream append(CharSequence csq) {
        NutsPrintStream.super.append(csq);
        return this;
    }

    @Override
    public NutsPrintStream append(CharSequence csq, int start, int end) {
        NutsPrintStream.super.append(csq, start, end);
        return this;
    }

    @Override
    public NutsPrintStream append(char c) {
        NutsPrintStream.super.append(c);
        return this;
    }

    public void writeRaw(String s) {
        super.print(s);
    }

    public void drawln(String text) {
        draw(text);
        println();
    }

    public void draw(String text) {
        if (text == null) {
            text = "<<<null>>>";
        }
        for (EnhancedTextChunck e : EnhancedTextParser.INSTANCE.parse(text)) {
            draw(e);
        }
    }

    public void draw(EnhancedTextChunck chunk) {
        if (chunk == null) {
            chunk = EnhancedTextChunck.NULL;
        }
        String s = chunk.getValue();
        if (s == null) {
            super.print("");
        } else {
            super.print(s);
        }
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return BOOT_SUPPORT;
    }
}
