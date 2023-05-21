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
                .redirectErrorStream()
                .run()
                .getOutputString();
        session.out().println(NMsg.ofC("result was %s", result));
    }

    public void executeSshCommand(NSession session) {
        String result = NExecCommand.of(session.copy()
                        .setBot(true).json())
                .setTarget("ssh://remoteUserName:remoteUserPassword@192.168.1.98")
                .addCommand("hostname", "-I")
                .setExecutionType(NExecutionType.SYSTEM)
                .grabOutputString()
                .getOutputString();
        session.out().println(result);
        session.out().println(NMsg.ofC("result was %s", result));

    }

    public void executeSshSudoCommand(NSession session) {
        session.out().println("Example of ## Exec ssh command ##");
        String result = NExecCommand.of(session.copy()
                        .setBot(true).json())
                .setTarget("ssh://remoteUserName:remoteUserPassword@192.168.1.98")
                .addCommand("hostname", "-I")
                .addExecutorOptions("--!sudo-prompt")
                .setExecutionType(NExecutionType.SYSTEM)
                .setRunAs(NRunAs.SUDO)
                .setIn(NExecInput.ofString("sudoPassword\n"))
                .grabOutputString()
                .getOutputString();
        session.out().println(NMsg.ofC("result was %s", result));
    }
}
