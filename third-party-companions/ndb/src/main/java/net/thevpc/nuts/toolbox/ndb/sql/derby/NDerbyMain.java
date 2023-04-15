/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndb.sql.derby;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.ndb.base.CmdRedirect;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.sql.util.SqlConnectionInfo;
import net.thevpc.nuts.toolbox.ndb.sql.util.SqlHelper;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NRef;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author thevpc
 */
public class NDerbyMain extends SqlSupport<NDerbyConfig> {

    public NDerbyMain(NSession session) {
        super("derby", NDerbyConfig.class, session, "org.apache.derby:derbyclient#10.16.1.1", "org.apache.derby.jdbc.ClientDriver");
    }

    @Override
    public void run(NSession session, NCmdLine cmdLine) {
        NArg a;
        NDerbyConfig options = new NDerbyConfig();
        cmdLine.setCommandName("derby");
        while (cmdLine.hasNext()) {
            if ((a = cmdLine.next("start").orNull()) != null) {
                options.setCmd(Command.start);
            } else if ((a = cmdLine.next("sys", "sysinfo", "sys-info").orNull()) != null) {
                options.setCmd(Command.sysinfo);
            } else if ((a = cmdLine.next("ping").orNull()) != null) {
                options.setCmd(Command.ping);
            } else if ((a = cmdLine.next("status").orNull()) != null) {
                status(cmdLine, options);
                return;
            } else if ((a = cmdLine.next("rt", "runtime", "runtimeinfo", "runtime-info").orNull()) != null) {
                options.setCmd(Command.runtimeinfo);
            } else if ((a = cmdLine.nextEntry("trace").orNull()) != null) {
                options.setCmd(Command.trace);
                options.setExtraArg(a.getStringValue().get(session));
            } else if ((a = cmdLine.nextEntry("trace-directory", "tracedirectory").orNull()) != null) {
                options.setCmd(Command.tracedirectory);
                options.setExtraArg(a.getStringValue().get(session));
            } else if ((a = cmdLine.nextEntry("max-threads", "maxthreads").orNull()) != null) {
                options.setCmd(Command.maxthreads);
                options.setExtraArg(a.getStringValue().get(session));
            } else if ((a = cmdLine.nextEntry("time-slice", "timeslice").orNull()) != null) {
                options.setCmd(Command.timeslice);
                options.setExtraArg(a.getStringValue().get(session));
            } else if ((a = cmdLine.nextEntry("log-connections", "logconnections").orNull()) != null) {
                options.setCmd(Command.logconnections);
                options.setExtraArg(a.getStringValue().get(session));
            } else if ((a = cmdLine.next("stop", "shutdown").orNull()) != null) {
                options.setCmd(Command.shutdown);
            } else if ((a = cmdLine.next("ps").orNull()) != null) {
                ps(cmdLine, options);
                return;
            } else if ((a = cmdLine.next("versions").orNull()) != null) {
                versions(cmdLine, options);
                return;
            } else if ((a = cmdLine.next("run-sql").orNull()) != null) {
                runSQL(cmdLine, options, session);
                return;
            } else if (_opt(cmdLine, options)) {
                //
            } else {
                cmdLine.setCommandName("derby").throwUnexpectedArgument();
            }
        }
        if (cmdLine.isExecMode()) {
            DerbyService srv = new DerbyService(session);
            int effectivePort = options.getPort() < 0 ? 1527 : options.getPort();
            if (options.getCmd() == Command.start) {
                NTexts factory = NTexts.of(session);
                if (cmdLine.isExecMode()) {
                    if (new DerbyService(session).isRunning()) {
                        session.out().println(NMsg.ofC("derby is %s on port %s",
                                factory.ofStyled("already running", NTextStyle.warn()),
                                factory.ofStyled("" + effectivePort, NTextStyle.number())
                        ));
                        throw new NExecutionException(session, NMsg.ofC("derby is already running on port %d", effectivePort), 3);
                    }
                }
            } else if (options.getCmd() == Command.shutdown) {
                NTexts factory = NTexts.of(session);
                if (cmdLine.isExecMode()) {
                    if (!new DerbyService(session).isRunning()) {
                        session.out().println(NMsg.ofC("derby is %s on port %s",
                                factory.ofStyled("already stopped", NTextStyle.warn()),
                                factory.ofStyled("" + effectivePort, NTextStyle.number())
                        ));
                        session.out().println(NMsg.ofC("derby is %s", factory.ofStyled("already stopped", NTextStyle.warn())));
                        throw new NExecutionException(session, NMsg.ofC("derby is already stopped on port %d", effectivePort), 3);
                    }
                }
            }
            srv.exec(options);
        }
    }

