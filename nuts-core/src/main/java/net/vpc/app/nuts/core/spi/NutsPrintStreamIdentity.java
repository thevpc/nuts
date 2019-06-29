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
 * Copyright (C) 2016-2019 Taha BEN SALAH
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
package net.vpc.app.nuts.core.spi;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import net.vpc.app.nuts.NutsTerminalMode;

/**
 *
 * @author vpc
 */
public class NutsPrintStreamIdentity extends PrintStream
        implements NutsPrintStreamExt {
    private final OutputStream base;
    private PrintStream ps;
    public NutsPrintStreamIdentity(OutputStream out) {
        super(out);
        this.base=out;
    }

    public NutsPrintStreamIdentity(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
        this.base=out;
    }

    public NutsPrintStreamIdentity(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
        this.base=out;
    }

    @Override
    public NutsTerminalMode getMode() {
        return NutsTerminalMode.INHERITED;
    }

    @Override
    public NutsTerminalMode getBaseMode() {
        return NutsTerminalMode.INHERITED;
    }

    @Override
    public PrintStream basePrintStream() {
        OutputStream b = baseOutputStream();
        if (b instanceof PrintStream) {
            return (PrintStream) b;
        }
        if (ps == null) {
            ps = new PrintStream(b);
        }
        return ps;
    }

    @Override
    public OutputStream baseOutputStream() {
        return base;
    }
    
}
