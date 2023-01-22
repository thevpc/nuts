package net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.CmdRedirect;
import net.thevpc.nuts.toolbox.ndb.base.cmd.DumpCmd;
import net.thevpc.nuts.toolbox.ndb.base.cmd.InsertCmd;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresConfig;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;
import net.thevpc.nuts.util.NRef;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class SqlDumpCmd<C extends NdbConfig> extends DumpCmd<C> {
    public SqlDumpCmd(SqlSupport<C> support, String... names) {
        super(support, names);
    }
    @Override
    public SqlSupport<C> getSupport() {
        return (SqlSupport<C>) super.getSupport();
    }

}
