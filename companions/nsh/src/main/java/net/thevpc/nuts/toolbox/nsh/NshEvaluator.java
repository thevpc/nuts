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

import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.toolbox.nsh.jshell.*;
import net.thevpc.nuts.toolbox.nsh.jshell.util.JavaShellNonBlockingInputStream;
import net.thevpc.nuts.toolbox.nsh.jshell.util.JavaShellNonBlockingInputStreamAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class NshEvaluator extends DefaultJShellEvaluator {

    @Override
    public int evalBinaryPipeOperation(JShellCommandNode left, JShellCommandNode right, JShellContext context) {
        final NOutStream nout;
        final PipedOutputStream out;
        final PipedInputStream in;
        final JavaShellNonBlockingInputStream in2;
        try {
            out = new PipedOutputStream();
            nout = NOutStream.of(out, NTerminalMode.FORMATTED,null, context.getSession());
            in = new PipedInputStream(out, 1024);
            in2 = (in instanceof JavaShellNonBlockingInputStream) ? (JavaShellNonBlockingInputStream) in : new JavaShellNonBlockingInputStreamAdapter("jpipe-" + right.toString(), in);
        } catch (IOException ex) {
            throw new JShellException(context.getSession(), ex, 1);
        }
        final JShellContext leftContext = context.getShell().createNewContext(context).setOut(nout.asPrintStream());
        final JShellUniformException[] a = new JShellUniformException[2];
        Thread j1 = new Thread() {
            @Override
            public void run() {
                try {
                    context.getShell().evalNode(left, leftContext);
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
        JShellContext rightContext = context.getShell().createNewContext(context).setIn((InputStream) in2);
        try {
            context.getShell().evalNode(right, rightContext);
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
    public String evalCommandAndReturnString(JShellCommandNode command, JShellContext context) {
        DefaultJShellContext newCtx = (DefaultJShellContext) context.getShell().createNewContext(context);
        NSession session = newCtx.getSession().copy();
        newCtx.setSession(session);
        session.setLogTermLevel(Level.OFF);

        session.setTerminal(NSessionTerminal.ofMem(session));
        context.getShell().evalNode(command, newCtx);
        String str = evalFieldSubstitutionAfterCommandSubstitution(session.out().toString(), context);
        String s = context.getShell().escapeString(str);
        context.err().print(session.err().toString());
        return s;
    }
}
