/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.toolbox.nutsserver.admin;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsSessionTerminal;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShell;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nutsserver.NutsServer;

/**
 *
 * @author thevpc
 */
public class AdminServerRunnable implements NutsServer, Runnable {

    private final String serverId;
    int finalPort;
    int finalBacklog;
    InetAddress address;
    Executor finalExecutor;
    NutsSession invokerSession;
    boolean running;
    ServerSocket serverSocket = null;
    NutsSession session = null;

    public AdminServerRunnable(String serverId, int finalPort, int finalBacklog, InetAddress address, Executor finalExecutor, NutsSession invokerSession, NutsSession session) {
        this.serverId = serverId;
        this.finalPort = finalPort;
        this.finalBacklog = finalBacklog;
        this.address = address;
        this.finalExecutor = finalExecutor;
        this.invokerSession = invokerSession;
        this.session = session;
    }

    @Override
    public String getServerId() {
        return serverId;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean stop() {
        if (running) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return true;
        }
        return false;
    }

    public void run() {
        running = true;
        try {
            try {
                serverSocket = new ServerSocket(finalPort, finalBacklog, address);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
            while (running) {
                try {
                    Socket accept = null;
                    try {
                        accept = serverSocket.accept();
                    } catch (Exception ex) {
                        running = false;
                        break;
                    }
                    final ServerSocket finalServerSocket = serverSocket;
                    final Socket finalAccept = accept;
                    finalExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            String[] args = {NutsConstants.Ids.NUTS_SHELL};
                            JShell cli = null;
                            try {
                                try {
                                    PrintStream out = new PrintStream(finalAccept.getOutputStream());
                                    NutsPrintStream eout = NutsPrintStream.of(out, NutsTerminalMode.FORMATTED, null,invokerSession);
                                    NutsSession session = invokerSession;
                                    session.setTerminal(
                                            NutsSessionTerminal.of(
                                                    finalAccept.getInputStream(),
                                                    eout,eout, invokerSession)
                                    );
                                    cli = new JShell(session,
                                            NutsIdResolver.of(invokerSession)
                                                    .resolveId(AdminServerRunnable.class),
                                            serverId,new String[0]);
                                    cli.getRootContext().builtins().unset("connect");
                                    cli.getRootContext().builtins().set(new StopServerBuiltin2(finalServerSocket));
                                    cli.run();
                                } finally {
                                    finalAccept.close();
                                }
                            } catch (IOException e) {
                                session.err().printf("%s\n", e);
                            }
                        }

                    });
                } catch (Exception ex) {
                    session.err().printf("%s\n", ex);
                }
            }
        } finally {
            running = false;
        }
    }

    @Override
    public String toString() {
        return "Nuts Admin Server{" + "running=" + running + '}';
    }

    @NutsComponentScope(NutsComponentScopeType.WORKSPACE)
    private static class StopServerBuiltin2 extends SimpleJShellBuiltin {

        private final ServerSocket socket;

        public StopServerBuiltin2(ServerSocket finalServerSocket) {
            super("stop-server", NutsComponent.DEFAULT_SUPPORT,Options.class);
            this.socket = finalServerSocket;
        }

        private static class Options {

        }

        @Override
        protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
            return false;
        }

        @Override
        protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().println("Stopping Server ...");
            }
            try {
                socket.close();
            } catch (IOException ex) {
                throw new NutsExecutionException(context.getSession(), NutsMessage.ofCstyle("%s",ex), ex, 100);
            }
        }
    }
}
