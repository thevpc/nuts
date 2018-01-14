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
package net.vpc.app.nuts.extensions.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

public class NullPrintStream extends PrintStream {
    public static final PrintStream INSTANCE=new NullPrintStream();
    private NullPrintStream() {
        super(NullOutputStream.INSTANCE);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean checkError() {
        return false;
    }

    @Override
    public void write(int b) {

    }

    @Override
    public void write(byte[] buf, int off, int len) {
    }

    @Override
    public void print(boolean b) {
    }

    @Override
    public void print(char c) {
    }

    @Override
    public void print(int i) {
    }

    @Override
    public void print(long l) {
    }

    @Override
    public void print(float f) {
    }

    @Override
    public void print(double d) {
    }

    @Override
    public void print(char[] s) {
    }

    @Override
    public void print(String s) {
    }

    @Override
    public void print(Object obj) {
    }

    @Override
    public void println() {
    }

    @Override
    public void println(boolean x) {
    }

    @Override
    public void println(char x) {
    }

    @Override
    public void println(int x) {
    }

    @Override
    public void println(long x) {
    }

    @Override
    public void println(float x) {
    }

    @Override
    public void println(double x) {
    }

    @Override
    public void println(char[] x) {
    }

    @Override
    public void println(String x) {
    }

    @Override
    public void println(Object x) {
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        return this;
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        return this;
    }

    @Override
    public PrintStream append(CharSequence csq) {
        return this;
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        return this;
    }

    @Override
    public PrintStream append(char c) {
        return this;
    }
}
