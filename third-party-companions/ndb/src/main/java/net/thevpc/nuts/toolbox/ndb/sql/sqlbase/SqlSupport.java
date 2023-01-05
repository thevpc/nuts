package net.thevpc.nuts.toolbox.ndb.sql.sqlbase;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.base.NdbSupportBase;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.cmd.*;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.util.SqlHelper;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class SqlSupport<C extends NdbConfig> extends NdbSupportBase<C> {
    protected String dbDriverPackage;
    protected String dbDriverClass;

    public SqlSupport(String dbType, Class<C> configClass, NApplicationContext appContext, String dbDriverPackage, String dbDriverClass) {
        super(dbType, configClass, appContext);
        this.dbDriverPackage = dbDriverPackage;
        this.dbDriverClass = dbDriverClass;
        declareNdbCmd(new SqlCountCmd<>(this));
        declareNdbCmd(new SqlCreateIndexCmd<>(this));
        declareNdbCmd(new SqlDeleteCmd<>(this));
        declareNdbCmd(new SqlFindCmd<>(this));
        declareNdbCmd(new SqlInsertCmd<>(this));
        declareNdbCmd(new SqlQueryCmd<>(this));
        declareNdbCmd(new SqlRenameTableCmd<>(this));
        declareNdbCmd(new SqlReplaceCmd<>(this));
        declareNdbCmd(new SqlRestoreTableCmd<>(this));
        declareNdbCmd(new SqlShowDatabasesCmd<>(this));
        declareNdbCmd(new SqlShowTablesCmd<>(this));
        declareNdbCmd(new SqlUpdateCmd<>(this));
    }

//    @Override
//    public void runRawQuery(ExtendedQuery eq, C options, NSession session) {
//        runSQL(Arrays.asList(eq.getRawQuery()), options, session);
//    }


    public void runSQL(List<String> sql, AtName name, Boolean forceShowSQL, NSession session) {
        C options = loadConfig(name).get();
        runSQL(sql, options, session);
    }

    public void runSQL(List<String> sql, C options, NSession session) {
        if (options == null) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("missing config"));
        }
        boolean forceShowSQL = session.isTrace();
        revalidateOptions(options);
        if (isRemoteCommand(options)) {
            //call self remotely
            NPrepareCommand.of(session).setUserName(options.getRemoteUser()).setTargetServer(options.getRemoteServer()).addIds(Arrays.asList(NId.of(dbDriverPackage).get())).run();
            run(sysSsh(options, session).addCommand("nuts").addCommand(appContext.getAppId().toString()).addCommand(dbType).addCommand("run-sql").addCommand("--host=" + options.getHost()).addCommand("--port=" + options.getPort()).addCommand("--dbname=" + options.getDatabaseName()).addCommand("--user=" + options.getUser()).addCommand("--password=" + options.getPassword()));
            return;
        }
        String jdbcUrl = createJdbcURL(options);
        SqlHelper.runAndWaitFor(sql, jdbcUrl, dbDriverPackage, dbDriverClass, options.getUser(), options.getPassword(), null, forceShowSQL, session);
    }

    public abstract String createJdbcURL(C options);

    public abstract void revalidateOptions(C options);


    public String createWhere(List<String> where, NSession session) {
        NElements elements = NElements.of(session);
        StringBuilder whereSb = new StringBuilder();
        for (String s : where) {
            s = s.trim();
            if (s.length() > 0) {
                if (s.startsWith("{")) {
                    Map<String, Object> row = elements.parse(s, Map.class);
                    for (Map.Entry<String, Object> e : row.entrySet()) {
                        if (whereSb.length() > 0) {
                            whereSb.append(" and ");
                        }
                        whereSb.append(e.getKey());
                        whereSb.append("=");
                        whereSb.append(formatLiteral(e.getValue()));
                    }
                } else {
                    if (whereSb.length() > 0) {
                        whereSb.append(" and ");
                    }
                    whereSb.append(s);
                }
            }
        }
        return whereSb.toString();
    }

    public String formatLiteral(Object o) {
        if (o == null || o instanceof Boolean || o instanceof Number) {
            return String.valueOf(o);
        }
        return NStringUtils.formatStringLiteral(String.valueOf(o), NStringUtils.QuoteType.SIMPLE);
    }


}
