package net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.cmd.ShowTablesCmd;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;

import java.util.Arrays;

public class SqlShowTablesCmd<C extends NdbConfig> extends ShowTablesCmd<C> {
    public SqlShowTablesCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }
    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

    @Override
    protected void runShowTables(ExtendedQuery eq, C options) {
        ((SqlSupport<C>)support).runSQL(Arrays.asList(createShowTablesSQL(options)), options);
    }

    protected String createShowTablesSQL(C options) {
        throw new NIllegalArgumentException(NMsg.ofPlain("unsupported createShowTablesSQL"));
    }
}
