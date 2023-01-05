package net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.cmd.DeleteCmd;
import net.thevpc.nuts.toolbox.ndb.base.cmd.InsertCmd;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;

import java.util.Arrays;
import java.util.Map;

public class SqlDeleteCmd<C extends NdbConfig> extends DeleteCmd<C> {
    public SqlDeleteCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }

    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

    @Override
    protected void runDelete(ExtendedQuery eq, C options, NSession session) {
        StringBuilder sql = new StringBuilder();
        sql.append("delete  from ").append(options.getName());
        String whereSQL = getSupport().createWhere(eq.getWhere(), session);
        if (!whereSQL.isEmpty()) {
            sql.append(" where ");
            sql.append(whereSQL);
        }
        getSupport().runSQL(Arrays.asList(sql.toString()), options, session);
    }

}
