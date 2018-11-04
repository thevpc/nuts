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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class NutsTerminalDelegate extends AbstractNutsTerminal {

    private NutsTerminal base;
    private InputStream inReplace;
    private BufferedReader inReplaceReader;
    private NutsPrintStream outReplace;
    private NutsPrintStream errReplace;

    public NutsTerminalDelegate(NutsTerminal base, InputStream inReplace, NutsPrintStream outReplace, NutsPrintStream errReplace) {
        if (base instanceof NutsTerminalDelegate) {
            base = ((NutsTerminalDelegate) base).base;
        }
        this.base = base;
        InputStream in0 = base.getIn();
        if (inReplace != null && in0 != inReplace) {
            this.inReplace = inReplace;
            this.inReplaceReader = inReplace == null ? null : new BufferedReader(new InputStreamReader(inReplace));
        }
        this.outReplace = outReplace;
        this.errReplace = errReplace;
    }

    @Override
    public void install(NutsWorkspace workspace, InputStream in, NutsPrintStream out, NutsPrintStream err) {

    }

    @Override
    public String readLine(String prompt) {
        if (inReplaceReader != null) {
            getOut().print(prompt);
            try {
                return inReplaceReader.readLine();
            } catch (IOException e) {
                throw new NutsIOException(e);
            }
        }
        return base.readLine(prompt);
    }

    @Override
    public String readPassword(String prompt) {
        if (inReplaceReader != null) {
            getOut().print(prompt);
            try {
                return inReplaceReader.readLine();
            } catch (IOException e) {
                throw new NutsIOException(e);
            }
        }
        return base.readPassword(prompt);
    }

    @Override
    public InputStream getIn() {
        if (inReplace != null) {
            return inReplace;
        }
        return base.getIn();
    }

    @Override
    public void setIn(InputStream in) {
        inReplace = in;
    }

    @Override
    public NutsPrintStream getOut() {
        if (outReplace != null) {
            return outReplace;
        }
        return base.getOut();
    }

    @Override
    public void setOut(NutsPrintStream out) {
        outReplace = out;
    }

    @Override
    public NutsPrintStream getErr() {
        if (errReplace != null) {
            return errReplace;
        }
        return base.getErr();
    }

    @Override
    public void setErr(NutsPrintStream err) {
        errReplace = err;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return base.getSupportLevel(criteria);
    }
}
