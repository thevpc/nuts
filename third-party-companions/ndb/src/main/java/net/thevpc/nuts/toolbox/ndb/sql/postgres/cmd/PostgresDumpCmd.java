package net.thevpc.nuts.toolbox.ndb.sql.postgres.cmd;

import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresSupport;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresConfig;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd.SqlDumpCmd;

public class PostgresDumpCmd extends SqlDumpCmd<NPostgresConfig> {
    public PostgresDumpCmd(NPostgresSupport support) {
        super(support);
    }

    @Override
    public NPostgresSupport getSupport() {
        return (NPostgresSupport) super.getSupport();
    }
}
