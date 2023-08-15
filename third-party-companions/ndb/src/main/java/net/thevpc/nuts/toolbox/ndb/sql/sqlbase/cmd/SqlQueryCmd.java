package net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.cmd.QueryCmd;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;

import java.util.Arrays;

public class SqlQueryCmd<C extends NdbConfig> extends QueryCmd<C> {
    public SqlQueryCmd(SqlSupport<C> support, String... names) {
        super(support, names);
        this.names.addAll(Arrays.asList("run-sql", "sql"));
    }
    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

    @Override
    protected void runRawQuery(ExtendedQuery eq, C options, NSession session) {
        getSupport().runSQL(Arrays.asList(eq.getRawQuery()), options, session);
    }
}
