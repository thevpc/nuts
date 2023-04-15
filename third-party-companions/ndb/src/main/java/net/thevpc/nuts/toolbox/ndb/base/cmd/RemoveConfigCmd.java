package net.thevpc.nuts.toolbox.ndb.base.cmd;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.util.NdbUtils;
import net.thevpc.nuts.util.NRef;

import java.util.Arrays;

public class RemoveConfigCmd<C extends NdbConfig> extends NdbCmd<C> {
    public RemoveConfigCmd(NdbSupportBase<C> support, String... names) {
        super(support,"remove-config");
        this.names.addAll(Arrays.asList(names));
    }

    @Override
    public void run(NSession session, NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get(session).key()) {
                    case "--config": {
                        readConfigNameOption(cmdLine, session, name);
                        break;
                    }
                    default: {
                        session.configureLast(cmdLine);
                    }
                }
            } else {
                if (name.isNull()) {
                    name.set(new AtName(cmdLine.next().get(session).asString().get(session)));
                } else {
                    cmdLine.throwUnexpectedArgument();
                }
            }
        }
        if (name.isNull()) {
            name.set(new AtName(""));
        }
        removeConfig(name.get());
    }

    private void removeConfig(AtName name) {
        NPath file = getSharedConfigFolder().resolve(asFullName(name) + NdbUtils.SERVER_CONFIG_EXT);
        if (file.exists()) {
            file.delete();
        }
    }


}
