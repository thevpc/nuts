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

import net.vpc.app.nuts.NutsPrintColors;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vpc on 2/20/17.
 */
public class NutsAnsiUnixTermPrintStream extends DefaultNutsPrintStream {

    public static final Color[] ALL_SUPPORTED_COLORS = new Color[]{
        NutsPrintColors.BLACK,
        NutsPrintColors.RED,
        NutsPrintColors.BLUE,
        NutsPrintColors.GREEN,
        NutsPrintColors.CYAN,
        NutsPrintColors.LIGHT_GRAY,
        NutsPrintColors.LIGHT_BLUE,
        NutsPrintColors.LIGHT_GREEN,
        NutsPrintColors.LIGHT_CYAN,
        NutsPrintColors.LIGHT_RED,
        NutsPrintColors.LIGHT_PURPLE,
        NutsPrintColors.DARK_GRAY,
        NutsPrintColors.PURPLE,
        NutsPrintColors.BROWN,
        NutsPrintColors.YELLOW,
        NutsPrintColors.WHITE
    };

    private Map<Color, String> escapes = new HashMap<>();

    {
        escapes.put(null, "\u001B[0m");
        escapes.put(NutsPrintColors.BLACK, "\u001B[0;30m");
        escapes.put(NutsPrintColors.BLUE, "\u001B[0;34m");
        escapes.put(NutsPrintColors.GREEN, "\u001B[0;32m");
        escapes.put(NutsPrintColors.CYAN, "\u001B[0;36m");
        escapes.put(NutsPrintColors.RED, "\u001B[0;31m");
        escapes.put(NutsPrintColors.PURPLE, "\u001B[0;35m");
        escapes.put(NutsPrintColors.BROWN, "\u001B[0;35m");
        escapes.put(NutsPrintColors.LIGHT_GRAY, "\u001B[0;37m");
        escapes.put(NutsPrintColors.DARK_GRAY, "\u001B[1;30m");
        escapes.put(NutsPrintColors.LIGHT_BLUE, "\u001B[1;34m");
        escapes.put(NutsPrintColors.LIGHT_GREEN, "\u001B[1;32m");
        escapes.put(NutsPrintColors.LIGHT_CYAN, "\u001B[1;36m");
        escapes.put(NutsPrintColors.LIGHT_RED, "\u001B[1;31m");
        escapes.put(NutsPrintColors.LIGHT_PURPLE, "\u001B[1;35m");
        escapes.put(NutsPrintColors.YELLOW, "\u001B[1;35m");
        escapes.put(NutsPrintColors.WHITE, "\u001B[1;35m");
    }

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
    protected void applyColor(Color color) {
        String s = escapes.get(color);
        if (s == null) {
            s = escapes.get(null);
        }
        if (s != null) {
            super.print(s);
        }
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT + 2;
    }

}
