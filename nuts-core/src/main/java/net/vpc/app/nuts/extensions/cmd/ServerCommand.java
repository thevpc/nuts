/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import net.vpc.app.nuts.extensions.cmd.cmdline.ArchitectureNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.DefaultNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.ServerNonOption;

/**
 * Created by vpc on 1/7/17.
 */
public class ServerCommand extends AbstractNutsCommand {

    public ServerCommand() {
        super("server", CORE_SUPPORT);
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) throws IOException, LoginException {
        NutsCommandAutoComplete autoComplete = context.getAutoComplete();
        boolean autoSave = false;
        CmdLine cmdLine = new CmdLine(autoComplete, args);

        class SrvInfo {

            String name = ("nuts-http-server");
            String addr = null;
            int port = -1;
            int backlog = -1;
            String serverType = "http";
            String sslCertificate = null;
            String sslPassphrase = null;
            Map<String, String> workspaceLocations = new HashMap<>();
            Map<String, NutsWorkspace> workspaces = new HashMap<>();
        }
        if (cmdLine.read("start")) {
            List<SrvInfo> servers = new ArrayList<SrvInfo>();
            boolean autocreate = false;
            boolean save = true;
            String archetype = "server"; //default archetype for server

            while (!cmdLine.isEmpty()) {
                if (cmdLine.readOnce("-c", "--create")) {
                    autocreate = true;
                } else if (cmdLine.readOnce("-h", "--archetype")) {
                    archetype = cmdLine.readNonOptionOrError(new ArchitectureNonOption("Archetype", context)).getStringOrError();
                } else if (cmdLine.readOnce("-!s", "--no-save")) {
                    save = false;
                } else if (cmdLine.readOnce("--http")) {
                    servers.add(new SrvInfo());
                    servers.get(servers.size() - 1).serverType = "http";
                } else if (cmdLine.readOnce("--https")) {
                    servers.add(new SrvInfo());
                    servers.get(servers.size() - 1).serverType = "https";
                } else if (cmdLine.readOnce("--admin")) {
                    servers.add(new SrvInfo());
                    servers.get(servers.size() - 1).serverType = "admin";
                } else if (cmdLine.readOnce("-n", "--name")) {
                    if (servers.size() == 0) {
                        throw new NutsIllegalArgumentsException("Server Type missing");
                    }
                    servers.get(servers.size() - 1).name = cmdLine.readNonOptionOrError(new DefaultNonOption("ServerName")).getString();
                } else if (cmdLine.readOnce("-a", "--address")) {
                    if (servers.size() == 0) {
                        throw new NutsIllegalArgumentsException("Server Type missing");
                    }
                    servers.get(servers.size() - 1).addr = cmdLine.readNonOptionOrError(new DefaultNonOption("ServerAddress")).getString();

                } else if (cmdLine.readOnce("-p", "--port")) {
                    if (servers.size() == 0) {
                        throw new NutsIllegalArgumentsException("Server Type missing");
                    }
                    servers.get(servers.size() - 1).port = cmdLine.readNonOptionOrError(new DefaultNonOption("ServerPort")).getIntOrError();

                } else if (cmdLine.readOnce("-l", "--backlog")) {
                    if (servers.size() == 0) {
                        throw new NutsIllegalArgumentsException("Server Type missing");
                    }
                    servers.get(servers.size() - 1).port = cmdLine.readNonOptionOrError(new DefaultNonOption("ServerBacklog")).getIntOrError();
                } else if (cmdLine.readOnce("--ssl-certificate")) {
                    if (servers.size() == 0) {
                        throw new NutsIllegalArgumentsException("Server Type missing");
                    }
                    servers.get(servers.size() - 1).sslCertificate = cmdLine.readNonOptionOrError(new DefaultNonOption("SslCertificate")).getStringOrError();
                } else if (cmdLine.readOnce("--ssl-passphrase")) {
                    if (servers.size() == 0) {
                        throw new NutsIllegalArgumentsException("Server Type missing");
                    }
                    servers.get(servers.size() - 1).sslPassphrase = cmdLine.readNonOptionOrError(new DefaultNonOption("SslPassPhrase")).getStringOrError();
                } else {
                    if (servers.size() == 0) {
                        throw new NutsIllegalArgumentsException("Server Type missing");
                    }
                    String s = cmdLine.readNonOptionOrError(new DefaultNonOption("Workspace")).getString();
                    int eq = s.indexOf('=');
                    if (eq >= 0) {
                        String serverContext = s.substring(0, eq);
                        String workspaceLocation = s.substring(eq + 1);
                        if (servers.get(servers.size() - 1).workspaceLocations.containsKey(serverContext)) {
                            throw new NutsIllegalArgumentsException("Server Workspace context Already defined " + serverContext);
                        }
                        servers.get(servers.size() - 1).workspaceLocations.put(serverContext, workspaceLocation);
                    } else {
                        if (servers.get(servers.size() - 1).workspaceLocations.containsKey("")) {
                            throw new NutsIllegalArgumentsException("Server Workspace context Already defined " + "");
                        }
                        servers.get(servers.size() - 1).workspaceLocations.put("", s);
                    }
                }

            }
            if (cmdLine.isExecMode()) {
                if (servers.isEmpty()) {
                    context.getTerminal().getErr().drawln("No Server config found.");
                    return 1;
                }
                Map<String, NutsWorkspace> allWorkspaces = new HashMap<>();
                for (SrvInfo server : servers) {
                    Map<String, NutsWorkspace> workspaces = new HashMap<>();
                    for (Map.Entry<String, String> entry : server.workspaceLocations.entrySet()) {
                        NutsWorkspace nutsWorkspace = null;
                        if (CoreStringUtils.isEmpty(entry.getValue())) {
                            if (context.getValidWorkspace() == null) {
                                throw new NutsIllegalArgumentsException("Missing workspace");
                            }
                            nutsWorkspace = context.getValidWorkspace();
                        } else {
                            nutsWorkspace = allWorkspaces.get(entry.getValue());
                            if (nutsWorkspace == null) {
                                nutsWorkspace = context.getValidWorkspace().openWorkspace(entry.getValue(),
                                        new NutsWorkspaceCreateOptions()
                                                .setCreateIfNotFound(autocreate)
                                                .setSaveIfCreated(save)
                                                .setArchetype(archetype)
                                );
                                allWorkspaces.put(entry.getValue(), nutsWorkspace);
                            }

                        }
                        workspaces.put(entry.getKey(), nutsWorkspace);
                    }
                }
                for (SrvInfo server : servers) {
                    ServerConfig config0 = null;
                    switch (server.serverType) {
                        case "http":
                        case "https": {
                            NutsHttpServerConfig config = new NutsHttpServerConfig();
                            config.setAddress(server.addr == null ? null : InetAddress.getByName(server.addr));
                            config.setServerId(server.name);
                            config.setPort(server.port);
                            config.setBacklog(server.backlog);
                            config.getWorkspaces().putAll(server.workspaces);
                            if ("https".equals(server.serverType)) {
                                config.setSsh(true);
                                if (server.sslCertificate == null) {
                                    throw new NutsIllegalArgumentsException("Missing SSL Certificate");
                                }
                                config.setSslKeystoreCertificate(CoreIOUtils.readStreamAsBytes(CoreIOUtils.createFileByCwd(server.sslCertificate, new File(context.getCommandLine().getCwd()))));
                                if (server.sslPassphrase == null) {
                                    throw new NutsIllegalArgumentsException("Missing SSL Passphrase");
                                }
                                config.setSslKeystorePassphrase(server.sslPassphrase.toCharArray());
                            }
                            config0 = config;
                            break;
                        }
                        case "admin": {
                            AdminServerConfig config = new AdminServerConfig();
                            config.setAddress(server.addr == null ? null : InetAddress.getByName(server.addr));
                            config.setServerId(server.name);
                            config.setPort(server.port);
                            config.setBacklog(server.backlog);
                            config0 = config;
                            break;
                        }
                        default:
                            throw new NutsIllegalArgumentsException("Unsupported server type " + server.serverType);
                    }
                    context.getValidWorkspace().startServer(config0);
                }
            }
        } else if (cmdLine.read("stop")) {
            String s = cmdLine.readNonOptionOrError(new ServerNonOption("ServerName", context)).getString();
            if (cmdLine.isExecMode()) {
                context.getValidWorkspace().stopServer(s);
            }
            while (!cmdLine.isEmpty()) {
                s = cmdLine.readNonOptionOrError(new ServerNonOption("ServerName", context)).getString();
                if (cmdLine.isExecMode()) {
                    context.getValidWorkspace().stopServer(s);
                }
            }
        } else if (cmdLine.read("list")) {
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                List<NutsServer> servers = context.getValidWorkspace().getServers();
                if (servers.isEmpty()) {
                    context.getTerminal().getOut().drawln("No Server is Running");
                }
                for (NutsServer o : servers) {
                    if (o.isRunning()) {
                        context.getTerminal().getOut().drawln("==Running== " + o.getServerId());
                    } else {
                        context.getTerminal().getOut().drawln("==Stopped== " + o.getServerId());
                    }
                }
            }
        } else {
            throw new NutsCommandSyntaxError("Invalid syntax from command 'server");
        }
        cmdLine.requireEmpty();
        return 0;
    }
}
