package net.thevpc.nuts.tutorial.cli;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineProcessor;

/**
 * Event Based Command line processing
 * @author vpc
 */
public class CustomCliA implements NApplication {

    public static void main(String[] args) {
        new CustomCliA().runAndExit(args);
    }

    @Override
    public void run(NSession session) {
        session.processAppCmdLine(new NCmdLineProcessor() {
            boolean noMoreOptions = false;
            boolean clean = false;
            List<String> params = new ArrayList<>();

            @Override
            public boolean onCmdNextOption(NArg option, NCmdLine cmdLine, NCmdLineContext context) {
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
            public boolean onCmdNextNonOption(NArg nonOption, NCmdLine cmdLine, NCmdLineContext context) {
                params.add(cmdLine.next().get().toString());
                return true;
            }

            @Override
            public void onCmdExec(NCmdLine cmdLine, NCmdLineContext context) {
                if (clean) {
                    cmdLine.getSession().out().println("cleaned!");
                }
            }
        });
    }

}
