package net.vpc.toolbox.worky;

import net.vpc.app.nuts.NutsApplication;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;

public class NWorkMain extends NutsApplication {

    private WorkspaceService service;

    public static void main(String[] args) {
        new NWorkMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        String[] args = appContext.getArguments();
        this.service = new WorkspaceService(appContext);
        NutsCommandLine cmdLine = appContext.getCommandLine();
        NutsArgument a;
        do {
            if (appContext.configureFirst(cmdLine)) {
                //
            } else if ((a = cmdLine.next("scan", "s")) != null) {
                service.scan(cmdLine, appContext);
                return;
            } else if ((a = cmdLine.next("status", "t")) != null) {
                if (a.getArgumentValue().isBoolean()) {
                    service.enableScan(cmdLine, appContext, a.getArgumentValue().getBoolean());
                } else {
                    service.status(cmdLine, appContext);
                }
                return;
            } else if ((a = cmdLine.next("list", "l")) != null) {
                service.list(cmdLine, appContext);
                return;
            } else if ((a = cmdLine.next("set")) != null) {
                service.setWorkspaceConfigParam(cmdLine, appContext);
                return;
            } else {
                cmdLine.setCommandName("nwork").unexpectedArgument();
            }
        } while (cmdLine.hasNext());
        cmdLine.required();
    }

}
