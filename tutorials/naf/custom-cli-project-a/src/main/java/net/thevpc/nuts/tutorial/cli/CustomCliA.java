package net.thevpc.nuts.tutorial.cli;

import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.NApp;
import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineRunner;

/**
 * Event Based Command line processing
 * @author vpc
 */
public class CustomCliA implements NApplication {

    public static void main(String[] args) {
        new CustomCliA().runAndExit(args);
    }

    @Override
    public void run() {
        NApp.of().processCmdLine(new NCmdLineRunner() {
            boolean noMoreOptions = false;
            boolean clean = false;
            List<String> params = new ArrayList<>();

            @Override
            public boolean nextOption(NArg option, NCmdLine cmdLine, NCmdLineContext context) {
                if (!noMoreOptions) {
                    return false;
                }
                switch (option.key()) {
                    case "-c":
                    case "--clean": {
                        NArg a = cmdLine.nextFlag().get();
                        if (a.isEnabled()) {
                            clean = a.getBooleanValue().get();
                        }
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean nextNonOption(NArg nonOption, NCmdLine cmdLine, NCmdLineContext context) {
                params.add(cmdLine.next().get().toString());
                return true;
            }

            @Override
            public void run(NCmdLine cmdLine, NCmdLineContext context) {
                if (clean) {
                    NSession session = NSession.get().get();
                    session.out().println("cleaned!");
                }
            }
        });
    }

}
