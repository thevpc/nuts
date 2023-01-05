package net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote.config;

import java.util.HashMap;
import java.util.Map;

public class RemoteMysqlConfig {

    private Map<String, RemoteMysqlDatabaseConfig> databases = new HashMap<>();

    public Map<String, RemoteMysqlDatabaseConfig> getDatabases() {
        return databases;
    }

    public void setDatabases(Map<String, RemoteMysqlDatabaseConfig> databases) {
        this.databases = databases;
    }
}
