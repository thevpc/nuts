package net.thevpc.nuts.core.test.tutorials;

import net.thevpc.nuts.NApp;
import net.thevpc.nuts.NOut;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.util.NBooleanRef;
import net.thevpc.nuts.util.NMsg;
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
                others.add(cmdLine.next().get().toString());
            }
        }
        NOut.println(NMsg.ofC("boolOption=%s stringOption=%s others=%s", boolOption, stringOption, others));
    }

    public static void cmdLineHelpExample2() {
        NCmdLine cmdLine = NApp.of().getCmdLine();
        NBooleanRef boolOption = NRef.ofBoolean(false);
        NRef<String> stringOption = NRef.ofNull();
        List<String> nonOptions = new ArrayList<>();
        cmdLine.run(new NCmdLineRunner() {
            @Override
            public boolean next(NArg arg, NCmdLine cmdLine) {
                if (arg.isOption()) {
                    switch (arg.key()) {
                        case "-o":
                        case "--option": {
                            cmdLine.withNextFlag((v) -> boolOption.set(v.booleanValue()));
                            return true;
                        }
                        case "-n":
                        case "--name": {
                            cmdLine.withNextEntry((v) -> stringOption.set(v.stringValue()));
                            return true;
                        }
                    }
                    return false;
                } else {
                    nonOptions.add(cmdLine.next().get().toString());
                    return true;
                }
            }

            @Override
            public void validate(NCmdLine cmdLine) {
                if (nonOptions.isEmpty()) {
                    cmdLine.throwMissingArgument();
                }
            }

            @Override
            public void run(NCmdLine cmdLine) {
                NOut.println(NMsg.ofC("running with nonOptions %s", nonOptions));
            }
        });
    }

    public static void cmdLineHelpExample3() {
        NCmdLine cmdLine = NApp.of().getCmdLine();
        NBooleanRef boolOption = NRef.ofBoolean(false);
        NRef<String> stringOption = NRef.ofNull();
        List<String> nonOptions = new ArrayList<>();
        cmdLine.forEachPeek((arg, cmdLine1) -> {
            NArg a = cmdLine1.peek().get();
            if (a.isOption()) {
                switch (a.key()) {
                    case "-o":
                    case "--option": {
                        cmdLine1.withNextFlag((v) -> boolOption.set(v.booleanValue()));
                        return true;
                    }
                    case "-n":
                    case "--name": {
                        cmdLine1.withNextEntry((v) -> stringOption.set(v.stringValue()));
                        return true;
                    }
                }
                return false;
            } else {
                nonOptions.add(cmdLine1.next().get().toString());
                return true;
            }
        });
    }

    public static void cmdLineHelpExample4() {
        NCmdLine cmdLine = NApp.of().getCmdLine();
        NBooleanRef boolOption = NRef.ofBoolean(false);
        NRef<String> stringOption = NRef.ofNull();
        List<String> nonOptions = new ArrayList<>();
        while (cmdLine.hasNext()) {
            if (!cmdLine.withFirst(
                    (arg, c) -> c.with("-o","--option").nextFlag((v) -> boolOption.set(v.booleanValue()))
                    , (arg, c) -> c.with("-n","--name").nextEntry((v) -> stringOption.set(v.stringValue()))
            )) {
                if (cmdLine.peekNonOption().isPresent()) {
                    nonOptions.add(cmdLine.next().get().toString());
                } else {
                    NSession.of().configureLast(cmdLine);
                }
            }
        }
    }
}
