package net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.cmd.ShowDatabasesCmd;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;

import java.util.Arrays;

public class SqlShowDatabasesCmd<C extends NdbConfig> extends ShowDatabasesCmd<C> {
    public SqlShowDatabasesCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }
    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

    @Override
    protected void runShowDatabases(ExtendedQuery eq, C options) {
        ((SqlSupport<C>)support).runSQL(Arrays.asList(createShowDatabasesSQL(options)), options);
    }

    protected String createShowDatabasesSQL(C options) {
        throw new NIllegalArgumentException(NMsg.ofPlain("unsupported createShowDatabasesSQL"));
    }
}
