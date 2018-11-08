package net.vpc.app.nuts.toolbox.nutsserver;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.DefaultNutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandSyntaxError;
import net.vpc.app.nuts.toolbox.nsh.options.ArchitectureNonOption;
import net.vpc.app.nuts.toolbox.nsh.options.ServerNonOption;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.common.commandline.CommandAutoComplete;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.DefaultNonOption;
import net.vpc.common.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NutsServerMain extends NutsApplication {

    public static void main(String[] args) {
        new NutsServerMain().launch(args);
    }

    @Override
    public int launch(String[] args, NutsWorkspace ws) {
        try {
            DefaultNutsCommandContext context = new DefaultNutsCommandContext(ws);
            boolean autoSave = false;
            NutsWorkspaceServerManager serverManager = new DefaultNutsWorkspaceServerManager(context.getValidWorkspace());

            net.vpc.common.commandline.CommandLine cmdLine = new CommandLine(args, (CommandAutoComplete) context.getAutoComplete());

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
                            throw new NutsIllegalArgumentException("Server Type missing");
                        }
                        servers.get(servers.size() - 1).name = cmdLine.readNonOptionOrError(new DefaultNonOption("ServerName")).getString();
                    } else if (cmdLine.readOnce("-a", "--address")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("Server Type missing");
                        }
                        servers.get(servers.size() - 1).addr = cmdLine.readNonOptionOrError(new DefaultNonOption("ServerAddress")).getString();

                    } else if (cmdLine.readOnce("-p", "--port")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("Server Type missing");
                        }
                        servers.get(servers.size() - 1).port = cmdLine.readNonOptionOrError(new DefaultNonOption("ServerPort")).getIntOrError();

                    } else if (cmdLine.readOnce("-l", "--backlog")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("Server Type missing");
                        }
                        servers.get(servers.size() - 1).port = cmdLine.readNonOptionOrError(new DefaultNonOption("ServerBacklog")).getIntOrError();
                    } else if (cmdLine.readOnce("--ssl-certificate")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("Server Type missing");
                        }
                        servers.get(servers.size() - 1).sslCertificate = cmdLine.readNonOptionOrError(new DefaultNonOption("SslCertificate")).getStringOrError();
                    } else if (cmdLine.readOnce("--ssl-passphrase")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("Server Type missing");
                        }
                        servers.get(servers.size() - 1).sslPassphrase = cmdLine.readNonOptionOrError(new DefaultNonOption("SslPassPhrase")).getStringOrError();
                    } else {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("Server Type missing");
                        }
                        String s = cmdLine.readNonOptionOrError(new DefaultNonOption("Workspace")).getString();
                        int eq = s.indexOf('=');
                        if (eq >= 0) {
                            String serverContext = s.substring(0, eq);
                            String workspaceLocation = s.substring(eq + 1);
                            if (servers.get(servers.size() - 1).workspaceLocations.containsKey(serverContext)) {
                                throw new NutsIllegalArgumentException("Server Workspace context Already defined " + serverContext);
                            }
                            servers.get(servers.size() - 1).workspaceLocations.put(serverContext, workspaceLocation);
                        } else {
                            if (servers.get(servers.size() - 1).workspaceLocations.containsKey("")) {
                                throw new NutsIllegalArgumentException("Server Workspace context Already defined " + "");
                            }
                            servers.get(servers.size() - 1).workspaceLocations.put("", s);
                        }
                    }

                }
                if (cmdLine.isExecMode()) {
                    if (servers.isEmpty()) {
                        context.getTerminal().getFormattedErr().printf("No Server config found.\n");
                        return 1;
                    }
                    Map<String, NutsWorkspace> allWorkspaces = new HashMap<>();
                    for (SrvInfo server : servers) {
                        Map<String, NutsWorkspace> workspaces = new HashMap<>();
                        for (Map.Entry<String, String> entry : server.workspaceLocations.entrySet()) {
                            NutsWorkspace nutsWorkspace = null;
                            if (CoreStringUtils.isEmpty(entry.getValue())) {
                                if (context.getValidWorkspace() == null) {
                                    throw new NutsIllegalArgumentException("Missing workspace");
                                }
                                nutsWorkspace = context.getValidWorkspace();
                            } else {
                                nutsWorkspace = allWorkspaces.get(entry.getValue());
                                if (nutsWorkspace == null) {
                                    nutsWorkspace = context.getValidWorkspace().openWorkspace(
                                            new NutsWorkspaceCreateOptions()
                                                    .setWorkspace(entry.getValue())
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
                                        throw new NutsIllegalArgumentException("Missing SSL Certificate");
                                    }
                                    config.setSslKeystoreCertificate(IOUtils.loadByteArray(new File(context.resolvePath(server.sslCertificate))));
                                    if (server.sslPassphrase == null) {
                                        throw new NutsIllegalArgumentException("Missing SSL Passphrase");
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
                                throw new NutsIllegalArgumentException("Unsupported server type " + server.serverType);
                        }
                        serverManager.startServer(config0);
                    }
                }
            } else if (cmdLine.read("stop")) {
                String s = cmdLine.readNonOptionOrError(new ServerNonOption("ServerName", context)).getString();
                if (cmdLine.isExecMode()) {
                    serverManager.stopServer(s);
                }
                while (!cmdLine.isEmpty()) {
                    s = cmdLine.readNonOptionOrError(new ServerNonOption("ServerName", context)).getString();
                    if (cmdLine.isExecMode()) {
                        serverManager.stopServer(s);
                    }
                }
            } else if (cmdLine.read("list")) {
                cmdLine.requireEmpty();
                if (cmdLine.isExecMode()) {
                    List<NutsServer> servers = serverManager.getServers();
                    NutsPrintStream out = context.getTerminal().getFormattedOut();
                    if (servers.isEmpty()) {
                        out.printf("No Server is Running\n");
                    }
                    for (NutsServer o : servers) {
                        if (o.isRunning()) {
                            out.printf("==Running== %s\n", o.getServerId());
                        } else {
                            out.printf("==Stopped== %s\n", o.getServerId());
                        }
                    }
                }
            } else {
                throw new NutsCommandSyntaxError("Invalid syntax from command 'server");
            }
            cmdLine.requireEmpty();
            return 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
