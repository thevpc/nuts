package net.thevpc.nuts.tutorial.customshell;

import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.toolbox.nsh.sys.JShellNoExternalExecutor;
import net.thevpc.nuts.toolbox.nsh.jshell.JShell;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellConfiguration;
import net.thevpc.nuts.tutorial.customshell.cmd.Hello;

/**
 * @author vpc
 */
public class CustomShell implements NApplication {

    public static void main(String[] args) {
        new CustomShell().runAndExit(args);
    }

    @Override
    public void run(NApplicationContext nac) {
        JShell sh = new JShell(
                new JShellConfiguration()
                        .setApplicationContext(nac)
        );
        sh.run();
    }

}
