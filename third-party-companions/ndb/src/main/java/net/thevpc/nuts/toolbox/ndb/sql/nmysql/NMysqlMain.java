package net.thevpc.nuts.toolbox.ndb.sql.nmysql;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.ndb.base.CmdRedirect;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.local.LocalMysqlConfigService;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.local.LocalMysqlDatabaseConfigService;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.local.config.LocalMysqlConfig;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.local.config.LocalMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote.RemoteMysqlConfigService;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote.RemoteMysqlDatabaseConfigService;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote.config.RemoteMysqlConfig;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote.config.RemoteMysqlDatabaseConfig;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.MysqlUtils;
import net.thevpc.nuts.toolbox.ndb.sql.util.SqlConnectionInfo;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;
import net.thevpc.nuts.toolbox.ndb.sql.util.SqlHelper;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NMysqlMain extends SqlSupport<NMySqlConfig> {

    public NMysqlMain(NSession session) {
        super("mysql", NMySqlConfig.class, session, "mysql:mysql-connector-java#8.0.26", "com.mysql.cj.jdbc.Driver");
    }

    @Override
    public void run(NSession session, NCmdLine cmdLine) {
        NMySqlService service = new NMySqlService(session);
        while (cmdLine.hasNext()) {
            switch (cmdLine.peek().get(session).key()) {
                case "add":
                case "create": {
                    cmdLine.skip();
                    createOrUpdate(cmdLine, true, service);
                    return;
                }
                case "update": {
                    cmdLine.skip();
                    createOrUpdate(cmdLine, false, service);
                    return;
                }
                case "remove": {
                    cmdLine.skip();
                    runRemove(cmdLine, service);
                    return;
                }
                case "list": {
                    cmdLine.skip();
                    runList(cmdLine, service, false);
                    return;
                }
                case "desc": {
                    cmdLine.skip();
                    runList(cmdLine, service, true);
                    return;
                }
                case "backup": {
                    cmdLine.skip();
                    runBackupOrRestore(cmdLine, true, service);
                    return;
                }
                case "restore": {
                    cmdLine.skip();
                    runBackupOrRestore(cmdLine, false, service);
                    return;
                }
                case "pull": {
                    cmdLine.skip();
                    runPushOrPull(cmdLine, true, service);
                    return;
                }
                case "push": {
                    cmdLine.skip();
                    runPushOrPull(cmdLine, false, service);
                    return;
                }
                case "run-sql": {
                    cmdLine.skip();
                    runSQL(cmdLine, service);
                    return;
                }
                default: {
                    session.configureLast(cmdLine);
                }
            }
        }
        runList(cmdLine, service, false);
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


    private void runPushOrPull(NCmdLine cmdLine, boolean pull, NMySqlService service) {
        NSession session = service.getSession();
        cmdLine.setCommandName("mysql --remote " + (pull ? "pull" : "push"));
        class Data {
            AtName name = null;
            String path = null;
        }
        Data d = new Data();
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get(session).key()) {
                    case "--name": {
                        cmdLine.withNextEntry((v, aa, s) -> {
                            if (d.name == null) {
                                d.name = new AtName(v);
                            } else {
                                cmdLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    case "--path": {
                        cmdLine.withNextEntry((v, aa, s) -> {
                            if (d.path == null) {
                                d.path = v;
                            } else {
                                cmdLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    default: {
                        service.getSession().configureLast(cmdLine);
                    }
                }
            } else {
                if (d.name == null) {
                    d.name = new AtName(cmdLine.next().get().asString().get());
                } else if (d.path == null) {
                    d.path = cmdLine.next().get().asString().get();
                } else {
                    cmdLine.throwUnexpectedArgument();
                }
            }
        }
        if (d.name == null) {
            d.name = new AtName("");
        }
        RemoteMysqlConfigService c = service.loadRemoteMysqlConfig(d.name.getConfigName(), NOpenMode.OPEN_OR_ERROR);
        RemoteMysqlDatabaseConfigService d1 = c.getDatabase(d.name.getDatabaseName(), NOpenMode.OPEN_OR_ERROR);
        if (pull) {
            d1.pull(d.path, true, true);
        } else {
            d1.push(d.path, true);
        }
    }

    private void runSQL(NCmdLine cmdLine, NMySqlService service) {
        NSession session = service.getSession();
        cmdLine.setCommandName("mysql run-sql");
        NRef<AtName> name = NRef.ofNull(AtName.class);
        List<String> sql = new ArrayList<>();
        NRef<Boolean> forceShowSQL = NRef.ofNull(Boolean.class);
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get(session).key()) {
                    case "--name": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (name.isNull()) {
                                name.set(new AtName(a.getStringValue().get(session)));
                            } else {
                                cmdLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    case "--show-sql": {
                        cmdLine.withNextFlag((v, a, s) -> {
                            forceShowSQL.set(v);
                        });
                        break;
                    }
                    default: {
                        service.getSession().configureLast(cmdLine);
                    }
                }
            } else {
                if (name.isNull()) {
                    name.set(new AtName(cmdLine.next().get(session).asString().get(session)));
                } else {
                    sql.add(cmdLine.next().flatMap(NLiteral::asString).get(session));
                }
            }
        }
        if (name.isNull()) {
            name.set(new AtName(""));
        }
        LocalMysqlConfigService c = service.loadLocalMysqlConfig(name.get().getConfigName(), NOpenMode.OPEN_OR_ERROR);
        LocalMysqlDatabaseConfigService d = c.getDatabase(name.get().getDatabaseName(), NOpenMode.OPEN_OR_ERROR);
        if (sql.isEmpty()) {
            cmdLine.throwMissingArgument(NMsg.ofPlain("sql"));
        }
        SqlConnectionInfo jdbcUrl = createSqlConnectionInfo(
                (NMySqlConfig)
                        new NMySqlConfig()
                                .setHost(d.getConfig().getServer())
                                .setPort(d.getConfig().getPort())
                                .setDatabaseName(d.getConfig().getDatabaseName())
        );
        SqlHelper.runAndWaitFor(sql, jdbcUrl, forceShowSQL.get(), session);
    }

    @Override
    public SqlConnectionInfo createSqlConnectionInfo(NMySqlConfig options) {
        String url = NMsg.ofV("jdbc:mysql://${server}:${port}/${database}",
                NMaps.of(
                        "server", NOptional.of(options.getHost()).ifBlank("localhost").get(),
                        "port", NOptional.of(options.getPort()).ifBlank(3306).get(),
                        "database", NOptional.of(options.getDatabaseName()).ifBlank("test").get()
                )).toString();
        return new SqlConnectionInfo()
                .setJdbcDriver(dbDriverClass)
                .setJdbcUrl(url)
                .setProperties(null)
                .setId(dbDriverPackage)
                .setUser(options.getUser())
                .setPassword(options.getPassword())
                ;
    }

    private void runBackupOrRestore(NCmdLine cmdLine, boolean backup, NMySqlService service) {
        NSession session = service.getSession();
        cmdLine.setCommandName("mysql " + (backup ? "backup" : "restore"));
        NRef<AtName> name = NRef.ofNull(AtName.class);
        NRef<String> path = NRef.ofNull(String.class);
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get(session).key()) {
                    case "--name": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (name.isNull()) {
                                name.set(new AtName(a.getStringValue().get(session)));
                            } else {
                                cmdLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    case "--path": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (path.isNull()) {
                                path.set(v);
                            } else {
                                cmdLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    default: {
                        service.getSession().configureLast(cmdLine);
                    }
                }
            } else {
                if (name.isNull()) {
                    name.set(new AtName(cmdLine.next().get(session).asString().get(session)));
                } else if (path.isNull()) {
                    path.set(cmdLine.next().flatMap(NLiteral::asString).get(session));
                } else {
                    cmdLine.throwUnexpectedArgument();
                }
            }
        }
        if (name.isNull()) {
            name.set(new AtName(""));
        }
        LocalMysqlConfigService c = service.loadLocalMysqlConfig(name.get().getConfigName(), NOpenMode.OPEN_OR_ERROR);
        LocalMysqlDatabaseConfigService d = c.getDatabase(name.get().getDatabaseName(), NOpenMode.OPEN_OR_ERROR);
        NSession s = session;
        if (backup) {
            if (path.isNull()) {
                path.set(d.getDatabaseName() + "-" + MysqlUtils.newDateString());
            }
            LocalMysqlDatabaseConfigService.ArchiveResult result = d.backup(path.get());
            s.out().println(result);
        } else {
            if (path.isNull()) {
                cmdLine.throwMissingArgument("--path");
            }
            LocalMysqlDatabaseConfigService.RestoreResult result = d.restore(path.get());
            s.out().println(result);
        }
    }


    private void createOrUpdate(NCmdLine cmdLine, boolean add, NMySqlService service) {
        NSession session = service.getSession();
        cmdLine.setCommandName("mysql " + (add ? "add" : "set"));
        class Data {
            AtName name = null;

            NArg a;
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
        Data d = new Data();
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get(session).key()) {
                    case "--name": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (d.name == null) {
                                d.name = new AtName(v);
                            } else {
                                cmdLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    case "--shutdown-wait-time": {
                        cmdLine.withNextEntryValue((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            d.c_shutdown_wait_time = v.asInt().get(session);
                        });

                        break;
                    }
                    case "--startup-wait-time": {
                        cmdLine.withNextEntryValue((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            d.c_startup_wait_time = v.asInt().get(session);
                        });
                        break;
                    }
                    case "--backup-folder": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            d.c_archive_folder = v;
                        });
                        break;
                    }
                    case "--running-folder": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            d.c_running_folder = v;
                        });
                        break;
                    }
                    case "--log-file": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            d.c_log_file = v;
                        });
                        break;
                    }
                    case "--mysql-command": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            d.c_mysql_command = v;
                        });
                        break;
                    }
                    case "--mysqldump-command": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            d.c_mysqldump_command = v;
                        });
                        break;
                    }
                    case "--kill": {
                        cmdLine.withNextFlag((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            d.c_kill = v;
                        });
                        break;
                    }
                    case "--user": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            d.user = v;
                        });
                        break;
                    }
                    case "--password": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            d.password = v;
                        });
                        break;
                    }
                    case "--ask-password": {
                        cmdLine.withNextFlag((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            d.askPassword = v;
                        });
                        break;
                    }
                    case "--db": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = false;
                            } else if (d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            d.dbname = v;
                        });
                        break;
                    }
                    case "--local-name": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = true;
                            } else if (!d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            if (d.forRemote_localName == null) {
                                d.forRemote_localName = new AtName(v);
                            } else {
                                cmdLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    case "--remote-name": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = true;
                            } else if (!d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            if (d.forRemote_remoteName == null) {
                                d.forRemote_remoteName = new AtName(v);
                            } else {
                                cmdLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    case "--server": {
                        cmdLine.withNextEntry((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = true;
                            } else if (!d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                            if (d.forRemote_server == null) {
                                d.forRemote_server = v;
                            } else {
                                cmdLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    case "--local": {
                        cmdLine.withNextFlag((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = !v;
                            } else if (d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                        });
                        break;
                    }
                    case "--remote": {
                        cmdLine.withNextFlag((v, a, s) -> {
                            if (d.expectedRemote == null) {
                                d.expectedRemote = v;
                            } else if (!d.expectedRemote) {
                                cmdLine.throwUnexpectedArgument();
                            }
                        });
                        break;
                    }
                    default: {
                        if (cmdLine.peek().get(session).isNonOption()) {
                            if (d.name == null) {
                                d.name = AtName.nextAppOption(cmdLine, session);
                            } else {
                                cmdLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        } else {
                            service.getSession().configureLast(cmdLine);
                        }
                        break;
                    }
                }
            } else {
                if (d.name == null) {
                    d.name = AtName.nextAppNonOption(cmdLine, session);
                } else {
                    cmdLine.throwUnexpectedArgument();
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
            cmdLine.throwMissingArgument("--server");
        }
        NTexts factory = NTexts.of(session);
        if (cmdLine.isExecMode()) {
            NPrintStream out = session.out();
            if (!d.expectedRemote) {
                LocalMysqlConfigService c = service.loadLocalMysqlConfig(d.name.getConfigName(), add ? NOpenMode.OPEN_OR_CREATE : NOpenMode.OPEN_OR_ERROR);
                boolean overrideExisting = false;
                if (add) {
                    if (d.name.getDatabaseName().isEmpty()) {
                        if (c.getDatabase(d.name.getDatabaseName(), NOpenMode.OPEN_OR_NULL) != null) {
                            overrideExisting = true;
                            if (!session.getTerminal().ask()
                                    .resetLine()
                                    .forBoolean(
                                            NMsg.ofC(
                                                    "already exists %s. override?", factory.ofStyled(d.name.toString(),
                                                            NTextStyle.primary3()
                                                    ))
                                    )
                                    .setDefaultValue(false).getBooleanValue()) {
                                throw new NExecutionException(session, NMsg.ofC("already exists %s", d.name), NExecutionException.ERROR_2);
                            }
                        }
                    } else {
                        if (c.getDatabase(d.name.getDatabaseName(), NOpenMode.OPEN_OR_NULL) != null) {
                            overrideExisting = true;
                            if (!session.getTerminal().ask()
                                    .resetLine()
                                    .forBoolean(
                                            NMsg.ofC("already exists %s. override?", factory.ofStyled(d.name.toString(), NTextStyle.primary3()
                                            )))
                                    .setDefaultValue(false).getBooleanValue()) {
                                throw new NExecutionException(session, NMsg.ofC("already exists %s", d.name), NExecutionException.ERROR_2);
                            }
                        }
                    }
                } else {
                    if (d.name.getDatabaseName().isEmpty()) {
                        if (c.getDatabase(d.name.getDatabaseName(), NOpenMode.OPEN_OR_NULL) == null) {
                            throw new NExecutionException(session, NMsg.ofC("not found %s", d.name), NExecutionException.ERROR_2);
                        }
                    } else {
                        if (c.getDatabase(d.name.getDatabaseName(), NOpenMode.OPEN_OR_NULL) == null) {
                            throw new NExecutionException(session, NMsg.ofC("not found  %s", d.name), NExecutionException.ERROR_2);
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
                                out.println(NMsg.ofC("adding local config (with override) %s",
                                        factory.ofStyled(
                                                NdbUtils.coalesce(d.name.getConfigName(), "default"), NTextStyle.primary3())
                                ));
                            } else {
                                out.println(NMsg.ofC("adding local config %s",
                                        factory.ofStyled(
                                                NdbUtils.coalesce(d.name.getConfigName(), "default"), NTextStyle.primary3())));
                            }
                        } else {
                            if (overrideExisting) {
                                out.println(NMsg.ofC("updating local config (with override) %s",
                                        factory.ofStyled(NdbUtils.coalesce(d.name.getConfigName(), "default"), NTextStyle.primary3())));
                            } else {
                                out.println(NMsg.ofC("updating local config %s",
                                        factory.ofStyled(
                                                NdbUtils.coalesce(d.name.getConfigName(), "default"), NTextStyle.primary3())));
                            }
                        }
                    }
                } else {
                    LocalMysqlDatabaseConfigService r = c.getDatabase(d.name.getDatabaseName(), NOpenMode.OPEN_OR_CREATE);
                    if (d.user != null) {
                        someUpdates = true;
                        r.getConfig().setUser(d.user);
                    }
                    if (d.password != null) {
                        someUpdates = true;
                        r.getConfig().setPassword(
                                new String(NWorkspaceSecurityManager.of(session).createCredentials(d.password.toCharArray(), true,
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
                        r.getConfig().setPassword(new String(NWorkspaceSecurityManager.of(session)
                                        .createCredentials(session.getTerminal().readPassword(NMsg.ofPlain("Password")), true,
                                                null)
                                )
                        );
                    }
                    if (r.getConfig().getUser() == null) {
                        throw new NExecutionException(session, NMsg.ofPlain("missing --user"), NExecutionException.ERROR_2);
                    }
                    if (r.getConfig().getPassword() == null) {
                        throw new NExecutionException(session, NMsg.ofPlain("missing --password"), NExecutionException.ERROR_2);
                    }
                    if (r.getConfig().getDatabaseName() == null) {
                        throw new NExecutionException(session, NMsg.ofPlain("missing --name"), NExecutionException.ERROR_2);
                    }
                    if (someUpdates && session.isPlainTrace()) {
                        if (add) {
                            if (overrideExisting) {
                                out.println(NMsg.ofC("adding local instance (with override) %s",
                                        factory.ofStyled(r.getFullName(), NTextStyle.primary3())));
                            } else {
                                out.println(NMsg.ofC("adding local instance %s",
                                        factory.ofStyled(r.getFullName(), NTextStyle.primary3())));
                            }
                        } else {
                            if (overrideExisting) {
                                out.println(NMsg.ofC("updating local instance (with override) %s",
                                        factory.ofStyled(r.getFullName(), NTextStyle.primary3())
                                ));
                            } else {
                                out.println(NMsg.ofC("updating local instance %s",
                                        factory.ofStyled(r.getFullName(), NTextStyle.primary3())
                                ));
                            }
                        }
                    }
                }
                if (!someUpdates) {
                    throw new NExecutionException(session, NMsg.ofPlain("nothing to save"), NExecutionException.ERROR_2);
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
                service.loadLocalMysqlConfig(d.forRemote_localName.getConfigName(), NOpenMode.OPEN_OR_ERROR)
                        .getDatabase(d.forRemote_localName.getDatabaseName(), NOpenMode.OPEN_OR_ERROR)
                ;
                RemoteMysqlConfigService c = service.loadRemoteMysqlConfig(d.name.getConfigName(), NOpenMode.OPEN_OR_CREATE);
                boolean overrideExisting = false;
                if (add) {
                    if (d.name.getDatabaseName().isEmpty()) {
                        if (c.getDatabase(d.name.getDatabaseName(), NOpenMode.OPEN_OR_NULL) != null) {
                            overrideExisting = true;
                            if (!session.getTerminal().ask()
                                    .resetLine()
                                    .forBoolean(
                                            NMsg.ofC("already exists %s. override?", factory.ofStyled(d.name.toString(), NTextStyle.primary3())
                                            ))
                                    .setDefaultValue(false).getBooleanValue()) {
                                throw new NExecutionException(session, NMsg.ofC("already exists %s", d.name), NExecutionException.ERROR_2);
                            }
                        }
                    } else {
                        if (c.getDatabase(d.name.getDatabaseName(), NOpenMode.OPEN_OR_NULL) != null) {
                            overrideExisting = true;
                            if (!session.getTerminal().ask()
                                    .resetLine()
                                    .forBoolean(
                                            NMsg.ofC("already exists %s. override?", factory.ofStyled(d.name.toString(), NTextStyle.primary3())
                                            ))
                                    .setDefaultValue(false).getBooleanValue()) {
                                throw new NExecutionException(session, NMsg.ofC("already exists %s", d.name), NExecutionException.ERROR_2);
                            }
                        }
                    }
                } else {
                    if (d.name.getDatabaseName().isEmpty()) {
                        if (c.getDatabase(d.name.getDatabaseName(), NOpenMode.OPEN_OR_NULL) == null) {
                            throw new NExecutionException(session, NMsg.ofC("not found %s", d.name), NExecutionException.ERROR_2);
                        }
                    } else {
                        if (c.getDatabase(d.name.getDatabaseName(), NOpenMode.OPEN_OR_NULL) == null) {
                            throw new NExecutionException(session, NMsg.ofPlain("not found  %s" + d.name), NExecutionException.ERROR_2);
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
                                out.println(NMsg.ofC("adding remote config (with override) %s",
                                        factory.ofStyled(NdbUtils.coalesce(d.name.getConfigName(), "default"), NTextStyle.primary3())));
                            } else {
                                out.println(NMsg.ofC("adding remote config %s",
                                        factory.ofStyled(NdbUtils.coalesce(d.name.getConfigName(), "default"), NTextStyle.primary3())));
                            }
                        } else {
                            if (overrideExisting) {
                                out.println(NMsg.ofC("updating remote config (with override) %s",
                                        factory.ofStyled(NdbUtils.coalesce(d.name.getConfigName(), "default"), NTextStyle.primary3())));
                            } else {
                                out.println(NMsg.ofC("updating remote config %s",
                                        factory.ofStyled(NdbUtils.coalesce(d.name.getConfigName(), "default"), NTextStyle.primary3())));
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
                                out.println(NMsg.ofC("adding remote instance (with override) %s",
                                        factory.ofStyled(r.getFullName(), NTextStyle.primary3())));
                            } else {
                                out.println(NMsg.ofC("adding remote instance %s",
                                        factory.ofStyled(r.getFullName(), NTextStyle.primary3())));
                            }
                        } else {
                            if (overrideExisting) {
                                out.println(NMsg.ofC("updating remote instance (with override) %s",
                                        factory.ofStyled(r.getFullName(), NTextStyle.primary3()))
                                );
                            } else {
                                out.println(NMsg.ofC("updating remote instance %s",
                                        factory.ofStyled(r.getFullName(), NTextStyle.primary3())));
                            }
                        }
                    }
                }
                if (!someUpdates) {
                    throw new NExecutionException(session, NMsg.ofPlain("nothing to save"), NExecutionException.ERROR_2);
                }

                c.saveConfig();
            }
        }
    }

    public void runRemove(NCmdLine cmdLine, NMySqlService service) {
        NSession session = service.getSession();
        cmdLine.setCommandName("mysql remove");
        List<AtName> localNames = new ArrayList<>();
        List<AtName> remoteNames = new ArrayList<>();
        boolean currentLocal = true;
        NArg a;

        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get(session).key()) {
                    case "--remote": {
                        cmdLine.nextFlag();
                        currentLocal = false;
                        break;
                    }
                    case "--local": {
                        cmdLine.nextFlag();
                        currentLocal = true;
                        break;
                    }
                    case "--name": {
                        if (currentLocal) {
                            localNames.add(AtName.nextAppOption(cmdLine, session));
                        } else {
                            remoteNames.add(AtName.nextAppOption(cmdLine, session));
                        }
                        break;
                    }
                    default: {
                        service.getSession().configureLast(cmdLine);
                    }
                }
            } else {
                if (currentLocal) {
                    localNames.add(AtName.nextAppNonOption(cmdLine, session));
                } else {
                    remoteNames.add(AtName.nextAppNonOption(cmdLine, session));
                }
            }
        }
        if (localNames.isEmpty() && remoteNames.isEmpty()) {
            cmdLine.peek().get(session);
        }
        for (AtName localName : localNames) {
            if (localName.getDatabaseName().isEmpty()) {
                service.loadLocalMysqlConfig(localName.getConfigName(), NOpenMode.OPEN_OR_ERROR).removeConfig();
            } else {
                LocalMysqlConfigService c = service.loadLocalMysqlConfig(localName.toString(), NOpenMode.OPEN_OR_NULL);
                if (c != null) {
                    LocalMysqlDatabaseConfigService v = c.getDatabase(localName.getDatabaseName(), NOpenMode.OPEN_OR_NULL);
                    if (v != null) {
                        v.remove();
                        c.saveConfig();
                    }
                }
            }
        }
        for (AtName remoteName : remoteNames) {
            if (remoteName.getDatabaseName().isEmpty()) {
                RemoteMysqlConfigService v = service.loadRemoteMysqlConfig(remoteName.getConfigName(), NOpenMode.OPEN_OR_NULL);
                if (v != null) {
                    v.removeConfig();
                }
            } else {
                RemoteMysqlConfigService c = service.loadRemoteMysqlConfig(remoteName.getConfigName(), NOpenMode.OPEN_OR_NULL);
                if (c != null) {
                    RemoteMysqlDatabaseConfigService v = c.getDatabase(remoteName.getDatabaseName(), NOpenMode.OPEN_OR_NULL);
                    if (v != null) {
                        v.remove();
                        c.saveConfig();
                    }
                }
            }
        }
    }

    public Object toObject(String dbName, String confName, LocalMysqlDatabaseConfig config, boolean describe, boolean plain, NSession session) {
        NTexts text = NTexts.of(session);
        if (!describe) {
            if (plain) {
                return text.ofBuilder()
                        .append(" [local ] ", NTextStyle.primary4())
                        .append(dbName).append("@").append(confName, NTextStyle.primary4())
                        ;
            } else {
                return new Object[]{"local", dbName, confName};
            }
        } else {
            if (plain) {
                return text.ofBuilder()
                        .append(" [local ] ", NTextStyle.primary4())
                        .append(dbName).append("@").append(confName, NTextStyle.primary4())
                        .append(" db=").append(config.getDatabaseName())
                        .append(" user=").append(config.getUser());
            } else {
                return new Object[]{"local", dbName, confName, config.getUser()};
            }
        }
    }

    public Object toObject(String dbName, String confName, RemoteMysqlDatabaseConfig config, boolean describe, boolean plain, NSession session) {
        NTexts text = NTexts.of(session);
        if (!describe) {
            if (plain) {
                return text.ofBuilder()
                        .append(" [remote] ", NTextStyle.primary4())
                        .append(dbName).append("@").append(confName, NTextStyle.primary4())
                        ;
            } else {
                return new Object[]{"remote", dbName, confName};
            }
        } else {
            if (plain) {
                return text.ofBuilder()
                        .append(" [remote] ", NTextStyle.primary4())
                        .append(dbName).append("@").append(confName, NTextStyle.primary4())
                        .append(" local=").append(config.getLocalName())
                        .append(" remote=").append(config.getRemoteName())
                        .append(" on=").append(config.getServer())
                        ;
            } else {
                return new Object[]{"remote", dbName, confName, config.getLocalName(), config.getRemoteName(), config.getServer()};
            }
        }
    }

    public void runList(NCmdLine cmdLine, NMySqlService service, boolean describe) {
        NSession session = service.getSession();
        cmdLine.setCommandName("mysql list");
        List<AtName> localNames = new ArrayList<>();
        List<AtName> remoteNames = new ArrayList<>();
        Boolean expectedLocal = null;
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get(session).key()) {
                    case "--local": {
                        cmdLine.nextFlag();
                        expectedLocal = true;
                        break;
                    }
                    case "--remote": {
                        cmdLine.nextFlag();
                        expectedLocal = false;
                        break;
                    }
                    default: {
                        service.getSession().configureLast(cmdLine);
                    }
                }
            } else {
                if (expectedLocal == null) {
                    expectedLocal = true;
                }
                if (expectedLocal) {
                    localNames.add(AtName.nextConfigNonOption(cmdLine, session));
                } else {
                    remoteNames.add(AtName.nextConfigNonOption(cmdLine, session));
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
                LocalMysqlConfigService c = service.loadLocalMysqlConfig(localName.getConfigName(), NOpenMode.OPEN_OR_ERROR);
                result.add(new LocaleOrRemote(c.getName(), c.getConfig(), null));
            }
            for (AtName localName : remoteNames) {
                RemoteMysqlConfigService c = service.loadRemoteMysqlConfig(localName.getConfigName(), NOpenMode.OPEN_OR_ERROR);
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
                            session.getIterableOutput().next(toObject(db.getKey(), cnf.name, db.getValue(), describe, false, service.getSession()), index++);
                        }
                    } else {
                        for (Map.Entry<String, RemoteMysqlDatabaseConfig> db : cnf.remote.getDatabases().entrySet()) {
                            session.getIterableOutput().next(toObject(db.getKey(), cnf.name, db.getValue(), describe, false, service.getSession()), index++);
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
                                session.out().println(NMsg.ofC("%s",
                                        toObject(db.getKey(), cnf.name, db.getValue(), describe, true, service.getSession())
                                ));
                            }
                        } else {
                            for (Map.Entry<String, RemoteMysqlDatabaseConfig> db : cnf.remote.getDatabases().entrySet()) {
                                session.out().println(NMsg.ofC("%s",
                                        toObject(db.getKey(), cnf.name, db.getValue(), describe, true, service.getSession())
                                ));
                            }
                        }
                    }
                    break;
                }
                default: {
                    session.out().println(result);
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


    @Override
    public void revalidateOptions(NMySqlConfig options) {

    }

    @Override
    public CmdRedirect createDumpCommand(NPath remoteSql, NMySqlConfig options, NSession session) {
        throw new RuntimeException("unsupported dump");
    }

    @Override
    public CmdRedirect createRestoreCommand(NPath remoteSql, NMySqlConfig options, NSession session) {
        throw new RuntimeException("unsupported restore");
    }
}
