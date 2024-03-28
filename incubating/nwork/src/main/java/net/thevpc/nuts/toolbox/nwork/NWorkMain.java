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
    public void run(NSession session) {
        this.service = new WorkspaceService(session);
        NCmdLine cmdLine = session.getAppCmdLine().setCommandName("nwork");
        NArg a;
        do {
            if (session.configureFirst(cmdLine)) {
                //
            } else if ((a = cmdLine.next("scan", "s").orNull()) != null) {
                service.scan(cmdLine, session);
                return;
            } else if ((a = cmdLine.next("find", "f").orNull()) != null) {
                service.find(cmdLine, session);
                return;
            } else if ((a = cmdLine.next("status", "t").orNull()) != null) {
                if (a.getValue().isBoolean()) {
                    service.enableScan(cmdLine, session, a.getBooleanValue().get(session));
                } else {
                    service.status(cmdLine, session);
                }
                return;
            } else if ((a = cmdLine.next("push").orNull()) != null) {
                if (a.getValue().isBoolean()) {
                    service.enableScan(cmdLine, session, a.getBooleanValue().get(session));
                } else {
                    service.push(cmdLine, session);
                }
                return;
            } else if ((a = cmdLine.next("list", "l").orNull()) != null) {
                service.list(cmdLine, session);
                return;
            } else if ((a = cmdLine.next("set").orNull()) != null) {
                service.setWorkspaceConfigParam(cmdLine, session);
                return;
            } else {
                cmdLine.setCommandName("nwork").throwUnexpectedArgument();
            }
        } while (cmdLine.hasNext());
        cmdLine.throwMissingArgument();
    }

}
