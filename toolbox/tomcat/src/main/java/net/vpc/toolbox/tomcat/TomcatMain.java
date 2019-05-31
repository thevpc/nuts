package net.vpc.toolbox.tomcat;

import net.vpc.app.nuts.NutsApplication;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.toolbox.tomcat.remote.RemoteTomcat;
import net.vpc.toolbox.tomcat.local.LocalTomcat;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsArgument;

public class TomcatMain extends NutsApplication {

    public static void main(String[] args) {
        new TomcatMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        NutsCommand cmdLine = appContext.commandLine();
        Boolean local = null;
        boolean skipFirst = false;
        while (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.next();
            if (local == null) {
                if (appContext.configureFirst(cmdLine)) {
                    //
                } else if ((a.getString().equals("--remote") || a.getString().equals("-r"))) {
                    local = false;
                } else if ((a.getString().equals("--local") || a.getString().equals("-l"))) {
                    local = true;
                } else if (a.isOption()) {
                    cmdLine.pushBack(a);
                } else {
                    cmdLine.pushBack(a);
                    local = true;
                }
            } else {
                cmdLine.pushBack(a);
            }
        }
        if (local == null) {
            local = true;
        }

        if (local) {
            LocalTomcat m = new LocalTomcat(appContext, cmdLine);
            m.runArgs();
        } else {
            RemoteTomcat m = new RemoteTomcat(appContext,cmdLine);
            m.runArgs();
        }
    }

}
