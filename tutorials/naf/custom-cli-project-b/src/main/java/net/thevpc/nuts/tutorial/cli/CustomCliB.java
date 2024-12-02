package net.thevpc.nuts.tutorial.cli;

import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.NApp;
import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;

/**
 *
 * @author vpc
 */
public class CustomCliB implements NApplication {

    public static void main(String[] args) {
        new CustomCliB().runAndExit(args);
    }

    @Override
    public void run() {
        NSession session = NSession.of();
        NCmdLine cmdLine = NApp.of().getCmdLine();
        boolean boolOption = false;
        String stringOption = null;
        List<String> others = new ArrayList<>();
        NArg a;
        while (cmdLine.hasNext()) {
            a = cmdLine.peek().get();
            if (a.isOption()) {
                switch (a.key()) {
                    case "-o":
                    case "--option": {
                        a = cmdLine.nextFlag().get();
                        if (a.isEnabled()) {
                            boolOption = a.getValue().asBoolean().get();
                        }
                        break;
                    }
                    case "-n":
                    case "--name": {
                        a = cmdLine.nextEntry().get();
                        if (a.isEnabled()) {
                            stringOption = a.getValue().asString().get();
                        }
                        break;
                    }
                    default: {
                        session.configureLast(cmdLine);
                    }
                }
            } else {
                others.add(cmdLine.next().get().toString());
            }
        }
        // test if application is running in exec mode
        // (and not in autoComplete mode)
        if (cmdLine.isExecMode()) {
            //do the good staff here
            session.out().println(NMsg.ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));
        }
    }

}
