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
package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.common.javashell.JavaShell;
import net.vpc.common.javashell.cmds.JavaShellInternalCmd;
import net.vpc.common.javashell.Env;
import net.vpc.common.javashell.JavaShellEvalContext;
import net.vpc.common.javashell.parser.nodes.BinoOp;
import net.vpc.common.javashell.parser.nodes.InstructionNode;
import net.vpc.common.javashell.parser.nodes.Node;
import net.vpc.common.javashell.util.JavaShellNonBlockingInputStream;
import net.vpc.common.javashell.util.JavaShellNonBlockingInputStreamAdapter;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NutsJavaShell extends JavaShell {

    private DefaultNutsConsole component;
    private NutsWorkspace workspace;

    public NutsJavaShell(DefaultNutsConsole component, NutsWorkspace workspace) {
        this.component = component;
        this.workspace = workspace;
        super.setCwd(workspace.getConfigManager().getCwd());
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
    public boolean containsCommand(String cmd) {
        NutsCommand ncommand = null;
        try {
            ncommand = component.getCommand(cmd);
            return ncommand != null;
        } catch (Exception ex) {
            //ignore
        }
        return false;
    }

    @Override
    public void declareCmd(JavaShellInternalCmd cmd) {
        component.installCommand(cmd == null ? null : new ShellToNutsCommand(cmd));
    }

    @Override
    public void undeclareCommand(String name) {
        component.uninstallCommand(name);
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
        component.getContext().getTerminal().getFormattedErr().printf("@@@%s@@@\n", message);
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
        workspace.getConfigManager().setCwd(cwd);
    }


    protected int evalBinaryPipeOperation(InstructionNode left, InstructionNode right, JavaShellEvalContext context) {
        final NutsPrintStream nout;
        final PipedOutputStream out;
        final PipedInputStream in;
        final JavaShellNonBlockingInputStream in2;
        try {
            out = new PipedOutputStream();
            nout = workspace.getExtensionManager().createPrintStream(out, false);
            in = new PipedInputStream(out, 1024);
            in2 = (in instanceof JavaShellNonBlockingInputStream) ? (JavaShellNonBlockingInputStream) in : new JavaShellNonBlockingInputStreamAdapter("jpipe-" + right.toString(), in);
        } catch (IOException ex) {
            Logger.getLogger(BinoOp.class.getName()).log(Level.SEVERE, null, ex);
            return 1;
        }
        final Integer[] a = new Integer[2];
        Thread j1 = new Thread() {
            @Override
            public void run() {
                a[0] = left.eval(context.getShell().createContext(context).setOut(nout));
                in2.noMoreBytes();
            }

        };
        j1.start();
        JavaShellEvalContext rightContext = context.getShell().createContext(context).setIn((InputStream) in2);
        a[1] = right.eval(rightContext);
        try {
            j1.join();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return a[1];
    }

}
