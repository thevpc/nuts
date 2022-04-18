package net.thevpc.nuts.toolbox.nwork;

import net.thevpc.nuts.NutsApplication;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;

public class NWorkMain implements NutsApplication {

    private WorkspaceService service;

    public static void main(String[] args) {
        new NWorkMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        this.service = new WorkspaceService(appContext);
        NutsCommandLine cmdLine = appContext.getCommandLine();
        NutsArgument a;
        do {
            if (appContext.configureFirst(cmdLine)) {
                //
            } else if ((a = cmdLine.next("scan", "s")) != null) {
                service.scan(cmdLine, appContext);
                return;
            } else if ((a = cmdLine.next("find", "f")) != null) {
                service.find(cmdLine, appContext);
                return;
            } else if ((a = cmdLine.next("status", "t")) != null) {
                if (a.getValue().isBoolean()) {
                    service.enableScan(cmdLine, appContext, a.getBooleanValue());
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
