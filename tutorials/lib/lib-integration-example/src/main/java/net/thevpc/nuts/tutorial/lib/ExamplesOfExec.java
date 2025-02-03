package net.thevpc.nuts.tutorial.lib;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.util.NMsg;

public class ExamplesOfExec {
    public void executeAll() {
        executeSomeCommand();
        executeSomeCommandRedirect();
    }

    public void executeSomeCommand() {
        NOut.println("Example of ## Exec ##");
        int result = NExecCmd.of()
                .addCommand("ls", "-l")
                .system()
                .run()
                .getResultCode();
        NOut.println(NMsg.ofC("result was %s", result));
    }

    public void executeSomeCommandRedirect() {
        NOut.println("Example of ## Exec with String Grab ##");
        String result = NExecCmd.of()
                .addCommand("ls", "-l")
                .system()
                .run()
                .getGrabbedAllString();
        NOut.println(NMsg.ofC("result was %s", result));
    }

    public void executeSshCommand() {
        String result = NExecCmd.of()
                .setTarget("ssh://remoteUserName:remoteUserPassword@192.168.1.98")
                .addCommand("hostname", "-I")
                .system()
                .getGrabbedAllString();
        NOut.println(result);
        NOut.println(NMsg.ofC("result was %s", result));

    }

    public void executeSshSudoCommand() {
        NOut.println("Example of ## Exec ssh command ##");
        String result = NExecCmd.of()
                .setTarget("ssh://remoteUserName:remoteUserPassword@192.168.1.98")
                .addCommand("hostname", "-I")
                .addExecutorOptions("--!sudo-prompt")
                .system()
                .sudo()
                .setIn(NExecInput.ofString("sudoPassword\n"))
                .getGrabbedAllString();
        NOut.println(NMsg.ofC("result was %s", result));
    }
}
