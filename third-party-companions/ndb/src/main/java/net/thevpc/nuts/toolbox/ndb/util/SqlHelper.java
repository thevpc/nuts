package net.thevpc.nuts.toolbox.ndb.util;

import net.thevpc.nuts.NutsContentType;
import net.thevpc.nuts.NutsDependencyFilters;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;

import java.io.Closeable;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SqlHelper implements Closeable {
    private String jdbcUrl;
    private String jdbcDriver;
    private Connection connection;
    private static Map<Object, ClassLoader> cachedClassLoaders = new HashMap<>();

    public static void runAndWaitFor(List<String> sql, String jdbcUrl, String id, String jdbcDriver, String user, String password, Properties properties, Boolean forceShowSQL, NutsSession session) {
        ClassloaderAwareCallable<Object> callable = new ClassloaderAwareCallable<>(session, null,
                SqlHelper.createClassLoader(session, id));
        callable.runAndWaitFor((ClassloaderAwareCallable.Context cc) -> {
            try (SqlHelper connection = new SqlHelper(jdbcUrl, jdbcDriver, user, password, properties, cc.getClassLoader())) {
                connection.runAndPrintSql(sql, forceShowSQL, session);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        });
    }

    public static ClassLoader createClassLoader(NutsSession session, String id) {
        String z = session.getWorkspace().getUuid() + "/" + id;
        return cachedClassLoaders.computeIfAbsent(z, x -> session.search().addId(id)
                .setDependencyFilter(NutsDependencyFilters.of(session).byRunnable())
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

    public void runAndPrintSql(List<String> sql, Boolean forceShowSQL, NutsSession session) {
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
                session.out().printlnf(NutsMessage.ofCode("sql", s));
            }
            try {
                List<Object> a = runSQL2(s);
                if (session.isPlainOut()) {
                    if (a.size() > 0) {
                        if (a.get(0) instanceof List) {
                            session.copy().setOutputFormat(NutsContentType.TABLE)
                                    .setOutputFormatOptions("--border=spaces")
                                    .out().printlnf(a);
                        } else {
                            for (Object o : a) {
                                session.out().printlnf(o);
                            }
                        }
                    }
                } else {
                    session.out().printlnf(a);
                }
            } catch (Exception e) {
                session.err().printlnf(NutsMessage.ofCstyle("Error : %s", e));
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

}
