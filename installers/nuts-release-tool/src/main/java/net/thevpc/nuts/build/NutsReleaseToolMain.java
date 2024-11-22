package net.thevpc.nuts.build;

import net.thevpc.nuts.NApp;
import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.build.builders.*;

public class NutsReleaseToolMain implements NApplication {

    public static void main(String[] args) {
        new NutsReleaseToolMain().runAndExit(args);
    }

    @Override
    public void run() {
        NSession session = NSession.get();
        NutsReleaseToolRunner nutsBuildRunner = new NutsReleaseToolRunner(
                new BaseConfRunner(),
                new JarsRunner(),
                new ReposRunner(),
                new SiteRunner(),
                new InstallerRunner(session)
        );
        nutsBuildRunner.run(
                NApp.of().getCmdLine()
        );
    }

}
