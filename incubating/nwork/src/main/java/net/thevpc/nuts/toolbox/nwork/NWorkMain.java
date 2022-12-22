package net.thevpc.nuts.toolbox.nwork;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;

public class NWorkMain implements NutsApplication {

    private WorkspaceService service;

    public static void main(String[] args) {
        new NWorkMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        this.service = new WorkspaceService(appContext);
        NutsSession session = appContext.getSession();
        NutsCommandLine cmdLine = appContext.getCommandLine().setCommandName("nwork");
        NutsArgument a;
        do {
            if (appContext.configureFirst(cmdLine)) {
                //
            } else if ((a = cmdLine.next("scan", "s").orNull()) != null) {
                service.scan(cmdLine, appContext);
                return;
            } else if ((a = cmdLine.next("find", "f").orNull()) != null) {
                service.find(cmdLine, appContext);
                return;
            } else if ((a = cmdLine.next("status", "t").orNull()) != null) {
                if (a.getValue().isBoolean()) {
                    service.enableScan(cmdLine, appContext, a.getBooleanValue().get(session));
                } else {
                    service.status(cmdLine, appContext);
                }
                return;
            } else if ((a = cmdLine.next("list", "l").orNull()) != null) {
                service.list(cmdLine, appContext);
                return;
            } else if ((a = cmdLine.next("set").orNull()) != null) {
                service.setWorkspaceConfigParam(cmdLine, appContext);
                return;
            } else {
                cmdLine.setCommandName("nwork").throwUnexpectedArgument();
            }
        } while (cmdLine.hasNext());
        cmdLine.throwMissingArgument();
    }

}
