package net.thevpc.nuts.toolbox.ndb.sql.nmysql.remote.config;

public class RemoteMysqlDatabaseConfig {

    private String localName;
    private String remoteName;
    private String server;

    public String getServer() {
        return server;
    }

    public RemoteMysqlDatabaseConfig setServer(String server) {
        this.server = server;
        return this;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

}
