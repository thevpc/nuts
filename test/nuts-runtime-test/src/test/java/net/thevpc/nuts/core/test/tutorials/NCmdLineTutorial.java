package net.thevpc.nuts.core.test.tutorials;

import net.thevpc.nuts.app.NApplication;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.util.NBooleanRef;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NRef;

import java.util.ArrayList;
import java.util.List;

public class NCmdLineTutorial {
    public static void cmdLineHelpExample1() {
        NCmdLine cmdLine = NCmdLine.of("");
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
                        if (a.isUncommented()) {
                            boolOption = a.getBooleanValue().get();
                        }
                        break;
                    }
                    case "-n":
                    case "--name": {
                        a = cmdLine.nextEntry().get();
                        if (a.isUncommented()) {
                            stringOption = a.getStringValue().get();
                        }
                        break;
                    }
                    default: {
                        NSession.of().configureLast(cmdLine);
                    }
                }
            } else {
                others.add(cmdLine.next().get().toString());
            }
        }
        NOut.println(NMsg.ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));
    }

    public static void cmdLineHelpExample2() {
        NCmdLine cmdLine = NApplication.of().cmdLine();
        NBooleanRef boolOption = NRef.ofBoolean(false);
        NRef<String> stringOption = NRef.ofNull();
        List<String> nonOptions = new ArrayList<>();
        cmdLine.matcher()
                        .when("-o","--option").asFlag(a->boolOption.set(a.booleanValue()))
                        .when("-n","--name").asEntry(a->stringOption.set(a.stringValue()))
                        .whenNonOption().asArg(a-> nonOptions.add(a.image()))
                        .requireAll();

        if (nonOptions.isEmpty()) {
            cmdLine.throwMissingArgument();
        }
        NOut.println(NMsg.ofC("running with nonOptions %s", nonOptions));
    }



    public static void cmdLineHelpExample4() {
        NCmdLine cmdLine = NApplication.of().cmdLine();
        NBooleanRef boolOption = NRef.ofBoolean(false);
        NRef<String> stringOption = NRef.ofNull();
        List<String> nonOptions = new ArrayList<>();
        while (cmdLine.hasNext()) {
            cmdLine.matcher()
                    .when("-o", "--option").asFlag((v) -> boolOption.set(v.booleanValue()))
                    .when("-n", "--name").asEntry((v) -> stringOption.set(v.stringValue()))
                    .whenNonOption().asArg(v -> nonOptions.add(v.image()))
                    .withDefaults()
                    .require()
            ;
        }
    }
}

