package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NRef;

import java.util.Arrays;

public class ShowDatabasesCmd<C extends NdbConfig> extends NdbCmd<C> {
    public ShowDatabasesCmd(NdbSupportBase<C> support, String... names) {
        super(support,"show-db","show-databases","databases","db");
        this.names.addAll(Arrays.asList(names));
    }


    public void run(NApplicationContext appContext, NCommandLine commandLine) {
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

        C options = loadFromName(name, otherOptions);
        revalidateOptions(options);
        runShowDatabases(eq, options, session);
    }

    protected void runShowDatabases(ExtendedQuery eq, C options, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid"));
    }


}
