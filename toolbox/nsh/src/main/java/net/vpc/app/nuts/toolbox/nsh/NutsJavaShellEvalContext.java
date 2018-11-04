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
package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.NutsFormattedPrintStream;
import net.vpc.app.nuts.NutsNonFormattedPrintStream;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.javashell.DefaultJavaShellEvalContext;
import net.vpc.common.javashell.Env;
import net.vpc.common.javashell.JavaShellEvalContext;
import net.vpc.common.javashell.parser.nodes.Node;

import java.io.InputStream;
import java.io.OutputStream;

class NutsJavaShellEvalContext extends DefaultJavaShellEvalContext {

    private NutsCommandContext commandContext;
    private NutsWorkspace workspace;

    public NutsJavaShellEvalContext(JavaShellEvalContext parentContext) {
        super(parentContext);
        if (parentContext instanceof NutsJavaShellEvalContext) {
            NutsJavaShellEvalContext parentContext1 = (NutsJavaShellEvalContext) parentContext;
            this.commandContext = parentContext1.commandContext.copy();
            this.workspace = parentContext1.workspace;
            this.commandContext.getUserProperties().put(JavaShellEvalContext.class.getName(), this);
        }
    }

    public NutsJavaShellEvalContext(NutsJavaShell shell, String[] args, Node root, Node parent, NutsCommandContext commandContext, NutsWorkspace workspace, Env env) {
        super(shell, env, root, parent, null, null, null, args);
        this.commandContext = commandContext;//.copy();
        this.workspace = workspace;
    }



    public NutsCommandContext getCommandContext() {
        return commandContext;
    }

    @Override
    public InputStream getIn() {
        return commandContext.getTerminal().getIn();
    }

    @Override
    public OutputStream getOut() {
        return commandContext.getTerminal().getOut();
    }

    @Override
    public OutputStream getErr() {
        return commandContext.getTerminal().getErr();
    }

    @Override
    public JavaShellEvalContext setOut(OutputStream out) {
        boolean formatted=true;
        if(out instanceof NutsNonFormattedPrintStream){
            formatted=false;
        }
        if(out instanceof NutsFormattedPrintStream){
            formatted=true;
        }
        commandContext.getTerminal().setOut(workspace.getExtensionManager().createPrintStream(out,formatted));
        return this;
    }

    @Override
    public JavaShellEvalContext setIn(InputStream in) {
        commandContext.getTerminal().setIn(in);
        return this;
    }
}
