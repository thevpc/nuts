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

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import net.vpc.app.nuts.NutsTerminalMode;
import net.vpc.common.javashell.DefaultJShellNodeEvaluator;
import net.vpc.common.javashell.JShellContext;
import net.vpc.common.javashell.JShellException;
import net.vpc.common.javashell.JShellNodeEvaluator;
import net.vpc.common.javashell.JShellUniformException;
import net.vpc.common.javashell.NodeEvalUnsafeRunnable;
import net.vpc.common.javashell.parser.nodes.InstructionNode;
import net.vpc.common.javashell.util.JavaShellNonBlockingInputStream;
import net.vpc.common.javashell.util.JavaShellNonBlockingInputStreamAdapter;

/**
 *
 * @author vpc
 */
public class NutsNodeEvaluator extends DefaultJShellNodeEvaluator implements JShellNodeEvaluator {

        @Override
        public void evalBinaryPipeOperation(InstructionNode left, InstructionNode right, JShellContext context) {
            final PrintStream nout;
            final PipedOutputStream out;
            final PipedInputStream in;
            final JavaShellNonBlockingInputStream in2;
            NutsShellContext ncontext = (NutsShellContext) context;
            try {
                out = new PipedOutputStream();
                nout = ncontext.getWorkspace().io().createPrintStream(out, NutsTerminalMode.FORMATTED);
                in = new PipedInputStream(out, 1024);
                in2 = (in instanceof JavaShellNonBlockingInputStream) ? (JavaShellNonBlockingInputStream) in : new JavaShellNonBlockingInputStreamAdapter("jpipe-" + right.toString(), in);
            } catch (IOException ex) {
                throw new JShellException(1, ex);
            }
            final JShellContext leftContext = context.getShell().createContext(context).setOut(nout);
            final JShellUniformException[] a = new JShellUniformException[2];
            Thread j1 = new Thread() {
                @Override
                public void run() {
                    try {
                        context.getShell().uniformException(new NodeEvalUnsafeRunnable(left, leftContext));
                    } catch (JShellUniformException e) {
                        if (e.isQuit()) {
                            e.throwQuit();
                            return;
                        }
                        a[0] = e;
                    }
                    in2.noMoreBytes();
                }

            };
            j1.start();
            JShellContext rightContext = context.getShell().createContext(context).setIn((InputStream) in2);
            try {
                context.getShell().uniformException(new NodeEvalUnsafeRunnable(right, rightContext));
            } catch (JShellUniformException e) {
                if (e.isQuit()) {
                    e.throwQuit();
                    return;
                }
                a[1] = e;
            }
            try {
                j1.join();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (a[1] != null) {
                a[1].throwAny();
            }
        }
    }
