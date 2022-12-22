/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndb.derby;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.toolbox.ndb.NdbSupport;
import net.thevpc.nuts.toolbox.ndb.nmysql.NMySqlService;
import net.thevpc.nuts.toolbox.ndb.nmysql.local.LocalMysqlConfigService;
import net.thevpc.nuts.toolbox.ndb.nmysql.local.LocalMysqlDatabaseConfigService;
import net.thevpc.nuts.toolbox.ndb.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.util.SqlHelper;
import net.thevpc.nuts.util.NutsMaps;
import net.thevpc.nuts.util.NutsRef;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author thevpc
 */
public class NDerbyMain implements NdbSupport {

    private NutsApplicationContext appContext;

    @Override
    public void run(NutsApplicationContext appContext, NutsCommandLine commandLine) {
        this.appContext = appContext;
        NutsSession session = appContext.getSession();
        NutsArgument a;
        DerbyOptions options = new DerbyOptions();
        commandLine.setCommandName("derby");
        while (commandLine.hasNext()) {
            if ((a = commandLine.next("start").orNull()) != null) {
                options.cmd = Command.start;
            } else if ((a = commandLine.next("sys", "sysinfo", "sys-info").orNull()) != null) {
                options.cmd = Command.sysinfo;
            } else if ((a = commandLine.next("ping").orNull()) != null) {
                options.cmd = Command.ping;
            } else if ((a = commandLine.next("status").orNull()) != null) {
                status(commandLine, options);
                return;
            } else if ((a = commandLine.next("rt", "runtime", "runtimeinfo", "runtime-info").orNull()) != null) {
                options.cmd = Command.runtimeinfo;
            } else if ((a = commandLine.nextString("trace").orNull()) != null) {
                options.cmd = Command.trace;
                options.extraArg = a.getStringValue().get(session);
            } else if ((a = commandLine.nextString("trace-directory", "tracedirectory").orNull()) != null) {
                options.cmd = Command.tracedirectory;
                options.extraArg = a.getStringValue().get(session);
            } else if ((a = commandLine.nextString("max-threads", "maxthreads").orNull()) != null) {
                options.cmd = Command.maxthreads;
                options.extraArg = a.getStringValue().get(session);
            } else if ((a = commandLine.nextString("time-slice", "timeslice").orNull()) != null) {
                options.cmd = Command.timeslice;
                options.extraArg = a.getStringValue().get(session);
            } else if ((a = commandLine.nextString("log-connections", "logconnections").orNull()) != null) {
                options.cmd = Command.logconnections;
                options.extraArg = a.getStringValue().get(session);
            } else if ((a = commandLine.next("stop", "shutdown").orNull()) != null) {
                options.cmd = Command.shutdown;
            } else if ((a = commandLine.next("ps").orNull()) != null) {
                ps(commandLine, options);
                return;
            } else if ((a = commandLine.next("versions").orNull()) != null) {
                versions(commandLine, options);
                return;
            } else if ((a = commandLine.next("run-sql").orNull()) != null) {
                runSQL(commandLine, options,session);
                return;
            } else if (_opt(commandLine, options)) {
                //
            } else {
                commandLine.setCommandName("derby").throwUnexpectedArgument();
            }
        }
        if (commandLine.isExecMode()) {
            DerbyService srv = new DerbyService(appContext);
            int effectivePort = options.port < 0 ? 1527 : options.port;
            if (options.cmd == Command.start) {
                NutsTexts factory = NutsTexts.of(session);
                if (commandLine.isExecMode()) {
                    if (new DerbyService(appContext).isRunning()) {
                        session.out().printf("derby is %s on port %s%n",
                                factory.ofStyled("already running", NutsTextStyle.warn()),
                                factory.ofStyled("" + effectivePort, NutsTextStyle.number())
                        );
                        throw new NutsExecutionException(session, NutsMessage.ofCstyle("derby is already running on port %d", effectivePort), 3);
                    }
                }
            } else if (options.cmd == Command.shutdown) {
                NutsTexts factory = NutsTexts.of(appContext.getSession());
                if (commandLine.isExecMode()) {
                    if (!new DerbyService(appContext).isRunning()) {
                        session.out().printf("derby is %s on port %s%n",
                                factory.ofStyled("already stopped", NutsTextStyle.warn()),
                                factory.ofStyled("" + effectivePort, NutsTextStyle.number())
                        );
                        session.out().printf("derby is %s%n", factory.ofStyled("already stopped", NutsTextStyle.warn()));
                        throw new NutsExecutionException(session, NutsMessage.ofCstyle("derby is already stopped on port %d", effectivePort), 3);
                    }
                }
            }
            srv.exec(options);
        }
    }
    private void runSQL(NutsCommandLine commandLine, DerbyOptions options,NutsSession session) {
        commandLine.setCommandName("derby run-sql");
        NutsRef<AtName> name = NutsRef.ofNull(AtName.class);
        List<String> sql = new ArrayList<>();
        NutsRef<Boolean> forceShowSQL = NutsRef.ofNull(Boolean.class);
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--name": {
                        commandLine.withNextString((v, a, s) -> {
                            if (name.isNull()) {
                                name.set(new AtName(a.getStringValue().get(session)));
                            } else {
                                commandLine.throwUnexpectedArgument(NutsMessage.ofPlain("already defined"));
                            }
                        });
                        break;
                    }
                    case "--show-sql": {
                        commandLine.withNextBoolean((v, a, s) -> {
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
                    sql.add(commandLine.next().flatMap(NutsValue::asString).get(session));
                }
            }
        }
        if (name.isNull()) {
            name.set(new AtName(""));
        }
        if (sql.isEmpty()) {
            commandLine.throwMissingArgument(NutsMessage.ofPlain("sql"));
        }

