package net.vpc.toolbox.worky;

import net.vpc.app.nuts.NutsApplication;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgument;

public class WorkyMain extends NutsApplication {

    private WorkspaceService service;

    public static void main(String[] args) {
        new WorkyMain().runAndExit(args);
    }


    @Override
    public void run(NutsApplicationContext appContext) {
        String[] args = appContext.getArgs();
        this.service = new WorkspaceService(appContext);
        NutsCommandLine cmdLine = appContext.newCommandLine();
        NutsArgument a;
        do {
            if (appContext.configure(cmdLine)) {
                //
            } else if ((a = cmdLine.readNonOption("scan")) != null) {
                service.scan(cmdLine, appContext);
                return ;
            } else if ((a = cmdLine.readNonOption("check")) != null) {
                service.check(cmdLine, appContext);
                return ;
            } else if (cmdLine.readAll("enable scan")) {
                service.enableScan(cmdLine, appContext, true);
                return ;
            } else if (cmdLine.readAll("disable scan")) {
                service.enableScan(cmdLine, appContext, false);
                return ;
            } else if ((a = cmdLine.readNonOption("list")) != null) {
                service.list(cmdLine, appContext);
                return ;
            } else if ((a = cmdLine.readNonOption("set")) != null) {
                service.setWorkspaceConfigParam(cmdLine, appContext);
                return ;
            } else {
                cmdLine.unexpectedArgument("worky");
            }
        } while (cmdLine.hasNext());
    }


}
