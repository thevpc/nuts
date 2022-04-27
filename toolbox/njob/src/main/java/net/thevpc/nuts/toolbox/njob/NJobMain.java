package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;

public class NJobMain implements NutsApplication {


    public static void main(String[] args) {
        new NJobMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        NutsSession session = appContext.getSession();
        JobServiceCmd ts = new JobServiceCmd(appContext);
        NutsCommandLine cmdLine = appContext.getCommandLine();
        NutsArgument a;
        while(!cmdLine.isEmpty()) {
            if (appContext.configureFirst(cmdLine)) {
                //
            } else if (
                    cmdLine.peek().get().toString().equals("-i")
                    ||cmdLine.peek().get().toString().equals("--interactive")
            ) {
                //interactive
                ts.runInteractive(cmdLine);
                return;
            } else if (ts.runCommands(cmdLine)) {
                //okkay
                return;
            } else {
                cmdLine.throwUnexpectedArgument(session);
            }
        };
        ts.runCommands(NutsCommandLine.of(new String[]{"summary"}));
    }

}
