package net.thevpc.nuts.toolbox.ndb.sql.postgres.cmd;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresSupport;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresConfig;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd.SqlShowDatabasesCmd;

public class PostgresShowDatabasesCmd extends SqlShowDatabasesCmd<NPostgresConfig> {
    public PostgresShowDatabasesCmd(NPostgresSupport support) {
        super(support);
    }

    @Override
    protected String createShowDatabasesSQL(NPostgresConfig options,NSession session) {
        return "SELECT distinct(schemaname) FROM pg_catalog.pg_tables WHERE schemaname != 'pg_catalog' AND schemaname != 'information_schema'";
    }
}
