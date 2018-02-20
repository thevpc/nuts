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

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;

import java.io.*;

/**
 * Created by vpc on 2/20/17.
 */
public class DefaultNutsTerminal implements NutsTerminal {

    private BufferedReader reader;
    private InputStream in;
    private NutsPrintStream out;
    private NutsPrintStream err;
    private NutsPrintStream outReplace;
    private NutsPrintStream errReplace;
    private InputStream inReplace;
    private BufferedReader inReplaceReader;

    public DefaultNutsTerminal() {
    }

    @Override
    public void install(NutsWorkspace workspace, InputStream in, NutsPrintStream out, NutsPrintStream err) {
        this.in = in == null ? System.in : in;
        this.out = out == null ? (workspace == null ? new DefaultNutsPrintStream(System.out) : workspace.createEnhancedPrintStream(System.out)) : out;
        this.err = err == null ? (workspace == null ? new DefaultNutsPrintStream(System.err) : workspace.createEnhancedPrintStream(System.err)) : err;
        reader = new BufferedReader(new InputStreamReader(this.in));
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return BOOT_SUPPORT;
    }

    @Override
    public void setCommandContext(NutsCommandContext context) {

    }

    @Override
    public String readLine(String prompt) {
        getOut().print(prompt);
        getIn();
        if (inReplaceReader != null) {
            try {
                return inReplaceReader.readLine();
            } catch (IOException e) {
                throw new NutsIOException(e);
            }
        }
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
    }

    @Override
    public String readPassword(String prompt) {
        return CoreIOUtils.readPassword(prompt, getIn(), getOut());
    }

    public BufferedReader getReader() {
        if (inReplaceReader != null) {
            return inReplaceReader;
        }
        return reader;
    }

    @Override
    public InputStream getIn() {
        if (inReplace != null) {
            return inReplace;
        }
        return in;
    }

    @Override
    public NutsPrintStream getOut() {
        if (outReplace != null) {
            return outReplace;
        }
        return out;
    }

    @Override
    public NutsPrintStream getErr() {
        if (errReplace != null) {
            return errReplace;
        }
        return err;
    }

    @Override
    public void setOut(NutsPrintStream out) {
        outReplace = out;
    }

    @Override
    public void setIn(InputStream in) {
        inReplace = in;
        if (in == null) {
            inReplaceReader = null;
        } else {
            inReplaceReader = new BufferedReader(new InputStreamReader(inReplace));
        }
    }

    @Override
    public void setErr(NutsPrintStream err) {
        errReplace = err;
    }

}
