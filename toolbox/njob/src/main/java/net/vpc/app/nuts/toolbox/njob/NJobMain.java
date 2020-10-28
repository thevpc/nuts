package net.vpc.app.nuts.toolbox.njob;

import net.vpc.app.nuts.NutsApplication;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;

public class NJobMain extends NutsApplication {


    public static void main(String[] args) {
        new NJobMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        String[] args = appContext.getArguments();
        JobServiceCmd ts = new JobServiceCmd(appContext);
        NutsCommandLine cmdLine = appContext.getCommandLine();
        NutsArgument a;
        do {
            if (appContext.configureFirst(cmdLine)) {
                //
            } else if (ts.runCommands(cmdLine)) {
                //okkay
                return;
            } else {
                cmdLine.unexpectedArgument();
            }
        } while (cmdLine.hasNext());
        cmdLine.required();
    }

}
