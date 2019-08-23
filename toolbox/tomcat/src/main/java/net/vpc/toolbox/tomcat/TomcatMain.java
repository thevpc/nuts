package net.vpc.toolbox.tomcat;

import net.vpc.app.nuts.*;
import net.vpc.toolbox.tomcat.remote.RemoteTomcat;
import net.vpc.toolbox.tomcat.local.LocalTomcat;
import net.vpc.toolbox.tomcat.util.ApacheTomcatRepositoryModel;

public class TomcatMain extends NutsApplication {

    public static void main(String[] args) {
        new TomcatMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        NutsRepository apacheRepo = appContext.getWorkspace().config().findRepository("apache-tomcat", false);
        if(apacheRepo==null){
            appContext.getWorkspace().config().addRepository(new ApacheTomcatRepositoryModel(), appContext.getSession());
        }
        NutsCommandLine cmdLine = appContext.commandLine();
        Boolean local = null;
        boolean skipFirst = false;
        if (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.peek();
            if ((a.getString().equals("--remote") || a.getString().equals("-r"))) {
                cmdLine.skip();
                local = false;
            } else if ((a.getString().equals("--local") || a.getString().equals("-l"))) {
                cmdLine.skip();
                local = true;
            }
        }
        if (local == null) {
            local = true;
        }
        if (local) {
            LocalTomcat m = new LocalTomcat(appContext, cmdLine);
            m.runArgs();
        } else {
            RemoteTomcat m = new RemoteTomcat(appContext, cmdLine);
            m.runArgs();
        }
    }

}
