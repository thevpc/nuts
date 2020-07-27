package net.vpc.toolbox.mysql.local.config;

public class LocalMysqlDatabaseConfig {

    private String user;
    private String password;
    private String databaseName;

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

}
