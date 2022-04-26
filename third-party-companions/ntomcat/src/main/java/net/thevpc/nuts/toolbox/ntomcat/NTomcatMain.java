package net.thevpc.nuts.toolbox.ntomcat;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.ntomcat.remote.RemoteTomcat;
import net.thevpc.nuts.toolbox.ntomcat.local.LocalTomcat;
import net.thevpc.nuts.toolbox.ntomcat.util.ApacheTomcatRepositoryModel;

public class NTomcatMain implements NutsApplication {

    public static void main(String[] args) {
        new NTomcatMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        NutsSession session = appContext.getSession();
        NutsRepository apacheRepo = session.repos().findRepository("apache-tomcat");
        if (apacheRepo == null) {
            session.repos().addRepository(
                    new NutsAddRepositoryOptions()
                            .setRepositoryModel(new ApacheTomcatRepositoryModel())
                            .setTemporary(true)
                            
            );
        }
        NutsCommandLine cmdLine = appContext.getCommandLine();
        Boolean local = null;
        boolean skipFirst = false;
        if (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.peek().get(session);
            if ((a.asString().equals   ("--remote") || a.asString().equals("-r"))) {
                cmdLine.skip();
                local = false;
            } else if ((a.asString().equals("--local") || a.asString().equals("-l"))) {
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
            session.flush();
        } else {
            RemoteTomcat m = new RemoteTomcat(appContext, cmdLine);
            m.runArgs();
            session.flush();
        }
    }

}
