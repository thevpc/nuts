package net.thevpc.nuts.toolbox.ndb.sql.util;

import java.util.Properties;

public class SqlConnectionInfo {
    private String jdbcUrl;
    private String id;
    private String jdbcDriver;
    private String user;
    private String password;
    private Properties properties;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public SqlConnectionInfo setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }

    public String getId() {
        return id;
    }

    public SqlConnectionInfo setId(String id) {
        this.id = id;
        return this;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public SqlConnectionInfo setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
        return this;
    }

    public String getUser() {
        return user;
    }

    public SqlConnectionInfo setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SqlConnectionInfo setPassword(String password) {
        this.password = password;
        return this;
    }

    public Properties getProperties() {
        return properties;
    }

    public SqlConnectionInfo setProperties(Properties properties) {
        this.properties = properties;
        return this;
    }

}
