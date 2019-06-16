/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.derby;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsApplication;


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
        NutsCommandLine cmdLine = appContext.commandLine();
        NutsArgument a;
        DerbyOptions options = new DerbyOptions();
        while (cmdLine.hasNext()) {
            if (appContext.configureFirst(cmdLine)) {
                //
            } else if ((a = cmdLine.nextString("--derby-version")) != null) {
                options.derbyVersion = a.getStringValue();
            } else if ((a = cmdLine.nextString("--db")) != null) {
                options.derbyDataHomeRoot = a.getStringValue();
            } else if ((a = cmdLine.nextString("--netbeans")) != null) {
                options.derbyDataHomeReplace = System.getProperty("user.home") + "/.netbeans-derby";
            } else if ((a = cmdLine.nextString("-h", "--host")) != null) {
                options.host = a.getStringValue();
            } else if ((a = cmdLine.nextString("-p", "--port")) != null) {
                options.port = a.getArgumentValue().getInt();
            } else if ((a = cmdLine.nextString("-ssl", "--ssl")) != null) {
                options.sslmode = SSLMode.valueOf(a.getStringValue());
            } else if ((a = cmdLine.next("start")) != null) {
                options.cmd = Command.start;
            } else if ((a = cmdLine.next("sys", "sysinfo")) != null) {
                options.cmd = Command.sysinfo;
            } else if ((a = cmdLine.next("ping")) != null) {
                options.cmd = Command.ping;
            } else if ((a = cmdLine.next("rt", "runtime")) != null) {
                options.cmd = Command.runtimeinfo;
            } else if ((a = cmdLine.nextString("trace")) != null) {
                options.cmd = Command.trace;
                options.extraArg = a.getStringValue();
            } else if ((a = cmdLine.nextString("trace-directory")) != null) {
                options.cmd = Command.trace;
                options.extraArg = a.getStringValue();
            } else if ((a = cmdLine.nextString("max-threads")) != null) {
                options.cmd = Command.maxthreads;
                options.extraArg = a.getStringValue();
            } else if ((a = cmdLine.nextString("time-slice")) != null) {
                options.cmd = Command.timeslice;
                options.extraArg = a.getStringValue();
            } else if ((a = cmdLine.nextString("log-connections")) != null) {
                options.cmd = Command.logconnections;
                options.extraArg = a.getStringValue();
            } else if ((a = cmdLine.next("stop", "shutdown")) != null) {
                options.cmd = Command.shutdown;
            } else {
                cmdLine.setCommandName("derby").unexpectedArgument();
            }
        }
        DerbyService srv = new DerbyService(appContext);
        srv.exec(options);
    }

}
