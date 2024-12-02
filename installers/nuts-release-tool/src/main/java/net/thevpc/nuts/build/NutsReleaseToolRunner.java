package net.thevpc.nuts.build;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.build.util.AbstractRunner;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.time.NChronometer;

public class NutsReleaseToolRunner {

    private AbstractRunner[] runners;

    public NutsReleaseToolRunner(AbstractRunner... runners) {
        this.runners = runners;
    }

    private void configure(NCmdLine args) {
        for (AbstractRunner runner : runners) {
            runner.configureBeforeOptions(args);
        }
        NCmdLineConfigurable.configure(cmdLine -> {
            for (AbstractRunner runner : runners) {
                if (runner.configureFirst(cmdLine)) {
                    return true;
                }
            }
            return false;
        }, false, args.toStringArray(), "nuts-builder");
        for (AbstractRunner runner : runners) {
            runner.configureAfterOptions();
        }
    }

    public NutsBuildRunnerContext context() {
        NSession session = NSession.of();
        NutsBuildRunnerContext s = (NutsBuildRunnerContext) session.getProperty(NutsBuildRunnerContext.class.getName(), NScopeType.SESSION).orNull();
        if (s == null) {
            s = new NutsBuildRunnerContext();
            session.setProperty(NutsBuildRunnerContext.class.getName(), NScopeType.SESSION, s);
        }
        return s;
    }


    public void run(NCmdLine args) {
        NChronometer chrono = NChronometer.startNow();
        NSession session = NSession.of();
        session.out().println("Process started");
        configure(args);
        for (AbstractRunner runner : runners) {
            runner.run();
        }
        session.out().println("Process finished in " + chrono.stop());
    }



}
