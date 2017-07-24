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
package net.vpc.app.nuts.boot;

import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.util.IOUtils;

import java.io.*;
import net.vpc.app.nuts.NutsWorkspace;

/**
 * Created by vpc on 2/20/17.
 */
public class DefaultNutsTerminal implements NutsTerminal {

    private BufferedReader reader;
    private InputStream in ;
    private NutsPrintStream out ;
    private NutsPrintStream err;

    public DefaultNutsTerminal() {
    }

    @Override
    public void install(NutsWorkspace workspace, InputStream in, NutsPrintStream out, NutsPrintStream err) throws IOException {
        this.in = in == null ? System.in : in;
        this.out = out == null ? (workspace==null?new NutsPrintStream(System.out): workspace.createEnhancedPrintStream(System.out)) : out;
        this.err = err == null ? (workspace==null?new NutsPrintStream(System.err): workspace.createEnhancedPrintStream(System.err)) : err;
        reader = new BufferedReader(new InputStreamReader(this.in));
    }

    @Override
    public int getSupportLevel(Object criteria)  {
        return BOOT_SUPPORT;
    }



    @Override
    public void setCommandContext(NutsCommandContext context) {

    }

    @Override
    public String readLine(String prompt) throws IOException {
        out.print(prompt);
        return reader.readLine();
    }

    @Override
    public String readPassword(String prompt) {
        return IOUtils.readPassword(prompt, in, out);
    }

    @Override
    public InputStream getIn() {
        return in;
    }

    @Override
    public NutsPrintStream getOut() {
        return out;
    }

    @Override
    public NutsPrintStream getErr() {
        return err;
    }
}
