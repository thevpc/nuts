package net.thevpc.nuts.toolbox.ndb.sql.postgres.cmd;

import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresSupport;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresConfig;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd.SqlDumpCmd;
import net.thevpc.nuts.util.NRef;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PostgresDumpCmd extends SqlDumpCmd<NPostgresConfig> {
    public PostgresDumpCmd(NPostgresSupport support) {
        super(support);
    }

    @Override
    public NPostgresSupport getSupport() {
        return (NPostgresSupport) super.getSupport();
    }
}
