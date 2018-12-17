package net.vpc.toolbox.worky;

import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.FolderNonOption;

import java.io.File;

public class WorkyMain extends NutsApplication {

    private WorkspaceService service;

    public static void main(String[] args) {
        new WorkyMain().launchAndExit(args);
    }


    @Override
    public int launch(NutsApplicationContext appContext) {
        String[] args = appContext.getArgs();
        this.service = new WorkspaceService(appContext);
        CommandLine cmdLine = new CommandLine(args, appContext.getAutoComplete());
        Argument a;
        do {
            if (appContext.configure(cmdLine)) {
                //
            } else if ((a = cmdLine.readNonOption("scan")) != null) {
                return service.scan(cmdLine, appContext);
            } else if ((a = cmdLine.readNonOption("check")) != null) {
                return service.check(cmdLine, appContext);
            } else if (cmdLine.readAll("enable scan")) {
                return service.enableScan(cmdLine, appContext, true);
            } else if (cmdLine.readAll("disable scan")) {
                return service.enableScan(cmdLine, appContext, false);
            } else if ((a = cmdLine.readNonOption("list")) != null) {
                return service.list(cmdLine, appContext);
            } else if ((a = cmdLine.readNonOption("set")) != null) {
                return service.setWorkspaceConfigParam(cmdLine, appContext);
            } else {
                cmdLine.unexpectedArgument("worky");
            }
        } while (cmdLine.hasNext());
        if (appContext.isRequiredExit()) {
            return appContext.getExitCode();
        }
        return 0;
    }


}
