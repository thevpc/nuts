package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.util.NRef;

import java.util.Arrays;

public class RenameTableCmd<C extends NdbConfig> extends NdbCmd<C> {
    public RenameTableCmd(NdbSupportBase<C> support, String... names) {
        super(support,"rename-table","rename-collection","rename-entity");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
    public void run(NApplicationContext appContext, NCommandLine commandLine) {
        NSession session = appContext.getSession();
        NRef<AtName> name = NRef.ofNull(AtName.class);
        C otherOptions = createConfigInstance();
        ExtendedQuery eq = new ExtendedQuery(getName());
        NRef<String> table = new NRef<>();
        while (commandLine.hasNext()) {
            NArg arg = commandLine.peek().get(session);
            switch (arg.key()) {
                case "--config": {
                    readConfigNameOption(commandLine, session, name);
                    break;
                }
                case "--entity":
                case "--table":
                case "--collection": {
                    commandLine.withNextEntry((v, a, s) -> table.set(v));
                    break;
                }
                default: {
                    if (arg.isOption()) {
                        fillOptionLast(commandLine, otherOptions);
                    } else {
                        eq.setNewName(arg.toString());
                    }
                }
            }
        }

        if (table.isBlank()) {
            commandLine.throwMissingArgumentByName("--entity");
        }
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            commandLine.throwMissingArgumentByName("--dbname");
        }
        C options = loadFromName(name, otherOptions);
        getSupport().revalidateOptions(options);
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            commandLine.throwMissingArgumentByName("--dbname");
        }
        runRenameTable(eq, options, session);
    }



    protected void runRenameTable(ExtendedQuery eq, C options, NSession session) {
        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid"));
    }

}
