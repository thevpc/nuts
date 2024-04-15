package net.thevpc.nuts.build;

import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NSession;

public class NutsBuildToolMain implements NApplication {
    public static void main(String[] args) {
        new NutsBuildToolMain().runAndExit(args);
    }

    @Override
    public void run(NSession session) {
        NutsBuildRunner nutsBuildRunner = new NutsBuildRunner(session);
        nutsBuildRunner.configure(session.getAppCmdLine());
        nutsBuildRunner.run();
    }

}
