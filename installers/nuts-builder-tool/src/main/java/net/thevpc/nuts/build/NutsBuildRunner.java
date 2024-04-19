package net.thevpc.nuts.build;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.build.util.AbstractRunner;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.time.NChronometer;

public class NutsBuildRunner {

    private AbstractRunner[] runners;

    public NutsBuildRunner(AbstractRunner... runners) {
        this.runners = runners;
    }

    private void configure(NCmdLine args) {
        for (AbstractRunner runner : runners) {
            runner.configureDefaults();
        }
        NCmdLineConfigurable.configure(new NCmdLineConfigurable() {
            @Override
            public boolean configureFirst(NCmdLine cmdLine) {
                for (AbstractRunner runner : runners) {
                    if (runner.configureFirst(cmdLine)) {
                        return true;
                    }
                }
                return false;
            }
        }, false, args.toStringArray(), "nuts-builder", session());
        for (AbstractRunner runner : runners) {
            runner.buildConfiguration();
        }
    }

    public void run(NCmdLine args) {
        NChronometer chrono = NChronometer.startNow();
        session().out().println("Process started");
        configure(args);
        for (AbstractRunner runner : runners) {
            runner.run();
        }
        session().out().println("Process finished in " + chrono.stop());
    }

    private NSession session() {
        return runners[0].session();
    }

}
