package net.vpc.app.nuts.toolbox.nutsserver;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsApplication;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NutsServerMain extends NutsApplication {

    public static void main(String[] args) {
        new NutsServerMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext context) {
        String[] args = context.getArguments();
        try {
            boolean autoSave = false;
            NutsWorkspaceServerManager serverManager = new DefaultNutsWorkspaceServerManager(context.getWorkspace());

            NutsCommandLine cmdLine = context.commandLine();

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
            if (cmdLine.next("start") != null) {
                List<SrvInfo> servers = new ArrayList<SrvInfo>();
                boolean autocreate = false;
                boolean readOnly = false;
                String archetype = "server"; //default archetype for server
                NutsArgument a;
                while (cmdLine.hasNext()) {
                    if (cmdLine.next("-c", "--create") != null) {
                        autocreate = true;
                    } else if ((a = cmdLine.nextString("-h", "--archetype")) != null) {
                        archetype = a.getStringValue();
                    } else if (cmdLine.next("-!s", "--no-save") != null) {
                        readOnly = true;
                    } else if (cmdLine.next("--read-only") != null) {
                        readOnly = true;
                    } else if (cmdLine.next("--http") != null) {
                        servers.add(new SrvInfo());
                        servers.get(servers.size() - 1).serverType = "http";
                    } else if (cmdLine.next("--https") != null) {
                        servers.add(new SrvInfo());
                        servers.get(servers.size() - 1).serverType = "https";
                    } else if (cmdLine.next("--admin") != null) {
                        servers.add(new SrvInfo());
                        servers.get(servers.size() - 1).serverType = "admin";
                    } else if (cmdLine.next("-n", "--name") != null) {
                        if (servers.isEmpty()) {
                            throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).name = cmdLine.required().nextNonOption(cmdLine.createName("ServerName")).getString();
                    } else if (cmdLine.next("-a", "--address") != null) {
                        if (servers.isEmpty()) {
                            throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).addr = cmdLine.required().nextNonOption(cmdLine.createName("ServerAddress")).getString();

                    } else if (cmdLine.next("-p", "--port") != null) {
                        if (servers.isEmpty()) {
                            throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).port = cmdLine.required().nextNonOption(cmdLine.createName("ServerPort")).getInt();

                    } else if (cmdLine.next("-l", "--backlog") != null) {
                        if (servers.isEmpty()) {
                            throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).port = cmdLine.required().nextNonOption(cmdLine.createName("ServerBacklog")).getInt();
                    } else if (cmdLine.next("--ssl-certificate") != null) {
                        if (servers.isEmpty()) {
                            throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).sslCertificate = cmdLine.required().nextNonOption(cmdLine.createName("SslCertificate")).required().getString();
                    } else if (cmdLine.next("--ssl-passphrase") != null) {
                        if (servers.isEmpty()) {
                            throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Type missing");
                        }
                        servers.get(servers.size() - 1).sslPassphrase = cmdLine.required().nextNonOption(cmdLine.createName("SslPassPhrase")).required().getString();
                    } else {
                        if (servers.isEmpty()) {
                            throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Type missing");
                        }
                        String s = cmdLine.required().nextNonOption(cmdLine.createName("Workspace")).getString();
                        int eq = s.indexOf('=');
                        if (eq >= 0) {
                            String serverContext = s.substring(0, eq);
                            String workspaceLocation = s.substring(eq + 1);
                            if (servers.get(servers.size() - 1).workspaceLocations.containsKey(serverContext)) {
                                throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Workspace context Already defined " + serverContext);
                            }
                            servers.get(servers.size() - 1).workspaceLocations.put(serverContext, workspaceLocation);
                        } else {
                            if (servers.get(servers.size() - 1).workspaceLocations.containsKey("")) {
                                throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Workspace context Already defined " + "");
                            }
                            servers.get(servers.size() - 1).workspaceLocations.put("", s);
                        }
                    }

                }
                if (cmdLine.isExecMode()) {
                    if (servers.isEmpty()) {
                        context.session().terminal().err().printf("No Server config found.\n");
                        throw new NutsExecutionException(context.getWorkspace(), "No Server config found", 1);
                    }
                    Map<String, NutsWorkspace> allWorkspaces = new HashMap<>();
                    for (SrvInfo server : servers) {
                        Map<String, NutsWorkspace> workspaces = new HashMap<>();
                        for (Map.Entry<String, String> entry : server.workspaceLocations.entrySet()) {
                            NutsWorkspace nutsWorkspace = null;
                            if (StringUtils.isBlank(entry.getValue())) {
                                if (context.getWorkspace() == null) {
                                    throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Missing workspace");
                                }
                                nutsWorkspace = context.getWorkspace();
                            } else {
                                nutsWorkspace = allWorkspaces.get(entry.getValue());
                                if (nutsWorkspace == null) {
                                    nutsWorkspace = context.getWorkspace().openWorkspace(
                                            new NutsWorkspaceOptions()
                                                    .setWorkspace(entry.getValue())
                                                    .setOpenMode(autocreate ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : NutsWorkspaceOpenMode.OPEN_EXISTING)
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
                                        throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Missing SSL Certificate");
                                    }
                                    config.setSslKeystoreCertificate(IOUtils.loadByteArray(new File(server.sslCertificate)));
                                    if (server.sslPassphrase == null) {
                                        throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Missing SSL Passphrase");
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
                                throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Unsupported server type " + server.serverType);
                        }
                        serverManager.startServer(config0);
                    }
                }
            } else if (cmdLine.next("stop") != null) {
                String s = cmdLine.required().nextNonOption(cmdLine.createName("ServerName")).getString();
                if (cmdLine.isExecMode()) {
                    serverManager.stopServer(s);
                }
                while (cmdLine.hasNext()) {
                    s = cmdLine.required().nextNonOption(cmdLine.createName("ServerName")).getString();
                    if (cmdLine.isExecMode()) {
                        serverManager.stopServer(s);
                    }
                }
            } else if (cmdLine.next("list") != null) {
                cmdLine.setCommandName("nuts-server list").unexpectedArgument();
                if (cmdLine.isExecMode()) {
                    List<NutsServer> servers = serverManager.getServers();
                    PrintStream out = context.session().out();
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
                throw new NutsExecutionException(context.getWorkspace(), "nuts-server: Invalid syntax for command server", 1);
            }
            cmdLine.setCommandName("nuts-server").unexpectedArgument();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