    private void runSQL(NCmdLine commandLine, NDerbyConfig options, NSession session) {
        commandLine.setCommandName("derby run-sql");
        NRef<AtName> name = NRef.ofNull(AtName.class);
        List<String> sql = new ArrayList<>();
        NRef<Boolean> forceShowSQL = NRef.ofNull(Boolean.class);
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--name": {
                        commandLine.withNextEntry((v, a, s) -> {
                            if (name.isNull()) {
                                name.set(new AtName(a.getStringValue().get(session)));
                            } else {
                                commandLine.throwUnexpectedArgument(NMsg.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    case "--show-sql": {
                        commandLine.withNextFlag((v, a, s) -> {
                            forceShowSQL.set(v);
                        });
                        break;
                    }
                    default: {
                        session.configureLast(commandLine);
                    }
                }
            } else {
                if (name.isNull()) {
                    name.set(new AtName(commandLine.next().get(session).asString().get(session)));
                } else {
                    sql.add(commandLine.next().flatMap(NLiteral::asString).get(session));
                }
            }
        }
        if (name.isNull()) {
            name.set(new AtName(""));
        }
        if (sql.isEmpty()) {
            commandLine.throwMissingArgument(NMsg.ofPlain("sql"));
        }

        SqlHelper.runAndWaitFor(sql, createSqlConnectionInfo(options),
                forceShowSQL.get(), session);
    }


    @Override
    public SqlConnectionInfo createSqlConnectionInfo(NDerbyConfig options) {
        String url = NMsg.ofV("jdbc:derby://${server}:${port}/${database};create=true",
                NMaps.of(
                        "server", NOptional.of(options.getHost()).ifBlank("localhost").get(),
                        "port", NOptional.of(options.getPort() <= 0 ? null : options.getPort()).ifBlank(1527).get(),
                        "database", NOptional.of(options.getDatabaseName()).ifBlank("db").get()
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

    public void status(NCmdLine cmdLine, NDerbyConfig options) {
        cmdLine.setCommandName("tomcat --local status");
        NArg a;
        while (cmdLine.hasNext()) {
            if (_opt(cmdLine, options)) {
                //
            } else {
                cmdLine.throwUnexpectedArgument();
            }
        }
        options.setCmd(Command.ping);
        NTexts factory = NTexts.of(session);
        if (cmdLine.isExecMode()) {
            if (new DerbyService(session).isRunning()) {
                session.out().println(NMsg.ofC("derby is %s", factory.ofStyled("running", NTextStyle.primary1())));
            } else {
                session.out().println(NMsg.ofC("derby is %s", factory.ofStyled("stopped", NTextStyle.error())));
            }
        }
    }

    private boolean _opt(NCmdLine cmdLine, NDerbyConfig options) {
        NArg a;
        if ((a = cmdLine.nextEntry("-v", "--derby-version").orNull()) != null) {
            options.setDerbyVersion(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextEntry("-H", "--home").orNull()) != null) {
            options.setDerbyDataHomeRoot(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextEntry("--nb").orNull()) != null) {
            options.setDerbyDataHomeRoot(System.getProperty("user.home") + File.separator + ".netbeans-derby");
            return true;
//        } else if ((a = cmdLine.nextString("--nb","--netbeans").orNull()) != null) {
//            options.derbyDataHomeReplace = System.getProperty("user.home") + "/.netbeans-derby";
//            return true;
        } else if ((a = cmdLine.nextEntry("-h", "--host").orNull()) != null) {
            options.setHost(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextEntry("-p", "--port").orNull()) != null) {
            options.setPort(a.getValue().asInt().get(session));
            return true;
        } else if ((a = cmdLine.nextEntry("-ssl", "--ssl").orNull()) != null) {
            options.setSslmode(SSLMode.valueOf(a.getStringValue().get(session)));
            return true;
        } else if ((a = cmdLine.nextEntry("-n", "--dbname").orNull()) != null) {
            options.setDatabaseName(a.getStringValue().get(session));
            return true;
        } else if (session.configureFirst(cmdLine)) {
            return true;
        } else {
            cmdLine.throwUnexpectedArgument();
            return false;
        }
    }

    public void ps(NCmdLine args, NDerbyConfig options) {
        String format = "default";
        args.setCommandName("tomcat --local ps");
        NArg a;
        while (args.hasNext()) {
            if ((a = args.nextFlag("-l", "--long").orNull()) != null) {
                format = "long";
            } else if ((a = args.nextFlag("-s", "--short").orNull()) != null) {
                format = "short";
            } else if (_opt(args, options)) {
                //
            } else {
                args.throwUnexpectedArgument();
            }
        }
        NTexts factory = NTexts.of(session);
        if (args.isExecMode()) {
            if (session.isPlainOut()) {
                NPrintStream out = session.out();
                for (RunningDerby jpsResult : DerbyUtils.getRunningInstances(session)) {
                    switch (format) {
                        case "short": {
                            out.println(NMsg.ofC("%s",
                                    factory.ofStyled(jpsResult.getPid(), NTextStyle.primary1())
                            ));
                            break;
                        }
                        case "long": {
                            out.println(NMsg.ofC("%s %s %s %s %s",
                                    factory.ofStyled(jpsResult.getPid(), NTextStyle.primary1()),
                                    factory.ofPlain("HOME:"),
                                    factory.ofStyled(jpsResult.getHome(), NTextStyle.path()),
                                    factory.ofPlain("CMD:"),
                                    NCmdLine.parseSystem(jpsResult.getArgsLine(), session))
                            );
                            break;
                        }
                        default: {
                            out.println(NMsg.ofC("%s %s",
                                    factory.ofStyled(jpsResult.getPid(), NTextStyle.primary1()),
                                    jpsResult.getHome())
                            );
                            break;
                        }
                    }
                }
            } else {
                session.out().println(DerbyUtils.getRunningInstances(session));
            }
        }
    }

    public void versions(NCmdLine args, NDerbyConfig options) {
        args.setCommandName("tomcat --local versions");
        while (args.hasNext()) {
            if (_opt(args, options)) {
                //
            } else {
                args.throwUnexpectedArgument();
            }
        }
        if (args.isExecMode()) {
            DerbyService srv = new DerbyService(session);
            session.out().println(srv.findVersions());
        }
    }


    @Override
    public void revalidateOptions(NDerbyConfig options) {

    }

    @Override
    public CmdRedirect createDumpCommand(NPath remoteSql, NDerbyConfig options, NSession session) {
        throw new RuntimeException("not supported dump");
    }

    @Override
    public CmdRedirect createRestoreCommand(NPath remoteSql, NDerbyConfig options, NSession session) {
        throw new RuntimeException("not supported restore");
    }
}
