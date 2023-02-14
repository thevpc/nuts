package net.thevpc.nuts.toolbox.nwork;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;

public class NWorkMain implements NApplication {

    private WorkspaceService service;

    public static void main(String[] args) {
        new NWorkMain().runAndExit(args);
    }

    @Override
    public void run(NApplicationContext appContext) {
        this.service = new WorkspaceService(appContext);
        NSession session = appContext.getSession();
        NCmdLine cmdLine = appContext.getCommandLine().setCommandName("nwork");
        NArg a;
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
            } else if ((a = cmdLine.next("push").orNull()) != null) {
                if (a.getValue().isBoolean()) {
                    service.enableScan(cmdLine, appContext, a.getBooleanValue().get(session));
                } else {
                    service.push(cmdLine, appContext);
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
