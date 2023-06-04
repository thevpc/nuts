package net.thevpc.nuts.build;

import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NSession;

public class NutsBuildToolMain implements NApplication {
    public static void main(String[] args) {
        new NutsBuildToolMain().runAndExit(args);
    }

    public void run(NSession session) {
        NutsBuildRunner nutsBuildRunner = new NutsBuildRunner(session);
        nutsBuildRunner.configure(session.getAppCommandLine());
        nutsBuildRunner.run();
    }

}
