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
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.apps.javashell.JavaShell;
import net.vpc.apps.javashell.cmds.JavaShellInternalCmd;
import net.vpc.apps.javashell.parser.Env;
import net.vpc.apps.javashell.parser.JavaShellEvalContext;
import net.vpc.apps.javashell.parser.nodes.Node;

import java.io.File;

public class NutsJavaShell extends JavaShell {
    private DefaultNutsConsole component;
    private NutsWorkspace workspace;

    public NutsJavaShell(DefaultNutsConsole component, NutsWorkspace workspace) {
        this.component = component;
        this.workspace = workspace;
        super.setCwd(workspace.getCwd().getPath());
    }

    @Override
    public JavaShellInternalCmd getInternalCommand(String n) {
        NutsCommand ncommand = null;
        try {
            ncommand = component.getCommand(n);
        } catch (Exception ex) {
            return null;
        }
        return new NutsShellInternalCmd(ncommand, component);
    }

    @Override
    public int execExternalCommand(String[] command, JavaShellEvalContext context) {
        try {
            JavaShellInternalCmd exec = getInternalCommand("exec");
            return exec.exec(command, context);
        } catch (Exception ex) {
            return onResult(1, ex);
        }
    }

    @Override
    public JavaShellEvalContext createContext(Node root, Node parent, Env env, String[] args) {
        return createContext(component.getContext(), root, parent, env, args);
    }

    @Override
    public JavaShellEvalContext createContext(JavaShellEvalContext parentContext) {
        return new NutsJavaShellEvalContext(parentContext);
    }

    public JavaShellEvalContext createContext(NutsCommandContext commandContext, Node root, Node parent, Env env, String[] args) {
        return new NutsJavaShellEvalContext(this, args, root, parent, commandContext, workspace, env);
    }

    @Override
    public String errorToMessage(Throwable th) {
        return CoreStringUtils.exceptionToString(th);
    }

    @Override
    public void onErrorImpl(String message, Throwable th) {
        component.getContext().getTerminal().getErr().println(message);
    }

    @Override
    public String which(String path0, JavaShellEvalContext context) {
        if (!path0.startsWith("/")) {
            return getCwd() + "/" + path0;
        }
        return path0;
    }

    @Override
    public void setCwd(String cwd) {
        super.setCwd(cwd);
        workspace.setCwd(new File(cwd));
    }
}
