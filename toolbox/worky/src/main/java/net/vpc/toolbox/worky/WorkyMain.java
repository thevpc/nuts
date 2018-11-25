package net.vpc.toolbox.worky;

import net.vpc.app.nuts.*;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.FolderNonOption;

import java.io.File;

public class WorkyMain extends NutsApplication {

    private WorkspaceService service;

    public static void main(String[] args) {
        new WorkyMain().launchAndExit(args);
    }


    @Override
    public int launch(String[] args, NutsWorkspace ws) {
        this.service = new WorkspaceService(ws);
        CommandLine cmd = new CommandLine(args);
        while (cmd.hasNext()) {
            if (cmd.readAll("scan")) {
                boolean interactive = false;
                while (cmd.hasNext()) {
                    if (cmd.readAll("-i", "--interactive")) {
                        interactive = true;
                    } else {
                        service.scan(new File(cmd.readNonOption(new FolderNonOption("Folder")).getExpression()), interactive);
                    }
                }
            } else if (cmd.readAll("check")) {
                service.check(service.findProjectServices(), cmd);
            } else if (cmd.readAll("enable", "scan")) {
                while (cmd.hasNext()) {
                    service.setNoScan(new File(cmd.read().getExpression()), true);
                }
            } else if (cmd.readAll("disable", "scan")) {
                while (cmd.hasNext()) {
                    service.setNoScan(new File(cmd.read().getExpression()), false);
                }
            } else if (cmd.readAll("set")) {
                while (cmd.hasNext()) {
                    service.setNoScan(new File(cmd.read().getExpression()), false);
                }
            } else if (cmd.readAll("list")) {
                service.showList();
            } else {
                cmd.unexpectedArgument();
            }
        }
        return 0;
    }


}
