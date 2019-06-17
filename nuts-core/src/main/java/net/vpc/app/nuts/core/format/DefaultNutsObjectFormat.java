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
package net.vpc.app.nuts.core.format;

import java.io.File;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsObjectFormat;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.format.json.NutsObjectFormatJson;
import net.vpc.app.nuts.core.format.plain.NutsObjectFormatPlain;
import net.vpc.app.nuts.core.format.props.NutsObjectFormatProps;
import net.vpc.app.nuts.core.format.table.NutsObjectFormatTable;
import net.vpc.app.nuts.core.format.tree.NutsObjectFormatTree;
import net.vpc.app.nuts.core.format.xml.NutsObjectFormatXml;

/**
 *
 * @author vpc
 */
public class DefaultNutsObjectFormat extends NutsObjectFormatBase {

    private NutsOutputFormat outputFormat;
    private NutsObjectFormat base;

    public DefaultNutsObjectFormat(NutsWorkspace ws) {
        super(ws, "object-format");
    }

    @Override
    public NutsObjectFormat setSession(NutsSession session) {
        super.setSession(session);
        if (base != null) {
            base.setSession(getValidSession());
        }
        return this;
    }

    @Override
    public NutsObjectFormat setValue(Object value) {
        super.setValue(value);
        if (base != null) {
            base.setValue(value);
        }
        return this;
    }

    public NutsOutputFormat getOutputFormat() {
        NutsOutputFormat t = getValidSession().getOutputFormat();
        return t == null ? NutsOutputFormat.PLAIN : t;
    }

    public NutsObjectFormat getBase() {
        if (base == null || outputFormat != getOutputFormat()) {
            base = createObjectFormat();
            base.setValue(getValue());
            base.setSession(getValidSession());
        }
        return base;
    }

    public NutsObjectFormat createObjectFormat() {
        switch (getOutputFormat()) {
            case JSON: {
                return new NutsObjectFormatJson(ws);
            }
            case PROPS: {
                return new NutsObjectFormatProps(ws);
            }
            case TREE: {
                return new NutsObjectFormatTree(ws);
            }
            case PLAIN: {
                return new NutsObjectFormatPlain(ws);
            }
            case XML: {
                return new NutsObjectFormatXml(ws);
            }
            case TABLE: {
                return new NutsObjectFormatTable(ws);
            }
        }
        throw new NutsException(ws, "Unsupported");
    }

//    @Override
//    public NutsTerminalFormat getTerminalFormat() {
//        return base!=null?base.getTerminalFormat():terminalFormat;
//    }
    @Override
    public NutsSession getSession() {
        return base != null ? base.getSession() : super.getSession();
    }

    @Override
    public String format() {
        return getBase().format();
    }

    @Override
    public void print(PrintStream out) {
        getBase().print(out);
    }

    @Override
    public void print(Writer out) {
        getBase().print(out);
    }

    @Override
    public void print(Path out) {
        getBase().print(out);
    }

    @Override
    public void print(File out) {
        getBase().print(out);
    }

    @Override
    public void print() {
        getBase().print();
    }

    @Override
    public void print(NutsTerminal terminal) {
        getBase().print(terminal);
    }

    @Override
    public void println(Writer w) {
        getBase().println(w);
    }

    @Override
    public void println(PrintStream out) {
        getBase().println(out);
    }

    @Override
    public void println(Path path) {
        getBase().println(path);
    }

    @Override
    public void println() {
        getBase().println();
    }

    @Override
    public void println(NutsTerminal terminal) {
        getBase().println(terminal);
    }

    @Override
    public void println(File file) {
        getBase().println(file);
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return getBase().configureFirst(commandLine);
    }
}
