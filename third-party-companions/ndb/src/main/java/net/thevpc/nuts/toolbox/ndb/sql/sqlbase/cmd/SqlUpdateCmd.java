package net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.cmd.UpdateCmd;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;

import java.util.Arrays;
import java.util.Map;

public class SqlUpdateCmd<C extends NdbConfig> extends UpdateCmd<C> {
    public SqlUpdateCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }
    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

    @Override
    protected void runUpdate(ExtendedQuery eq, C options) {
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(eq.getTable()).append(" set ");
        StringBuilder setSb = new StringBuilder();
        for (String s : eq.getSet()) {
            s = s.trim();
            if (s.length() > 0) {
                if (s.startsWith("{")) {
                    Map<String, Object> row = NElements.of().parse(s, Map.class);
                    for (Map.Entry<String, Object> e : row.entrySet()) {
                        if (setSb.length() > 0) {
                            setSb.append(",");
                        }
                        setSb.append(e.getKey());
                        setSb.append("=");
                        setSb.append(getSupport().formatLiteral(e.getValue()));
                    }
                } else {
                    if (setSb.length() > 0) {
                        setSb.append(",");
                    }
                    setSb.append(s);
                }
            }
        }
        if (setSb.length() == 0) {
            throw new NIllegalArgumentException(NMsg.ofPlain("missing set"));
        }
        String whereSQL = getSupport().createWhere(eq.getWhere());
        if (!whereSQL.isEmpty()) {
            sql.append(" where ");
            sql.append(whereSQL);
        }
        getSupport().runSQL(Arrays.asList(sql.toString()), options);
    }
}
