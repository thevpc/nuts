package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NRef;

import java.util.Arrays;

public class QueryCmd<C extends NdbConfig> extends NdbCmd<C> {
    public QueryCmd(NdbSupportBase<C> support, String... names) {
        super(support,"query");
        this.names.addAll(Arrays.asList(names));
    }

    public void run(NApplicationContext appContext, NCommandLine commandLine) {
        NSession session = appContext.getSession();
        NRef<AtName> name = NRef.ofNull(AtName.class);
        ExtendedQuery eq = new ExtendedQuery(getName());
        C otherOptions = createConfigInstance();
        String status = "";
        while (commandLine.hasNext()) {
            switch (status) {
                case "": {
                    switch (commandLine.peek().get(session).key()) {
                        case "--config": {
                            readConfigNameOption(commandLine, session, name);
                            break;
                        }
                        case "--command": {
                            commandLine.withNextString((v, a, s) -> eq.setCommand(v));
                            break;
                        }
                        case "--entity":
                        case "--table":
                        case "--collection": {
                            commandLine.withNextString((v, a, s) -> eq.setTable(v));
                            break;
                        }
                        case "--where": {
                            status = "--where";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        case "--one": {
                            commandLine.withNextBoolean((v, a, s) -> eq.setOne(v));
                            break;
                        }
                        case "--set": {
                            status = "--set";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        case "--sort": {
                            status = "--sort";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            fillOptionLast(commandLine, otherOptions);
                        }
                    }
                    break;
                }
                case "--where": {
                    switch (commandLine.peek().get(session).key()) {
                        case "--set": {
                            status = "--set";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        case "--sort": {
                            status = "--sort";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            eq.getWhere().add(commandLine.next().get().toString());
                        }
                    }
                    break;
                }
                case "--set": {
                    switch (commandLine.peek().get(session).key()) {
                        case "--where": {
                            status = "--where";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        case "--sort": {
                            status = "--sort";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            eq.getSet().add(commandLine.next().get().toString());
                        }
                    }
                    break;
                }
                case "--sort": {
                    switch (commandLine.peek().get(session).key()) {
                        case "--where": {
                            status = "--where";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        case "--set": {
                            status = "--set";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            eq.getSet().add(commandLine.next().get().toString());
                        }
                    }
                    break;
                }
                case "--sql":
                case "--query": {
                    eq.setRawQuery(commandLine.next().get().toString());
                    break;
                }
            }
        }
        if (NBlankable.isBlank(eq.getTable())) {
            commandLine.throwMissingArgumentByName("--table");
        }

        C options = loadFromName(name, otherOptions);
        revalidateOptions(options);
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            commandLine.throwMissingArgumentByName("--dbname");
        }
        switch ("" + eq.getCommand()) {
//            case "find": {
//                runFind(eq, options, session);
//                break;
//            }
//            case "create-index": {
//                runCreateIndex(eq, options, session);
//                break;
//            }
//            case "update": {
//                runUpdate(eq, options, session);
//                break;
//            }
//            case "replace": {
//                runReplace(eq, options, session);
//                break;
//            }
//            case "rename-table": {
//                runRenameTable(eq, options, session);
//                break;
//            }
//            case "delete": {
//                runDelete(eq, options, session);
//                break;
//            }
//            case "insert": {
//                runInsert(eq, options, session);
//                break;
//            }
//            case "show-tables": {
//                runShowTables(eq, options, session);
//                break;
//            }
//            case "show-db": {
//                runShowDatabases(eq, options, session);
//                break;
//            }
            case "query": {
                runRawQuery(eq, options, session);
                break;
            }
            default: {
                throw new NIllegalArgumentException(session, NMsg.ofCstyle("unsupported %s", eq.getCommand()));
            }
        }
    }

    protected void runRawQuery(ExtendedQuery eq, C options, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid"));
    }

}
