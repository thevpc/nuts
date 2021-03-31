package net.thevpc.nuts.toolbox.nnote;

import java.util.logging.Handler;
import java.util.logging.Level;
import net.thevpc.nuts.NutsApplication;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteGuiApp;

public class NNoteMain extends NutsApplication {

    public static void main(String[] args) {
        NNoteSplashScreen.get();
        java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
//        rootLogger.setLevel(Level.FINEST);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.FINEST);
        }
        rootLogger = java.util.logging.Logger.getLogger("net.thevpc");
        rootLogger.setLevel(Level.FINEST);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.FINEST);
        }
        NNoteSplashScreen.get().tic();
        new NNoteMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        NNoteSplashScreen.get().tic();
        NutsCommandLine cmdLine = appContext.getCommandLine();
        NutsArgument a;
        boolean interactive = false;
        boolean console = false;
        boolean gui = false;
        boolean cui = false;
        while (!cmdLine.isEmpty()) {
            if (appContext.configureFirst(cmdLine)) {
                //
            } else if ((a = cmdLine.nextBoolean("-i", "--interactive")) != null) {
                if (a.isEnabled()) {
                    interactive = a.getBooleanValue();
                }
            } else if ((a = cmdLine.nextBoolean("-w", "--gui")) != null) {
                if (a.isEnabled()) {
                    gui = a.getBooleanValue();
                }
            } else if ((a = cmdLine.nextBoolean("--cui")) != null) {
                if (a.isEnabled()) {
                    cui = a.getBooleanValue();
                }
            } else {
                cmdLine.unexpectedArgument();
            }
        }
        if (interactive) {
            console = true;
        }
        if (!console && !gui && !cui) {
            console = true;
        }
        gui = true;//foce for now
        NNoteSplashScreen.get().tic();
        if (cui || gui) {
            runGui(appContext);
        } else if (interactive) {
            runInteractiveConsole(appContext);
        } else {
            runNonInteractiveConsole(appContext);
        }
    }

    private void runGui(NutsApplicationContext appContext) {
        new NNoteGuiApp(appContext).run();
    }

    private void runInteractiveConsole(NutsApplicationContext appContext) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void runNonInteractiveConsole(NutsApplicationContext appContext) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
