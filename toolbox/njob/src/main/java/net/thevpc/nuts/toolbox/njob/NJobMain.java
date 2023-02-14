package net.thevpc.nuts.toolbox.njob;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;

public class NJobMain implements NApplication {


    public static void main(String[] args) {
        new NJobMain().runAndExit(args);
    }

    @Override
    public void run(NApplicationContext appContext) {
        NSession session = appContext.getSession();
        JobServiceCmd ts = new JobServiceCmd(appContext);
        NCmdLine cmdLine = appContext.getCommandLine();
        NArg a;
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
                cmdLine.throwUnexpectedArgument();
            }
        };
        ts.runCommands(NCmdLine.of(new String[]{"summary"}));
    }

}
