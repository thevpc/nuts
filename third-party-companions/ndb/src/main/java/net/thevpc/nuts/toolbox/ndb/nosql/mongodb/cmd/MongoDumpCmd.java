package net.thevpc.nuts.toolbox.ndb.nosql.mongodb.cmd;

import net.thevpc.nuts.toolbox.ndb.base.cmd.DumpCmd;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoConfig;
import net.thevpc.nuts.toolbox.ndb.nosql.mongodb.NMongoSupport;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresConfig;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresSupport;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd.SqlDumpCmd;

public class MongoDumpCmd extends DumpCmd<NMongoConfig> {
    public MongoDumpCmd(NMongoSupport support) {
        super(support);
    }

    @Override
    public NMongoSupport getSupport() {
        return (NMongoSupport) super.getSupport();
    }
}
