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
package net.vpc.app.nuts.extensions.terminals;

import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.NutsTerminalBase;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.*;

public class NutsTerminalDelegate extends AbstractNutsTerminal {

    private NutsTerminalBase base;
    private boolean installBase;

    public NutsTerminalDelegate(NutsTerminalBase base, boolean installBase) {
        this(base, null, null, null,installBase);
    }

    public NutsTerminalDelegate(NutsTerminalBase base, InputStream inReplace, PrintStream outReplace, PrintStream errReplace, boolean installBase) {
        if (base instanceof NutsTerminalDelegate) {
            base = ((NutsTerminalDelegate) base).base;
        }
        this.base = base;
        if (inReplace != null) {
            this.inReplace = inReplace;
            this.inReplaceReader = new BufferedReader(new InputStreamReader(inReplace));
        }
        this.outReplace = outReplace;
        this.errReplace = errReplace;
        this.installBase = installBase;
    }

    public NutsTerminal copy(){
        NutsTerminalDelegate d = new NutsTerminalDelegate(base, false);
        d.copyFrom(this);
        d.installBase=installBase;
        return d;
    }

    @Override
    public void install(NutsWorkspace workspace, InputStream in, PrintStream out, PrintStream err) {
        super.install(workspace, in, out, err);
        if (installBase) {
            base.install(workspace, in, out, err);
        }
    }

    @Override
    public String readLine(String promptFormat, Object[] params) {
        if (isInOverridden()) {
            super.readLine(promptFormat, params);
        }
        return base.readLine(promptFormat, params);
    }

    @Override
    public String readPassword(String prompt) {
        if (isInOverridden()) {
            super.readPassword(prompt);
        }
        return base.readPassword(prompt);
    }

    @Override
    public InputStream getIn() {
        if (isInOverridden()) {
            return super.getIn();
        }
        return base.getIn();
    }

    @Override
    public PrintStream getOut() {
        if (isOutOverridden()) {
            return super.getOut();
        }
        return base.getOut();
    }

    @Override
    public PrintStream getErr() {
        if (isErrOverridden()) {
            return super.getErr();
        }
        return base.getErr();
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return base.getSupportLevel(criteria);
    }
}
