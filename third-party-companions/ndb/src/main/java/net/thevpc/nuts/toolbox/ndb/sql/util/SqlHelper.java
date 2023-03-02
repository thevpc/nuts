package net.thevpc.nuts.toolbox.ndb.sql.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.ndb.util.ClassloaderAwareCallable;
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

    public static void runAndWaitFor(List<String> sql, String jdbcUrl, String id, String jdbcDriver, String user, String password, Properties properties, Boolean forceShowSQL, NSession session) {
        runAndWaitFor(
                new SqlRunnable() {
                    @Override
                    public void run(SqlHelper h, NSession session) {
                        h.runAndPrintSql(sql, forceShowSQL, session);
                    }
                }, jdbcUrl, id, jdbcDriver, user, password, properties, session
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
            case DatabaseMetaData.tableIndexClustered           : return "tableIndexClustered";
            case DatabaseMetaData.tableIndexHashed              : return "tableIndexHashed";
            case DatabaseMetaData.tableIndexOther               : return "tableIndexOther";
        }
        return "unknown["+value+"]";
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

    public static void runAndWaitFor(SqlRunnable sql, String jdbcUrl, String id, String jdbcDriver, String user, String password, Properties properties, NSession session) {
        ClassloaderAwareCallable<Object> callable = new ClassloaderAwareCallable<>(session, null,
                SqlHelper.createClassLoader(session, id));
        callable.runAndWaitFor((ClassloaderAwareCallable.Context cc) -> {
            try (SqlHelper connection = new SqlHelper(jdbcUrl, jdbcDriver, user, password, properties, cc.getClassLoader())) {
                sql.run(connection, session);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        });
    }

    public static <T> T callAndWaitFor(SqlCallable<T> sql, String jdbcUrl, String id, String jdbcDriver, String user, String password, Properties properties, NSession session) {
        ClassloaderAwareCallable<Object> callable = new ClassloaderAwareCallable<>(session, null,
                SqlHelper.createClassLoader(session, id));
        Object o = callable.runAndWaitFor((ClassloaderAwareCallable.Context cc) -> {
            try (SqlHelper connection = new SqlHelper(jdbcUrl, jdbcDriver, user, password, properties, cc.getClassLoader())) {
                return sql.run(connection, session);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        return (T) o;
    }

    public static ClassLoader createClassLoader(NSession session, String id) {
        String z = session.getWorkspace().getUuid() + "/" + id;
        return cachedClassLoaders.computeIfAbsent(z, x -> NSearchCommand.of(session).addId(id)
                .setDependencyFilter(NDependencyFilters.of(session).byRunnable())
                .getResultClassLoader(SqlHelper.class.getClassLoader()));
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

}
