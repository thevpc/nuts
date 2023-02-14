package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NRef;

import java.util.Arrays;

public class ShowTablesCmd<C extends NdbConfig> extends NdbCmd<C> {
    public ShowTablesCmd(NdbSupportBase<C> support, String... names) {
        super(support, "show-tables", "show-collections", "tables", "collections");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
    public void run(NApplicationContext appContext, NCmdLine commandLine) {
        NSession session = appContext.getSession();
        NRef<AtName> name = NRef.ofNull(AtName.class);
        C otherOptions = createConfigInstance();
        ExtendedQuery eq = new ExtendedQuery(getName());
        while (commandLine.hasNext()) {
            if (commandLine.isNextOption()) {
                switch (commandLine.peek().get(session).key()) {
                    case "--config": {
                        readConfigNameOption(commandLine, session, name);
                        break;
                    }
                    case "--long": {
                        commandLine.withNextFlag((v, a, s)-> eq.setLongMode(v));
                        break;
                    }
                    default: {
                        fillOptionLast(commandLine, otherOptions);
                    }
                }
            } else {
                commandLine.throwUnexpectedArgument();
            }
        }
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            commandLine.throwMissingArgumentByName("--dbname");
        }
        C options = loadFromName(name, otherOptions);
        support.revalidateOptions(options);
        runShowTables(eq, options, session);
    }

    protected void runShowTables(ExtendedQuery eq, C options, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid"));
    }


}
