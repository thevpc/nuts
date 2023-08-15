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

public class ShowTablesCmd<C extends NdbConfig> extends NdbCmd<C> {
    public ShowTablesCmd(NdbSupportBase<C> support, String... names) {
        super(support, "show-tables", "show-collections", "tables", "collections");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
    public void run(NSession session, NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        C otherOptions = createConfigInstance();
        ExtendedQuery eq = new ExtendedQuery(getName());
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get(session).key()) {
                    case "--config": {
                        readConfigNameOption(cmdLine, session, name);
                        break;
                    }
                    case "--long": {
                        cmdLine.withNextFlag((v, a, s)-> eq.setLongMode(v));
                        break;
                    }
                    default: {
                        fillOptionLast(cmdLine, otherOptions);
                    }
                }
            } else {
                cmdLine.throwUnexpectedArgument();
            }
        }
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            cmdLine.throwMissingArgument("--dbname");
        }
        C options = loadFromName(name, otherOptions);
        support.revalidateOptions(options);
        runShowTables(eq, options, session);
    }

    protected void runShowTables(ExtendedQuery eq, C options, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid"));
    }


}
