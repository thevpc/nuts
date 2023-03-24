package net.thevpc.nuts.toolbox.ndb.sql.postgres.cmd;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresSupport;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresConfig;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd.SqlShowTablesCmd;

public class PostgresShowTablesCmd extends SqlShowTablesCmd<NPostgresConfig> {
    public PostgresShowTablesCmd(NPostgresSupport support) {
        super(support);
    }

    @Override
    protected String createShowTablesSQL(NPostgresConfig options,NSession session) {
        return "SELECT schemaname,tablename, tablename FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema'";
    }
}