        String jdbcUrl = NutsMessage.ofVstyle("jdbc:derby://${server}:${port}/${database};create=true",
                NutsMaps.of(
                        "server", NutsOptional.of(options.host).ifBlank("localhost").get(),
                        "port", NutsOptional.of(options.port<=0?null:options.port).ifBlank(1527).get(),
                        "database", NutsOptional.of(options.databaseName).ifBlank("db").get()
                )).toString();
        SqlHelper.runAndWaitFor(sql, jdbcUrl, "org.apache.derby:derbyclient#10.16.1.1", "org.apache.derby.jdbc.ClientDriver",
                options.user,options.password,null,
                forceShowSQL.get(), session);
    }
    public void status(NutsCommandLine cmdLine, DerbyOptions options) {
        NutsSession session = appContext.getSession();
        cmdLine.setCommandName("tomcat --local status");
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (_opt(cmdLine, options)) {
                //
            } else {
                cmdLine.throwUnexpectedArgument();
            }
        }
        options.cmd = Command.ping;
        NutsTexts factory = NutsTexts.of(session);
        if (cmdLine.isExecMode()) {
            if (new DerbyService(appContext).isRunning()) {
                session.out().printf("derby is %s%n", factory.ofStyled("running", NutsTextStyle.primary1()));
            } else {
                session.out().printf("derby is %s%n", factory.ofStyled("stopped", NutsTextStyle.error()));
            }
        }
    }

    private boolean _opt(NutsCommandLine cmdLine, DerbyOptions options) {
        NutsSession session = appContext.getSession();
        NutsArgument a;
        if ((a = cmdLine.nextString("-v", "--derby-version").orNull()) != null) {
            options.derbyVersion = a.getStringValue().get(session);
            return true;
        } else if ((a = cmdLine.nextString("-H", "--home").orNull()) != null) {
            options.derbyDataHomeRoot = a.getStringValue().get(session);
            return true;
        } else if ((a = cmdLine.nextString("--nb").orNull()) != null) {
            options.derbyDataHomeRoot = System.getProperty("user.home") + File.separator + ".netbeans-derby";
            return true;
//        } else if ((a = cmdLine.nextString("--nb","--netbeans").orNull()) != null) {
//            options.derbyDataHomeReplace = System.getProperty("user.home") + "/.netbeans-derby";
//            return true;
        } else if ((a = cmdLine.nextString("-h", "--host").orNull()) != null) {
            options.host = a.getStringValue().get(session);
            return true;
        } else if ((a = cmdLine.nextString("-p", "--port").orNull()) != null) {
            options.port = a.getValue().asInt().get(session);
            return true;
        } else if ((a = cmdLine.nextString("-ssl", "--ssl").orNull()) != null) {
            options.sslmode = SSLMode.valueOf(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.nextString("-n", "--dbname").orNull()) != null) {
            options.databaseName = a.getStringValue().get(session);
            return true;
        } else if (appContext.configureFirst(cmdLine)) {
            return true;
        } else {
            cmdLine.throwUnexpectedArgument();
            return false;
        }
    }

    public void ps(NutsCommandLine args, DerbyOptions options) {
        NutsSession session = appContext.getSession();
        String format = "default";
        args.setCommandName("tomcat --local ps");
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.nextBoolean("-l", "--long").orNull()) != null) {
                format = "long";
            } else if ((a = args.nextBoolean("-s", "--short").orNull()) != null) {
                format = "short";
            } else if (_opt(args, options)) {
                //
            } else {
                args.throwUnexpectedArgument();
            }
        }
        NutsTexts factory = NutsTexts.of(session);
        if (args.isExecMode()) {
            if (session.isPlainOut()) {
                NutsPrintStream out = session.out();
                for (RunningDerby jpsResult : DerbyUtils.getRunningInstances(appContext)) {
                    switch (format) {
                        case "short": {
                            out.printf("%s\n",
                                    factory.ofStyled(jpsResult.getPid(), NutsTextStyle.primary1())
                            );
                            break;
                        }
                        case "long": {
                            out.printf("%s %s %s %s %s%n",
                                    factory.ofStyled(jpsResult.getPid(), NutsTextStyle.primary1()),
                                    factory.ofPlain("HOME:"),
                                    factory.ofStyled(jpsResult.getHome(), NutsTextStyle.path()),
                                    factory.ofPlain("CMD:"),
                                    NutsCommandLine.parseSystem(jpsResult.getArgsLine(), session)
                            );
                            break;
                        }
                        default: {
                            out.printf("%s %s\n",
                                    factory.ofStyled(jpsResult.getPid(), NutsTextStyle.primary1()),
                                    jpsResult.getHome()
                            );
                            break;
                        }
                    }
                }
            } else {
                session.out().printlnf(DerbyUtils.getRunningInstances(appContext));
            }
        }
    }

    public void versions(NutsCommandLine args, DerbyOptions options) {
        NutsSession session = appContext.getSession();
        args.setCommandName("tomcat --local versions");
        NutsArgument a;
        while (args.hasNext()) {
            if (_opt(args, options)) {
                //
            } else {
                args.throwUnexpectedArgument();
            }
        }
        if (args.isExecMode()) {
            DerbyService srv = new DerbyService(appContext);
            session.out().printlnf(srv.findVersions());
        }
    }

}
