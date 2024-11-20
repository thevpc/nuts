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

public class FindCmd<C extends NdbConfig> extends NdbCmd<C> {
    public FindCmd(NdbSupportBase<C> support, String... names) {
        super(support,"find");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
    public void run(NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        ExtendedQuery eq = new ExtendedQuery(getName());
        C otherOptions = createConfigInstance();

        String status = "";
        while (cmdLine.hasNext()) {
            switch (status) {
                case "": {
                    switch (cmdLine.peek().get().key()) {
                        case "--config": {
                            readConfigNameOption(cmdLine, name);
                            break;
                        }
                        case "--entity":
                        case "--table":
                        case "--collection": {
                            cmdLine.withNextEntry((v, a) -> eq.setTable(v));
                            break;
                        }
                        case "--where": {
                            status = "--where";
                            cmdLine.withNextFlag((v, a) -> {
                            });
                            break;
                        }
                        case "--sort": {
                            status = "--sort";
                            cmdLine.withNextFlag((v, a) -> {
                            });
                            break;
                        }
                        case "--limit": {
                            cmdLine.withNextValue((v, a) -> eq.setLimit(v.asLong().get()));
                            break;
                        }
                        case "--skip": {
                            cmdLine.withNextValue((v, a) -> eq.setSkip(v.asLong().get()));
                            break;
                        }
                        default: {
                            fillOptionLast(cmdLine, otherOptions);
                        }
                    }
                    break;
                }
                case "--where": {
                    switch (cmdLine.peek().get().key()) {
                        case "--sort": {
                            status = "--sort";
                            cmdLine.withNextFlag((v, a) -> {
                            });
                            break;
                        }
                        default: {
                            eq.getWhere().add(cmdLine.next().get().toString());
                        }
                    }
                    break;
                }
                case "--sort": {
                    switch (cmdLine.peek().get().key()) {
                        case "--where": {
                            status = "--where";
                            cmdLine.withNextFlag((v, a) -> {
                            });
                            break;
                        }
                        default: {
                            eq.getSort().add(cmdLine.next().get().toString());
                        }
                    }
                    break;
                }
            }
        }
        if (NBlankable.isBlank(eq.getTable())) {
            cmdLine.throwMissingArgument("--table");
        }
        C options = loadFromName(name, otherOptions);
        support.revalidateOptions(options);
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            cmdLine.throwMissingArgument("--dbname");
        }
        run(eq, options);
    }

    protected void run(ExtendedQuery eq, C options) {
        throw new NIllegalArgumentException(NMsg.ofPlain("invalid"));
    }


}
