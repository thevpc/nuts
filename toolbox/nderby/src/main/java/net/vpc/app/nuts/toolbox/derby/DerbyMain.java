/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.derby;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsApplication;

import java.io.File;
import java.io.PrintStream;

/**
 * @author vpc
 */
public class DerbyMain extends NutsApplication {

    private NutsApplicationContext appContext;

    public static void main(String[] args) {
        new DerbyMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        this.appContext = appContext;
        NutsWorkspace ws = appContext.getWorkspace();
        NutsCommandLine cmdLine = appContext.getCommandLine();
        NutsArgument a;
        DerbyOptions options = new DerbyOptions();
        cmdLine.setCommandName("derby");
        while (cmdLine.hasNext()) {
            if ((a = cmdLine.next("start")) != null) {
                options.cmd = Command.start;
            } else if ((a = cmdLine.next("sys", "sysinfo", "sys-info")) != null) {
                options.cmd = Command.sysinfo;
            } else if ((a = cmdLine.next("ping")) != null) {
                options.cmd = Command.ping;
            } else if ((a = cmdLine.next("status")) != null) {
                status(cmdLine, options);
                return;
            } else if ((a = cmdLine.next("rt", "runtime","runtimeinfo","runtime-info")) != null) {
                options.cmd = Command.runtimeinfo;
            } else if ((a = cmdLine.nextString("trace")) != null) {
                options.cmd = Command.trace;
                options.extraArg = a.getStringValue();
            } else if ((a = cmdLine.nextString("trace-directory","tracedirectory")) != null) {
                options.cmd = Command.tracedirectory;
                options.extraArg = a.getStringValue();
            } else if ((a = cmdLine.nextString("max-threads","maxthreads")) != null) {
                options.cmd = Command.maxthreads;
                options.extraArg = a.getStringValue();
            } else if ((a = cmdLine.nextString("time-slice","timeslice")) != null) {
                options.cmd = Command.timeslice;
                options.extraArg = a.getStringValue();
            } else if ((a = cmdLine.nextString("log-connections","logconnections")) != null) {
                options.cmd = Command.logconnections;
                options.extraArg = a.getStringValue();
            } else if ((a = cmdLine.next("stop", "shutdown")) != null) {
                options.cmd = Command.shutdown;
            } else if ((a = cmdLine.next("ps")) != null) {
                ps(cmdLine, options);
                return;
            } else if ((a = cmdLine.next("versions")) != null) {
                versions(cmdLine, options);
                return;
            } else if (_opt(cmdLine, options)) {
                //
            } else {
                cmdLine.setCommandName("derby").unexpectedArgument();
            }
        }
        if (cmdLine.isExecMode()) {
            DerbyService srv = new DerbyService(appContext);
            srv.exec(options);
        }
    }

    public void status(NutsCommandLine cmdLine, DerbyOptions options) {
        cmdLine.setCommandName("tomcat --local status");
        NutsArgument a;
        while (cmdLine.hasNext()) {
            if (_opt(cmdLine, options)) {
                //
            } else {
                cmdLine.unexpectedArgument();
            }
        }
        options.cmd = Command.ping;
        if (cmdLine.isExecMode()) {
            DerbyService srv = new DerbyService(appContext);
            String q = null;
            try {
                q = srv.command(options).setFailFast(true).grabOutputString().getOutputString();
            } catch (NutsExecutionException ex) {
                //
            }
            if (q != null) {
                appContext.getSession().out().println("derby is ##running##");
            } else {
                appContext.getSession().out().println("derby is @@stopped@@");
            }
        }
    }

    private boolean _opt(NutsCommandLine cmdLine, DerbyOptions options) {
        NutsArgument a;
        if ((a = cmdLine.nextString("-v", "--derby-version")) != null) {
            options.derbyVersion = a.getStringValue();
            return true;
        } else if ((a = cmdLine.nextString("-d", "--db")) != null) {
            options.derbyDataHomeRoot = a.getStringValue();
            return true;
        } else if ((a = cmdLine.nextString("--nb")) != null) {
            options.derbyDataHomeRoot = System.getProperty("user.home") + File.separator + ".netbeans-derby";
            return true;
//        } else if ((a = cmdLine.nextString("--nb","--netbeans")) != null) {
//            options.derbyDataHomeReplace = System.getProperty("user.home") + "/.netbeans-derby";
//            return true;
        } else if ((a = cmdLine.nextString("-h", "--host")) != null) {
            options.host = a.getStringValue();
            return true;
        } else if ((a = cmdLine.nextString("-p", "--port")) != null) {
            options.port = a.getArgumentValue().getInt();
            return true;
        } else if ((a = cmdLine.nextString("-ssl", "--ssl")) != null) {
            options.sslmode = SSLMode.valueOf(a.getStringValue());
            return true;
        } else if (appContext.configureFirst(cmdLine)) {
            return true;
        } else {
            cmdLine.unexpectedArgument();
            return false;
        }
    }

    public void ps(NutsCommandLine args, DerbyOptions options) {
        String format = "default";
        args.setCommandName("tomcat --local ps");
        NutsArgument a;
        while (args.hasNext()) {
            if ((a = args.nextBoolean("-l", "--long")) != null) {
                format = "long";
            } else if ((a = args.nextBoolean("-s", "--short")) != null) {
                format = "short";
            } else if (_opt(args, options)) {
                //
            } else {
                args.unexpectedArgument();
            }
        }
        if (args.isExecMode()) {
            NutsSession session = appContext.getSession();
            if (session.isPlainOut()) {
                PrintStream out = session.out();
                for (RunningDerby jpsResult : DerbyUtils.getRunningInstances(appContext)) {
                    switch (format) {
                        case "short": {
                            out.printf("##%s##\n",
                                    jpsResult.getPid()
                            );
                            break;
                        }
                        case "long": {
                            out.printf("##%s## ==HOME:== %s ==CMD:== " +
                                            appContext.getWorkspace().commandLine().setValue(
                                                    appContext.getCommandLine().parseLine(jpsResult.getArgsLine())
                                            ).format()
                                            + "\n",
                                    jpsResult.getPid(),
                                    jpsResult.getHome()
                            );
                            break;
                        }
                        default: {
                            out.printf("##%s## %s\n",
                                    jpsResult.getPid(),
                                    jpsResult.getHome()
                            );
                            break;
                        }
                    }
                }
            } else {
                session.formatObject(DerbyUtils.getRunningInstances(appContext))
                        .println();
            }
        }
    }

    public void versions(NutsCommandLine args, DerbyOptions options) {
        args.setCommandName("tomcat --local versions");
        NutsArgument a;
        while (args.hasNext()) {
            if (_opt(args, options)) {
                //
            } else {
                args.unexpectedArgument();
            }
        }
        if (args.isExecMode()) {
            DerbyService srv = new DerbyService(appContext);
            appContext.getSession().formatObject(srv.findVersions()).println();
        }
    }

}
