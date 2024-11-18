package net.thevpc.nuts.toolbox.ndb.base.cmd;

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


    public void run(NCmdLine cmdLine) {
        List<String> fromOptions = new ArrayList<>();
        List<String> toOptions = new ArrayList<>();
        NRef<NPath> tempDataFile = NRef.ofNull();
        NRef<Boolean> keepFile = NRef.ofNull();
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                String key = cmdLine.peek().get().key();
                switch (key) {
                    case "--from":
                    {
                        cmdLine.withNextEntry((v, a) ->
                                fromOptions.addAll(Arrays.asList("--db", v)));
                        break;
                    }
                    case "--to":
                    {
                        cmdLine.withNextEntry((v, a) ->
                                toOptions.addAll(Arrays.asList("--db", v)));
                        break;
                    }
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
                    case "--from-db":
                    {
                        cmdLine.withNextEntry((v, a) ->
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
                        cmdLine.withNextEntry((v, a) -> toOptions.addAll(Arrays.asList(
                                "--" + key.substring("--to-".length())
                                , v)));
                        break;
                    }
                    case "--file": {
                        cmdLine.withNextEntry((v, a) -> {
                            if (!v.endsWith(".zip")) {
                                v = v + ".zip";
                            }
                            tempDataFile.set(NPath.of(v).toAbsolute());
                        });
                        break;
                    }
                    case "--keep-file": {
                        cmdLine.withNextFlag((v, a) -> keepFile.set(v));
                        break;
                    }
                    default: {
                        if (support.getSession().configureFirst(cmdLine)) {

                        } else {
                            NSession session = NSession.of().get();
                            session.configureLast(cmdLine);
                        }
                    }
                }
            } else {
                cmdLine.throwUnexpectedArgument();
            }
        }
        if (tempDataFile.isNull()) {
            tempDataFile.set(NPath.ofUserDirectory().resolve(
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
        getSupport().findCommand("dump").get().run(NCmdLine.of(fromOptions));
        getSupport().findCommand("restore").get().run(NCmdLine.of(toOptions));
        if (!keepFile.get()) {
            tempDataFile.get().deleteTree();
        }
    }
}
