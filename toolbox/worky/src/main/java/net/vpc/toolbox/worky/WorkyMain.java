package net.vpc.toolbox.worky;

import net.vpc.app.nuts.NutsApplication;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsArgument;

public class WorkyMain extends NutsApplication {

    private WorkspaceService service;

    public static void main(String[] args) {
        new WorkyMain().runAndExit(args);
    }


    @Override
    public void run(NutsApplicationContext appContext) {
        String[] args = appContext.getArguments();
        this.service = new WorkspaceService(appContext);
        NutsCommand cmdLine = appContext.getCommandLine();
        NutsArgument a;
        do {
            if (appContext.configureFirst(cmdLine)) {
                //
            } else if ((a = cmdLine.next("scan")) != null) {
                service.scan(cmdLine, appContext);
                return ;
            } else if ((a = cmdLine.next("check")) != null) {
                service.check(cmdLine, appContext);
                return ;
            } else if (cmdLine.next("enable scan")!=null) {
                service.enableScan(cmdLine, appContext, true);
                return ;
            } else if (cmdLine.next("disable scan")!=null) {
                service.enableScan(cmdLine, appContext, false);
                return ;
            } else if ((a = cmdLine.next("list")) != null) {
                service.list(cmdLine, appContext);
                return ;
            } else if ((a = cmdLine.next("set")) != null) {
                service.setWorkspaceConfigParam(cmdLine, appContext);
                return ;
            } else {
                cmdLine.setCommandName("worky").unexpectedArgument();
            }
        } while (cmdLine.hasNext());
    }


}
