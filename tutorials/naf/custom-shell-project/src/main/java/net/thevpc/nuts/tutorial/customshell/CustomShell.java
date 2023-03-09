package net.thevpc.nuts.tutorial.customshell;

import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.nshell.NShellConfiguration;

/**
 * @author vpc
 */
public class CustomShell implements NApplication {

    public static void main(String[] args) {
        new CustomShell().runAndExit(args);
    }

    @Override
    public void run(NApplicationContext nac) {
        NShell sh = new NShell(
                new NShellConfiguration()
                        .setApplicationContext(nac)
        );
        sh.run();
    }

}
