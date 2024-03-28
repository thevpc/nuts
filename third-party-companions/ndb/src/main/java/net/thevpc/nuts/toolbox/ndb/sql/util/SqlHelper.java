package net.thevpc.nuts.toolbox.ndb.sql.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.toolbox.ndb.ExtendedQuery;
import net.thevpc.nuts.toolbox.ndb.NdbConfig;
import net.thevpc.nuts.toolbox.ndb.sql.sqlbase.SqlSupport;
import net.thevpc.nuts.toolbox.ndb.util.ClassloaderAwareCallable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.io.Closeable;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SqlHelper implements Closeable {
    private String jdbcUrl;
    private String jdbcDriver;
    private Connection connection;
    private static Map<Object, ClassLoader> cachedClassLoaders = new HashMap<>();

    public static void runAndWaitFor(List<String> sql, SqlConnectionInfo info, Boolean forceShowSQL, NSession session) {
        runAndWaitFor(
                new SqlRunnable() {
                    @Override
                    public void run(SqlHelper h, NSession session) {
                        h.runAndPrintSql(sql, forceShowSQL, session);
                    }
                }, info, session
        );
    }

    public static List<Object> callSqlAndWaitGet(String sql, SqlConnectionInfo info, Boolean forceShowSQL, NSession session) {
        return callAndWaitFor(
                new SqlCallable<List<Object>>() {

                    @Override
                    public List<Object> run(SqlHelper h, NSession session) {
                        if (forceShowSQL != null && forceShowSQL) {
                            session.out().println(NMsg.ofCode("sql", sql));
                        }
                        return h.runSQL2(sql);
                    }
                }, info, session
        );
    }

    public static String getColumnNullableName(int columnNull) {
        switch (columnNull) {
            case DatabaseMetaData.columnNoNulls:
                return "columnNoNulls";
            case DatabaseMetaData.columnNullable:
                return "columnNullable";
            case DatabaseMetaData.columnNullableUnknown:
                return "columnNullableUnknown";
        }
        return "unknown[" + columnNull + "]";
    }

    public static String getColumnYesNoEmptyName(String columnNull) {
        switch (NStringUtils.trim(columnNull).toUpperCase()) {
            case "YES":
                return "YES";
            case "NO":
                return "NO";
            case "":
                return "UNKNOWN";
        }
        return "UNKNOWN[" + columnNull + "]";
    }

    public static String getSqlTypeName(int sqlType) {
        switch (sqlType) {
            case Types.BIT:
                return "BIT";
            case Types.TINYINT:
                return "TINYINT";
            case Types.SMALLINT:
                return "SMALLINT";
            case Types.INTEGER:
                return "INTEGER";
            case Types.BIGINT:
                return "BIGINT";
            case Types.FLOAT:
                return "FLOAT";
            case Types.REAL:
                return "REAL";
            case Types.DOUBLE:
                return "DOUBLE";
            case Types.NUMERIC:
                return "NUMERIC";
            case Types.DECIMAL:
                return "DECIMAL";
            case Types.CHAR:
                return "CHAR";
            case Types.VARCHAR:
                return "VARCHAR";
            case Types.LONGVARCHAR:
                return "LONGVARCHAR";
            case Types.DATE:
                return "DATE";
            case Types.TIME:
                return "TIME";
            case Types.TIMESTAMP:
                return "TIMESTAMP";
            case Types.BINARY:
                return "BINARY";
            case Types.VARBINARY:
                return "VARBINARY";
            case Types.LONGVARBINARY:
                return "LONGVARBINARY";
            case Types.NULL:
                return "NULL";
            case Types.OTHER:
                return "OTHER";
            case Types.JAVA_OBJECT:
                return "JAVA_OBJECT";
            case Types.DISTINCT:
                return "DISTINCT";
            case Types.STRUCT:
                return "STRUCT";
            case Types.ARRAY:
                return "ARRAY";
            case Types.BLOB:
                return "BLOB";
            case Types.CLOB:
                return "CLOB";
            case Types.REF:
                return "REF";
            case Types.DATALINK:
                return "DATALINK";
            case Types.BOOLEAN:
                return "BOOLEAN";
            case Types.ROWID:
                return "ROWID";
            case Types.NCHAR:
                return "NCHAR";
            case Types.NVARCHAR:
                return "NVARCHAR";
            case Types.LONGNVARCHAR:
                return "LONGNVARCHAR";
            case Types.NCLOB:
                return "NCLOB";
            case Types.SQLXML:
                return "SQLXML";
            case Types.REF_CURSOR:
                return "REF_CURSOR";
            case Types.TIME_WITH_TIMEZONE:
                return "TIME_WITH_TIMEZONE";
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return "TIMESTAMP_WITH_TIMEZONE";
        }
        return "UNKNOWN[" + sqlType + "]";
    }

    public static String getIndexTypeName(short value) {
        switch (value) {
            case DatabaseMetaData.tableIndexStatistic:
                return "tableIndexStatistic";
            case DatabaseMetaData.tableIndexClustered:
                return "tableIndexClustered";
            case DatabaseMetaData.tableIndexHashed:
                return "tableIndexHashed";
            case DatabaseMetaData.tableIndexOther:
                return "tableIndexOther";
        }
        return "unknown[" + value + "]";
    }

    public static String getImportedKeyDeferrability(short updateRule) {
        switch (updateRule) {
            case DatabaseMetaData.importedKeyInitiallyDeferred:
                return "importedKeyInitiallyDeferred";
            case DatabaseMetaData.importedKeyInitiallyImmediate:
                return "importedKeyInitiallyImmediate";
            case DatabaseMetaData.importedKeyNotDeferrable:
                return "importedKeyNotDeferrable";
        }
        return "unknown[" + updateRule + "]";
    }

    public static String getImportedKeyRuleName(short updateRule) {
        switch (updateRule) {
            case DatabaseMetaData.importedKeyNoAction:
                return "importedKeyNoAction";
            case DatabaseMetaData.importedKeyCascade:
                return "importedKeyCascade";
            case DatabaseMetaData.importedKeyRestrict:
                return "importedKeyRestrict";
            case DatabaseMetaData.importedKeySetNull:
                return "importedKeySetNull";
            case DatabaseMetaData.importedKeySetDefault:
                return "importedKeySetDefault";

//            case DatabaseMetaData.bestRowTemporary              : return "bestRowTemporary";
//            case DatabaseMetaData.bestRowTemporary              : return "bestRowTemporary";
//            case DatabaseMetaData.bestRowTransaction            : return "bestRowTransaction";
//            case DatabaseMetaData.bestRowSession                : return "bestRowSession";
//            case DatabaseMetaData.bestRowUnknown                : return "bestRowUnknown";
//            case DatabaseMetaData.bestRowNotPseudo              : return "bestRowNotPseudo";
//            case DatabaseMetaData.bestRowPseudo                 : return "bestRowPseudo";
//            case DatabaseMetaData.versionColumnUnknown          : return "versionColumnUnknown";
//            case DatabaseMetaData.versionColumnNotPseudo        : return "versionColumnNotPseudo";
//            case DatabaseMetaData.versionColumnPseudo           : return "versionColumnPseudo";
//            case DatabaseMetaData.typeNoNulls                   : return "typeNoNulls";
//            case DatabaseMetaData.typeNullable                  : return "typeNullable";
//            case DatabaseMetaData.typeNullableUnknown           : return "typeNullableUnknown";
//            case DatabaseMetaData.typePredNone                  : return "typePredNone";
//            case DatabaseMetaData.typePredChar                  : return "typePredChar";
//            case DatabaseMetaData.typePredBasic                 : return "typePredBasic";
//            case DatabaseMetaData.typeSearchable                : return "typeSearchable";
//            case DatabaseMetaData.attributeNoNulls              : return "attributeNoNulls";
//            case DatabaseMetaData.attributeNullable             : return "attributeNullable";
//            case DatabaseMetaData.attributeNullableUnknown      : return "attributeNullableUnknown";
//            case DatabaseMetaData.sqlStateXOpen                 : return "sqlStateXOpen";
//            case DatabaseMetaData.sqlStateSQL                   : return "sqlStateSQL";
//            case DatabaseMetaData.sqlStateSQL99                 : return "sqlStateSQL99";
//            case DatabaseMetaData.functionColumnUnknown         : return "functionColumnUnknown";
//            case DatabaseMetaData.functionColumnIn              : return "functionColumnIn";
//            case DatabaseMetaData.functionColumnInOut           : return "functionColumnInOut";
//            case DatabaseMetaData.functionColumnOut             : return "functionColumnOut";
//            case DatabaseMetaData.functionReturn                : return "functionReturn";
//            case DatabaseMetaData.functionColumnResult          : return "functionColumnResult";
//            case DatabaseMetaData.functionNoNulls               : return "functionNoNulls";
//            case DatabaseMetaData.functionNullable              : return "functionNullable";
//            case DatabaseMetaData.functionNullableUnknown       : return "functionNullableUnknown";
//            case DatabaseMetaData.functionResultUnknown         : return "functionResultUnknown";
//            case DatabaseMetaData.functionNoTable               : return "functionNoTable";
//            case DatabaseMetaData.functionReturnsTable          : return "functionReturnsTable";
        }
        return "unknown[" + updateRule + "]";
    }

    public Connection getConnection() {
        return connection;
    }

    public static void runAndWaitFor(SqlRunnable sql, SqlConnectionInfo info, NSession session) {
        ClassloaderAwareCallable<Object> callable = new ClassloaderAwareCallable<>(session, null,
                SqlHelper.createClassLoader(session, info.getId()));
        callable.runAndWaitFor((ClassloaderAwareCallable.Context cc) -> {
            try (SqlHelper connection = new SqlHelper(info, cc.getClassLoader())) {
                sql.run(connection, session);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        });
    }

    public static <T> T callAndWaitFor(SqlCallable<T> sql, SqlConnectionInfo info, NSession session) {
        ClassloaderAwareCallable<Object> callable = new ClassloaderAwareCallable<>(session, null,
                SqlHelper.createClassLoader(session, info.getId()));
        Object o = callable.runAndWaitFor((ClassloaderAwareCallable.Context cc) -> {
            try (SqlHelper connection = new SqlHelper(info, cc.getClassLoader())) {
                return sql.run(connection, session);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        return (T) o;
    }

    public static ClassLoader createClassLoader(NSession session, String id) {
        String z = session.getWorkspace().getUuid() + "/" + id;
        return cachedClassLoaders.computeIfAbsent(z, x -> NSearchCmd.of(session).addId(id)
                .setDependencyFilter(NDependencyFilters.of(session).byRunnable())
                .getResultClassLoader(SqlHelper.class.getClassLoader()));
    }

    public SqlHelper(SqlConnectionInfo info, ClassLoader classLoader) {
        this(info.getJdbcUrl(), info.getJdbcDriver(), info.getUser(), info.getPassword(), info.getProperties(), classLoader);
    }

    public SqlHelper(String jdbcUrl, String jdbcDriver, String user, String password, Properties properties, ClassLoader classLoader) {
        this.jdbcUrl = jdbcUrl;
        this.jdbcDriver = jdbcDriver;
        Properties info = new Properties();
        if (properties != null) {
            properties.putAll(info);
        }
        if (user != null) {
            info.put("user", user);
        }
        if (password != null) {
            info.put("password", password);
        }
        try {
            Class<?> z = Class.forName(jdbcDriver, true, classLoader);
            Driver d = (Driver) z.newInstance();
            DriverManager.registerDriver(new DriverShim(d));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        try {
            connection = DriverManager.getConnection(jdbcUrl, info);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public List<String> splitSQLList(List<String> sql) {
        List<String> a = new ArrayList<>();
        for (String s : sql) {
            a.addAll(splitSQL(s));
        }
        return a;
    }

    public List<String> splitSQL(String sql) {
        return Arrays.asList(sql);
    }

    public List<Object> runSQL2(String sql) {
        return runSQL(sql).stream().map(x -> x.size() == 1 ? x.get(0) : x).collect(Collectors.toList());
    }

    public List<List<Object>> runSQL(String sql) {
        try {
            List<List<Object>> all = new ArrayList<>();
            if (sql.toLowerCase().startsWith("select")) {
                try (Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery(sql);
                    ResultSetMetaData md = resultSet.getMetaData();
                    int columnCount = md.getColumnCount();
                    while (resultSet.next()) {
                        List<Object> row = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.add(resultSet.getObject(i));
                        }
                        all.add(row);
                    }
                }
            } else {
                try (Statement statement = connection.createStatement()) {
                    int result = statement.executeUpdate(sql);
                    all.add(new ArrayList<>(Arrays.asList(result)));
                }
            }
            return all;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void runAndPrintSql(List<String> sql, Boolean forceShowSQL, NSession session) {
        List<String> sqlList = splitSQLList(sql);
        boolean showSQL = true;
        if (sqlList.size() == 1) {
            showSQL = false;
        }
        if (forceShowSQL != null) {
            showSQL = forceShowSQL;
        }
        for (String s : sqlList) {
            if (showSQL) {
                session.out().println(NMsg.ofCode("sql", s));
            }
            try {
                List<Object> a = runSQL2(s);
                if (session.isPlainOut()) {
                    if (a.size() > 0) {
                        if (a.get(0) instanceof List) {
                            session.copy().setOutputFormat(NContentType.TABLE)
                                    .setOutputFormatOptions("--border=spaces")
                                    .out().println(a);
                        } else {
                            for (Object o : a) {
                                session.out().println(o);
                            }
                        }
                    }
                } else {
                    session.out().println(a);
                }
            } catch (Exception e) {
                session.err().println(NMsg.ofC("Error : %s", e));
            }
        }
    }

    private static class DriverShim implements Driver {
        private Driver driver;

        DriverShim(Driver d) {
            this.driver = d;
        }

        public boolean acceptsURL(String u) throws SQLException {
            return this.driver.acceptsURL(u);
        }

        public Connection connect(String u, Properties p) throws SQLException {
            return this.driver.connect(u, p);
        }

        public int getMajorVersion() {
            return this.driver.getMajorVersion();
        }

        public int getMinorVersion() {
            return this.driver.getMinorVersion();
        }

        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws
                SQLException {
            return this.driver.getPropertyInfo(u, p);
        }

        public boolean jdbcCompliant() {
            return this.driver.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return this.driver.getParentLogger();
        }
    }

    public static class SqlResultMetaData {
        SResultSetColumn[] columns;
    }

    public static class SResultSetColumn {

        public int columnDisplaySize;
        public String label;
        public String columnName;
        public int columnType;
        public String columnTypeName;
        public int precision;
        public int scale;
        public String schemaName;
        public String catalogName;
        public String tableName;
        public String columnsClassName;

        @Override
        public String toString() {
            return String.valueOf(columnName);
        }
    }

    public static SqlResultMetaData toSResultMetaData(ResultSet rs) {
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            SResultSetColumn[] cols = new SResultSetColumn[columnCount];
            for (int i = 0; i < cols.length; i++) {
                SResultSetColumn c = new SResultSetColumn();
                cols[i] = c;
                c.columnDisplaySize = metaData.getColumnDisplaySize(i + 1);
                c.label = metaData.getColumnLabel(i + 1);
                c.columnName = metaData.getColumnName(i + 1);
                c.columnType = metaData.getColumnType(i + 1);
                c.columnTypeName = metaData.getColumnTypeName(i + 1);
                c.precision = metaData.getPrecision(i + 1);
                c.scale = metaData.getScale(i + 1);
                c.schemaName = metaData.getSchemaName(i + 1);
                c.catalogName = metaData.getCatalogName(i + 1);
                c.tableName = metaData.getTableName(i + 1);
                c.columnsClassName = metaData.getColumnClassName(i + 1);
            }
            SqlResultMetaData d = new SqlResultMetaData();
            d.columns = cols;
            return d;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loop(ResultSet rs, SqlResultRunner mapper) {
        try {
            SqlResultMetaData sResultMetaData = toSResultMetaData(rs);
            while (rs.next()) {
                mapper.run(rs, sResultMetaData);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static <T> List<T> mapResultSet(ResultSet rs, Function<ResultSet, T> mapper) {
        List<T> all = new ArrayList<>();
        try {
            while (rs.next()) {
                T z = mapper.apply(rs);
                if (z != null) {
                    all.add(z);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return all;
    }


    public static <C extends NdbConfig> SqlDB computeSchema(ExtendedQuery eq, SqlSupport<C> ss, C options, NSession session) {
        return ss.callInDb(new SqlCallable<SqlDB>() {
            @Override
            public SqlDB run(SqlHelper c, NSession session) throws Exception {
                String catalog = null;
                String schema = null;
                SqlDB d = new SqlDB();

                Connection connection = c.getConnection();
                DatabaseMetaData metaData = connection.getMetaData();
                String connSchema = connection.getSchema();
                String connCat = connection.getCatalog();
                SqlHelper.loop(
                        metaData.getTables(connCat, connSchema, null, null),
                        (r, m) -> {
                            try {
                                String tabCat = r.getString("TABLE_CAT");
                                String tabSchema = r.getString("TABLE_SCHEM");
                                if (tabCat == null) {
                                    tabCat = connCat;
                                }
                                if (tabSchema == null) {
                                    tabSchema = connSchema;
                                }
                                if (
                                        Objects.equals(tabCat, connCat)
                                                && Objects.equals(tabSchema, connSchema)
                                ) {
                                    String tableType = r.getString("TABLE_TYPE");

                                    SqlTable t = d.getOrCreateTable(tabCat, tabSchema, r.getString("TABLE_NAME"), tableType);
                                    t.remarks = r.getString("REMARKS");
                                    t.typesCatalog = NStringUtils.trimToNull(r.getString("TYPE_CAT"));
                                    t.typesSchema = NStringUtils.trimToNull(r.getString("TYPE_SCHEM"));
                                    t.typeName = NStringUtils.trimToNull(r.getString("TYPE_NAME"));
                                    t.selfReferencingColName = NStringUtils.trimToNull(r.getString("SELF_REFERENCING_COL_NAME"));
                                    t.refGeneration = NStringUtils.trimToNull(r.getString("REF_GENERATION"));
                                }
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
                for (SqlCatalog sCat : d.catalogs.values()) {
                    for (SqlSchema sSchema : sCat.schemas.values()) {
                        for (Map<String, SqlTable> value : sSchema.tableMaps().values()) {
                            for (SqlTable sTable : value.values()) {
                                fillTableColumns(metaData, connSchema, connCat, sCat, sSchema, sTable);
                                fillTableIndexes(metaData, sCat, sSchema, sTable);
                            }
                        }
                    }
                }
                return d.sort();
            }
        }, options, session);
    }

    private static void fillTableIndexes(DatabaseMetaData metaData, SqlCatalog sCat, SqlSchema sSchema, SqlTable sTable) throws SQLException {
        SqlHelper.loop(
                metaData.getIndexInfo(sCat.catalogName, sSchema.schemaName, sTable.tableName, false, true),
                (r, m) -> {
                    try {
                        String tabCat = r.getString("TABLE_CAT");
                        String tabSchema = r.getString("TABLE_SCHEM");
                        String tabName = r.getString("TABLE_NAME");
                        if (tabCat == null) {
                            tabCat = sCat.catalogName;
                        }
                        if (tabSchema == null) {
                            tabSchema = sSchema.schemaName;
                        }
                        if (tabName == null) {
                            tabName = sTable.tableName;
                        }
                        if (
                                Objects.equals(tabCat, sCat.catalogName)
                                        && Objects.equals(tabSchema, sSchema.schemaName)
                                        && Objects.equals(tabName, sTable.tableName)
                        ) {

                            String indexQualifier = r.getString("INDEX_QUALIFIER");
                            String indexName = r.getString("INDEX_NAME");
                            SqlIndex t = sTable.indexes.stream().filter(x ->
                                    Objects.equals(x.indexName, indexName)
                                            && Objects.equals(x.indexQualifier, indexQualifier)
                            ).findFirst().orElse(null);
                            if (t == null) {
                                t = new SqlIndex();
                                t.indexQualifier = indexQualifier;
                                t.indexName = indexName;
                                sTable.indexes.add(t);
                            }
                            SqlIndexColumn c = new SqlIndexColumn();
                            c.nonUnique = r.getBoolean("NON_UNIQUE");
                            c.type = SqlHelper.getIndexTypeName(r.getShort("TYPE"));
                            c.ordinalPosition = r.getShort("ORDINAL_POSITION");
                            c.columnName = r.getString("COLUMN_NAME");
                            String ascOrDesc = NStringUtils.trim(r.getString("ASC_OR_DESC")).toLowerCase();
                            c.asc = ascOrDesc.isEmpty() ? null : ascOrDesc.equals("a");
                            c.pages = r.getLong("PAGES");
                            c.filterCondition = r.getString("FILTER_CONDITION");
                            t.columns.add(c);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    private static void fillTableColumns(DatabaseMetaData metaData, String connSchema, String connCat, SqlCatalog sCat, SqlSchema sSchema, SqlTable sTable) throws SQLException {
        SqlHelper.loop(
                metaData.getColumns(sCat.catalogName, sSchema.schemaName, sTable.tableName, null),
                (r, m) -> {
                    try {
                        String tabCat = r.getString("TABLE_CAT");
                        String tabSchema = r.getString("TABLE_SCHEM");
                        if (tabCat == null) {
                            tabCat = connCat;
                        }
                        if (tabSchema == null) {
                            tabSchema = connSchema;
                        }
                        if (
                                Objects.equals(tabCat, connCat)
                                        && Objects.equals(tabSchema, connSchema)
                        ) {
                            SqlColumn t = new SqlColumn();
                            t.columnName = r.getString("COLUMN_NAME");
                            t.dataType = SqlHelper.getSqlTypeName(r.getInt("DATA_TYPE")).toLowerCase();
                            t.typeName = r.getString("TYPE_NAME");
                            t.columnSize = r.getInt("COLUMN_SIZE");
                            //t.bufferLength = r.getInt("BUFFER_LENGTH");
                            t.decimalDigits = r.getInt("DECIMAL_DIGITS");
                            t.numPrecRadix = r.getInt("NUM_PREC_RADIX");
                            t.nullable = SqlHelper.getColumnNullableName(r.getInt("NULLABLE"));
                            t.nullable2 = SqlHelper.getColumnYesNoEmptyName(r.getString("IS_NULLABLE"));
                            t.remarks = r.getString("REMARKS");
                            t.columnDef = r.getString("COLUMN_DEF");
                            //t.sqlDataType = r.getInt("SQL_DATA_TYPE");
                            //t.sqlDateTimeSub = r.getInt("SQL_DATETIME_SUB");
                            t.charOctetLength = r.getInt("CHAR_OCTET_LENGTH");
                            t.ordinalPosition = r.getInt("ORDINAL_POSITION");
                            t.scopeCatalog = r.getString("SCOPE_CATALOG");
                            t.scopeSchema = r.getString("SCOPE_SCHEMA");
                            t.scopeTable = r.getString("SCOPE_TABLE");
                            t.sourceDataType = r.getShort("SOURCE_DATA_TYPE");
                            t.autoIncrement = SqlHelper.getColumnYesNoEmptyName(r.getString("IS_AUTOINCREMENT"));
                            t.generatedColumn = SqlHelper.getColumnYesNoEmptyName(r.getString("IS_GENERATEDCOLUMN"));
                            sTable.columns.add(t);

                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        SqlHelper.loop(
                metaData.getPrimaryKeys(sCat.catalogName, sSchema.schemaName, sTable.tableName),
                (r, m) -> {
                    try {
                        String tabCat = r.getString("TABLE_CAT");
                        String tabSchema = r.getString("TABLE_SCHEM");
                        String tabName = r.getString("TABLE_NAME");
                        if (tabCat == null) {
                            tabCat = connCat;
                        }
                        if (tabSchema == null) {
                            tabSchema = connSchema;
                        }
                        if (tabName == null) {
                            tabName = sTable.tableName;
                        }
                        if (
                                Objects.equals(tabCat, connCat)
                                        && Objects.equals(tabSchema, connSchema)
                                        && Objects.equals(tabName, sTable.tableName)
                        ) {
                            SqlPrimaryKey t = new SqlPrimaryKey();
                            t.columnName = r.getString("COLUMN_NAME");
                            t.keySeq = r.getShort("KEY_SEQ");
                            t.pkName = r.getString("PK_NAME");
                            sTable.primaryKeys.add(t);
                            SqlColumn c = sTable.columns.stream().filter(x -> x.columnName.equals(t.columnName))
                                    .findFirst().get();
                            c.primaryKeySeq = t.keySeq;
                            c.primaryKey = true;
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        SqlHelper.loop(
                metaData.getImportedKeys(sCat.catalogName, sSchema.schemaName, sTable.tableName),
                (r, m) -> {
                    try {
//                        String fkTableCat = r.getString("FKTABLE_CAT");
//                        String fkTableSchema = r.getString("FKTABLE_SCHEM");
//                        String fkTableName = r.getString("FKTABLE_NAME");
                        String fkColumnName = r.getString("FKCOLUMN_NAME");
                        SqlImportedColumn t = new SqlImportedColumn();
                        t.pkTableCat = r.getString("PKTABLE_CAT");
                        t.pkTableSchema = r.getString("PKTABLE_SCHEM");
                        t.pkTableName = r.getString("PKTABLE_NAME");
                        t.pkColumnName = r.getString("PKCOLUMN_NAME");
                        t.keySeq = r.getShort("KEY_SEQ");
                        t.updateRule = SqlHelper.getImportedKeyRuleName(r.getShort("UPDATE_RULE"));
                        t.deleteRule = SqlHelper.getImportedKeyRuleName(r.getShort("DELETE_RULE"));
                        t.fkName = r.getString("FK_NAME");
                        t.pkName = r.getString("PK_NAME");
                        t.deferrability = SqlHelper.getImportedKeyDeferrability(r.getShort("DEFERRABILITY"));
                        SqlColumn ff = sTable.columns.stream().filter(c -> c.columnName.equals(fkColumnName))
                                .findFirst().get();
                        ff.foreignKeys.add(t);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

}
