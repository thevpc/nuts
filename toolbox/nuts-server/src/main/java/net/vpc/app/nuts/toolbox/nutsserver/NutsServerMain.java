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
import java.net.UnknownHostException;
import java.util.*;

public class NutsServerMain extends NutsApplication {

    public static void main(String[] args) {
        new NutsServerMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext context) {
        boolean autoSave = false;
        NutsWorkspaceServerManager serverManager = new DefaultNutsWorkspaceServerManager(context.getWorkspace());
        NutsCommandLineFormat commandLineFormat = context.workspace().commandLine();
        NutsCommandLine cmdLine = context.commandLine().setCommandName("nuts-server");

        while (cmdLine.hasNext()) {
            if (cmdLine.next("start") != null) {
                start(context, cmdLine);
            } else if (cmdLine.next("stop") != null) {
                stop(context, cmdLine);
            } else if (cmdLine.next("list") != null) {
                list(context, cmdLine);
            } else if (context.configureFirst(cmdLine)) {
                //okkay
            } else {
                cmdLine.setCommandName("nuts-server").unexpectedArgument();
            }
        }
    }

    private void list(NutsApplicationContext context, NutsCommandLine cmdLine) {
        NutsWorkspaceServerManager serverManager = new DefaultNutsWorkspaceServerManager(context.getWorkspace());
        cmdLine.setCommandName("nuts-server list").unexpectedArgument();
        if (cmdLine.isExecMode()) {
            List<NutsServer> servers = serverManager.getServers();
            PrintStream out = context.session().out();
            if (servers.isEmpty()) {
                out.print("No Server is Running\n");
            }
            for (NutsServer o : servers) {
                if (o.isRunning()) {
                    out.printf("==Running== %s\n", o.getServerId());
                } else {
                    out.printf("==Stopped== %s\n", o.getServerId());
                }
            }
        }
    }

    private void stop(NutsApplicationContext context, NutsCommandLine cmdLine) {
        NutsWorkspaceServerManager serverManager = new DefaultNutsWorkspaceServerManager(context.getWorkspace());
        NutsCommandLineFormat commandLineFormat = context.workspace().commandLine();
        String s;
        int count=0;
        while (cmdLine.hasNext()) {
            if(count==0){
                cmdLine.required();
            }else if(cmdLine.isEmpty()){
                break;
            }
            count++;
            s = cmdLine.nextRequiredNonOption(commandLineFormat.createName("ServerName")).getString();
            if (cmdLine.isExecMode()) {
                serverManager.stopServer(s);
            }
        }
    }

