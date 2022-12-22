package net.thevpc.nuts.toolbox.ndb.nmysql.local.config;

public class LocalMysqlDatabaseConfig {

    private String user;
    private String password;
    private String databaseName;
    private String server;
    private Integer port;

    public String getUser() {
        return user;
    }

    public LocalMysqlDatabaseConfig setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public LocalMysqlDatabaseConfig setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public LocalMysqlDatabaseConfig setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public String getServer() {
        return server;
    }

    public LocalMysqlDatabaseConfig setServer(String server) {
        this.server = server;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public LocalMysqlDatabaseConfig setPort(Integer port) {
        this.port = port;
        return this;
    }
}
