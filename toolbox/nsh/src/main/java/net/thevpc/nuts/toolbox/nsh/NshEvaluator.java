/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.toolbox.nsh.bundles.jshell.*;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.util.JavaShellNonBlockingInputStream;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.util.JavaShellNonBlockingInputStreamAdapter;
import net.thevpc.nuts.NutsPrintStream;
import net.thevpc.nuts.NutsSessionTerminal;
import net.thevpc.nuts.NutsTerminalMode;

import java.io.*;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class NshEvaluator extends DefaultJShellEvaluator {

    @Override
    public int evalBinaryPipeOperation(JShellCommandNode left, JShellCommandNode right, JShellFileContext context) {
        final NutsPrintStream nout;
        final PipedOutputStream out;
        final PipedInputStream in;
        final JavaShellNonBlockingInputStream in2;
        NutsShellContext ncontext = (NutsShellContext) (context.getShellContext());
        try {
            out = new PipedOutputStream();
            nout = ncontext.getWorkspace().io().createPrintStream(out, NutsTerminalMode.FORMATTED);
            in = new PipedInputStream(out, 1024);
            in2 = (in instanceof JavaShellNonBlockingInputStream) ? (JavaShellNonBlockingInputStream) in : new JavaShellNonBlockingInputStreamAdapter("jpipe-" + right.toString(), in);
        } catch (IOException ex) {
            throw new JShellException(1, ex);
        }
        final JShellFileContext leftContext = context.getShell().createNewContext(context).setOut(nout.asPrintStream());
        final JShellUniformException[] a = new JShellUniformException[2];
        Thread j1 = new Thread() {
            @Override
            public void run() {
                try {
                    context.getShell().uniformException(new JShellNodeUnsafeRunnable(left, leftContext));
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
        JShellFileContext rightContext = context.getShell().createNewContext(context).setIn((InputStream) in2);
        try {
            context.getShell().uniformException(new JShellNodeUnsafeRunnable(right, rightContext));
        } catch (JShellUniformException e) {
            if (e.isQuit()) {
                e.throwQuit();
                return 0;
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
        return 0;
    }

    @Override
    public String evalCommandAndReturnString(JShellCommandNode command, JShellFileContext context) {
        JShellFileContext c1 = context.getShell().createNewContext(context);
        DefaultNutsShellContext c2 = (DefaultNutsShellContext) c1.getShellContext();
        c2.setSession(c2.getSession().copy());
        c2.getSession().setLogLevel(Level.OFF);
        c2.getSession().setTrace(false);

        NutsPrintStream out = c2.getWorkspace().io().createMemoryPrintStream();

        NutsSessionTerminal terminal = c2.getWorkspace().term().createTerminal(new ByteArrayInputStream(new byte[0]), out, out);
        c2.getSession().setTerminal(terminal);
        try {
            command.eval(c1);
        } catch (Exception ex) {
            terminal.getErr().println(ex);
        }
        String str = evalFieldSubstitutionAfterCommandSubstitution(out.toString(), context);
        return (context.getShell().escapeString(str));
    }
}
