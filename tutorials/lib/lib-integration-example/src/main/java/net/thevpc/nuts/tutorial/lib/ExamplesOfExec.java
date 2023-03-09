package net.thevpc.nuts.tutorial.lib;

import net.thevpc.nuts.*;

public class ExamplesOfExec {
    public void executeAll(NSession session) {
        executeSomeCommand(session);
        executeSomeCommandRedirect(session);
    }
    public void executeSomeCommand(NSession session) {
        session.out().println("Example of ## Exec ##");
        int result = NExecCommand.of(session)
                .addCommand("ls", "-l")
                .setExecutionType(NExecutionType.SYSTEM)
                .run()
                .getResult();
        session.out().println(NMsg.ofC("result was %s", result));
    }

    public void executeSomeCommandRedirect(NSession session) {
        session.out().println("Example of ## Exec with String Grab ##");
        String result = NExecCommand.of(session)
                .addCommand("ls", "-l")
                .setExecutionType(NExecutionType.SYSTEM)
                .grabOutputString()
                .setRedirectErrorStream(true)
                .run()
                .getOutputString();
        session.out().println(NMsg.ofC("result was %s", result));
    }
}
