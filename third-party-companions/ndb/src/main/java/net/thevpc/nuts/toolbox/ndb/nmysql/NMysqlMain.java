package net.thevpc.nuts.toolbox.ndb.nmysql;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.toolbox.ndb.NdbSupport;
import net.thevpc.nuts.toolbox.ndb.nmysql.local.LocalMysqlConfigService;
import net.thevpc.nuts.toolbox.ndb.nmysql.local.LocalMysqlDatabaseConfigService;
import net.thevpc.nuts.toolbox.ndb.nmysql.local.config.LocalMysqlConfig;
import net.thevpc.nuts.toolbox.ndb.nmysql.local.config.LocalMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.ndb.nmysql.remote.RemoteMysqlConfigService;
import net.thevpc.nuts.toolbox.ndb.nmysql.remote.RemoteMysqlDatabaseConfigService;
import net.thevpc.nuts.toolbox.ndb.nmysql.remote.config.RemoteMysqlConfig;
import net.thevpc.nuts.toolbox.ndb.nmysql.remote.config.RemoteMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.ndb.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.nmysql.util.MysqlUtils;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;
import net.thevpc.nuts.util.NutsRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NMysqlMain implements NdbSupport {


    @Override
    public void run(NutsApplicationContext context, NutsCommandLine commandLine) {
        NutsSession session = context.getSession();
        NMySqlService service = new NMySqlService(context);
        while (commandLine.hasNext()) {
            switch (commandLine.peek().get(session).key()) {
                case "add":
                case "create": {
                    commandLine.skip();
                    createOrUpdate(commandLine, true, service);
                    return;
                }
                case "update": {
                    commandLine.skip();
                    createOrUpdate(commandLine, false, service);
                    return;
                }
                case "remove": {
                    commandLine.skip();
                    runRemove(commandLine, service);
                    return;
                }
                case "list": {
                    commandLine.skip();
                    runList(commandLine, service, false);
                    return;
                }
                case "desc": {
                    commandLine.skip();
                    runList(commandLine, service, true);
                    return;
                }
                case "backup": {
                    commandLine.skip();
                    runBackupOrRestore(commandLine, true, service);
                    return;
                }
                case "restore": {
                    commandLine.skip();
                    runBackupOrRestore(commandLine, false, service);
                    return;
                }
                case "pull": {
                    commandLine.skip();
                    runPushOrPull(commandLine, true, service);
                    return;
                }
                case "push": {
                    commandLine.skip();
                    runPushOrPull(commandLine, false, service);
                    return;
                }
                default: {
                    context.configureLast(commandLine);
                }
            }
        }
        runList(commandLine, service, false);
//        String[] args = context.getArguments();
//        if (args.length == 0) {
//            throw new NutsExecutionException(context.getWorkspace(), "Expected --remote or --local", 2);
//        }
//        if (args[0].equals("--remote") || args[0].equals("-c")) {
//            RemoteMysql m = new RemoteMysql(context);
//            m.runArgs(Arrays.copyOfRange(args, 1, args.length));
//        } else if (args[0].equals("--local") || args[0].equals("-s")) {
//            LocalMysql m = new LocalMysql(context);
//            m.runArgs(Arrays.copyOfRange(args, 1, args.length));
//        } else {
//            LocalMysql m = new LocalMysql(context);
//            m.runArgs(args);
//        }
    }


    private void runPushOrPull(NutsCommandLine commandLine, boolean pull, NMySqlService service) {
        NutsSession session = service.getContext().getSession();
        commandLine.setCommandName("mysql --remote " + (pull ? "pull" : "push"));
        class Data {
            AtName name = null;
            String path = null;
        }
        Data d = new Data();
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--name": {
                        commandLine.withNextString((v, aa, s) -> {
                            if (d.name == null) {
                                d.name = new AtName(v);
                            } else {
                                commandLine.throwUnexpectedArgument(NutsMessage.ofPlain("already defined"), session);
                            }
                        }, session);
                        break;
                    }
                    case "--path": {
                        commandLine.withNextString((v, aa, s) -> {
                            if (d.path == null) {
                                d.path = v;
                            } else {
                                commandLine.throwUnexpectedArgument(NutsMessage.ofPlain("already defined"), session);
                            }
                        }, session);
                        break;
                    }
                    default: {
                        service.getContext().configureLast(commandLine);
                    }
                }
            } else {
                if (d.name == null) {
                    d.name = new AtName(commandLine.next().get().asString().get());
                } else if (d.path == null) {
                    d.path = commandLine.next().get().asString().get();
                } else {
                    commandLine.throwUnexpectedArgument(session);
                }
            }
        }
        if (d.name == null) {
            d.name = new AtName("");
        }
        RemoteMysqlConfigService c = service.loadRemoteMysqlConfig(d.name.getConfigName(), NutsOpenMode.OPEN_OR_ERROR);
        RemoteMysqlDatabaseConfigService d1 = c.getDatabase(d.name.getDatabaseName(), NutsOpenMode.OPEN_OR_ERROR);
        if (pull) {
            d1.pull(d.path, true, true);
        } else {
            d1.push(d.path, true);
        }
    }

    private void runBackupOrRestore(NutsCommandLine commandLine, boolean backup, NMySqlService service) {
        NutsSession session = service.getContext().getSession();
        commandLine.setCommandName("mysql " + (backup ? "backup" : "restore"));
        NutsRef<AtName> name = NutsRef.ofNull(AtName.class);
        NutsRef<String> path = NutsRef.ofNull(String.class);
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--name": {
                        commandLine.withNextString((v, a, s) -> {
                            if (name.isNull()) {
                                name.set(new AtName(a.getStringValue().get(session)));
                            } else {
                                commandLine.throwUnexpectedArgument(NutsMessage.ofPlain("already defined"), session);
                            }
                        }, session);
                        break;
                    }
                    case "--path": {
                        commandLine.withNextString((v, a, s) -> {
                            if (path.isNull()) {
                                path.set(v);
                            } else {
                                commandLine.throwUnexpectedArgument(NutsMessage.ofPlain("already defined"), session);
                            }
                        }, session);
                        break;
                    }
                    default: {
                        service.getContext().configureLast(commandLine);
                    }
                }
            } else {
                if (name.isNull()) {
                    name.set(new AtName(commandLine.next().get(session).asString().get(session)));
                } else if (path.isNull()) {
                    path.set(commandLine.next().flatMap(NutsValue::asString).get(session));
                } else {
                    commandLine.throwUnexpectedArgument(session);
                }
            }
        }
        if (name.isNull()) {
            name.set(new AtName(""));
        }
        LocalMysqlConfigService c = service.loadLocalMysqlConfig(name.get().getConfigName(), NutsOpenMode.OPEN_OR_ERROR);
        LocalMysqlDatabaseConfigService d = c.getDatabase(name.get().getDatabaseName(), NutsOpenMode.OPEN_OR_ERROR);
        NutsSession s = session;
        if (backup) {
            if (path.isNull()) {
                path.set(d.getDatabaseName() + "-" + MysqlUtils.newDateString());
            }
            LocalMysqlDatabaseConfigService.ArchiveResult result = d.backup(path.get());
            s.out().printlnf(result);
        } else {
            if (path.isNull()) {
                commandLine.throwMissingArgument(NutsMessage.ofPlain("missing --path"), session);
            }
            LocalMysqlDatabaseConfigService.RestoreResult result = d.restore(path.get());
            s.out().printlnf(result);
        }
    }


    private void createOrUpdate(NutsCommandLine commandLine, boolean add, NMySqlService service) {
        NutsSession session = service.getContext().getSession();
        commandLine.setCommandName("mysql " + (add ? "add" : "set"));
        class Data{
            AtName name = null;

            NutsArgument a;
            Integer c_shutdown_wait_time = null;
            Integer c_startup_wait_time = null;
            Boolean c_kill = null;
            String c_archive_folder = null;
            String c_running_folder = null;
            String c_log_file = null;
            String c_mysql_command = null;
            String c_mysqldump_command = null;
            String user = null;
            String password = null;
            String dbname = null;
            AtName forRemote_remoteName = null;
            AtName forRemote_localName = null;
            String forRemote_server = null;
            Boolean expectedRemote = null;
            boolean askPassword = false;
        }
        Data d=new Data();
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--name": {
                        commandLine.withNextString((v,a,s)->{
                            if (d.name == null) {
                                d.name = new AtName(v);
                            } else {
                                commandLine.throwUnexpectedArgument(NutsMessage.ofPlain("already defined"), session);
                            }
                        },session);
                        break;
                    }
                    case "--shutdown-wait-time": {
                        commandLine.withNextValue((v,a,s)->{
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            d.c_shutdown_wait_time = v.asInt().get(session);
                        },session);

                        break;
                    }
                    case "--startup-wait-time": {
                        commandLine.withNextValue((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            d.c_startup_wait_time = v.asInt().get(session);
                        },session);
                        break;
                    }
                    case "--backup-folder": {
                        commandLine.withNextString((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            d.c_archive_folder = v;
                        },session);
                        break;
                    }
                    case "--running-folder": {
                        commandLine.withNextString((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            d.c_running_folder = v;
                        },session);
                        break;
                    }
                    case "--log-file": {
                        commandLine.withNextString((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            d.c_log_file = v;
                        },session);
                        break;
                    }
                    case "--mysql-command": {
                        commandLine.withNextString((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            d.c_mysql_command = v;
                        },session);
                        break;
                    }
                    case "--mysqldump-command": {
                        commandLine.withNextString((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            d.c_mysqldump_command = v;
                        },session);
                        break;
                    }
                    case "--kill": {
                        commandLine.withNextBoolean((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            d.c_kill = v;
                        },session);
                        break;
                    }
                    case "--user": {
                        commandLine.withNextString((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            d.user = v;
                        },session);
                        break;
                    }
                    case "--password": {
                        commandLine.withNextString((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            d.password = v;
                        },session);
                        break;
                    }
                    case "--ask-password": {
                        commandLine.withNextBoolean((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            d.askPassword = v;
                        },session);
                        break;
                    }
                    case "--db": {
                        commandLine.withNextString((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            d.dbname = v;
                        },session);
                        break;
                    }
                    case "--local-name": {
                        commandLine.withNextString((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = true;
                            } else if (!d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            if (d.forRemote_localName == null) {
                                d.forRemote_localName = new AtName(v);
                            } else {
                                commandLine.throwUnexpectedArgument(NutsMessage.ofPlain("already defined"), session);
                            }
                        },session);
                        break;
                    }
                    case "--remote-name": {
                        commandLine.withNextString((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = true;
                            } else if (!d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            if (d.forRemote_remoteName == null) {
                                d.forRemote_remoteName = new AtName(v);
                            } else {
                                commandLine.throwUnexpectedArgument(NutsMessage.ofPlain("already defined"), session);
                            }
                        },session);
                        break;
                    }
                    case "--server": {
                        commandLine.withNextString((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = true;
                            } else if (!d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                            if (d.forRemote_server == null) {
                                d.forRemote_server = v;
                            } else {
                                commandLine.throwUnexpectedArgument(NutsMessage.ofPlain("already defined"), session);
                            }
                        },session);
                        break;
                    }
                    case "--local": {
                        commandLine.withNextBoolean((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = !v;
                            } else if (d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                        },session);
                        break;
                    }
                    case "--remote": {
                        commandLine.withNextBoolean((v,a,s)-> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = v;
                            } else if (!d.expectedRemote) {
                                commandLine.throwUnexpectedArgument(session);
                            }
                        },session);
                        break;
                    }
                    default: {
                        if (commandLine.peek().get(session).isNonOption()) {
                            if (d.name == null) {
                                d.name = AtName.nextAppOption(commandLine, session);
                            } else {
                                commandLine.throwUnexpectedArgument(NutsMessage.ofPlain("already defined"), session);
                            }
                        } else {
                            service.getContext().configureLast(commandLine);
                        }
                        break;
                    }
                }
            } else {
                if (d.name == null) {
                    d.name = AtName.nextAppNonOption(commandLine, session);
                } else {
                    commandLine.throwUnexpectedArgument(session);
                }
            }
        }
        if (d.name == null) {
            d.name = new AtName("");
        }
        if (d.expectedRemote == null) {
            d.expectedRemote = false;
        }
        if (d.expectedRemote && d.forRemote_server == null) {
            commandLine.throwMissingArgument(NutsMessage.ofPlain("required --server option"), session);
        }
        NutsTexts factory = NutsTexts.of(session);
        if (commandLine.isExecMode()) {
            if (!d.expectedRemote) {
                LocalMysqlConfigService c = service.loadLocalMysqlConfig(d.name.getConfigName(), add ? NutsOpenMode.OPEN_OR_CREATE : NutsOpenMode.OPEN_OR_ERROR);
                boolean overrideExisting = false;
                if (add) {
                    if (d.name.getDatabaseName().isEmpty()) {
                        if (c.getDatabase(d.name.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL) != null) {
                            overrideExisting = true;
                            if (!session.getTerminal().ask()
                                    .resetLine()
                                    .forBoolean(
                                            NutsMessage.ofCstyle(
                                                    "already exists %s. override?", factory.ofStyled(d.name.toString(),
                                                            NutsTextStyle.primary3()
                                                    ))
                                    )
                                    .setDefaultValue(false).getBooleanValue()) {
                                throw new NutsExecutionException(session, NutsMessage.ofCstyle("already exists %s", d.name), 2);
                            }
                        }
                    } else {
                        if (c.getDatabase(d.name.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL) != null) {
                            overrideExisting = true;
                            if (!session.getTerminal().ask()
                                    .resetLine()
                                    .forBoolean(
                                            NutsMessage.ofCstyle("already exists %s. override?", factory.ofStyled(d.name.toString(), NutsTextStyle.primary3()
                                            )))
                                    .setDefaultValue(false).getBooleanValue()) {
                                throw new NutsExecutionException(session, NutsMessage.ofCstyle("already exists %s", d.name), 2);
                            }
                        }
                    }
                } else {
                    if (d.name.getDatabaseName().isEmpty()) {
                        if (c.getDatabase(d.name.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL) == null) {
                            throw new NutsExecutionException(session, NutsMessage.ofCstyle("not found %s", d.name), 2);
                        }
                    } else {
                        if (c.getDatabase(d.name.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL) == null) {
                            throw new NutsExecutionException(session, NutsMessage.ofCstyle("not found  %s", d.name), 2);
                        }
                    }
                }
                boolean someUpdates = false;
                if (d.name.getDatabaseName().isEmpty()) {
                    if (d.c_shutdown_wait_time != null) {
                        someUpdates = true;
                        c.getConfig().setShutdownWaitTime(d.c_shutdown_wait_time);
                    }
                    if (d.c_shutdown_wait_time != null) {
                        someUpdates = true;
                        c.getConfig().setStartupWaitTime(d.c_startup_wait_time);
                    }
                    if (d.c_archive_folder != null) {
                        someUpdates = true;
                        c.getConfig().setBackupFolder(d.c_archive_folder);
                    }
                    if (d.c_log_file != null) {
                        someUpdates = true;
                        c.getConfig().setLogFile(d.c_log_file);
                    }
                    if (d.c_running_folder != null) {
                        someUpdates = true;
                        c.getConfig().setRunningFolder(d.c_running_folder);
                    }
                    if (d.c_mysql_command != null) {
                        someUpdates = true;
                        c.getConfig().setMysqlCommand(d.c_mysql_command);
                    }
                    if (d.c_mysqldump_command != null) {
                        someUpdates = true;
                        c.getConfig().setMysqldumpCommand(d.c_mysqldump_command);
                    }
                    if (d.c_kill != null) {
                        someUpdates = true;
                        c.getConfig().setKill(d.c_kill);
                    }
                    if (someUpdates && session.isPlainTrace()) {
                        if (add) {
                            if (overrideExisting) {
                                session.out().printf("adding local config (with override) %s%n",
                                        factory.ofStyled(
                                                NdbUtils.coalesce(d.name.getConfigName(), "default"), NutsTextStyle.primary3())
                                );
                            } else {
                                session.out().printf("adding local config %s%n",
                                        factory.ofStyled(
                                                NdbUtils.coalesce(d.name.getConfigName(), "default"), NutsTextStyle.primary3()));
                            }
                        } else {
                            if (overrideExisting) {
                                session.out().printf("updating local config (with override) %s%n",
                                        factory.ofStyled(NdbUtils.coalesce(d.name.getConfigName(), "default"), NutsTextStyle.primary3()));
                            } else {
                                session.out().printf("updating local config %s%n",
                                        factory.ofStyled(
                                                NdbUtils.coalesce(d.name.getConfigName(), "default"), NutsTextStyle.primary3()));
                            }
                        }
                    }
                } else {
                    LocalMysqlDatabaseConfigService r = c.getDatabase(d.name.getDatabaseName(), NutsOpenMode.OPEN_OR_CREATE);
                    if (d.user != null) {
                        someUpdates = true;
                        r.getConfig().setUser(d.user);
                    }
                    if (d.password != null) {
                        someUpdates = true;
                        r.getConfig().setPassword(
                                new String(session.security().createCredentials(d.password.toCharArray(), true,
                                        null))
                        );
                    }
                    if (add && d.dbname == null) {
                        d.dbname = d.name.getDatabaseName();
                    }
                    if (d.dbname != null) {
                        someUpdates = true;
                        r.getConfig().setDatabaseName(d.dbname);
                    }
                    if (d.askPassword || (!add && d.password == null)) {
                        r.getConfig().setPassword(new String(session.security()
                                        .createCredentials(session.getTerminal().readPassword("Password"), true,
                                                null)
                                )
                        );
                    }
                    if (r.getConfig().getUser() == null) {
                        throw new NutsExecutionException(session, NutsMessage.ofPlain("missing --user"), 2);
                    }
                    if (r.getConfig().getPassword() == null) {
                        throw new NutsExecutionException(session, NutsMessage.ofPlain("missing --password"), 2);
                    }
                    if (r.getConfig().getDatabaseName() == null) {
                        throw new NutsExecutionException(session, NutsMessage.ofPlain("missing --name"), 2);
                    }
                    if (someUpdates && session.isPlainTrace()) {
                        if (add) {
                            if (overrideExisting) {
                                session.out().printf("adding local instance (with override) %s%n",
                                        factory.ofStyled(r.getFullName(), NutsTextStyle.primary3()));
                            } else {
                                session.out().printf("adding local instance %s%n",
                                        factory.ofStyled(r.getFullName(), NutsTextStyle.primary3()));
                            }
                        } else {
                            if (overrideExisting) {
                                session.out().printf("updating local instance (with override) %s%n",
                                        factory.ofStyled(r.getFullName(), NutsTextStyle.primary3())
                                );
                            } else {
                                session.out().printf("updating local instance %s%n",
                                        factory.ofStyled(r.getFullName(), NutsTextStyle.primary3())
                                );
                            }
                        }
                    }
                }
                if (!someUpdates) {
                    throw new NutsExecutionException(session, NutsMessage.ofPlain("nothing to save"), 2);
                }

                c.saveConfig();
            } else {
                if (d.forRemote_localName == null && d.forRemote_remoteName == null) {
                    d.forRemote_localName = d.name;
                    d.forRemote_remoteName = d.name;
                } else if (d.forRemote_localName == null) {
                    d.forRemote_localName = d.forRemote_remoteName;
                } else if (d.forRemote_remoteName == null) {
                    d.forRemote_remoteName = d.forRemote_localName;
                }
                service.loadLocalMysqlConfig(d.forRemote_localName.getConfigName(), NutsOpenMode.OPEN_OR_ERROR)
                        .getDatabase(d.forRemote_localName.getDatabaseName(), NutsOpenMode.OPEN_OR_ERROR)
                ;
                RemoteMysqlConfigService c = service.loadRemoteMysqlConfig(d.name.getConfigName(), NutsOpenMode.OPEN_OR_CREATE);
                boolean overrideExisting = false;
                if (add) {
                    if (d.name.getDatabaseName().isEmpty()) {
                        if (c.getDatabase(d.name.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL) != null) {
                            overrideExisting = true;
                            if (!session.getTerminal().ask()
                                    .resetLine()
                                    .forBoolean(
                                            NutsMessage.ofCstyle("already exists %s. override?", factory.ofStyled(d.name.toString(), NutsTextStyle.primary3())
                                            ))
                                    .setDefaultValue(false).getBooleanValue()) {
                                throw new NutsExecutionException(session, NutsMessage.ofCstyle("already exists %s", d.name), 2);
                            }
                        }
                    } else {
                        if (c.getDatabase(d.name.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL) != null) {
                            overrideExisting = true;
                            if (!session.getTerminal().ask()
                                    .resetLine()
                                    .forBoolean(
                                            NutsMessage.ofCstyle("already exists %s. override?", factory.ofStyled(d.name.toString(), NutsTextStyle.primary3())
                                            ))
                                    .setDefaultValue(false).getBooleanValue()) {
                                throw new NutsExecutionException(session, NutsMessage.ofCstyle("already exists %s", d.name), 2);
                            }
                        }
                    }
                } else {
                    if (d.name.getDatabaseName().isEmpty()) {
                        if (c.getDatabase(d.name.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL) == null) {
                            throw new NutsExecutionException(session, NutsMessage.ofCstyle("not found %s", d.name), 2);
                        }
                    } else {
                        if (c.getDatabase(d.name.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL) == null) {
                            throw new NutsExecutionException(session, NutsMessage.ofPlain("not found  %s" + d.name), 2);
                        }
                    }
                }
                boolean someUpdates = false;
                if (d.name.getDatabaseName().isEmpty()) {
//                if (c_mysqldump_command != null) {
//                    someUpdates = true;
//                    c.getConfig().setMysqldumpCommand(c_mysqldump_command);
//                }
                    if (someUpdates && session.isPlainTrace()) {
                        if (add) {
                            if (overrideExisting) {
                                session.out().printf("adding remote config (with override) %s%n",
                                        factory.ofStyled(NdbUtils.coalesce(d.name.getConfigName(), "default"), NutsTextStyle.primary3()));
                            } else {
                                session.out().printf("adding remote config %s%n",
                                        factory.ofStyled(NdbUtils.coalesce(d.name.getConfigName(), "default"), NutsTextStyle.primary3()));
                            }
                        } else {
                            if (overrideExisting) {
                                session.out().printf("updating remote config (with override) %s%n",
                                        factory.ofStyled(NdbUtils.coalesce(d.name.getConfigName(), "default"), NutsTextStyle.primary3()));
                            } else {
                                session.out().printf("updating remote config %s%n",
                                        factory.ofStyled(NdbUtils.coalesce(d.name.getConfigName(), "default"), NutsTextStyle.primary3()));
                            }
                        }
                    }
                } else {
                    RemoteMysqlDatabaseConfigService r = c.getDatabaseOrCreate(d.name.getDatabaseName());
                    if (d.forRemote_localName != null) {
                        someUpdates = true;
                        r.getConfig().setLocalName(d.forRemote_localName.toString());
                    }
                    if (d.forRemote_remoteName != null) {
                        someUpdates = true;
                        r.getConfig().setRemoteName(d.forRemote_remoteName.toString());
                    }
                    if (d.forRemote_server != null) {
                        someUpdates = true;
                        r.getConfig().setServer(d.forRemote_server);
                    }
                    if (someUpdates && session.isPlainTrace()) {
                        if (add) {
                            if (overrideExisting) {
                                session.out().printf("adding remote instance (with override) %s%n",
                                        factory.ofStyled(r.getFullName(), NutsTextStyle.primary3()));
                            } else {
                                session.out().printf("adding remote instance %s%n",
                                        factory.ofStyled(r.getFullName(), NutsTextStyle.primary3()));
                            }
                        } else {
                            if (overrideExisting) {
                                session.out().printf("updating remote instance (with override) %s%n",
                                        factory.ofStyled(r.getFullName(), NutsTextStyle.primary3())
                                );
                            } else {
                                session.out().printf("updating remote instance %s%n",
                                        factory.ofStyled(r.getFullName(), NutsTextStyle.primary3()));
                            }
                        }
                    }
                }
                if (!someUpdates) {
                    throw new NutsExecutionException(session, NutsMessage.ofPlain("nothing to save"), 2);
                }

                c.saveConfig();
            }
        }
    }

    public void runRemove(NutsCommandLine commandLine, NMySqlService service) {
        NutsSession session = service.getContext().getSession();
        commandLine.setCommandName("mysql remove");
        List<AtName> localNames = new ArrayList<>();
        List<AtName> remoteNames = new ArrayList<>();
        boolean currentLocal = true;
        NutsArgument a;

        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--remote": {
                        commandLine.nextBoolean();
                        currentLocal = false;
                        break;
                    }
                    case "--local": {
                        commandLine.nextBoolean();
                        currentLocal = true;
                        break;
                    }
                    case "--name": {
                        if (currentLocal) {
                            localNames.add(AtName.nextAppOption(commandLine, session));
                        } else {
                            remoteNames.add(AtName.nextAppOption(commandLine, session));
                        }
                        break;
                    }
                    default: {
                        service.getContext().configureLast(commandLine);
                    }
                }
            } else {
                if (currentLocal) {
                    localNames.add(AtName.nextAppNonOption(commandLine, session));
                } else {
                    remoteNames.add(AtName.nextAppNonOption(commandLine, session));
                }
            }
        }
        if (localNames.isEmpty() && remoteNames.isEmpty()) {
            commandLine.peek().get(session);
        }
        for (AtName localName : localNames) {
            if (localName.getDatabaseName().isEmpty()) {
                service.loadLocalMysqlConfig(localName.getConfigName(), NutsOpenMode.OPEN_OR_ERROR).removeConfig();
            } else {
                LocalMysqlConfigService c = service.loadLocalMysqlConfig(localName.toString(), NutsOpenMode.OPEN_OR_NULL);
                if (c != null) {
                    LocalMysqlDatabaseConfigService v = c.getDatabase(localName.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL);
                    if (v != null) {
                        v.remove();
                        c.saveConfig();
                    }
                }
            }
        }
        for (AtName remoteName : remoteNames) {
            if (remoteName.getDatabaseName().isEmpty()) {
                RemoteMysqlConfigService v = service.loadRemoteMysqlConfig(remoteName.getConfigName(), NutsOpenMode.OPEN_OR_NULL);
                if (v != null) {
                    v.removeConfig();
                }
            } else {
                RemoteMysqlConfigService c = service.loadRemoteMysqlConfig(remoteName.getConfigName(), NutsOpenMode.OPEN_OR_NULL);
                if (c != null) {
                    RemoteMysqlDatabaseConfigService v = c.getDatabase(remoteName.getDatabaseName(), NutsOpenMode.OPEN_OR_NULL);
                    if (v != null) {
                        v.remove();
                        c.saveConfig();
                    }
                }
            }
        }
    }

    public Object toObject(String dbName, String confName, LocalMysqlDatabaseConfig config, boolean describe, boolean plain, NutsApplicationContext context) {
        NutsTexts text = NutsTexts.of(context.getSession());
        if (!describe) {
            if (plain) {
                return text.ofBuilder()
                        .append(" [local ] ", NutsTextStyle.primary4())
                        .append(dbName).append("@").append(confName, NutsTextStyle.primary4())
                        ;
            } else {
                return new Object[]{"local", dbName, confName};
            }
        } else {
            if (plain) {
                return text.ofBuilder()
                        .append(" [local ] ", NutsTextStyle.primary4())
                        .append(dbName).append("@").append(confName, NutsTextStyle.primary4())
                        .append(" db=").append(config.getDatabaseName())
                        .append(" user=").append(config.getUser());
            } else {
                return new Object[]{"local", dbName, confName, config.getUser()};
            }
        }
    }

    public Object toObject(String dbName, String confName, RemoteMysqlDatabaseConfig config, boolean describe, boolean plain, NutsApplicationContext context) {
        NutsTexts text = NutsTexts.of(context.getSession());
        if (!describe) {
            if (plain) {
                return text.ofBuilder()
                        .append(" [remote] ", NutsTextStyle.primary4())
                        .append(dbName).append("@").append(confName, NutsTextStyle.primary4())
                        ;
            } else {
                return new Object[]{"remote", dbName, confName};
            }
        } else {
            if (plain) {
                return text.ofBuilder()
                        .append(" [remote] ", NutsTextStyle.primary4())
                        .append(dbName).append("@").append(confName, NutsTextStyle.primary4())
                        .append(" local=").append(config.getLocalName())
                        .append(" remote=").append(config.getRemoteName())
                        .append(" on=").append(config.getServer())
                        ;
            } else {
                return new Object[]{"remote", dbName, confName, config.getLocalName(), config.getRemoteName(), config.getServer()};
            }
        }
    }

    public void runList(NutsCommandLine commandLine, NMySqlService service, boolean describe) {
        NutsSession session = service.getContext().getSession();
        commandLine.setCommandName("mysql list");
        List<AtName> localNames = new ArrayList<>();
        List<AtName> remoteNames = new ArrayList<>();
        Boolean expectedLocal = null;
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--local": {
                        commandLine.nextBoolean();
                        expectedLocal = true;
                        break;
                    }
                    case "--remote": {
                        commandLine.nextBoolean();
                        expectedLocal = false;
                        break;
                    }
                    default: {
                        service.getContext().configureLast(commandLine);
                    }
                }
            } else {
                if (expectedLocal == null) {
                    expectedLocal = true;
                }
                if (expectedLocal) {
                    localNames.add(AtName.nextConfigNonOption(commandLine, session));
                } else {
                    remoteNames.add(AtName.nextConfigNonOption(commandLine, session));
                }
            }
        }
        List<LocaleOrRemote> result = new ArrayList<>();
        if (localNames.isEmpty() && remoteNames.isEmpty()) {
            if (expectedLocal == null || expectedLocal) {
                for (LocalMysqlConfigService c : service.listLocalConfig()) {
                    result.add(new LocaleOrRemote(c.getName(), c.getConfig(), null));
                }
            }
            if (expectedLocal == null || !expectedLocal) {
                for (RemoteMysqlConfigService c : service.listRemoteConfig()) {
                    result.add(new LocaleOrRemote(c.getName(), null, c.getConfig()));
                }
            }
        } else {
            for (AtName localName : localNames) {
                LocalMysqlConfigService c = service.loadLocalMysqlConfig(localName.getConfigName(), NutsOpenMode.OPEN_OR_ERROR);
                result.add(new LocaleOrRemote(c.getName(), c.getConfig(), null));
            }
            for (AtName localName : remoteNames) {
                RemoteMysqlConfigService c = service.loadRemoteMysqlConfig(localName.getConfigName(), NutsOpenMode.OPEN_OR_ERROR);
                result.add(new LocaleOrRemote(c.getName(), null, c.getConfig()));
            }
        }

        if (session.isIterableOut()) {
            long index = 0;
            try {
                session.getIterableOutput().start();
                for (LocaleOrRemote cnf : result) {
                    if (cnf.local != null) {
                        for (Map.Entry<String, LocalMysqlDatabaseConfig> db : cnf.local.getDatabases().entrySet()) {
                            session.getIterableOutput().next(toObject(db.getKey(), cnf.name, db.getValue(), describe, false, service.getContext()), index++);
                        }
                    } else {
                        for (Map.Entry<String, RemoteMysqlDatabaseConfig> db : cnf.remote.getDatabases().entrySet()) {
                            session.getIterableOutput().next(toObject(db.getKey(), cnf.name, db.getValue(), describe, false, service.getContext()), index++);
                        }
                    }
                }
            } finally {
                session.getIterableOutput().complete(index);
            }
        } else {
            switch (session.getOutputFormat()) {
                case PLAIN: {
                    for (LocaleOrRemote cnf : result) {
                        if (cnf.local != null) {
                            for (Map.Entry<String, LocalMysqlDatabaseConfig> db : cnf.local.getDatabases().entrySet()) {
                                session.out().printf("%s%n",
                                        toObject(db.getKey(), cnf.name, db.getValue(), describe, true, service.getContext())
                                );
                            }
                        } else {
                            for (Map.Entry<String, RemoteMysqlDatabaseConfig> db : cnf.remote.getDatabases().entrySet()) {
                                session.out().printf("%s%n",
                                        toObject(db.getKey(), cnf.name, db.getValue(), describe, true, service.getContext())
                                );
                            }
                        }
                    }
                    break;
                }
                default: {
                    session.out().printlnf(result);
                }
            }
        }
    }

    private static class LocaleOrRemote {
        String name;
        LocalMysqlConfig local;
        RemoteMysqlConfig remote;

        public LocaleOrRemote(String name, LocalMysqlConfig local, RemoteMysqlConfig remote) {
            this.name = name;
            this.local = local;
            this.remote = remote;
        }
    }

}
