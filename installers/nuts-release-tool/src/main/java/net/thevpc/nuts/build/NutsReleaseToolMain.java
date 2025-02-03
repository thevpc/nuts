package net.thevpc.nuts.build;

import net.thevpc.nuts.*;
import net.thevpc.nuts.build.builders.*;
import net.thevpc.nuts.io.NPath;

public class NutsReleaseToolMain implements NApplication {

    public static void main(String[] args) {
        new NutsReleaseToolMain().run(args);
    }

    @Override
    public void run() {
        NSession session = NSession.of();
        NPath currentDir = NPath.ofUserDirectory();
        //always yes!!
        session.copy().setConfirm(NConfirmationMode.YES)
                .runWith(() -> {
                    NutsReleaseToolRunner nutsBuildRunner = new NutsReleaseToolRunner(
                            new BaseConfRunner(),
                            new JarsRunner(),
                            new ReposRunner(),
                            new InstallerRunner(),
                            new SiteRunner()
                    );
                    nutsBuildRunner.run(
                            NApp.of().getCmdLine()
                    );
                });
    }

}
