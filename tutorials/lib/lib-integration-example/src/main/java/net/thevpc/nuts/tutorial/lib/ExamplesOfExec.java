package net.thevpc.nuts.tutorial.lib;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.util.NMsg;

public class ExamplesOfExec {
    public void executeAll(NSession session) {
        executeSomeCommand(session);
        executeSomeCommandRedirect(session);
    }

    public void executeSomeCommand(NSession session) {
        session.out().println("Example of ## Exec ##");
        int result = NExecCmd.of()
                .addCommand("ls", "-l")
                .system()
                .run()
                .getResultCode();
        session.out().println(NMsg.ofC("result was %s", result));
    }

    public void executeSomeCommandRedirect(NSession session) {
        session.out().println("Example of ## Exec with String Grab ##");
        String result = NExecCmd.of()
                .addCommand("ls", "-l")
                .system()
                .run()
                .getGrabbedAllString();
        session.out().println(NMsg.ofC("result was %s", result));
    }

    public void executeSshCommand(NSession session) {
        String result = NExecCmd.of()
                .setTarget("ssh://remoteUserName:remoteUserPassword@192.168.1.98")
                .addCommand("hostname", "-I")
                .system()
                .getGrabbedAllString();
        session.out().println(result);
        session.out().println(NMsg.ofC("result was %s", result));

    }

    public void executeSshSudoCommand(NSession session) {
        session.out().println("Example of ## Exec ssh command ##");
        String result = NExecCmd.of()
                .setTarget("ssh://remoteUserName:remoteUserPassword@192.168.1.98")
                .addCommand("hostname", "-I")
                .addExecutorOptions("--!sudo-prompt")
                .system()
                .sudo()
                .setIn(NExecInput.ofString("sudoPassword\n"))
                .getGrabbedAllString();
        session.out().println(NMsg.ofC("result was %s", result));
    }
}
