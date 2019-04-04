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
package net.vpc.app.nuts.toolbox.nutsserver;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.*;
import net.vpc.common.strings.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsAdminServerComponent implements NutsServerComponent {

    private static final Logger log = Logger.getLogger(NutsAdminServerComponent.class.getName());

    @Override
    public int getSupportLevel(ServerConfig config) {
        return (config == null || config instanceof AdminServerConfig) ? DEFAULT_SUPPORT : NO_SUPPORT;
    }

    public NutsServer start(NutsWorkspace invokerWorkspace, ServerConfig config) {
        AdminServerConfig httpConfig = (AdminServerConfig) config;
        if (invokerWorkspace == null) {
            throw new NutsIllegalArgumentException("Missing Workspace");
        }
        NutsSessionTerminal terminal = invokerWorkspace.io().createTerminal();
        String serverId = httpConfig.getServerId();
        InetAddress address = httpConfig.getAddress();
        int port = httpConfig.getPort();
        int backlog = httpConfig.getBacklog();
        Executor executor = httpConfig.getExecutor();
        if (executor == null) {
            executor = new ThreadPoolExecutor(2, 10, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
        }
        if (StringUtils.isEmpty(serverId)) {
            String serverName = NutsConstants.DEFAULT_ADMIN_SERVER;
            try {
                serverName = InetAddress.getLocalHost().getHostName();
                if (serverName != null && serverName.length() > 0) {
                    serverName = "nuts-" + serverName;
                }
            } catch (Exception e) {
                //
            }
            if (serverName == null) {
                serverName = NutsConstants.DEFAULT_ADMIN_SERVER;
            }

            serverId = serverName;//+ "-" + new File(workspace.getWorkspaceLocation()).getName();
        }

        if (port <= 0) {
            port = NutsConstants.DEFAULT_ADMIN_SERVER_PORT;
        }
        if (backlog <= 0) {
            backlog = 10;
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
        PrintStream out = terminal.getFormattedOut();
        out.printf("Nuts Admin Service '%s' running at %s\n", serverId, inetSocketAddress);
        out.printf("Serving workspace : %s\n", invokerWorkspace.config().getWorkspaceLocation());
        MyNutsServer myNutsServer = new MyNutsServer(serverId, port, backlog, address, executor, invokerWorkspace, terminal);

        executor.execute(myNutsServer);
        return myNutsServer;
    }

    private static class MyNutsServer implements NutsServer, Runnable {

        private final String serverId;
        int finalPort;
        int finalBacklog;
        InetAddress address;
        Executor finalExecutor;
        NutsWorkspace invokerWorkspace;
        boolean running;
        ServerSocket serverSocket = null;
        NutsSessionTerminal terminal = null;

        public MyNutsServer(String serverId, int finalPort, int finalBacklog, InetAddress address, Executor finalExecutor, NutsWorkspace invokerWorkspace, NutsSessionTerminal terminal) {
            this.serverId = serverId;
            this.finalPort = finalPort;
            this.finalBacklog = finalBacklog;
            this.address = address;
            this.finalExecutor = finalExecutor;
            this.invokerWorkspace = invokerWorkspace;
            this.terminal = terminal;
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
                                String[] args = {NutsConstants.NUTS_SHELL};
                                NutsJavaShell cli = null;
                                try {
                                    PrintStream out = new PrintStream(finalAccept.getOutputStream());
                                    PrintStream eout = invokerWorkspace.io().createPrintStream(out,NutsTerminalMode.FORMATTED);
                                    NutsSession session = invokerWorkspace.createSession();
                                    NutsSessionTerminal terminal = invokerWorkspace.io().createTerminal();
                                    terminal.setIn(finalAccept.getInputStream());
                                    terminal.setOut(eout);
                                    terminal.setErr(eout);
                                    session.setTerminal(terminal);
                                    cli = new NutsJavaShell(invokerWorkspace,session);
//                                    cli.uninstallCommand("server");
                                    cli.undeclareCommand("connect");
                                    cli.setServiceName(serverId);
                                    cli.declareCommand(new AbstractNutsCommand("stop-server", DEFAULT_SUPPORT) {
                                        @Override
                                        public int exec(String[] args, NutsCommandContext context) throws Exception {
                                            PrintStream out2 = MyNutsServer.this.terminal.getFormattedOut();
                                            out2.println("Stopping Server ...");
                                            finalServerSocket.close();
                                            return 0;
                                        }
                                    });
                                    cli.runCommand(args);
                                    finalAccept.close();
                                } catch (IOException e) {
                                    terminal.getFormattedErr().printf("%s\n", e);
                                }
                            }
                        });
                    } catch (Exception ex) {
                        terminal.getFormattedErr().printf("%s\n", ex);
                    }
                }
            } finally {
                running = false;
            }
        }

        @Override
        public String toString() {
            return "Nuts Admin Server{"
                    + "running=" + running
                    + '}';
        }

    }
}