    private void start(NutsApplicationContext context, NutsCommandLine cmdLine) {
        NutsWorkspaceServerManager serverManager = new DefaultNutsWorkspaceServerManager(context.getWorkspace());
        NutsCommandLineFormat commandLineFormat = context.workspace().commandLine();
        List<SrvInfo> servers = new ArrayList<SrvInfo>();
        boolean readOnly = false;
        String archetype = "server"; //default archetype for server
        NutsArgument a;
        NutsWorkspaceOpenMode openMode = NutsWorkspaceOpenMode.OPEN_OR_CREATE;
        while (cmdLine.hasNext()) {
            if (context.configureFirst(cmdLine)) {
            } else if ((a = cmdLine.nextString("-o", "--open-mode")) != null) {
                String v = a.getStringValue();
                if (a.isEnabled()) {
                    openMode = parseNutsWorkspaceOpenMode(v);
                }
            } else if ((a = cmdLine.nextBoolean("-open")) != null) {
                if (a.isEnabled()) {
                    openMode = NutsWorkspaceOpenMode.OPEN_EXISTING;
                }
            } else if ((a = cmdLine.nextBoolean("-create")) != null) {
                if (a.isEnabled()) {
                    openMode = NutsWorkspaceOpenMode.CREATE_NEW;
                }
            } else if ((a = cmdLine.nextString("-A", "--archetype")) != null) {
                archetype = a.getStringValue();
            } else if (cmdLine.next("-R", "--read-only") != null) {
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
                servers.get(servers.size() - 1).name = cmdLine.required().nextNonOption(commandLineFormat.createName("ServerName")).getString();
            } else if (cmdLine.next("-a", "--address") != null) {
                if (servers.isEmpty()) {
                    throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Type missing");
                }
                servers.get(servers.size() - 1).addr = cmdLine.required().nextNonOption(commandLineFormat.createName("ServerAddress")).getString();

            } else if (cmdLine.next("-p", "--port") != null) {
                if (servers.isEmpty()) {
                    throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Type missing");
                }
                servers.get(servers.size() - 1).port = cmdLine.required().nextNonOption(commandLineFormat.createName("ServerPort")).getInt();

            } else if (cmdLine.next("-l", "--backlog") != null) {
                if (servers.isEmpty()) {
                    throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Type missing");
                }
                servers.get(servers.size() - 1).port = cmdLine.required().nextNonOption(commandLineFormat.createName("ServerBacklog")).getInt();
            } else if (cmdLine.next("--ssl-certificate") != null) {
                if (servers.isEmpty()) {
                    throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Type missing");
                }
                servers.get(servers.size() - 1).sslCertificate = cmdLine.required().nextNonOption(commandLineFormat.createName("SslCertificate")).required().getString();
            } else if (cmdLine.next("--ssl-passphrase") != null) {
                if (servers.isEmpty()) {
                    throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Type missing");
                }
                servers.get(servers.size() - 1).sslPassphrase = cmdLine.required().nextNonOption(commandLineFormat.createName("SslPassPhrase")).required().getString();
            } else {
                if (servers.isEmpty()) {
                    throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Type missing");
                }
                NutsArgument s = cmdLine.required().nextNonOption(commandLineFormat.createName("Workspace"));
                if (s.isKeyValue()) {
                    String serverContext = s.getStringKey();
                    String workspaceLocation = s.getStringValue();
                    if (servers.get(servers.size() - 1).workspaceLocations.containsKey(serverContext)) {
                        throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Workspace context Already defined " + serverContext);
                    }
                    servers.get(servers.size() - 1).workspaceLocations.put(serverContext, workspaceLocation);
                } else {
                    if (servers.get(servers.size() - 1).workspaceLocations.containsKey("")) {
                        throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Server Workspace context Already defined " + "");
                    }
                    servers.get(servers.size() - 1).workspaceLocations.put("", s.getString());
                }
            }

        }
        if (cmdLine.isExecMode()) {
            if (servers.isEmpty()) {
                context.session().terminal().err().println("No Server config found.");
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
                            nutsWorkspace = Nuts.openWorkspace(
                                    new NutsDefaultWorkspaceOptions()
                                            .setWorkspace(entry.getValue())
                                            .setOpenMode(openMode)
                                            .setReadOnly(readOnly)
                                            .setArchetype(archetype)
                            );
                            allWorkspaces.put(entry.getValue(), nutsWorkspace);
                        }

                    }
                    workspaces.put(entry.getKey(), nutsWorkspace);
                }
            }
            HashSet<String> visitedNames = new HashSet<>();
            for (SrvInfo server : servers) {
                ServerConfig config0 = null;
                switch (server.serverType) {
                    case "http":
                    case "https": {
                        NutsHttpServerConfig config = new NutsHttpServerConfig();
                        try {
                            config.setAddress(server.addr == null ? null : InetAddress.getByName(server.addr));
                        } catch (UnknownHostException e) {
                            throw new UncheckedIOException(e);
                        }
                        config.setServerId(server.name=validateName(server.name,"https".equals(server.serverType)?"nuts-https-server":"nuts-http-server", visitedNames));
                        config.setPort(server.port);
                        config.setBacklog(server.backlog);
                        config.getWorkspaces().putAll(server.workspaces);
                        if ("https".equals(server.serverType)) {
                            config.setTls(true);
                            if (server.sslCertificate == null) {
                                throw new NutsIllegalArgumentException(context.getWorkspace(), "nuts-server: Missing SSL Certificate");
                            }
                            try {
                                config.setSslKeystoreCertificate(IOUtils.loadByteArray(new File(server.sslCertificate)));
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
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
                        try {
                            config.setAddress(server.addr == null ? null : InetAddress.getByName(server.addr));
                        } catch (UnknownHostException e) {
                            throw new UncheckedIOException(e);
                        }
                        config.setServerId(server.name=validateName(server.name,"nuts-admin-server",visitedNames));
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
        final Object lock=new Object();
        synchronized (lock){
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class SrvInfo {

        String name = null;
        String addr = null;
        int port = -1;
        int backlog = -1;
        String serverType = "http";
        String sslCertificate = null;
        String sslPassphrase = null;
        Map<String, String> workspaceLocations = new HashMap<>();
        Map<String, NutsWorkspace> workspaces = new HashMap<>();
    }



    private static NutsWorkspaceOpenMode parseNutsWorkspaceOpenMode(String s) {
        String s0 = s;
        if (s == null || s.isEmpty()) {
            return null;
        }
        s = s.toUpperCase().replace('-', '_').replace('/', '_');
        switch (s) {
            case "R":
            case "READ":
            case "O":
            case "OPEN": {
                return NutsWorkspaceOpenMode.OPEN_EXISTING;
            }
            case "W":
            case "WRITE":
            case "N":
            case "NEW":
            case "C":
            case "CREATE": {
                return NutsWorkspaceOpenMode.CREATE_NEW;
            }
            case "RW":
            case "R_W":
            case "READ_WRITE":
            case "ON":
            case "O_N":
            case "OPEN_NEW":
            case "OC":
            case "O_C":
            case "OPEN_CREATE": {
                return NutsWorkspaceOpenMode.OPEN_OR_CREATE;
            }
        }
        throw new IllegalArgumentException("Unable to parse value for NutsWorkspaceOpenMode : " + s0);
    }

    private String validateName(String name, String defaultName, Set<String> visited){
        if(name==null){
            name="";
        }
        name=name.trim();
        if(name.isEmpty()){
            name=defaultName;
        }
        int x=1;
        while(true){
            String n=name+(x==1?"":("-"+x));
            if(!visited.contains(n)){
                visited.add(n);
                return n;
            }
            x++;
        }
    }

}
