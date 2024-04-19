package net.thevpc.nuts.build;

import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.build.builders.NutsBaseRunner;
import net.thevpc.nuts.build.builders.NutsExecRunner;

public class NutsBuildToolMain implements NApplication {

    public static void main(String[] args) {
        new NutsBuildToolMain().runAndExit(args);
    }

    @Override
    public void run(NSession session) {
        if (true) {
            throw new RuntimeException("some error");
        }
        NutsBuildRunner nutsBuildRunner = new NutsBuildRunner(
                new NutsBaseRunner(session),
                new NutsExecRunner(session)
        );
        nutsBuildRunner.run(
                session.getAppCmdLine()
        );
    }

}
