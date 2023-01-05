package net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.cmd.InsertCmd;
import net.thevpc.nuts.toolbox.ndb.base.cmd.RenameTableCmd;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;

import java.util.Arrays;
import java.util.Map;

public class SqlRenameTableCmd<C extends NdbConfig> extends RenameTableCmd<C> {
    public SqlRenameTableCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }
    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

    @Override
    protected void runRenameTable(ExtendedQuery eq, C options, NSession session) {
        StringBuilder sql = new StringBuilder();

        StringBuilder setKeys = new StringBuilder();
        StringBuilder setVals = new StringBuilder();
        for (String s : eq.getSet()) {
            s = s.trim();
            if (s.length() > 0) {
                if (s.startsWith("{")) {
                    Map<String, Object> row = NElements.of(session).parse(s, Map.class);
                    for (Map.Entry<String, Object> e : row.entrySet()) {
                        if (setKeys.length() > 0) {
                            setKeys.append(",");
                            setVals.append(",");
                        }
                        setKeys.append(e.getKey());
                        setVals.append(((SqlSupport<C>) support).formatLiteral(e.getValue()));
                    }
                } else {
                    if (setKeys.length() > 0) {
                        setKeys.append(",");
                    }
                    int i = s.indexOf('=');
                    if (i < 0) {
                        throw new NIllegalArgumentException(session, NMsg.ofCstyle("invalid %s", s));
                    }
                    setKeys.append(s, 0, i);
                    setVals.append(s, i + 1, s.length());
                }
            }
        }
        if (setKeys.length() == 0) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("missing set"));
        }


        sql.append("insert into ").append(eq.getTable()).append(" (");
        sql.append(setKeys);
        sql.append(") values (");
        sql.append(setVals);
        sql.append(")");
        ((SqlSupport<C>) support).runSQL(Arrays.asList(sql.toString()), options, session);
    }
}
