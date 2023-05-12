package net.thevpc.nuts.toolbox.ndb.sql.postgres.cmd;

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.base.NdbCmd;
import net.thevpc.nuts.toolbox.ndb.sql.nmysql.util.AtName;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresConfig;
import net.thevpc.nuts.toolbox.ndb.sql.postgres.NPostgresSupport;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;
import net.thevpc.nuts.toolbox.ndb.sql.util.*;
import net.thevpc.nuts.util.NMemorySize;
import net.thevpc.nuts.util.NRef;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class PostgresShowTableSizeCmd extends NdbCmd<NPostgresConfig> {
    public PostgresShowTableSizeCmd(NPostgresSupport support) {
        super(support, "show-table-size");
    }

    private static class Result {
        String tableName;
        Long size;
        String sizePretty;
        Long rows;
        Long columns;

        public Result(String tableName, Long size, Long rows, Long columns) {
            this.tableName = tableName;
            this.size = size;
            this.sizePretty = this.size == null ? null : new NMemorySize(this.size, 0, false).toString();
            this.rows = rows;
            this.columns = columns;
        }
    }

    @Override
    public void run(NSession session, NCmdLine cmdLine) {
        NRef<AtName> name = NRef.ofNull(AtName.class);
        NPostgresConfig otherOptions = createConfigInstance();
        ExtendedQuery eq = new ExtendedQuery(getName());
        while (cmdLine.hasNext()) {
            if (cmdLine.isNextOption()) {
                switch (cmdLine.peek().get(session).key()) {
                    case "--config": {
                        readConfigNameOption(cmdLine, session, name);
                        break;
                    }
                    case "--long": {
                        cmdLine.withNextFlag((v, a, s) -> eq.setLongMode(v));
                        break;
                    }
                    default: {
                        fillOptionLast(cmdLine, otherOptions);
                    }
                }
            } else {
                cmdLine.throwUnexpectedArgument();
            }
        }
        if (NBlankable.isBlank(otherOptions.getDatabaseName())) {
            cmdLine.throwMissingArgument("--dbname");
        }
        NPostgresConfig options = loadFromName(name, otherOptions);
        support.revalidateOptions(options);
        SqlSupport<NPostgresConfig> support1 = (SqlSupport<NPostgresConfig>) getSupport();
        // SELECT pg_size_pretty(pg_database_size('mydb'));
        String sizeQuery="select\n" +
                "  table_name,\n" +
                "  pg_size_pretty(pg_relation_size(quote_ident(table_name))),\n" +
                "  pg_relation_size(quote_ident(table_name))\n" +
                "from information_schema.tables\n" +
                "where table_schema = 'public'\n" +
                "order by 3 desc;";
        SqlDB sqlDB = SqlHelper.computeSchema(eq, support1, options, session);

        LinkedHashMap<String, Result> all = new LinkedHashMap<>();
        for (SqlCatalog cat : sqlDB.catalogs.values()) {
            for (SqlSchema schem : cat.schemas.values()) {
                for (SqlTable s : schem.tables.values()) {
                    String tableName = s.tableName;
                    List<Object> sizeResult = support1.callSqlAndWaitGet("Select pg_total_relation_size('" + tableName + "')", options, false, session);
                    List<Object> countResult = support1.callSqlAndWaitGet("Select count(1) from \"" + tableName + "\"", options, false, session);
                    all.put(tableName, new Result(tableName,
                            sizeResult.isEmpty() ? 0L : ((Number) sizeResult.get(0)).longValue(),
                            countResult.isEmpty() ? 0L : ((Number) countResult.get(0)).longValue(),
                            (long) s.columns.size()
                    ));
                }
            }
        }
        List<Result> rr = new ArrayList<>(all.values());
        rr.sort(Comparator.<Result>comparingLong(x -> x.size == null ? -1 : x.size).reversed());
        long totalRows = 0;
        long totalColumns = 0;
        long totalSize = 0;
        for (Result result : rr) {
            if (result.rows != null) {
                totalRows += result.rows;
            }
            if (result.columns != null) {
                totalColumns += result.columns;
            }
            if (result.size != null) {
                totalSize += result.size;
            }
        }
        rr.add(new Result(
                "<TOTAL> ("+rr.size()+")",
                totalSize,
                totalRows,
                totalColumns
        ));
        session.out().println(rr);
        //SqlHelper.callSqlAndWaitGet("")
    }
}
