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

import java.io.File;

/**
 * @author thevpc
 */
public class NDerbyMain implements NdbSupport {

    private NutsApplicationContext appContext;

    @Override
    public void run(NutsApplicationContext appContext, NutsCommandLine cmdLine) {
        this.appContext = appContext;
        NutsSession session = appContext.getSession();
        NutsArgument a;
        DerbyOptions options = new DerbyOptions();
        cmdLine.setCommandName("derby");
        while (cmdLine.hasNext()) {
            if ((a = cmdLine.next("start").orNull()) != null) {
                options.cmd = Command.start;
            } else if ((a = cmdLine.next("sys", "sysinfo", "sys-info").orNull()) != null) {
                options.cmd = Command.sysinfo;
            } else if ((a = cmdLine.next("ping").orNull()) != null) {
                options.cmd = Command.ping;
            } else if ((a = cmdLine.next("status").orNull()) != null) {
                status(cmdLine, options);
                return;
            } else if ((a = cmdLine.next("rt", "runtime", "runtimeinfo", "runtime-info").orNull()) != null) {
                options.cmd = Command.runtimeinfo;
            } else if ((a = cmdLine.nextString("trace").orNull()) != null) {
                options.cmd = Command.trace;
                options.extraArg = a.getStringValue().get(session);
            } else if ((a = cmdLine.nextString("trace-directory", "tracedirectory").orNull()) != null) {
                options.cmd = Command.tracedirectory;
                options.extraArg = a.getStringValue().get(session);
            } else if ((a = cmdLine.nextString("max-threads", "maxthreads").orNull()) != null) {
                options.cmd = Command.maxthreads;
                options.extraArg = a.getStringValue().get(session);
            } else if ((a = cmdLine.nextString("time-slice", "timeslice").orNull()) != null) {
                options.cmd = Command.timeslice;
                options.extraArg = a.getStringValue().get(session);
            } else if ((a = cmdLine.nextString("log-connections", "logconnections").orNull()) != null) {
                options.cmd = Command.logconnections;
                options.extraArg = a.getStringValue().get(session);
            } else if ((a = cmdLine.next("stop", "shutdown").orNull()) != null) {
                options.cmd = Command.shutdown;
            } else if ((a = cmdLine.next("ps").orNull()) != null) {
                ps(cmdLine, options);
                return;
            } else if ((a = cmdLine.next("versions").orNull()) != null) {
                versions(cmdLine, options);
                return;
            } else if (_opt(cmdLine, options)) {
                //
            } else {
                cmdLine.setCommandName("derby").throwUnexpectedArgument(session);
            }
        }
        if (cmdLine.isExecMode()) {
            DerbyService srv = new DerbyService(appContext);
            int effectivePort = options.port < 0 ? 1527 : options.port;
            if (options.cmd == Command.start) {
                NutsTexts factory = NutsTexts.of(session);
                if (cmdLine.isExecMode()) {
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
                if (cmdLine.isExecMode()) {
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

    public void status(NutsCommandLine cmdLine, DerbyOptions options) {
        NutsSession session = appContext.getSession();
        cmdLine.setCommandName("tomcat --local status");
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (_opt(cmdLine, options)) {
                //
            } else {
                cmdLine.throwUnexpectedArgument(session);
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
        } else if ((a = cmdLine.nextString("-d", "--db").orNull()) != null) {
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
        } else if (appContext.configureFirst(cmdLine)) {
            return true;
        } else {
            cmdLine.throwUnexpectedArgument(session);
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
                args.throwUnexpectedArgument(session);
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
                args.throwUnexpectedArgument(session);
            }
        }
        if (args.isExecMode()) {
            DerbyService srv = new DerbyService(appContext);
            session.out().printlnf(srv.findVersions());
        }
    }

}
