package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.NutsApplication;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;

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
        while(!cmdLine.isEmpty()) {
            if (appContext.configureFirst(cmdLine)) {
                //
            } else if (
                    cmdLine.peek().toString().equals("-i")
                    ||cmdLine.peek().toString().equals("--interactive")
            ) {
                //interactive
                ts.runInteractive(cmdLine);
                return;
            } else if (ts.runCommands(cmdLine)) {
                //okkay
                return;
            } else {
                cmdLine.unexpectedArgument();
            }
        };
        ts.runCommands(appContext.getWorkspace().commandLine().create("summary"));
    }

}
