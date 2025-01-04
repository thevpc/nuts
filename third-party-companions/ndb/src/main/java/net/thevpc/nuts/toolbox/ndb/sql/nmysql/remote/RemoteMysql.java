package net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote;

//package net.thevpc.nuts.toolbox.nmysql.remote;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.toolbox.nmysql.NMySqlConfigVersions;
//import net.thevpc.nuts.toolbox.nmysql.remote.config.RemoteMysqlConfig;
//import net.thevpc.nuts.toolbox.nmysql.remote.config.RemoteMysqlDatabaseConfig;
//
//import java.io.IOException;
//import java.io.UncheckedIOException;
//import java.nio.file.DirectoryStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//import net.thevpc.nuts.toolbox.nmysql.util.AtName;
//
//public class RemoteMysql {
//
//    public NSession session;
//    private Path sharedConfigFolder;
//    public RemoteMysql(NSession session) {
//        this.session = session;
//        sharedConfigFolder = Paths.get(getContext().getVersionFolderFolder(NutsStoreLocation.CONFIG, NMySqlConfigVersions.CURRENT));
//    }
//
//    public void runArgs(String[] args) {
//        NutsCommandLine cmd = context.getWorkspace().commandLine().create(args)
//                .setCommandName("mysql --remote");
//        NutsArgument a;
//        while (cmd.hasNext()) {
//            if ((a = cmd.next("list", "ls").orNull())!=null) {
//                list(cmd);
//                return;
//            } else if ((a = cmd.next("add", "create").orNull())!=null) {
//                add(cmd);
//                return;
//            } else if ((a = cmd.next("set", "update").orNull())!=null) {
//                set(cmd);
//                return;
//            } else if ((a = cmd.next("remove").orNull())!=null) {
//                remove(cmd);
//                return;
//            } else if ((a = cmd.next("push").orNull())!=null) {
//                push(cmd);
//                return;
//            } else if ((a = cmd.next("pull").orNull())!=null) {
//                pull(cmd);
//                return;
//            } else {
//                cmd.throwUnexpectedArgument();
//            }
//        }
//    }
//
//    public void list(NutsCommandLine args) {
//        args.setCommandName("mysql --remote list");
//        AtName name = null;
//        while (args.hasNext()) {
//            if (args.peek().get(session).getKey().getString().get(session).equals("--name")) {
//                name = AtName.nextConfigOption(args);
//            } else if (name == null && args.peek().get(session).isNonOption()) {
//                name = AtName.nextConfigNonOption(args);
//            } else {
//                context.configureLast(args);
//            }
//        }
//        LinkedHashMap<String, RemoteMysqlConfig> result = new LinkedHashMap<>();
//        if (name != null) {
//            RemoteMysqlConfigService c = loadOrCreateRemoteMysqlConfig(name.getConfigName());
//            result.put(c.getName(), c.getConfig());
//        } else {
//            for (RemoteMysqlConfigService c : listRemoteConfig()) {
//                result.put(c.getName(), c.getConfig());
//            }
//        }
//        NSession session = context.getSession();
//        if (session.isIterableOut()) {
//            session.getIterableOutput().start();
//            for (Map.Entry<String, RemoteMysqlConfig> cnf : result.entrySet()) {
//                for (Map.Entry<String, RemoteMysqlDatabaseConfig> db : cnf.getValue().getDatabases().entrySet()) {
//                    session.getIterableOutput().next(new Object[]{db.getKey(), cnf.getKey()});
//                }
//            }
//            session.getIterableOutput().complete();
//        } else {
//            switch (session.getOutputFormat()) {
//                case PLAIN: {
//                    for (Map.Entry<String, RemoteMysqlConfig> cnf : result.entrySet()) {
//                        for (Map.Entry<String, RemoteMysqlDatabaseConfig> db : cnf.getValue().getDatabases().entrySet()) {
//                            getContext().getSession().out().print(NMsg.ofC("%s\\@#####%s#####%n", db.getKey(), cnf.getKey());
//                        }
//                    }
//                    break;
//                }
//                default: {
//                    context.getSession().formatObject(result).println();
//                }
//            }
//        }
//    }
//
//    public void add(NutsCommandLine args) {
//        createOrUpdate(args, true);
//    }
//
//    public void set(NutsCommandLine args) {
//        createOrUpdate(args, false);
//    }
//
//    private void createOrUpdate(NutsCommandLine commandLine, boolean add) {
//        commandLine.setCommandName("mysql --local " + (add ? "add" : "set"));
//        AtName name = null;
//
//        NutsArgument a;
//        AtName localName = null;
//        AtName remoteName = null;
//        String server = null;
//        while (commandLine.hasNext()) {
//            if (commandLine.isNextOption()) {
//                switch (commandLine.peek().get(session).getKey().getString().get(session)) {
//                    case "--name": {
//                        if (name == null) {
//                            name = AtName.nextAppOption(commandLine);
//                        } else {
//                            commandLine.throwUnexpectedArgument("already defined");
//                        }
//                        break;
//                    }
//                    case "--local-name": {
//                        if (localName == null) {
//                            localName = AtName.nextAppOption(commandLine);
//                        } else {
//                            commandLine.throwUnexpectedArgument("already defined");
//                        }
//                        break;
//                    }
//                    case "--remote-name": {
//                        if (remoteName == null) {
//                            remoteName = AtName.nextAppOption(commandLine);
//                        } else {
//                            commandLine.throwUnexpectedArgument("already defined");
//                        }
//                        break;
//                    }
//                    case "--server": {
//                        if (server == null) {
//                            server = commandLine.nextString().getStringValue().get(session);
//                        } else {
//                            commandLine.throwUnexpectedArgument("already defined");
//                        }
//                        break;
//                    }
//                    default: {
//                        if(commandLine.peek().get(session).isNonOption()){
//                            if (name == null) {
//                                name = AtName.nextAppOption(commandLine);
//                            } else {
//                                commandLine.throwUnexpectedArgument("already defined");
//                            }
//                        }else{
//                            context.configureLast(commandLine);
//                        }
//                        break;
//                    }
//                }
//            } else {
//                if (name == null) {
//                    name = AtName.nextAppNonOption(commandLine);
//                } else {
//                    commandLine.throwUnexpectedArgument();
//                }
//            }
//        }
//        if (name == null) {
//            name = new AtName("");
//        }
//        if (commandLine.isExecMode()) {
//            if(localName==null && remoteName==null){
//                localName=name;
//                remoteName=name;
//            }else if(localName==null){
//                localName=remoteName;
//            }else if(remoteName==null){
//                remoteName=localName;
//            }
//            RemoteMysqlConfigService c = loadOrCreateRemoteMysqlConfig(name.getConfigName());
//            boolean overrideExisting = false;
//            if (add) {
//                if (name.getDatabaseName().isEmpty()) {
//                    if (c.getDatabaseOrNull(name.getDatabaseName()) != null) {
//                        overrideExisting = true;
//                        if (!context.getSession().getTerminal().ask()
//                                .forBoolean("Already exists ####%s####. override?", name)
//                                .defaultValue(false).getBooleanValue()) {
//                            throw new NutsExecutionException(context.getWorkspace(), "Already exists " + name, 2);
//                        }
//                    }
//                } else {
//                    if (c.getDatabaseOrNull(name.getDatabaseName()) != null) {
//                        overrideExisting = true;
//                        if (!context.getSession().getTerminal().ask()
//                                .forBoolean("Already exists ####%s####. override?", name)
//                                .defaultValue(false).getBooleanValue()) {
//                            throw new NutsExecutionException(context.getWorkspace(), "already exists " + name, 2);
//                        }
//                    }
//                }
//            } else {
//                if (name.getDatabaseName().isEmpty()) {
//                    if (c.getDatabaseOrNull(name.getDatabaseName()) == null) {
//                        throw new NutsExecutionException(context.getWorkspace(), "not found " + name, 2);
//                    }
//                } else {
//                    if (c.getDatabaseOrNull(name.getDatabaseName()) == null) {
//                        throw new NutsExecutionException(context.getWorkspace(), "not found  " + name, 2);
//                    }
//                }
//            }
//            boolean someUpdates = false;
//            if (name.getDatabaseName().isEmpty()) {
////                if (c_mysqldump_command != null) {
////                    someUpdates = true;
////                    c.getConfig().setMysqldumpCommand(c_mysqldump_command);
////                }
//                if (context.getSession().isPlainTrace()) {
//                    if (add) {
//                        if (overrideExisting) {
//                            context.getSession().out().print(NMsg.ofC("adding config (with override) ####%s####%n", NdbUtils.coalesce(name.getConfigName(), "default"));
//                        } else {
//                            context.getSession().out().print(NMsg.ofC("adding config ####%s####%n", NdbUtils.coalesce(name.getConfigName(), "default"));
//                        }
//                    } else {
//                        if (overrideExisting) {
//                            context.getSession().out().print(NMsg.ofC("updating config (with override) ####%s####%n", NdbUtils.coalesce(name.getConfigName(), "default"));
//                        } else {
//                            context.getSession().out().print(NMsg.ofC("updating config ####%s####%n", NdbUtils.coalesce(name.getConfigName(), "default"));
//                        }
//                    }
//                }
//            } else {
//                RemoteMysqlDatabaseConfigService r = c.getDatabaseOrCreate(name.getDatabaseName());
//                if (localName != null) {
//                    someUpdates = true;
//                    r.getConfig().setLocalName(localName.toString());
//                }
//                if (remoteName != null) {
//                    someUpdates = true;
//                    r.getConfig().setRemoteName(remoteName.toString());
//                }
//                if (server != null) {
//                    someUpdates = true;
//                    r.getConfig().setServer(server);
//                }
//                if (context.getSession().isPlainTrace()) {
//                    if (add) {
//                        if (overrideExisting) {
//                            context.getSession().out().print(NMsg.ofC("adding db (with override) ####%s####%n", r.getFullName());
//                        } else {
//                            context.getSession().out().print(NMsg.ofC("adding db ####%s####%n", r.getFullName());
//                        }
//                    } else {
//                        if (overrideExisting) {
//                            context.getSession().out().print(NMsg.ofC("updating db (with override) ####%s####%n", r.getFullName());
//                        } else {
//                            context.getSession().out().print(NMsg.ofC("updating db ####%s####%n", r.getFullName());
//                        }
//                    }
//                }
//            }
//            if (!someUpdates) {
//                throw new NutsExecutionException(context.getWorkspace(), "Nothing to save", 2);
//            }
//
//            c.saveConfig();
//        }
//    }
//
//    public void remove(NutsCommandLine commandLine) {
//        commandLine.setCommandName("mysql --local remove");
//        AtName name = null;
//        NutsArgument a;
//        while (commandLine.hasNext()) {
//             if (commandLine.isNextOption()) {
//                switch (commandLine.peek().get(session).getKey().getString().get(session)) {
//                    case "--name": {
//                        if (name == null) {
//                            name = AtName.nextAppOption(commandLine);
//                        } else {
//                            commandLine.throwUnexpectedArgument("already defined");
//                        }
//                        break;
//                    }
//                    default: {
//                        context.configureLast(commandLine);
//                    }
//                }
//            } else {
//                if (name == null) {
//                    name = AtName.nextAppNonOption(commandLine);
//                } else {
//                    commandLine.throwUnexpectedArgument("already defined");
//                }
//                commandLine.throwUnexpectedArgument();
//            }
//        }
//        if (name == null) {
//            name = new AtName("");
//        }
//        if (name.getDatabaseName().isEmpty()) {
//            loadRemoteMysqlConfig(name.getConfigName()).removeConfig();
//        } else {
//            RemoteMysqlConfigService c = loadRemoteMysqlConfig(name.getConfigName());
//            c.getDatabaseOrError(name.getDatabaseName()).remove();
//            c.saveConfig();
//        }
//    }
//
//    private void pushOrPull(NutsCommandLine commandLine, String command) {
//        commandLine.setCommandName("mysql --remote " + command);
//        AtName name = null;
//        String path = null;
//        NutsArgument a;
//        while (commandLine.hasNext()) {
//            if (commandLine.isNextOption()) {
//                switch (commandLine.peek().get(session).getKey().getString().get(session)) {
//                    case "--name": {
//                        if (name == null) {
//                            name = AtName.nextAppOption(commandLine);
//                        } else {
//                            commandLine.throwUnexpectedArgument("already defined");
//                        }
//                        break;
//                    }
//                    case "--path": {
//                        if (path == null) {
//                            switch (command) {
//                                case "push":
//                                case "pull": {
//                                    commandLine.throwUnexpectedArgument();
//                                    break;
//                                }
//                            }
//                            path = commandLine.nextString().getStringValue().get(session);
//                        } else {
//                            commandLine.throwUnexpectedArgument("already defined");
//                        }
//                        break;
//                    }
//                    default:{
//                        context.configureLast(commandLine);
//                    }
//                }
//            } else {
//                if (name == null) {
//                    name = AtName.nextAppNonOption(commandLine);
//                } else if (path == null) {
//                    switch (command) {
//                        case "push":
//                        case "pull": {
//                            commandLine.throwUnexpectedArgument();
//                            break;
//                        }
//                    }
//                    path = commandLine.next().getString();
//                } else {
//                    commandLine.throwUnexpectedArgument();
//                }
//            }
//        }
//        if (name == null) {
//            name = new AtName("");
//        }
//        RemoteMysqlConfigService c = loadRemoteMysqlConfig(name.getConfigName());
//        RemoteMysqlDatabaseConfigService d = c.getDatabaseOrError(name.getDatabaseName());
//        switch (command) {
//            case "push": {
////                if (path == null) {
////                    commandLine.required("missing --path");
////                }
//                d.push(path, true);
//                break;
//            }
//            case "pull": {
////                if (path == null) {
////                    path = d.getName() + new SimpleDateFormat("yyyy-MM-dd-HHmmss-SSS").format(new Date());
////                }
//                d.pull(path, true, true);
//                break;
//            }
//        }
//    }
//
//    private void pull(NutsCommandLine args) {
//        pushOrPull(args, "pull");
//    }
//
//    private void push(NutsCommandLine args) {
//        pushOrPull(args, "push");
//    }
//
//    public RemoteMysqlConfigService[] listRemoteConfig() {
//        List<RemoteMysqlConfigService> all = new ArrayList<>();
//        if(Files.isDirectory(sharedConfigFolder)) {
//            try (DirectoryStream<Path> configFiles = Files.newDirectoryStream(sharedConfigFolder, x -> x.getFileName().toString().endsWith(RemoteMysqlConfigService.CLIENT_CONFIG_EXT))) {
//                for (Path file1 : configFiles) {
//                    try {
//                        String nn = file1.getFileName().toString();
//                        RemoteMysqlConfigService c = loadRemoteMysqlConfig(nn.substring(0, nn.length() - RemoteMysqlConfigService.CLIENT_CONFIG_EXT.length()));
//                        all.add(c);
//                    } catch (Exception ex) {
//                        //ignore
//                    }
//                }
//            } catch (IOException ex) {
//                throw new UncheckedIOException(ex);
//            }
//        }
//        return all.toArray(new RemoteMysqlConfigService[0]);
//    }
//
//    public RemoteMysqlConfigService loadRemoteMysqlConfig(String name) {
//        RemoteMysqlConfigService t = new RemoteMysqlConfigService(name, this);
//        t.loadConfig();
//        return t;
//    }
//
//    public RemoteMysqlConfigService createMysqlConfig(String name) {
//        RemoteMysqlConfigService t = new RemoteMysqlConfigService(name, this);
//        t.setConfig(new RemoteMysqlConfig());
//        return t;
//    }
//
//    public RemoteMysqlConfigService loadOrCreateRemoteMysqlConfig(String name) {
//        RemoteMysqlConfigService t = new RemoteMysqlConfigService(name, this);
//        if (t.existsConfig()) {
//            t.loadConfig();
//        } else {
//            t.setConfig(new RemoteMysqlConfig());
//        }
//        return t;
//    }
//
//    public NSession getContext() {
//        return context;
//    }
//
//    public void setContext(NSession session) {
//        this.session = session;
//    }
//}
