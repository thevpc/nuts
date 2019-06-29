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
import net.vpc.app.nuts.NutsTerminalMode;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.terminals.NutsPrintStreamFormattedUnixAnsi;
import net.vpc.app.nuts.core.util.NutsUnexpectedEnumException;
import net.vpc.app.nuts.core.util.fprint.FormattedPrintStream;

/**
 *
 * @author vpc
 */
public class NutsPrintStreamAdapterHelper {

    public static PrintStream convert(OutputStream out, NutsTerminalMode m, NutsWorkspace ws) {
        if (out == null) {
            return null;
        }
        if (out instanceof NutsPrintStreamExt) {
            NutsPrintStreamExt a = (NutsPrintStreamExt) out;
            NutsTerminalMode am = a.getMode();
            switch (m) {
                case FORMATTED: {
                    switch (am) {
                        case FORMATTED: {
                            return (PrintStream) a;
                        }
                        case FILTERED: {
                            return a.basePrintStream();
                        }
                        case INHERITED: {
                            return new NutsPrintStreamFormattedUnixAnsi(out);
                        }
                        default: {
                            throw new NutsUnexpectedEnumException(ws, am);
                        }
                    }
                }
                case FILTERED: {
                    switch (am) {
                        case FORMATTED: {
                            return (PrintStream) a;
                        }
                        case FILTERED: {
                            return a.basePrintStream();
                        }
                        case INHERITED: {
                            return new NutsPrintStreamFormattedUnixAnsi(out);
                        }
                        default: {
                            throw new NutsUnexpectedEnumException(ws, am);
                        }
                    }
                }
            }
        }
        if (out instanceof PrintStream) {
            while (out != null) {
                if (out instanceof NutsPrintStreamExt) {
                    PrintStream p = ((NutsPrintStreamExt) out).basePrintStream();
                    if (p == null || p == out) {
                        return (PrintStream) out;
                    }
                    out = p;
                } else {
                    return (PrintStream) out;
                }
            }
        } else {
            return new PrintStream(out);
        }
        return new PrintStream(out);
    }

    public static PrintStream compress(PrintStream out) {
        if (out == null) {
            return null;
        }
        if (out instanceof NutsPrintStreamExt) {
            NutsPrintStreamExt a = (NutsPrintStreamExt) out;
            PrintStream out2 = a.basePrintStream();
            if (out2 instanceof NutsPrintStreamExt) {
                NutsPrintStreamExt b = (NutsPrintStreamExt) out2;
                switch (a.getMode()) {
                    case FILTERED: {
                        switch (b.getMode()) {
                            case FORMATTED: {
                                return compress(b.basePrintStream());
                            }
                            case FILTERED: {
                                return compress(out2);
                            }
                            case INHERITED: {
                                return out;
                            }
                            default: {
                                throw new IllegalArgumentException("Unexpected " + b.getMode());
                            }
                        }
                    }
                    case FORMATTED: {
                        switch (b.getMode()) {
                            case FORMATTED: {
                                return compress(out2);
                            }
                            case FILTERED: {
                                return compress(b.basePrintStream());
                            }
                            case INHERITED: {
                                return out;
                            }
                            default: {
                                throw new IllegalArgumentException("Unexpected " + b.getMode());
                            }
                        }
                    }
                }
            }
        }
        if (out instanceof PrintStream) {
            while (out != null) {
                if (out instanceof NutsPrintStreamExt) {
                    PrintStream p = ((NutsPrintStreamExt) out).basePrintStream();
                    if (p == null || p == out) {
                        return (PrintStream) out;
                    }
                    out = p;
                } else {
                    return (PrintStream) out;
                }
            }
        } else {
            return new PrintStream(out);
        }
        return new PrintStream(out);
    }
}
