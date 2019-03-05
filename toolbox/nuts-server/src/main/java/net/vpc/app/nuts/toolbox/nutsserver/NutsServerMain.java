package net.vpc.app.nuts.toolbox.nutsserver;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandSyntaxError;
import net.vpc.app.nuts.app.options.ArchitectureNonOption;
import net.vpc.app.nuts.app.options.ServerNonOption;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.DefaultNonOption;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
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
    public int launch(NutsApplicationContext appContext) {
        String[] args=appContext.getArgs();
        try {
            boolean autoSave = false;
            NutsWorkspaceServerManager serverManager = new DefaultNutsWorkspaceServerManager(appContext.getWorkspace());

            net.vpc.common.commandline.CommandLine cmdLine = new CommandLine(args, appContext.getAutoComplete());

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
            if (cmdLine.readAll("start")) {
                List<SrvInfo> servers = new ArrayList<SrvInfo>();
                boolean autocreate = false;
                boolean readOnly = false;
                String archetype = "server"; //default archetype for server

                while (cmdLine.hasNext()) {
                    if (cmdLine.readAllOnce("-c", "--create")) {
                        autocreate = true;
                    } else if (cmdLine.readAllOnce("-h", "--archetype")) {
                        archetype = cmdLine.readRequiredNonOption(new ArchitectureNonOption("Archetype", appContext.getWorkspace())).getStringOrError();
                    } else if (cmdLine.readAllOnce("-!s", "--no-save")) {
                        readOnly = true;
                    } else if (cmdLine.readAllOnce("--read-only")) {
                        readOnly = true;
                    } else if (cmdLine.readAllOnce("--http")) {
                        servers.add(new SrvInfo());
                        servers.get(servers.size() - 1).serverType = "http";
                    } else if (cmdLine.readAllOnce("--https")) {
                        servers.add(new SrvInfo());
                        servers.get(servers.size() - 1).serverType = "https";
                    } else if (cmdLine.readAllOnce("--admin")) {
                        servers.add(new SrvInfo());
                        servers.get(servers.size() - 1).serverType = "admin";
                    } else if (cmdLine.readAllOnce("-n", "--name")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).name = cmdLine.readRequiredNonOption(new DefaultNonOption("ServerName")).getStringExpression();
                    } else if (cmdLine.readAllOnce("-a", "--address")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).addr = cmdLine.readRequiredNonOption(new DefaultNonOption("ServerAddress")).getStringExpression();

                    } else if (cmdLine.readAllOnce("-p", "--port")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).port = cmdLine.readRequiredNonOption(new DefaultNonOption("ServerPort")).getInt();

                    } else if (cmdLine.readAllOnce("-l", "--backlog")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).port = cmdLine.readRequiredNonOption(new DefaultNonOption("ServerBacklog")).getInt();
                    } else if (cmdLine.readAllOnce("--ssl-certificate")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).sslCertificate = cmdLine.readRequiredNonOption(new DefaultNonOption("SslCertificate")).getStringOrError();
                    } else if (cmdLine.readAllOnce("--ssl-passphrase")) {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).sslPassphrase = cmdLine.readRequiredNonOption(new DefaultNonOption("SslPassPhrase")).getStringOrError();
                    } else {
                        if (servers.size() == 0) {
                            throw new NutsIllegalArgumentException("nuts-server: Server Type missing");
                        }
                        String s = cmdLine.readRequiredNonOption(new DefaultNonOption("Workspace")).getStringExpression();
                        int eq = s.indexOf('=');
                        if (eq >= 0) {
                            String serverContext = s.substring(0, eq);
                            String workspaceLocation = s.substring(eq + 1);
                            if (servers.get(servers.size() - 1).workspaceLocations.containsKey(serverContext)) {
                                throw new NutsIllegalArgumentException("nuts-server: Server Workspace context Already defined " + serverContext);
                            }
                            servers.get(servers.size() - 1).workspaceLocations.put(serverContext, workspaceLocation);
                        } else {
                            if (servers.get(servers.size() - 1).workspaceLocations.containsKey("")) {
                                throw new NutsIllegalArgumentException("nuts-server: Server Workspace context Already defined " + "");
                            }
                            servers.get(servers.size() - 1).workspaceLocations.put("", s);
                        }
                    }

                }
                if (cmdLine.isExecMode()) {
                    if (servers.isEmpty()) {
                        appContext.getTerminal().getFormattedErr().printf("No Server config found.\n");
                        return 1;
                    }
                    Map<String, NutsWorkspace> allWorkspaces = new HashMap<>();
                    for (SrvInfo server : servers) {
                        Map<String, NutsWorkspace> workspaces = new HashMap<>();
                        for (Map.Entry<String, String> entry : server.workspaceLocations.entrySet()) {
                            NutsWorkspace nutsWorkspace = null;
                            if (StringUtils.isEmpty(entry.getValue())) {
                                if (appContext.getWorkspace() == null) {
                                    throw new NutsIllegalArgumentException("nuts-server: Missing workspace");
                                }
                                nutsWorkspace = appContext.getWorkspace();
                            } else {
                                nutsWorkspace = allWorkspaces.get(entry.getValue());
                                if (nutsWorkspace == null) {
                                    nutsWorkspace = appContext.getWorkspace().openWorkspace(
                                            new NutsWorkspaceOptions()
                                                    .setWorkspace(entry.getValue())
                                                    .setOpenMode(autocreate?NutsWorkspaceOpenMode.DEFAULT : NutsWorkspaceOpenMode.OPEN)
                                                    .setReadOnly(readOnly)
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
                                        throw new NutsIllegalArgumentException("nuts-server: Missing SSL Certificate");
                                    }
                                    config.setSslKeystoreCertificate(IOUtils.loadByteArray(new File(server.sslCertificate)));
                                    if (server.sslPassphrase == null) {
                                        throw new NutsIllegalArgumentException("nuts-server: Missing SSL Passphrase");
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
                                throw new NutsIllegalArgumentException("nuts-server: Unsupported server type " + server.serverType);
                        }
                        serverManager.startServer(config0);
                    }
                }
            } else if (cmdLine.readAll("stop")) {
                String s = cmdLine.readRequiredNonOption(new ServerNonOption("ServerName", appContext.getWorkspace())).getStringExpression();
                if (cmdLine.isExecMode()) {
                    serverManager.stopServer(s);
                }
                while (cmdLine.hasNext()) {
                    s = cmdLine.readRequiredNonOption(new ServerNonOption("ServerName", appContext.getWorkspace())).getStringExpression();
                    if (cmdLine.isExecMode()) {
                        serverManager.stopServer(s);
                    }
                }
            } else if (cmdLine.readAll("list")) {
                cmdLine.unexpectedArgument("nuts-server list");
                if (cmdLine.isExecMode()) {
                    List<NutsServer> servers = serverManager.getServers();
                    PrintStream out = appContext.getTerminal().getFormattedOut();
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
                throw new NutsCommandSyntaxError("nuts-server: Invalid syntax for command server");
            }
            cmdLine.unexpectedArgument("nuts-server");
            return 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
