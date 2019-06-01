package net.vpc.toolbox.mysql.remote.config;

public class RemoteMysqlDatabaseConfig {

    private String localInstance;
    private String localDatabase;
    private String path;
    private String remoteInstance;
    private String remoteDatabase;
    private String server;
    private String remoteTempPath;

    public String getPath() {
        return path;
    }

    public void setPath(String appPath) {
        this.path = appPath;
    }

    public String getLocalInstance() {
        return localInstance;
    }

    public String getRemoteDatabase() {
        return remoteDatabase;
    }

    public void setRemoteDatabase(String remoteDatabase) {
        this.remoteDatabase = remoteDatabase;
    }

    public RemoteMysqlDatabaseConfig setLocalInstance(String localInstance) {
        this.localInstance = localInstance;
        return this;
    }

    public String getLocalDatabase() {
        return localDatabase;
    }

    public RemoteMysqlDatabaseConfig setLocalDatabase(String localDatabase) {
        this.localDatabase = localDatabase;
        return this;
    }

    public String getRemoteInstance() {
        return remoteInstance;
    }

    public RemoteMysqlDatabaseConfig setRemoteInstance(String remoteInstance) {
        this.remoteInstance = remoteInstance;
        return this;
    }

    public String getServer() {
        return server;
    }

    public RemoteMysqlDatabaseConfig setServer(String server) {
        this.server = server;
        return this;
    }

    public String getRemoteTempPath() {
        return remoteTempPath;
    }

    public RemoteMysqlDatabaseConfig setRemoteTempPath(String remoteTempPath) {
        this.remoteTempPath = remoteTempPath;
        return this;
    }
}
