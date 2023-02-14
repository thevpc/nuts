package net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd;

import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.cmd.DumpCmd;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;

public class SqlDumpCmd<C extends NdbConfig> extends DumpCmd<C> {
    public SqlDumpCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }
    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

}
