package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NRef;

import java.util.Arrays;

public class QueryCmd<C extends NdbConfig> extends NdbCmd<C> {
    public QueryCmd(NdbSupportBase<C> support, String... names) {
        super(support,"query");
        this.names.addAll(Arrays.asList(names));
    }

    public void run(NSession session, NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        ExtendedQuery eq = new ExtendedQuery(getName());
        C otherOptions = createConfigInstance();
        String status = "";
        while (cmdLine.hasNext()) {
            switch (status) {
                case "": {
                    switch (cmdLine.peek().get(session).key()) {
                        case "--config": {
                            readConfigNameOption(cmdLine, session, name);
                            break;
                        }
                        case "--command": {
                            cmdLine.withNextEntry((v, a, s) -> eq.setCommand(v));
                            break;
                        }
                        case "--entity":
                        case "--table":
                        case "--collection": {
                            cmdLine.withNextEntry((v, a, s) -> eq.setTable(v));
                            break;
                        }
                        case "--where": {
                            status = "--where";
                            cmdLine.withNextFlag((v, a, s) -> {
                            });
                            break;
                        }
                        case "--one": {
                            cmdLine.withNextFlag((v, a, s) -> eq.setOne(v));
                            break;
                        }
                        case "--set": {
                            status = "--set";
                            cmdLine.withNextFlag((v, a, s) -> {
                            });
                            break;
                        }
                        case "--sort": {
                            status = "--sort";
                            cmdLine.withNextFlag((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            fillOptionLast(cmdLine, otherOptions);
                        }
                    }
                    break;
                }
                case "--where": {
                    switch (cmdLine.peek().get(session).key()) {
                        case "--set": {
                            status = "--set";
                            cmdLine.withNextFlag((v, a, s) -> {
                            });
                            break;
                        }
                        case "--sort": {
                            status = "--sort";
                            cmdLine.withNextFlag((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            eq.getWhere().add(cmdLine.next().get().toString());
                        }
                    }
                    break;
                }
                case "--set": {
                    switch (cmdLine.peek().get(session).key()) {
                        case "--where": {
                            status = "--where";
                            cmdLine.withNextFlag((v, a, s) -> {
                            });
                            break;
                        }
                        case "--sort": {
                            status = "--sort";
                            cmdLine.withNextFlag((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            eq.getSet().add(cmdLine.next().get().toString());
                        }
                    }
                    break;
                }
                case "--sort": {
                    switch (cmdLine.peek().get(session).key()) {
                        case "--where": {
                            status = "--where";
                            cmdLine.withNextFlag((v, a, s) -> {
                            });
                            break;
                        }
                        case "--set": {
                            status = "--set";
                            cmdLine.withNextFlag((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            eq.getSet().add(cmdLine.next().get().toString());
                        }
                    }
                    break;
                }
                case "--sql":
                case "--query": {
                    eq.setRawQuery(cmdLine.next().get().toString());
                    break;
                }
            }
        }
        if (NBlankable.isBlank(eq.getTable())) {
            cmdLine.throwMissingArgument("--table");
        }

        C options = loadFromName(name, otherOptions);
        revalidateOptions(options);
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            cmdLine.throwMissingArgument("--dbname");
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
                throw new NIllegalArgumentException(session, NMsg.ofC("unsupported %s", eq.getCommand()));
            }
        }
    }

    protected void runRawQuery(ExtendedQuery eq, C options, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid"));
    }

}
