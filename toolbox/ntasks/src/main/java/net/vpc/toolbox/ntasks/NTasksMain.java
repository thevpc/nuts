package net.vpc.toolbox.ntasks;

import net.vpc.app.nuts.NutsApplication;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;

public class NTasksMain extends NutsApplication {


    public static void main(String[] args) {
        new NTasksMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        String[] args = appContext.getArguments();
        TaskServiceCmd ts = new TaskServiceCmd(appContext);
        NutsCommandLine cmdLine = appContext.getCommandLine();
        NutsArgument a;
        do {
            if (appContext.configureFirst(cmdLine)) {
                //
            } else if (ts.runCommands(cmdLine)) {
                //okkay
                return;
            } else {
                cmdLine.setCommandName("ntasks").unexpectedArgument();
            }
        } while (cmdLine.hasNext());
        cmdLine.required();
    }

}
