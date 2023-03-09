package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.util.NRef;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CopyDBCmd<C extends NdbConfig> extends NdbCmd<C> {
    public CopyDBCmd(NdbSupportBase<C> support, String... names) {
        super(support, "copydb", "copy-db");
        this.names.addAll(Arrays.asList(names));
    }


    public void run(NApplicationContext appContext, NCmdLine commandLine) {
        NSession session = appContext.getSession();
        List<String> fromOptions = new ArrayList<>();
        List<String> toOptions = new ArrayList<>();
        NRef<NPath> tempDataFile = NRef.ofNull();
        NRef<Boolean> keepFile = NRef.ofNull();
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                String key = commandLine.peek().get(session).key();
                switch (key) {
                    case "--from-name":
                    case "--from-host":
                    case "--from-port":
                    case "--from-dbname":
                    case "--from-user":
                    case "--from-password":
                    case "--from-remote-server":
                    case "--from-remote-user":
                    case "--from-remote-temp-folder":
                    case "--from-ssh":
                    case "--from-db": {
                        commandLine.withNextEntry((v, a, s) ->
                                fromOptions.addAll(Arrays.asList(
                                        "--" + key.substring("--from-".length())
                                        , v)));
                        break;
                    }
                    case "--to-name":
                    case "--to-host":
                    case "--to-port":
                    case "--to-dbname":
                    case "--to-user":
                    case "--to-password":
                    case "--to-remote-server":
                    case "--to-remote-user":
                    case "--to-remote-temp-folder":
                    case "--to-ssh":
                    case "--to-db": {
                        commandLine.withNextEntry((v, a, s) -> toOptions.addAll(Arrays.asList(
                                "--" + key.substring("--to-".length())
                                , v)));
                        break;
                    }
                    case "--file": {
                        commandLine.withNextEntry((v, a, s) -> {
                            if (!v.endsWith(".zip")) {
                                v = v + ".zip";
                            }
                            tempDataFile.set(NPath.of(v, session).toAbsolute());
                        });
                        break;
                    }
                    case "--keep-file": {
                        commandLine.withNextFlag((v, a, s) -> keepFile.set(v));
                        break;
                    }
                    default: {
                        if (support.getAppContext().configureFirst(commandLine)) {

                        } else {
                            commandLine.getSession().configureLast(commandLine);
                        }
                    }
                }
            } else {
                commandLine.throwUnexpectedArgument();
            }
        }
        if (tempDataFile.isNull()) {
            tempDataFile.set(NPath.ofUserDirectory(session).resolve(
                    new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date())
                            + ".zip"
            ));
            if (keepFile.isNull()) {
                keepFile.set(false);
            }
        } else if (keepFile.isNull()) {
            keepFile.set(true);
        }
        fromOptions.addAll(Arrays.asList("--file", tempDataFile.toString()));
        toOptions.addAll(Arrays.asList("--file", tempDataFile.toString()));
        getSupport().findCommand("dump").get().run(appContext, NCmdLine.of(fromOptions).setSession(session));
        getSupport().findCommand("restore").get().run(appContext, NCmdLine.of(toOptions).setSession(session));
        if (!keepFile.get()) {
            tempDataFile.get().deleteTree();
        }
    }
}
