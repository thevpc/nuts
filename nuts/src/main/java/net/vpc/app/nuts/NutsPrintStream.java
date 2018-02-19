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
package net.vpc.app.nuts;

import java.io.*;
import java.util.Locale;

/**
 * Created by vpc on 2/20/17.
 */
public class NutsPrintStream extends PrintStream implements NutsComponent<Object> {

    private boolean formatEnabled = true;

    public NutsPrintStream() {
        super(NullOutputStream.INSTANCE);
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

    protected void startFormat(NutsTextFormat format) {
    }

    protected void endFormat(NutsTextFormat format) {
    }

    private void doWithFormat(NutsTextFormat format, Runnable runnable) {
        runnable.run();
    }

    public NutsPrintStream write(NutsTextFormat c, int b) {
        super.write(b);
        return this;
    }

    public NutsPrintStream write(NutsTextFormat c, byte[] buf, int off, int len) {
        super.write(buf, off, len);
        return this;
    }

    public NutsPrintStream print(NutsTextFormat c, boolean b) {
        super.print(b);
        return this;
    }

    public NutsPrintStream print(NutsTextFormat format, char c) {
        super.print(c);
        return this;
    }

    public NutsPrintStream print(NutsTextFormat c, int i) {
        super.print(i);
        return this;
    }

    public NutsPrintStream print(NutsTextFormat c, long l) {
        super.print(l);
        return this;
    }

    public NutsPrintStream print(NutsTextFormat c, float f) {
        super.print(f);
        return this;
    }

    public NutsPrintStream print(NutsTextFormat c, double d) {
        super.print(d);
        return this;
    }

    public NutsPrintStream print(NutsTextFormat c, char[] s) {
        super.print(s);
        return this;
    }

    public NutsPrintStream print(NutsTextFormat c, final String s) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                NutsPrintStream.super.print(s);
            }
        });
        return this;
    }

    public NutsPrintStream print(NutsTextFormat c, Object obj) {
        super.print(obj);
        return this;
    }

    public NutsPrintStream println(NutsTextFormat c, boolean x) {
        super.println(x);
        return this;
    }

    public NutsPrintStream println(NutsTextFormat c, char x) {
        super.println(x);
        return this;
    }

    public NutsPrintStream println(NutsTextFormat c, final int x) {
        doWithFormat(c, new Runnable() {
            @Override
            public void run() {
                NutsPrintStream.super.println(x);
            }
        });
        return this;
    }

    public NutsPrintStream println(NutsTextFormat c, long x) {
        super.println(x);
        return this;
    }

    public void println(NutsTextFormat c, float x) {
        super.println(x);
    }

    public NutsPrintStream println(NutsTextFormat c, double x) {
        super.println(x);
        return this;
    }

    public NutsPrintStream println(NutsTextFormat c, char[] x) {
        super.println(x);
        return this;
    }

    public NutsPrintStream println(NutsTextFormat c, String x) {
        super.println(x);
        return this;
    }

    public NutsPrintStream println(NutsTextFormat c, Object x) {
        super.println(x);
        return this;
    }

    public PrintStream printf(NutsTextFormat c, String format, Object... args) {
        super.printf(format, args);
        return this;
    }

    public NutsPrintStream printf(NutsTextFormat c, Locale l, String format, Object... args) {
        super.printf(l, format, args);
        return this;
    }

    public NutsPrintStream format(NutsTextFormat c, String format, Object... args) {
        super.format(format, args);
        return this;
    }

    public NutsPrintStream format(NutsTextFormat c, Locale l, String format, Object... args) {
        super.format(l, format, args);
        return this;
    }

    public NutsPrintStream append(NutsTextFormat c, CharSequence csq) {
        super.append(csq);
        return this;
    }

    public NutsPrintStream append(NutsTextFormat c, CharSequence csq, int start, int end) {
        super.append(csq, start, end);
        return this;
    }

    public NutsPrintStream append(NutsTextFormat format, char c) {
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

    public void drawln(String text) {
        draw(text);
        println();
    }

    public void draw(String text) {
        print(text);
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return BOOT_SUPPORT;
    }

    public boolean isFormatEnabled() {
        return formatEnabled;
    }

    public void setFormatEnabled(boolean formatEnabled) {
        this.formatEnabled = formatEnabled;
    }

}
