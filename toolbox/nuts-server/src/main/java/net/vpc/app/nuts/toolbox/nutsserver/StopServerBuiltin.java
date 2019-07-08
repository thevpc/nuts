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
 * Copyright (C) 2016-2019 Taha BEN SALAH
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
package net.vpc.app.nuts.toolbox.nutsserver;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsComponent;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsSessionTerminal;
import net.vpc.app.nuts.NutsTerminalMode;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.toolbox.nsh.NutsJavaShell;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;

/**
 *
 * @author vpc
 */
class StopServerBuiltin implements NutsServer, Runnable {

    private final String serverId;
    int finalPort;
    int finalBacklog;
    InetAddress address;
    Executor finalExecutor;
    NutsWorkspace invokerWorkspace;
    boolean running;
    ServerSocket serverSocket = null;
    NutsSession session = null;

    public StopServerBuiltin(String serverId, int finalPort, int finalBacklog, InetAddress address, Executor finalExecutor, NutsWorkspace invokerWorkspace, NutsSession session) {
        this.serverId = serverId;
        this.finalPort = finalPort;
        this.finalBacklog = finalBacklog;
        this.address = address;
        this.finalExecutor = finalExecutor;
        this.invokerWorkspace = invokerWorkspace;
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
                            NutsJavaShell cli = null;
                            try {
                                try {
                                    PrintStream out = new PrintStream(finalAccept.getOutputStream());
                                    PrintStream eout = invokerWorkspace.io().createPrintStream(out, NutsTerminalMode.FORMATTED);
                                    NutsSession session = invokerWorkspace.createSession();
                                    final NutsSessionTerminal terminal = invokerWorkspace.io().createTerminal();
                                    terminal.setIn(finalAccept.getInputStream());
                                    terminal.setOut(eout);
                                    terminal.setErr(eout);
                                    session.setTerminal(terminal);
                                    cli = new NutsJavaShell(invokerWorkspace, session,
                                            invokerWorkspace.id().resolveId(StopServerBuiltin.class),
                                            serverId);
                                    cli.getRootContext().builtins().unset("connect");
                                    cli.getRootContext().builtins().set(new StopServerBuiltin2(finalServerSocket));
                                    cli.executeCommand(args);
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

    private static class StopServerBuiltin2 extends SimpleNshBuiltin {

        private final ServerSocket socket;

        public StopServerBuiltin2(ServerSocket finalServerSocket) {
            super("stop-server", NutsComponent.DEFAULT_SUPPORT);
            this.socket = finalServerSocket;
        }

        private static class Options {

        }

        @Override
        protected Object createOptions() {
            return new Options();
        }

        @Override
        protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
            return false;
        }

        @Override
        protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().println("Stopping Server ...");
            }
            try {
                socket.close();
            } catch (IOException ex) {
                throw new NutsExecutionException(context.getWorkspace(), ex.getMessage(), ex, 100);
            }
        }
    }
}
