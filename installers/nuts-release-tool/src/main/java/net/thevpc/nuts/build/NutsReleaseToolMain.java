package net.thevpc.nuts.build;

import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.build.builders.*;

public class NutsReleaseToolMain implements NApplication {

    public static void main(String[] args) {
        new NutsReleaseToolMain().runAndExit(args);
    }

    @Override
    public void run(NSession session) {
        NutsReleaseToolRunner nutsBuildRunner = new NutsReleaseToolRunner(
                new BaseConfRunner(session),
                new JarsRunner(session),
                new ReposRunner(session),
                new WebSiteRunner(session),
                new InstallerRunner(session)
        );
        nutsBuildRunner.run(
                session.getAppCmdLine()
        );
    }

}
