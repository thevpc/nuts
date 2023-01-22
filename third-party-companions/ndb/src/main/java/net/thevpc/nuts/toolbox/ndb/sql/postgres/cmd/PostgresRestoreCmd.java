package net.thevpc.nuts.toolbox.ndb.sql.postgres.cmd;

import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresConfig;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresSupport;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd.SqlRestoreCmd;

public class PostgresRestoreCmd extends SqlRestoreCmd<NPostgresConfig> {
    public PostgresRestoreCmd(NPostgresSupport support) {
        super(support);
    }

    @Override
    public NPostgresSupport getSupport() {
        return (NPostgresSupport) super.getSupport();
    }


}
