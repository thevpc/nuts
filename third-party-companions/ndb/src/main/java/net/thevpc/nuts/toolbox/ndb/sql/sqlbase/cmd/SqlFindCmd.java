package net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd;

import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.cmd.FindCmd;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;

import java.util.Arrays;

public class SqlFindCmd<C extends NdbConfig> extends FindCmd<C> {
    public SqlFindCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }

    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

    @Override
    protected void run(ExtendedQuery eq, C options) {
        getSupport().runSQL(Arrays.asList(eq.getRawQuery()), options);
    }

}
