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

public class FindCmd<C extends NdbConfig> extends NdbCmd<C> {
    public FindCmd(NdbSupportBase<C> support, String... names) {
        super(support,"find");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
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
                        case "--sort": {
                            status = "--sort";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        case "--limit": {
                            commandLine.withNextLiteral((v, a, s) -> eq.setLimit(v.asLong().get()));
                            break;
                        }
                        case "--skip": {
                            commandLine.withNextLiteral((v, a, s) -> eq.setSkip(v.asLong().get()));
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
                case "--sort": {
                    switch (commandLine.peek().get(session).key()) {
                        case "--where": {
                            status = "--where";
                            commandLine.withNextBoolean((v, a, s) -> {
                            });
                            break;
                        }
                        default: {
                            eq.getSort().add(commandLine.next().get().toString());
                        }
                    }
                    break;
                }
            }
        }
        if (NBlankable.isBlank(eq.getTable())) {
            commandLine.throwMissingArgumentByName("--table");
        }
        C options = loadFromName(name, otherOptions);
        support.revalidateOptions(options);
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            commandLine.throwMissingArgumentByName("--dbname");
        }
        run(eq, options, session);
    }

    protected void run(ExtendedQuery eq, C options, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid"));
    }


}
