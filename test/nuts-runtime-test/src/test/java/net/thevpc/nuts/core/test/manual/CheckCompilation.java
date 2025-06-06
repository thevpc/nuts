package net.thevpc.nuts.core.test.manual;

import net.thevpc.nuts.NApp;
import net.thevpc.nuts.NOut;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.util.NBooleanRef;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NRef;

import java.util.ArrayList;
import java.util.List;

public class CheckCompilation {
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
                        if (a.isNonCommented()) {
                            boolOption = a.getBooleanValue().get();
                        }
                        break;
                    }
                    case "-n":
                    case "--name": {
                        a = cmdLine.nextEntry().get();
                        if (a.isNonCommented()) {
                            stringOption = a.getStringValue().get();
                        }
                        break;
                    }
                    default: {
                        NSession.of().configureLast(cmdLine);
                    }
                }
            } else {
                others. add(cmdLine. next().get().toString());
            }
        }
        NOut.println(NMsg. ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));
    }

    public static void cmdLineHelpExample2() {
        NCmdLine cmdLine = NApp.of().getCmdLine();
        NBooleanRef boolOption = NRef.ofBoolean(false);
        NRef<String>  stringOption = NRef.ofNull();
        List<String>  nonOptions = new ArrayList<>();
        cmdLine.run(new NCmdLineRunner() {
            @Override
            public boolean nextOption(NArg a, NCmdLine cmdLine) {
                switch (a.key()) {
                    case "-o":
                    case "--option": {
                        cmdLine.withNextFlag((v, e)->boolOption.set(v));
                        return true;
                    }
                    case "-n":
                    case "--name": {
                        cmdLine.withNextEntry((v, e)->stringOption.set(v));
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean nextNonOption(NArg nonOption, NCmdLine cmdLine) {
                nonOptions.add(cmdLine.next().get().toString());
                return true;
            }
        });
    }

    public static void cmdLineHelpExample3() {
        NCmdLine cmdLine = NApp.of().getCmdLine();
        NBooleanRef boolOption = NRef.ofBoolean(false);
        NRef<String>  stringOption = NRef.ofNull();
        List<String>  nonOptions = new ArrayList<>();
        cmdLine.forEachPeek(new NCmdLineProcessor() {
            @Override
            public boolean process(NCmdLine cmdLine) {
                NArg a = cmdLine.peek().get();
                if (a.isOption()) {
                    switch (a.key()) {
                        case "-o":
                        case "--option": {
                            cmdLine.withNextFlag((v, e)->boolOption.set(v));
                            return true;
                        }
                        case "-n":
                        case "--name": {
                            cmdLine.withNextEntry((v, e)->stringOption.set(v));
                            return true;
                        }
                    }
                    return false;
                } else {
                    nonOptions.add(cmdLine.next().get().toString());
                    return true;
                }
            }
        });
    }
}
