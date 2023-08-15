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
package net.thevpc.nuts.toolbox.nutsserver.admin;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinBase;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.nshell.NShellConfiguration;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.toolbox.nutsserver.NServer;
import net.thevpc.nuts.util.NMsg;

/**
 * @author thevpc
 */
public class AdminServerRunnable implements NServer, Runnable {

    private final String serverId;
    int finalPort;
    int finalBacklog;
    InetAddress address;
    Executor finalExecutor;
    NSession invokerSession;
    boolean running;
    ServerSocket serverSocket = null;
    NSession session = null;

    public AdminServerRunnable(String serverId, int finalPort, int finalBacklog, InetAddress address, Executor finalExecutor, NSession invokerSession, NSession session) {
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
                            String[] args = {NConstants.Ids.NUTS_SHELL};
                            NShell cli = null;
                            try {
                                try {
                                    PrintStream out = new PrintStream(finalAccept.getOutputStream());
                                    NPrintStream eout = NPrintStream.of(out, NTerminalMode.FORMATTED, null, invokerSession);
                                    NSession session = invokerSession;
                                    session.setTerminal(
                                            NSessionTerminal.of(
                                                    finalAccept.getInputStream(),
                                                    eout, eout, invokerSession)
                                    );
                                    cli = new NShell(
                                            new NShellConfiguration()
                                                    .setSession(session)
                                                    .setAppId(NIdResolver.of(invokerSession)
                                                            .resolveId(AdminServerRunnable.class))
                                                    .setServiceName(serverId)
                                                    .setArgs(new String[0])
                                    );
                                    cli.getRootContext().builtins().unset("connect");
                                    cli.getRootContext().builtins().set(new StopServerBuiltin2(finalServerSocket));
                                    cli.run();
                                } finally {
                                    finalAccept.close();
                                }
                            } catch (IOException e) {
                                session.err().println(e);
                            }
                        }

                    });
                } catch (Exception ex) {
                    session.err().println(ex);
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

    @NComponentScope(NScopeType.WORKSPACE)
    private static class StopServerBuiltin2 extends NShellBuiltinBase {

        private final ServerSocket socket;

        public StopServerBuiltin2(ServerSocket finalServerSocket) {
            super("stop-server", NConstants.Support.DEFAULT_SUPPORT, Options.class);
            this.socket = finalServerSocket;
        }

        private static class Options {

        }

        @Override
        protected boolean onCmdNextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
            return false;
        }

        @Override
        protected boolean onCmdNextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
            return false;
        }

        @Override
        protected void onCmdExec(NCmdLine cmdLine, NShellExecutionContext context) {
            if (context.getSession().isPlainTrace()) {
                context.getSession().out().println("Stopping Server ...");
            }
            try {
                socket.close();
            } catch (IOException ex) {
                throw new NExecutionException(context.getSession(), NMsg.ofC("%s", ex), ex, NExecutionException.ERROR_2);
            }
        }
    }
}
